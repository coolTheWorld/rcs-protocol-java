package io.github.cooltheworld.rcs.protocol.vda5050.v3.validation;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.ProtocolTimestamp;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.ProtocolVersion;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.RobotIdentity;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.topic.TopicName;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

final class RejectedInboundMessageTest {
    private static final ValidationIssue ERROR = new ValidationIssue(
        "UNSUPPORTED_PROTOCOL_VERSION",
        ValidationSeverity.ERROR,
        "/version",
        "Protocol version has no explicit support profile",
        "VDA3-SHARED-003"
    );

    @Test
    @DisplayName("[VDA3-SHARED-008] 非法输入以带安全强类型上下文的数据表达")
    void representsRejectedInputAsStructuredData() {
        RobotIdentity identity = new RobotIdentity("ACME", "SN-1");
        ProtocolTimestamp timestamp = ProtocolTimestamp.from(
            Instant.parse("2026-08-07T05:00:00.123Z")
        );
        ProtocolVersion version = ProtocolVersion.parse("3.1.0");

        ValidationResult<String> result = RejectedInboundMessage.<String>builder(
            TopicName.CONNECTION,
            List.of(ERROR)
        )
            .robotIdentity(identity)
            .headerId(42L)
            .timestamp(timestamp)
            .version(version)
            .build();

        RejectedInboundMessage<String> rejected = (RejectedInboundMessage<String>) result;
        assertAll(
            () -> assertFalse(result.isAccepted()),
            () -> assertFalse(rejected.isDecoded()),
            () -> assertEquals(TopicName.CONNECTION, rejected.topic()),
            () -> assertEquals(identity, rejected.robotIdentity()),
            () -> assertEquals(42L, rejected.headerId()),
            () -> assertEquals(timestamp, rejected.timestamp()),
            () -> assertEquals(version, rejected.version()),
            () -> assertEquals(List.of(ERROR), rejected.issues()),
            () -> assertThrows(
                UnsupportedOperationException.class,
                () -> rejected.issues().add(ERROR)
            )
        );
    }

    @Test
    void allowsUnavailableHeaderContextToRemainAbsent() {
        RejectedInboundMessage<String> rejected = RejectedInboundMessage.<String>builder(
            TopicName.ORDER,
            List.of(ERROR)
        ).headerId(null).build();

        assertAll(
            () -> assertNull(rejected.robotIdentity()),
            () -> assertNull(rejected.headerId()),
            () -> assertNull(rejected.timestamp()),
            () -> assertNull(rejected.version())
        );
    }

    @Test
    void rejectsResultsWithoutAnError() {
        ValidationIssue warning = new ValidationIssue(
            "IGNORED_EXTENSION",
            ValidationSeverity.WARNING,
            "/vendorField",
            "Unknown extension has no executable semantics",
            "VDA3-SHARED-007"
        );

        assertAll(
            () -> assertThrows(
                IllegalArgumentException.class,
                () -> RejectedInboundMessage.<String>builder(
                    TopicName.STATE,
                    List.of()
                ).build()
            ),
            () -> assertThrows(
                IllegalArgumentException.class,
                () -> RejectedInboundMessage.<String>builder(
                    TopicName.STATE,
                    List.of(warning)
                ).build()
            )
        );
    }

    @Test
    void refusesUnsafeHeaderIdContext() {
        assertAll(
            () -> assertThrows(
                IllegalArgumentException.class,
                () -> RejectedInboundMessage.<String>builder(
                    TopicName.CONNECTION,
                    List.of(ERROR)
                ).headerId(-1L)
            ),
            () -> assertThrows(
                IllegalArgumentException.class,
                () -> RejectedInboundMessage.<String>builder(
                    TopicName.CONNECTION,
                    List.of(ERROR)
                ).headerId(4_294_967_296L)
            )
        );
    }

    @Test
    void rejectsMissingRequiredRejectionState() {
        assertAll(
            () -> assertThrows(
                NullPointerException.class,
                () -> RejectedInboundMessage.<String>builder(null, List.of(ERROR))
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> RejectedInboundMessage.<String>builder(TopicName.STATE, null)
            )
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-008] 拒绝对象不暴露 payload 或动态 JSON 容器")
    void exposesNoDynamicPayloadState() {
        boolean hasUnsafeField = Arrays.stream(
            RejectedInboundMessage.class.getDeclaredFields()
        ).map(Field::getType).anyMatch(type ->
            JsonNode.class.isAssignableFrom(type)
                || Map.class.isAssignableFrom(type)
                || byte[].class.equals(type)
        );

        assertFalse(hasUnsafeField);
    }
}
