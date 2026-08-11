package io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.internal;

import com.networknt.schema.Error;
import com.networknt.schema.InputFormat;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.codec.JsonCodecLimits;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.codec.Vda5050JsonCodec;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.topic.TopicName;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.DecodingResult;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.ValidationIssue;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.ValidationSeverity;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.Vda5050SchemaValidator;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/** NetworkNT 2.x Schema Validator 实现。 */
public final class NetworkntVda5050SchemaValidator
    implements Vda5050SchemaValidator {
    private static final String TIMESTAMP_REQUIREMENT = "VDA3-SHARED-001";
    private static final Map<TopicName, String> TOPIC_REQUIREMENTS =
        topicRequirements();

    private final Vda5050JsonCodec preflightCodec;
    private final Vda5050SchemaRegistry schemaRegistry;

    /** @param limits Schema 解析前执行的 JSON 资源硬上限 */
    public NetworkntVda5050SchemaValidator(JsonCodecLimits limits) {
        this.preflightCodec = Vda5050JsonCodec.create(
            Objects.requireNonNull(limits, "limits")
        );
        this.schemaRegistry = Vda5050SchemaRegistry.create();
    }

    @Override
    public List<ValidationIssue> validate(TopicName topic, byte[] payload) {
        Objects.requireNonNull(topic, "topic");
        Objects.requireNonNull(payload, "payload");

        DecodingResult<Object> preflight = preflightCodec.decode(
            topic,
            payload,
            Object.class
        );
        if (!preflight.isDecoded()) {
            return preflight.issues();
        }

        List<Error> errors = schemaRegistry.schemaFor(topic).validate(
            new String(payload, StandardCharsets.UTF_8),
            InputFormat.JSON
        );
        return errors.stream().map(error -> mapIssue(topic, error)).toList();
    }

    private static ValidationIssue mapIssue(TopicName topic, Error error) {
        String path = pathOf(error);
        return new ValidationIssue(
            codeOf(error),
            ValidationSeverity.ERROR,
            path,
            "JSON value does not satisfy the bundled schema constraint",
            requirementOf(topic, error)
        );
    }

    static String codeOf(Error error) {
        String keyword = error.getKeyword();
        if (keyword == null || keyword.isBlank()) {
            return "SCHEMA_VIOLATION";
        }
        String normalized = keyword
            .replaceAll("([a-z0-9])([A-Z])", "$1_$2")
            .replaceAll("[^A-Za-z0-9]+", "_")
            .toUpperCase(Locale.ROOT);
        return "SCHEMA_" + normalized;
    }

    private static String pathOf(Error error) {
        String path = error.getInstanceLocation().toString();
        if ("required".equals(error.getKeyword())) {
            path += "/" + escapePointer(error.getProperty());
        }
        return path;
    }

    private static String requirementOf(
        TopicName topic,
        Error error
    ) {
        if ("format".equals(error.getKeyword())) {
            return TIMESTAMP_REQUIREMENT;
        }
        return TOPIC_REQUIREMENTS.get(topic);
    }

    private static String escapePointer(String value) {
        return value.replace("~", "~0").replace("/", "~1");
    }

    private static Map<TopicName, String> topicRequirements() {
        EnumMap<TopicName, String> requirements = new EnumMap<>(TopicName.class);
        requirements.put(TopicName.CONNECTION, "VDA3-CONNECTION-001");
        requirements.put(TopicName.FACTSHEET, "VDA3-FACTSHEET-001");
        requirements.put(
            TopicName.INSTANT_ACTIONS,
            "VDA3-INSTANT_ACTIONS-001"
        );
        requirements.put(TopicName.ORDER, "VDA3-ORDER-001");
        requirements.put(TopicName.RESPONSES, "VDA3-RESPONSES-001");
        requirements.put(TopicName.STATE, "VDA3-STATE-001");
        requirements.put(TopicName.VISUALIZATION, "VDA3-VISUALIZATION-001");
        requirements.put(TopicName.ZONE_SET, "VDA3-ZONE_SET-001");
        return Map.copyOf(requirements);
    }
}
