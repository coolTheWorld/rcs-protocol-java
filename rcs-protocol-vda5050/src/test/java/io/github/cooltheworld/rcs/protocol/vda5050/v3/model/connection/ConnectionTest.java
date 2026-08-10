package io.github.cooltheworld.rcs.protocol.vda5050.v3.model.connection;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.common.ProtocolHeader;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.common.ProtocolTimestamp;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.common.ProtocolVersion;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.common.RobotIdentity;
import java.time.Instant;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

final class ConnectionTest {
    private static final ProtocolHeader HEADER = ProtocolHeader.builder()
        .headerId(0L)
        .timestamp(ProtocolTimestamp.from(Instant.parse("2026-08-07T08:00:00.123Z")))
        .version(ProtocolVersion.parse("3.0.0"))
        .robotIdentity(new RobotIdentity("Acme", "R-001"))
        .build();

    @Test
    @DisplayName("[VDA3-CONNECTION-001] Connection 以不可变消息头和封闭状态构造")
    void buildsAnImmutableConnectionMessage() {
        Connection connection = Connection.builder()
            .header(HEADER)
            .connectionState(ConnectionState.ONLINE)
            .build();
        Connection equalConnection = Connection.builder()
            .header(HEADER)
            .connectionState(ConnectionState.ONLINE)
            .build();
        Connection differentConnection = Connection.builder()
            .header(HEADER)
            .connectionState(ConnectionState.OFFLINE)
            .build();

        assertAll(
            () -> assertSame(HEADER, connection.header()),
            () -> assertEquals(ConnectionState.ONLINE, connection.connectionState()),
            () -> assertTrue(connection.extensionFields().isEmpty()),
            () -> assertEquals(connection, equalConnection),
            () -> assertEquals(connection.hashCode(), equalConnection.hashCode()),
            () -> assertNotEquals(connection, differentConnection)
        );
    }

    @Test
    @DisplayName("[VDA3-CONNECTION-001] Connection 拒绝缺失的必填模型字段")
    void rejectsMissingRequiredModelFields() {
        assertAll(
            () -> assertThrows(
                NullPointerException.class,
                () -> Connection.builder()
                    .connectionState(ConnectionState.ONLINE)
                    .build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> Connection.builder().header(HEADER).build()
            )
        );
    }

    @Test
    @DisplayName("[VDA3-CONNECTION-001] ConnectionState 只包含正文规定的四个状态")
    void exposesOnlyTheNormativeConnectionStates() {
        assertEquals(
            Set.of(
                ConnectionState.ONLINE,
                ConnectionState.OFFLINE,
                ConnectionState.HIBERNATING,
                ConnectionState.CONNECTION_BROKEN
            ),
            Set.of(ConnectionState.values())
        );
    }
}
