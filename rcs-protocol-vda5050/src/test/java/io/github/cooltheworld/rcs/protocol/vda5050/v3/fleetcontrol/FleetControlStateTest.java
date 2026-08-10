package io.github.cooltheworld.rcs.protocol.vda5050.v3.fleetcontrol;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.connection.Connection;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.connection.ConnectionState;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.common.ProtocolHeader;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.common.ProtocolTimestamp;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.common.ProtocolVersion;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.common.ProtocolVersionProfile;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.common.RobotIdentity;
import java.time.Instant;
import org.junit.jupiter.api.Test;

final class FleetControlStateTest {
    private static final RobotIdentity ROBOT = new RobotIdentity("Acme", "R-001");

    @Test
    void enforcesSessionIdentityAndVersionInvariants() {
        Connection otherRobot = connection(
            new RobotIdentity("Other", "R-002"),
            ProtocolVersion.parse("3.0.0")
        );
        Connection unsupportedVersion = connection(
            ROBOT,
            ProtocolVersion.parse("3.1.0")
        );

        assertAll(
            () -> assertThrows(
                IllegalArgumentException.class,
                () -> stateBuilder().lastConnection(otherRobot).build()
            ),
            () -> assertThrows(
                IllegalArgumentException.class,
                () -> stateBuilder().lastConnection(unsupportedVersion).build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> FleetControlState.builder()
                    .versionProfile(ProtocolVersionProfile.V3_0_0)
                    .build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> FleetControlState.builder()
                    .robotIdentity(ROBOT)
                    .build()
            )
        );
    }

    @Test
    void comparesStatesByTheirCompleteStronglyTypedContent() {
        FleetControlState first = stateBuilder().build();
        FleetControlState same = first.toBuilder().build();
        FleetControlState ready = first.toBuilder().recovering(false).build();

        assertAll(
            () -> assertEquals(first, same),
            () -> assertEquals(first.hashCode(), same.hashCode()),
            () -> assertNotEquals(first, ready),
            () -> assertNotEquals(first, null)
        );
    }

    private static FleetControlState.Builder stateBuilder() {
        return FleetControlState.builder()
            .robotIdentity(ROBOT)
            .versionProfile(ProtocolVersionProfile.V3_0_0);
    }

    private static Connection connection(
        RobotIdentity robotIdentity,
        ProtocolVersion version
    ) {
        ProtocolHeader header = ProtocolHeader.builder()
            .headerId(7L)
            .timestamp(ProtocolTimestamp.from(
                Instant.parse("2026-08-10T04:59:59.123Z")
            ))
            .version(version)
            .robotIdentity(robotIdentity)
            .build();
        return Connection.builder()
            .header(header)
            .connectionState(ConnectionState.ONLINE)
            .build();
    }
}
