package io.github.cooltheworld.rcs.protocol.vda5050.v3.model.common;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

final class ProtocolHeaderTest {
    private static final Long HEADER_ID = 42L;
    private static final ProtocolTimestamp TIMESTAMP = ProtocolTimestamp.from(
        Instant.parse("2026-08-07T05:00:00.123Z")
    );
    private static final ProtocolVersion VERSION = ProtocolVersion.parse("3.0.0");
    private static final RobotIdentity ROBOT_IDENTITY = new RobotIdentity("ACME", "SN-1");

    @Test
    @DisplayName("[VDA3-SHARED-006] Builder 组合公共协议消息头必填字段")
    void buildsCommonProtocolHeader() {
        ProtocolHeader header = completeBuilder().build();

        assertAll(
            () -> assertEquals(HEADER_ID, header.headerId()),
            () -> assertEquals(TIMESTAMP, header.timestamp()),
            () -> assertEquals(VERSION, header.version()),
            () -> assertEquals(ROBOT_IDENTITY, header.robotIdentity())
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-006] Header 保留未知但格式正确的协议版本")
    void preservesRepresentableUnknownProtocolVersion() {
        ProtocolVersion unknownVersion = ProtocolVersion.parse("3.1.0");

        ProtocolHeader header = completeBuilder().version(unknownVersion).build();

        assertEquals(unknownVersion, header.version());
    }

    @Test
    void rejectsEveryMissingRequiredField() {
        assertAll(
            () -> assertThrows(
                NullPointerException.class,
                () -> completeBuilder().headerId(null).build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> completeBuilder().timestamp(null).build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> completeBuilder().version(null).build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> completeBuilder().robotIdentity(null).build()
            )
        );
    }

    @Test
    void usesValueEquality() {
        ProtocolHeader header = completeBuilder().build();
        ProtocolHeader equalHeader = completeBuilder().build();
        ProtocolHeader differentHeader = completeBuilder().headerId(43L).build();

        assertEquals(header, header);
        assertEquals(header, equalHeader);
        assertEquals(header.hashCode(), equalHeader.hashCode());
        assertNotEquals(header, differentHeader);
        assertNotEquals(header, null);
        assertNotEquals(header, "header");
    }

    private static ProtocolHeader.Builder completeBuilder() {
        return ProtocolHeader.builder()
            .headerId(HEADER_ID)
            .timestamp(TIMESTAMP)
            .version(VERSION)
            .robotIdentity(ROBOT_IDENTITY);
    }
}
