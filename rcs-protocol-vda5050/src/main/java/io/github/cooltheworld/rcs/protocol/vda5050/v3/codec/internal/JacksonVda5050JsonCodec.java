package io.github.cooltheworld.rcs.protocol.vda5050.v3.codec.internal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.core.exc.StreamConstraintsException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.codec.JsonCodecLimits;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.codec.Vda5050JacksonModule;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.codec.Vda5050JsonCodec;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.topic.TopicName;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.DecodedMessage;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.DecodingResult;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.RejectedInboundMessage;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.ValidationIssue;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.ValidationSeverity;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * Jackson 2 默认 Codec 实现。
 *
 * @see <a href="https://github.com/FasterXML/jackson-core#processing-limits">
 *     Jackson Core processing limits</a>
 */
public final class JacksonVda5050JsonCodec implements Vda5050JsonCodec {
    private static final String RESOURCE_REQUIREMENT = "VDA3-SHARED-009";

    private final JsonCodecLimits limits;
    private final ObjectMapper mapper;
    private final JsonPreflightScanner preflightScanner;

    /**
     * @param limits 启动时固定的部署硬上限
     */
    public JacksonVda5050JsonCodec(JsonCodecLimits limits) {
        this.limits = Objects.requireNonNull(limits, "limits");
        this.mapper = createMapper(limits);
        this.preflightScanner = new JsonPreflightScanner(mapper, limits);
    }

    @Override
    public <T> DecodingResult<T> decode(
        TopicName topic,
        byte[] payload,
        Class<T> messageType
    ) {
        Objects.requireNonNull(topic, "topic");
        Objects.requireNonNull(payload, "payload");
        Objects.requireNonNull(messageType, "messageType");
        if (payload.length > limits.maxPayloadBytes()) {
            return rejected(
                topic,
                "PAYLOAD_TOO_LARGE",
                "Payload exceeds the configured byte limit",
                RESOURCE_REQUIREMENT,
                ""
            );
        }
        try {
            preflightScanner.scan(payload, messageType);
            return new DecodedMessage<>(mapper.readValue(payload, messageType));
        } catch (JsonPreflightException exception) {
            return rejected(
                topic,
                exception.code(),
                exception.description(),
                exception.requirementId(),
                exception.path()
            );
        } catch (StreamConstraintsException exception) {
            return rejected(
                topic,
                "JSON_LIMIT_EXCEEDED",
                "JSON input exceeds a configured resource limit",
                RESOURCE_REQUIREMENT,
                ""
            );
        } catch (JsonParseException exception) {
            return rejected(
                topic,
                "INVALID_JSON",
                "Payload is not valid JSON",
                RESOURCE_REQUIREMENT,
                ""
            );
        } catch (MismatchedInputException exception) {
            return rejected(
                topic,
                "INVALID_JSON_TYPE",
                "JSON value does not match the protocol field type",
                RESOURCE_REQUIREMENT,
                pathOf(exception)
            );
        } catch (IOException exception) {
            return rejected(
                topic,
                "INVALID_JSON",
                "Payload cannot be decoded as JSON",
                RESOURCE_REQUIREMENT,
                ""
            );
        }
    }

    @Override
    public byte[] encode(Object message) {
        Objects.requireNonNull(message, "message");
        try {
            return mapper.writeValueAsBytes(message);
        } catch (IOException exception) {
            throw new IllegalArgumentException(
                "Protocol message cannot be encoded as JSON",
                exception
            );
        }
    }

    private static ObjectMapper createMapper(JsonCodecLimits limits) {
        StreamReadConstraints constraints = StreamReadConstraints.builder()
            .maxDocumentLength(limits.maxPayloadBytes())
            .maxTokenCount(limits.maxTokens())
            .maxNestingDepth(limits.maxNestingDepth())
            .maxStringLength(limits.maxPayloadBytes())
            .maxNameLength(limits.maxPayloadBytes())
            .maxNumberLength(limits.maxNumberCharacters())
            .build();
        JsonFactory factory = JsonFactory.builder()
            .streamReadConstraints(constraints)
            .enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION)
            .build();
        return JsonMapper.builder(factory)
            .addModule(new Jdk8Module())
            .addModule(new JavaTimeModule())
            .addModule(new Vda5050JacksonModule())
            .defaultPropertyInclusion(
                JsonInclude.Value.construct(
                    JsonInclude.Include.NON_NULL,
                    JsonInclude.Include.ALWAYS
                )
            )
            .disable(MapperFeature.ALLOW_COERCION_OF_SCALARS)
            .disable(DeserializationFeature.ACCEPT_FLOAT_AS_INT)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
            .deactivateDefaultTyping()
            .build();
    }

    private static String pathOf(JsonMappingException exception) {
        StringBuilder path = new StringBuilder();
        for (JsonMappingException.Reference reference : exception.getPath()) {
            if (reference.getFieldName() != null) {
                path.append('/').append(escapePointer(reference.getFieldName()));
            } else if (reference.getIndex() >= 0) {
                path.append('/').append(reference.getIndex());
            }
        }
        return path.toString();
    }

    private static String escapePointer(String value) {
        return value.replace("~", "~0").replace("/", "~1");
    }

    private static <T> RejectedInboundMessage<T> rejected(
        TopicName topic,
        String code,
        String description,
        String requirementId,
        String path
    ) {
        ValidationIssue issue = new ValidationIssue(
            code,
            ValidationSeverity.ERROR,
            path,
            description,
            requirementId
        );
        return RejectedInboundMessage.<T>builder(topic, List.of(issue)).build();
    }
}
