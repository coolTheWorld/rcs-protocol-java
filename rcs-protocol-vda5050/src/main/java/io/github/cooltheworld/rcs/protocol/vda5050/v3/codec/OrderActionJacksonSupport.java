package io.github.cooltheworld.rcs.protocol.vda5050.v3.codec;

import static io.github.cooltheworld.rcs.protocol.vda5050.v3.codec.FactsheetFragmentJacksonSupport.readObject;
import static io.github.cooltheworld.rcs.protocol.vda5050.v3.codec.FactsheetFragmentJacksonSupport.readOptional;
import static io.github.cooltheworld.rcs.protocol.vda5050.v3.codec.FactsheetFragmentJacksonSupport.readOptionalList;
import static io.github.cooltheworld.rcs.protocol.vda5050.v3.codec.FactsheetFragmentJacksonSupport.readRequired;
import static io.github.cooltheworld.rcs.protocol.vda5050.v3.codec.FactsheetFragmentJacksonSupport.requireObjectMapper;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.ExtensionFields;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.internal.ExtensionFieldsJacksonSupport;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.action.Action;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.action.ActionParameter;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.action.ActionParameterValue;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.action.BlockingType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** 注册 Order 与 Instant Actions 共享的 Action Jackson 线路映射。 */
final class OrderActionJacksonSupport {
    private static final Set<String> ACTION_FIELDS = Set.of(
        "actionType",
        "actionId",
        "actionDescriptor",
        "blockingType",
        "actionParameters",
        "retriable"
    );
    private static final Set<String> ACTION_PARAMETER_FIELDS = Set.of(
        "key",
        "value"
    );

    private OrderActionJacksonSupport() {}

    static void register(SimpleModule module) {
        module.addSerializer(Action.class, new ActionSerializer());
        module.addDeserializer(Action.class, new ActionDeserializer());
        module.addSerializer(
            ActionParameter.class,
            new ActionParameterSerializer()
        );
        module.addDeserializer(
            ActionParameter.class,
            new ActionParameterDeserializer()
        );
        module.addSerializer(
            ActionParameterValue.class,
            new ActionParameterValueSerializer()
        );
        module.addDeserializer(
            ActionParameterValue.class,
            new ActionParameterValueDeserializer()
        );
    }

    private static final class ActionSerializer extends StdSerializer<Action> {
        private ActionSerializer() {
            super(Action.class);
        }

        @Override
        public void serialize(
            Action value,
            JsonGenerator generator,
            SerializerProvider provider
        ) throws IOException {
            ObjectMapper mapper = requireObjectMapper(generator);
            ObjectNode target = mapper.createObjectNode();
            target.put("actionType", value.actionType());
            target.put("actionId", value.actionId());
            putOptional(target, "actionDescriptor", value.actionDescriptor());
            target.put("blockingType", value.blockingType().name());
            if (value.actionParameters() != null) {
                target.set(
                    "actionParameters",
                    mapper.valueToTree(value.actionParameters())
                );
            }
            if (value.retriable() != null) {
                target.put("retriable", value.retriable());
            }
            ExtensionFieldsJacksonSupport.merge(
                mapper,
                target,
                value.extensionFields(),
                ACTION_FIELDS
            );
            generator.writeTree(target);
        }
    }

    private static final class ActionDeserializer
        extends StdDeserializer<Action> {
        private ActionDeserializer() {
            super(Action.class);
        }

        @Override
        public Action deserialize(
            JsonParser parser,
            DeserializationContext context
        ) throws IOException {
            ObjectMapper mapper = requireObjectMapper(parser);
            ObjectNode object = readObject(
                mapper,
                parser,
                context,
                Action.class,
                "Action"
            );
            String actionType = readRequired(
                object,
                "actionType",
                String.class,
                Action.class,
                context
            );
            String actionId = readRequired(
                object,
                "actionId",
                String.class,
                Action.class,
                context
            );
            String actionDescriptor = readOptional(
                object,
                "actionDescriptor",
                String.class,
                Action.class,
                context
            );
            BlockingType blockingType = readRequired(
                object,
                "blockingType",
                BlockingType.class,
                Action.class,
                context
            );
            List<ActionParameter> actionParameters = readOptionalList(
                object,
                "actionParameters",
                ActionParameter.class,
                Action.class,
                context
            );
            Boolean retriable = readOptional(
                object,
                "retriable",
                Boolean.class,
                Action.class,
                context
            );
            ExtensionFields extensionFields = ExtensionFieldsJacksonSupport.capture(
                mapper,
                object,
                ACTION_FIELDS
            );
            return Action.builder()
                .actionType(actionType)
                .actionId(actionId)
                .actionDescriptor(actionDescriptor)
                .blockingType(blockingType)
                .actionParameters(actionParameters)
                .retriable(retriable)
                .extensionFields(extensionFields)
                .build();
        }
    }

    private static final class ActionParameterSerializer
        extends StdSerializer<ActionParameter> {
        private ActionParameterSerializer() {
            super(ActionParameter.class);
        }

        @Override
        public void serialize(
            ActionParameter value,
            JsonGenerator generator,
            SerializerProvider provider
        ) throws IOException {
            ObjectMapper mapper = requireObjectMapper(generator);
            ObjectNode target = mapper.createObjectNode();
            target.put("key", value.key());
            target.set("value", mapper.valueToTree(value.value()));
            ExtensionFieldsJacksonSupport.merge(
                mapper,
                target,
                value.extensionFields(),
                ACTION_PARAMETER_FIELDS
            );
            generator.writeTree(target);
        }
    }

    private static final class ActionParameterDeserializer
        extends StdDeserializer<ActionParameter> {
        private ActionParameterDeserializer() {
            super(ActionParameter.class);
        }

        @Override
        public ActionParameter deserialize(
            JsonParser parser,
            DeserializationContext context
        ) throws IOException {
            ObjectMapper mapper = requireObjectMapper(parser);
            ObjectNode object = readObject(
                mapper,
                parser,
                context,
                ActionParameter.class,
                "Action parameter"
            );
            String key = readRequired(
                object,
                "key",
                String.class,
                ActionParameter.class,
                context
            );
            ActionParameterValue value = readRequired(
                object,
                "value",
                ActionParameterValue.class,
                ActionParameter.class,
                context
            );
            ExtensionFields extensionFields = ExtensionFieldsJacksonSupport.capture(
                mapper,
                object,
                ACTION_PARAMETER_FIELDS
            );
            return ActionParameter.builder()
                .key(key)
                .value(value)
                .extensionFields(extensionFields)
                .build();
        }
    }

    private static final class ActionParameterValueSerializer
        extends StdSerializer<ActionParameterValue> {
        private ActionParameterValueSerializer() {
            super(ActionParameterValue.class);
        }

        @Override
        public void serialize(
            ActionParameterValue value,
            JsonGenerator generator,
            SerializerProvider provider
        ) throws IOException {
            switch (value) {
                case ActionParameterValue.BooleanValue booleanValue ->
                    generator.writeBoolean(booleanValue.value());
                case ActionParameterValue.NumberValue numberValue -> {
                    if (!Double.isFinite(numberValue.value())) {
                        throw JsonMappingException.from(
                            generator,
                            "Action parameter number must be finite"
                        );
                    }
                    generator.writeNumber(numberValue.value());
                }
                case ActionParameterValue.IntegerValue integerValue ->
                    generator.writeNumber(integerValue.value());
                case ActionParameterValue.StringValue stringValue ->
                    generator.writeString(stringValue.value());
                case ActionParameterValue.ObjectValue objectValue -> {
                    generator.writeStartObject();
                    Set<String> names = new HashSet<>();
                    for (ActionParameterValue.ObjectMember member
                        : objectValue.members()) {
                        if (!names.add(member.name())) {
                            throw JsonMappingException.from(
                                generator,
                                "Action parameter object contains duplicate member names"
                            );
                        }
                        generator.writeFieldName(member.name());
                        provider.defaultSerializeValue(member.value(), generator);
                    }
                    generator.writeEndObject();
                }
                case ActionParameterValue.ArrayValue arrayValue -> {
                    generator.writeStartArray();
                    for (ActionParameterValue element : arrayValue.values()) {
                        provider.defaultSerializeValue(element, generator);
                    }
                    generator.writeEndArray();
                }
            }
        }
    }

    private static final class ActionParameterValueDeserializer
        extends StdDeserializer<ActionParameterValue> {
        private ActionParameterValueDeserializer() {
            super(ActionParameterValue.class);
        }

        @Override
        public ActionParameterValue deserialize(
            JsonParser parser,
            DeserializationContext context
        ) throws IOException {
            ObjectMapper mapper = requireObjectMapper(parser);
            return readParameterValue(mapper.readTree(parser), context);
        }
    }

    private static ActionParameterValue readParameterValue(
        JsonNode value,
        DeserializationContext context
    ) throws IOException {
        if (value.isBoolean()) {
            return new ActionParameterValue.BooleanValue(value.booleanValue());
        }
        if (value.isIntegralNumber()) {
            if (!value.canConvertToLong()) {
                throw invalidParameterValue(context);
            }
            return new ActionParameterValue.IntegerValue(value.longValue());
        }
        if (value.isFloatingPointNumber()) {
            double number = value.doubleValue();
            if (!Double.isFinite(number)) {
                throw invalidParameterValue(context);
            }
            return new ActionParameterValue.NumberValue(number);
        }
        if (value.isTextual()) {
            return new ActionParameterValue.StringValue(value.textValue());
        }
        if (value.isObject()) {
            List<ActionParameterValue.ObjectMember> members = new ArrayList<>();
            for (var property : value.properties()) {
                members.add(new ActionParameterValue.ObjectMember(
                    property.getKey(),
                    readParameterValue(property.getValue(), context)
                ));
            }
            return new ActionParameterValue.ObjectValue(members);
        }
        if (value.isArray()) {
            List<ActionParameterValue> values = new ArrayList<>();
            for (JsonNode element : (ArrayNode) value) {
                values.add(readParameterValue(element, context));
            }
            return new ActionParameterValue.ArrayValue(values);
        }
        throw invalidParameterValue(context);
    }

    private static MismatchedInputException invalidParameterValue(
        DeserializationContext context
    ) {
        return MismatchedInputException.from(
            context.getParser(),
            ActionParameterValue.class,
            "Action parameter value does not match a supported JSON value type"
        );
    }

    private static void putOptional(
        ObjectNode target,
        String fieldName,
        String value
    ) {
        if (value != null) {
            target.put(fieldName, value);
        }
    }
}
