package io.github.cooltheworld.rcs.protocol.vda5050.v3.validation;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.codec.JsonCodecLimits;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.common.ProtocolVersionProfile;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.Factsheet;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.topic.DefaultTopicLayout;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.topic.TopicLayout;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.topic.TopicName;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

final class FactsheetValidatorTest {
    private static final String FIXTURE =
        "vda5050/v3.0.0/fixtures/factsheet/factsheet-cases.json";
    private static final String FACTSHEET_TOPIC =
        "vda5050/v3/ACME/R-1/factsheet";
    private static final TopicLayout TOPIC_LAYOUT = DefaultTopicLayout.standard();
    private static final ObjectMapper TEST_MAPPER = JsonMapper.builder().build();

    private final FactsheetValidator validator = FactsheetValidator.createDefault();

    @Test
    @DisplayName("[VDA3-SHARED-008] 只有全部前三层通过的 Factsheet 获得凭证")
    void mintsCredentialOnlyAfterEveryValidationLayerPasses() throws Exception {
        byte[] payload = fixture("/valid");
        byte[] snapshot = Arrays.copyOf(payload, payload.length);

        ValidationResult<Factsheet> result = validator.validate(
            TOPIC_LAYOUT,
            FACTSHEET_TOPIC,
            payload
        );
        ValidatedMessage<?> validated = assertInstanceOf(
            ValidatedMessage.class,
            result
        );
        Factsheet factsheet = assertInstanceOf(
            Factsheet.class,
            validated.message()
        );
        ValidatedMessage<?> repeated = assertInstanceOf(
            ValidatedMessage.class,
            validator.validate(TOPIC_LAYOUT, FACTSHEET_TOPIC, payload)
        );

        assertAll(
            () -> assertTrue(result.isAccepted()),
            () -> assertEquals(4_294_967_295L, factsheet.header().headerId()),
            () -> assertEquals(
                ProtocolVersionProfile.V3_0_0,
                validated.versionProfile()
            ),
            () -> assertEquals(factsheet, repeated.message()),
            () -> assertEquals(List.of(), validated.issues()),
            () -> assertThrows(
                UnsupportedOperationException.class,
                validated.issues()::clear
            ),
            () -> assertArrayEquals(snapshot, payload)
        );

        assertTrue(validator.validate(
            TOPIC_LAYOUT,
            "vda5050/v3/A/1/factsheet",
            fixture("/boundary")
        ).isAccepted());
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] 语法、资源与 Schema 错误在绑定前返回")
    void returnsSyntaxResourceAndSchemaFailuresBeforeBinding() throws Exception {
        ObjectNode missingField = validTree();
        missingField.remove("loadSpecification");
        byte[] validPayload = fixture("/valid");
        FactsheetValidator limited = FactsheetValidator.create(
            JsonCodecLimits.builder()
                .maxPayloadBytes(validPayload.length - 1)
                .build()
        );

        RejectedInboundMessage<?> syntax = rejected(validator.validate(
            TOPIC_LAYOUT,
            FACTSHEET_TOPIC,
            "{".getBytes(StandardCharsets.UTF_8)
        ));
        RejectedInboundMessage<?> resource = rejected(limited.validate(
            TOPIC_LAYOUT,
            FACTSHEET_TOPIC,
            validPayload
        ));
        RejectedInboundMessage<?> schema = rejected(validator.validate(
            TOPIC_LAYOUT,
            FACTSHEET_TOPIC,
            TEST_MAPPER.writeValueAsBytes(missingField)
        ));

        assertAll(
            () -> assertEquals("INVALID_JSON", syntax.issues().getFirst().code()),
            () -> assertEquals(
                "PAYLOAD_TOO_LARGE",
                resource.issues().getFirst().code()
            ),
            () -> assertEquals(
                "SCHEMA_REQUIRED",
                schema.issues().getFirst().code()
            ),
            () -> assertEquals(
                "/loadSpecification",
                schema.issues().getFirst().path()
            ),
            () -> assertNull(schema.robotIdentity()),
            () -> assertNull(schema.headerId()),
            () -> assertEquals(TopicName.FACTSHEET, schema.topic())
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-001] Schema 合法时间仍须通过严格 Header 绑定")
    void rejectsSchemaValidButProtocolInvalidTimestamp() throws Exception {
        ObjectNode root = validTree();
        root.put("timestamp", "2026-08-11T03:00:00Z");
        byte[] payload = TEST_MAPPER.writeValueAsBytes(root);

        assertTrue(Vda5050SchemaValidator.createDefault()
            .validate(TopicName.FACTSHEET, payload)
            .isEmpty());
        RejectedInboundMessage<?> rejected = rejected(validator.validate(
            TOPIC_LAYOUT,
            FACTSHEET_TOPIC,
            payload
        ));

        assertAll(
            () -> assertEquals(
                "INVALID_JSON_TYPE",
                rejected.issues().getFirst().code()
            ),
            () -> assertEquals(
                "/timestamp",
                rejected.issues().getFirst().path()
            ),
            () -> assertNull(rejected.robotIdentity())
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-002/003] Header 范围与显式版本配置都必须通过")
    void rejectsInvalidHeaderIdAndUnsupportedVersion() throws Exception {
        ObjectNode invalidHeaderId = validTree();
        invalidHeaderId.put("headerId", 4_294_967_296L);
        byte[] invalidHeaderPayload = TEST_MAPPER.writeValueAsBytes(
            invalidHeaderId
        );
        ObjectNode unsupportedVersion = validTree();
        unsupportedVersion.put("version", "3.1.0");
        byte[] unsupportedPayload = TEST_MAPPER.writeValueAsBytes(
            unsupportedVersion
        );

        assertAll(
            () -> assertTrue(Vda5050SchemaValidator.createDefault()
                .validate(TopicName.FACTSHEET, invalidHeaderPayload)
                .isEmpty()),
            () -> assertTrue(Vda5050SchemaValidator.createDefault()
                .validate(TopicName.FACTSHEET, unsupportedPayload)
                .isEmpty())
        );
        RejectedInboundMessage<?> header = rejected(validator.validate(
            TOPIC_LAYOUT,
            FACTSHEET_TOPIC,
            invalidHeaderPayload
        ));
        RejectedInboundMessage<?> version = rejected(validator.validate(
            TOPIC_LAYOUT,
            FACTSHEET_TOPIC,
            unsupportedPayload
        ));

        assertAll(
            () -> assertEquals("UINT32_OUT_OF_RANGE", header.issues().getFirst().code()),
            () -> assertNull(header.headerId()),
            () -> assertEquals("ACME", header.robotIdentity().manufacturer()),
            () -> assertEquals(
                "UNSUPPORTED_PROTOCOL_VERSION",
                version.issues().getFirst().code()
            ),
            () -> assertEquals("3.1.0", version.version().toString()),
            () -> assertEquals(4_294_967_295L, version.headerId())
        );
    }

    @ParameterizedTest(name = "[VDA3-SHARED-004/011] Topic 拒绝 {0}")
    @MethodSource("invalidTopics")
    void rejectsInvalidTopicSemantics(
        String topicPath,
        String expectedCode,
        String expectedPath
    ) throws Exception {
        RejectedInboundMessage<?> rejected = rejected(validator.validate(
            TOPIC_LAYOUT,
            topicPath,
            fixture("/valid")
        ));

        assertAll(
            () -> assertEquals(expectedCode, rejected.issues().getFirst().code()),
            () -> assertEquals(expectedPath, rejected.issues().getFirst().path()),
            () -> assertEquals("ACME", rejected.robotIdentity().manufacturer())
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] 聚合全部片段语义并使用根 JSON Pointer")
    void aggregatesEveryFragmentValidatorWithRootPaths() throws Exception {
        byte[] payload = semanticFailurePayload();
        assertTrue(Vda5050SchemaValidator.createDefault()
            .validate(TopicName.FACTSHEET, payload)
            .isEmpty());

        RejectedInboundMessage<?> rejected = rejected(validator.validate(
            TOPIC_LAYOUT,
            FACTSHEET_TOPIC,
            payload
        ));

        assertAll(
            () -> assertFalse(rejected.isAccepted()),
            () -> assertEquals(
                List.of(
                    "INVALID_LINEAR_SPEED_RANGE",
                    "DUPLICATE_OPTIONAL_PARAMETER",
                    "SELF_INTERSECTING_ENVELOPE2D_POLYGON",
                    "DUPLICATE_LOAD_SET_NAME",
                    "INVALID_CHARGING_RANGE"
                ),
                rejected.issues().stream().map(ValidationIssue::code).toList()
            ),
            () -> assertEquals(
                List.of(
                    "/physicalParameters/maximumSpeed",
                    "/protocolFeatures/optionalParameters/1/parameter",
                    "/mobileRobotGeometry/envelopes2d/0/vertices",
                    "/loadSpecification/loadSets/1/setName",
                    "/mobileRobotConfiguration/batteryCharging/maximumDesiredChargingLevel"
                ),
                rejected.issues().stream().map(ValidationIssue::path).toList()
            ),
            () -> assertTrue(rejected.issues().stream().noneMatch(issue ->
                issue.description().contains("secret-extension-value")
            )),
            () -> assertEquals(4_294_967_295L, rejected.headerId())
        );
    }

    @Test
    void rejectsMissingProgrammingArguments() throws Exception {
        byte[] payload = fixture("/valid");

        assertAll(
            () -> assertThrows(
                NullPointerException.class,
                () -> FactsheetValidator.create(null)
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> validator.validate(null, FACTSHEET_TOPIC, payload)
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> validator.validate(TOPIC_LAYOUT, null, payload)
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> validator.validate(TOPIC_LAYOUT, FACTSHEET_TOPIC, null)
            )
        );
    }

    private static Stream<Arguments> invalidTopics() {
        return Stream.of(
            Arguments.of(
                "vda5050/v3/ACME/R-1/state",
                "TOPIC_MESSAGE_TYPE_MISMATCH",
                ""
            ),
            Arguments.of(
                "vda5050/v3/acme/R-1/factsheet",
                "TOPIC_HEADER_MISMATCH",
                "/manufacturer"
            ),
            Arguments.of(
                "vda5050/v3/ACME/R-2/factsheet",
                "TOPIC_HEADER_MISMATCH",
                "/serialNumber"
            ),
            Arguments.of(
                "not-a-vda-topic",
                "INVALID_TOPIC_ADDRESS",
                ""
            )
        );
    }

    private static byte[] semanticFailurePayload() throws IOException {
        ObjectNode root = validTree();
        root.put("vendorSecret", "secret-extension-value");

        ObjectNode physical = object(root, "physicalParameters");
        physical.put("minimumSpeed", 2.0D);
        physical.put("maximumSpeed", 1.0D);

        ObjectNode features = object(root, "protocolFeatures");
        ObjectNode parameter = TEST_MAPPER.createObjectNode()
            .put("parameter", "order.nodes.nodePosition")
            .put("support", "SUPPORTED");
        ArrayNode parameters = TEST_MAPPER.createArrayNode()
            .add(parameter)
            .add(parameter.deepCopy());
        features.set("optionalParameters", parameters);

        ObjectNode geometry = object(root, "mobileRobotGeometry");
        ArrayNode vertices = TEST_MAPPER.createArrayNode();
        vertices.add(point(0.0D, 0.0D));
        vertices.add(point(2.0D, 2.0D));
        vertices.add(point(0.0D, 2.0D));
        vertices.add(point(2.0D, 0.0D));
        ObjectNode envelope = TEST_MAPPER.createObjectNode()
            .put("envelope2dId", "crossing");
        envelope.set("vertices", vertices);
        geometry.set(
            "envelopes2d",
            TEST_MAPPER.createArrayNode().add(envelope)
        );

        ObjectNode loads = object(root, "loadSpecification");
        ObjectNode loadSet = TEST_MAPPER.createObjectNode()
            .put("setName", "DUPLICATE")
            .put("loadType", "BOX");
        loads.set(
            "loadSets",
            TEST_MAPPER.createArrayNode()
                .add(loadSet)
                .add(loadSet.deepCopy())
        );

        ObjectNode charging = object(
            object(root, "mobileRobotConfiguration"),
            "batteryCharging"
        );
        charging.put("minimumDesiredChargingLevel", 80.0D);
        charging.put("maximumDesiredChargingLevel", 20.0D);
        return TEST_MAPPER.writeValueAsBytes(root);
    }

    private static ObjectNode point(double x, double y) {
        return TEST_MAPPER.createObjectNode().put("x", x).put("y", y);
    }

    private static ObjectNode object(ObjectNode parent, String name) {
        return (ObjectNode) parent.get(name);
    }

    private static RejectedInboundMessage<?> rejected(
        ValidationResult<Factsheet> result
    ) {
        return assertInstanceOf(RejectedInboundMessage.class, result);
    }

    private static ObjectNode validTree() throws IOException {
        return (ObjectNode) TEST_MAPPER.readTree(fixture("/valid"));
    }

    private static byte[] fixture(String pointer) throws IOException {
        try (InputStream input = FactsheetValidatorTest.class
            .getClassLoader()
            .getResourceAsStream(FIXTURE)) {
            if (input == null) {
                throw new IllegalArgumentException("Missing fixture: " + FIXTURE);
            }
            JsonNode fixture = TEST_MAPPER.readTree(input).at(pointer);
            if (fixture.isMissingNode()) {
                throw new IllegalArgumentException(
                    "Missing fixture case: " + pointer
                );
            }
            return TEST_MAPPER.writeValueAsBytes(fixture);
        }
    }
}
