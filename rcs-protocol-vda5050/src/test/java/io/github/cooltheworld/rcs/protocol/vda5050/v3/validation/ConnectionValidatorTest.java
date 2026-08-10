package io.github.cooltheworld.rcs.protocol.vda5050.v3.validation;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.connection.Connection;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.common.ProtocolVersionProfile;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.topic.DefaultTopicLayout;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.topic.TopicLayout;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.topic.TopicName;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

final class ConnectionValidatorTest {
    private static final TopicLayout TOPIC_LAYOUT = DefaultTopicLayout.standard();
    private static final String CONNECTION_TOPIC =
        "vda5050/v3/Acme/R-001/connection";

    private final ConnectionValidator validator =
        ConnectionValidator.createDefault();

    @Test
    @DisplayName("[VDA3-SHARED-008] 只有通过前三层校验的 Connection 获得成功凭证")
    void mintsCredentialOnlyAfterAllThreeValidationLayersPass() {
        ValidationResult<Connection> result = validator.validate(
            TOPIC_LAYOUT,
            CONNECTION_TOPIC,
            fixture("connection/valid/minimal.json")
        );

        ValidatedMessage<?> validated = assertInstanceOf(
            ValidatedMessage.class,
            result
        );
        Connection connection = assertInstanceOf(
            Connection.class,
            validated.message()
        );
        assertAll(
            () -> assertTrue(result.isAccepted()),
            () -> assertEquals(0L, connection.header().headerId()),
            () -> assertEquals(ProtocolVersionProfile.V3_0_0, validated.versionProfile()),
            () -> assertTrue(validated.issues().isEmpty())
        );
    }

    @Test
    @DisplayName("[VDA3-CONNECTION-001] Schema 错误独立返回且不执行后续语义")
    void returnsSchemaIssuesBeforeConnectionSemantics() {
        ValidationResult<Connection> result = validator.validate(
            TOPIC_LAYOUT,
            CONNECTION_TOPIC,
            fixture("connection/invalid/missing-connection-state.json")
        );

        RejectedInboundMessage<?> rejected = assertInstanceOf(
            RejectedInboundMessage.class,
            result
        );
        assertAll(
            () -> assertFalse(result.isAccepted()),
            () -> assertEquals("SCHEMA_REQUIRED", rejected.issues().getFirst().code()),
            () -> assertEquals("/connectionState", rejected.issues().getFirst().path()),
            () -> assertEquals(1, rejected.issues().size()),
            () -> assertNull(rejected.robotIdentity()),
            () -> assertNull(rejected.headerId())
        );
    }

    @ParameterizedTest(name = "[VDA3-SHARED-002] Connection headerId={0} 越界")
    @ValueSource(longs = {-1L, 4_294_967_296L})
    void rejectsOutOfRangeHeaderIdAtTheSemanticLayer(long headerId) {
        byte[] payload = replace(
            fixture("connection/valid/minimal.json"),
            "\"headerId\": 0",
            "\"headerId\": " + headerId
        );
        assertTrue(
            Vda5050SchemaValidator.createDefault()
                .validate(TopicName.CONNECTION, payload)
                .isEmpty()
        );

        RejectedInboundMessage<?> rejected = assertInstanceOf(
            RejectedInboundMessage.class,
            validator.validate(TOPIC_LAYOUT, CONNECTION_TOPIC, payload)
        );
        ValidationIssue issue = rejected.issues().getFirst();
        assertAll(
            () -> assertEquals("UINT32_OUT_OF_RANGE", issue.code()),
            () -> assertEquals("/headerId", issue.path()),
            () -> assertEquals("VDA3-SHARED-002", issue.requirementId()),
            () -> assertNull(rejected.headerId()),
            () -> assertEquals("Acme", rejected.robotIdentity().manufacturer())
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-002] Connection headerId 接受 uint32 最大值")
    void acceptsMaximumUint32HeaderId() {
        byte[] payload = replace(
            fixture("connection/valid/minimal.json"),
            "\"headerId\": 0",
            "\"headerId\": 4294967295"
        );

        ValidatedMessage<?> validated = assertInstanceOf(
            ValidatedMessage.class,
            validator.validate(TOPIC_LAYOUT, CONNECTION_TOPIC, payload)
        );
        Connection connection = assertInstanceOf(
            Connection.class,
            validated.message()
        );

        assertEquals(4_294_967_295L, connection.header().headerId());
    }

    @Test
    @DisplayName("[VDA3-SHARED-003] 未注册版本返回稳定 Issue 且不产生凭证")
    void rejectsUnsupportedProtocolVersion() {
        byte[] payload = replace(
            fixture("connection/valid/minimal.json"),
            "\"version\": \"3.0.0\"",
            "\"version\": \"3.1.0\""
        );

        RejectedInboundMessage<?> rejected = assertInstanceOf(
            RejectedInboundMessage.class,
            validator.validate(TOPIC_LAYOUT, CONNECTION_TOPIC, payload)
        );
        ValidationIssue issue = rejected.issues().getFirst();
        assertAll(
            () -> assertEquals("UNSUPPORTED_PROTOCOL_VERSION", issue.code()),
            () -> assertEquals("/version", issue.path()),
            () -> assertEquals("VDA3-SHARED-003", issue.requirementId()),
            () -> assertEquals("3.1.0", rejected.version().toString())
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-001] Schema 较宽的 date-time 仍不能绕过严格时间戳类型")
    void rejectsSchemaValidButProtocolInvalidTimestamp() {
        byte[] payload = replace(
            fixture("connection/valid/minimal.json"),
            "2026-08-07T08:00:00.123Z",
            "2026-08-07T08:00:00Z"
        );
        assertTrue(
            Vda5050SchemaValidator.createDefault()
                .validate(TopicName.CONNECTION, payload)
                .isEmpty()
        );

        RejectedInboundMessage<?> rejected = assertInstanceOf(
            RejectedInboundMessage.class,
            validator.validate(TOPIC_LAYOUT, CONNECTION_TOPIC, payload)
        );

        assertAll(
            () -> assertEquals("INVALID_JSON_TYPE", rejected.issues().getFirst().code()),
            () -> assertEquals("/timestamp", rejected.issues().getFirst().path())
        );
    }

    @ParameterizedTest(name = "[VDA3-SHARED-004] Topic/Header 不一致路径 {1}")
    @MethodSource("mismatchedTopicIdentities")
    void rejectsTopicIdentityThatDoesNotMatchTheHeader(
        String topicPath,
        String expectedPath
    ) {
        RejectedInboundMessage<?> rejected = assertInstanceOf(
            RejectedInboundMessage.class,
            validator.validate(
                TOPIC_LAYOUT,
                topicPath,
                fixture("connection/valid/minimal.json")
            )
        );
        ValidationIssue issue = rejected.issues().getFirst();
        assertAll(
            () -> assertEquals("TOPIC_HEADER_MISMATCH", issue.code()),
            () -> assertEquals(expectedPath, issue.path()),
            () -> assertEquals("VDA3-SHARED-004", issue.requirementId()),
            () -> assertEquals("Acme", rejected.robotIdentity().manufacturer())
        );
    }

    @Test
    @DisplayName("[VDA3-CONNECTION-001] Connection Validator 拒绝其他标准 Topic 路径")
    void rejectsAPathForAnotherStandardTopic() {
        RejectedInboundMessage<?> rejected = assertInstanceOf(
            RejectedInboundMessage.class,
            validator.validate(
                TOPIC_LAYOUT,
                "vda5050/v3/Acme/R-001/state",
                fixture("connection/valid/minimal.json")
            )
        );
        ValidationIssue issue = rejected.issues().getFirst();
        assertAll(
            () -> assertEquals("TOPIC_MESSAGE_TYPE_MISMATCH", issue.code()),
            () -> assertEquals("", issue.path()),
            () -> assertEquals("VDA3-CONNECTION-001", issue.requirementId())
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-011] 非法 Topic 路径作为结构化问题返回")
    void returnsMalformedTopicPathAsData() {
        RejectedInboundMessage<?> rejected = assertInstanceOf(
            RejectedInboundMessage.class,
            validator.validate(
                TOPIC_LAYOUT,
                "not-a-vda-topic",
                fixture("connection/valid/minimal.json")
            )
        );
        ValidationIssue issue = rejected.issues().getFirst();
        assertAll(
            () -> assertEquals("INVALID_TOPIC_ADDRESS", issue.code()),
            () -> assertEquals("", issue.path()),
            () -> assertEquals("VDA3-SHARED-011", issue.requirementId()),
            () -> assertEquals(0L, rejected.headerId())
        );
    }

    @Test
    void rejectsMissingProgrammingArguments() {
        byte[] payload = fixture("connection/valid/minimal.json");

        assertAll(
            () -> assertThrows(
                NullPointerException.class,
                () -> ConnectionValidator.create(null)
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> validator.validate(null, CONNECTION_TOPIC, payload)
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> validator.validate(TOPIC_LAYOUT, null, payload)
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> validator.validate(TOPIC_LAYOUT, CONNECTION_TOPIC, null)
            )
        );
    }

    private static byte[] replace(byte[] payload, String target, String replacement) {
        return new String(payload, StandardCharsets.UTF_8)
            .replace(target, replacement)
            .getBytes(StandardCharsets.UTF_8);
    }

    private static Stream<Arguments> mismatchedTopicIdentities() {
        return Stream.of(
            Arguments.of(
                "vda5050/v3/acme/R-001/connection",
                "/manufacturer"
            ),
            Arguments.of(
                "vda5050/v3/Acme/R-002/connection",
                "/serialNumber"
            )
        );
    }

    private static byte[] fixture(String path) {
        String resource = "vda5050/v3.0.0/fixtures/" + path;
        try (InputStream input = ConnectionValidatorTest.class
            .getClassLoader()
            .getResourceAsStream(resource)) {
            if (input == null) {
                throw new IllegalArgumentException("Missing fixture: " + resource);
            }
            return input.readAllBytes();
        } catch (IOException exception) {
            throw new IllegalStateException("Fixture cannot be read", exception);
        }
    }
}
