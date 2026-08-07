package io.github.cooltheworld.rcs.protocol.vda5050.v3.codec.internal;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.codec.JsonCodecLimits;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** 在创建完整协议对象前，不构造 JSON 树地遍历 Token。 */
final class JsonPreflightScanner {
    private static final String RESOURCE_REQUIREMENT = "VDA3-SHARED-009";
    private static final String NULL_REQUIREMENT = "VDA3-SHARED-010";

    private final ObjectMapper mapper;
    private final JsonCodecLimits limits;
    private final JavaType untypedType;
    private final Map<JavaType, Map<String, JavaType>> propertyCache;

    JsonPreflightScanner(ObjectMapper mapper, JsonCodecLimits limits) {
        this.mapper = Objects.requireNonNull(mapper, "mapper");
        this.limits = Objects.requireNonNull(limits, "limits");
        this.untypedType = mapper.constructType(Object.class);
        this.propertyCache = new ConcurrentHashMap<>();
    }

    void scan(byte[] payload, Class<?> messageType)
        throws IOException, JsonPreflightException {
        JavaType rootType = mapper.constructType(messageType);
        Deque<ContainerState> containers = new ArrayDeque<>();
        long tokenCount = 0L;

        try (JsonParser parser = mapper.getFactory().createParser(payload)) {
            JsonToken token;
            while ((token = parser.nextToken()) != null) {
                tokenCount++;
                requireAtMost(tokenCount, limits.maxTokens(), pointer(parser));

                if (token == JsonToken.FIELD_NAME) {
                    ContainerState current = requireContainer(containers, parser);
                    current.acceptField(parser.currentName(), pointer(parser), limits);
                    continue;
                }
                if (token == JsonToken.END_OBJECT || token == JsonToken.END_ARRAY) {
                    requireContainer(containers, parser).close(token);
                    containers.pop();
                    continue;
                }

                ValueSlot slot = containers.isEmpty()
                    ? new ValueSlot(rootType, true)
                    : containers.peek().consumeValue(pointer(parser), limits);
                switch (token) {
                    case START_OBJECT -> {
                        requireDepth(containers.size() + 1, parser);
                        containers.push(objectState(slot));
                    }
                    case START_ARRAY -> {
                        requireDepth(containers.size() + 1, parser);
                        containers.push(arrayState(slot));
                    }
                    case VALUE_STRING -> requireAtMost(
                        parser.getTextLength(),
                        limits.maxStringCharacters(),
                        pointer(parser)
                    );
                    case VALUE_NUMBER_INT, VALUE_NUMBER_FLOAT -> requireAtMost(
                        parser.getTextLength(),
                        limits.maxNumberCharacters(),
                        pointer(parser)
                    );
                    case VALUE_NULL -> rejectStandardNull(slot, parser);
                    default -> {
                        // Boolean values are bounded by their fixed JSON spelling.
                    }
                }
            }
        }
    }

    private ContainerState objectState(ValueSlot slot) {
        Map<String, JavaType> properties = slot.standard()
            && !slot.type().isJavaLangObject()
            ? propertyCache.computeIfAbsent(slot.type(), this::inspectProperties)
            : Map.of();
        return ContainerState.object(slot.standard(), properties, untypedType);
    }

    private Map<String, JavaType> inspectProperties(JavaType type) {
        Optional<Map<String, JavaType>> explicitProperties =
            ProtocolJsonTypeRegistry.findProperties(mapper, type);
        if (explicitProperties.isPresent()) {
            return explicitProperties.get();
        }
        Map<String, JavaType> properties = new HashMap<>();
        for (BeanPropertyDefinition property : mapper
            .getDeserializationConfig()
            .introspect(type)
            .findProperties()) {
            if (property.couldDeserialize()) {
                properties.put(property.getName(), property.getPrimaryType());
            }
        }
        return Map.copyOf(properties);
    }

    private ContainerState arrayState(ValueSlot slot) {
        JavaType elementType = slot.type().getContentType();
        if (elementType == null) {
            elementType = untypedType;
        }
        return ContainerState.array(slot.standard(), elementType);
    }

    private void requireDepth(int depth, JsonParser parser)
        throws JsonPreflightException {
        requireAtMost(depth, limits.maxNestingDepth(), pointer(parser));
    }

    private static void rejectStandardNull(ValueSlot slot, JsonParser parser)
        throws JsonPreflightException {
        if (slot.standard()) {
            throw new JsonPreflightException(
                "EXPLICIT_NULL",
                "Standard protocol field must not contain explicit null",
                NULL_REQUIREMENT,
                pointer(parser)
            );
        }
    }

    private static ContainerState requireContainer(
        Deque<ContainerState> containers,
        JsonParser parser
    ) throws JsonPreflightException {
        if (containers.isEmpty()) {
            throw resourceFailure(pointer(parser));
        }
        return containers.peek();
    }

    private static void requireAtMost(long actual, long maximum, String path)
        throws JsonPreflightException {
        if (actual > maximum) {
            throw resourceFailure(path);
        }
    }

    private static JsonPreflightException resourceFailure(String path) {
        return new JsonPreflightException(
            "JSON_LIMIT_EXCEEDED",
            "JSON input exceeds a configured resource limit",
            RESOURCE_REQUIREMENT,
            path
        );
    }

    private static String pointer(JsonParser parser) {
        return parser.getParsingContext().pathAsPointer().toString();
    }

    private record ValueSlot(JavaType type, boolean standard) {}

    private static final class ContainerState {
        private final JsonToken closingToken;
        private final boolean standard;
        private final Map<String, JavaType> properties;
        private final JavaType elementType;
        private final JavaType untypedType;
        private int count;
        private ValueSlot pendingValue;

        private ContainerState(
            JsonToken closingToken,
            boolean standard,
            Map<String, JavaType> properties,
            JavaType elementType,
            JavaType untypedType
        ) {
            this.closingToken = closingToken;
            this.standard = standard;
            this.properties = properties;
            this.elementType = elementType;
            this.untypedType = untypedType;
        }

        static ContainerState object(
            boolean standard,
            Map<String, JavaType> properties,
            JavaType untypedType
        ) {
            return new ContainerState(
                JsonToken.END_OBJECT,
                standard,
                Map.copyOf(properties),
                null,
                untypedType
            );
        }

        static ContainerState array(boolean standard, JavaType elementType) {
            return new ContainerState(
                JsonToken.END_ARRAY,
                standard,
                Map.of(),
                elementType,
                elementType
            );
        }

        void acceptField(String name, String path, JsonCodecLimits limits)
            throws JsonPreflightException {
            if (closingToken != JsonToken.END_OBJECT || pendingValue != null) {
                throw resourceFailure(path);
            }
            requireAtMost(name.length(), limits.maxNameCharacters(), path);
            requireAtMost(++count, limits.maxObjectProperties(), path);
            JavaType propertyType = properties.get(name);
            pendingValue = propertyType == null
                ? new ValueSlot(untypedType, false)
                : new ValueSlot(propertyType, standard);
        }

        ValueSlot consumeValue(String path, JsonCodecLimits limits)
            throws JsonPreflightException {
            if (closingToken == JsonToken.END_OBJECT) {
                if (pendingValue == null) {
                    throw resourceFailure(path);
                }
                ValueSlot result = pendingValue;
                pendingValue = null;
                return result;
            }
            requireAtMost(++count, limits.maxArrayElements(), path);
            return new ValueSlot(elementType, standard);
        }

        void close(JsonToken actual) throws JsonPreflightException {
            if (closingToken != actual || pendingValue != null) {
                throw resourceFailure("");
            }
        }
    }
}
