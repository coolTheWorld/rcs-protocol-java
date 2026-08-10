package io.github.cooltheworld.rcs.protocol.vda5050.v3.mobilerobot;

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

final class MobileRobotStateTest {
    private static final RobotIdentity ROBOT = new RobotIdentity("Acme", "R-001");

    @Test
    void enforcesRequiredFieldsAndTheConnectionCounterRange() {
        assertAll(
            () -> assertThrows(
                NullPointerException.class,
                () -> MobileRobotState.builder()
                    .versionProfile(ProtocolVersionProfile.V3_0_0)
                    .build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> MobileRobotState.builder().robotIdentity(ROBOT).build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> stateBuilder().nextConnectionHeaderId(null).build()
            ),
            () -> assertThrows(
                IllegalArgumentException.class,
                () -> stateBuilder().nextConnectionHeaderId(-1L).build()
            ),
            () -> assertThrows(
                IllegalArgumentException.class,
                () -> stateBuilder().nextConnectionHeaderId(4_294_967_296L).build()
            )
        );
    }

    @Test
    void enforcesConnectionIdentityVersionAndLastWillState() {
        RobotIdentity otherRobot = new RobotIdentity("Other", "R-002");
        Connection validOnline = connection(
            ROBOT,
            ProtocolVersion.parse("3.0.0"),
            ConnectionState.ONLINE
        );
        Connection validBroken = connection(
            ROBOT,
            ProtocolVersion.parse("3.0.0"),
            ConnectionState.CONNECTION_BROKEN
        );

        assertAll(
            () -> assertThrows(
                IllegalArgumentException.class,
                () -> stateBuilder().lastConnection(connection(
                    otherRobot,
                    ProtocolVersion.parse("3.0.0"),
                    ConnectionState.ONLINE
                )).build()
            ),
            () -> assertThrows(
                IllegalArgumentException.class,
                () -> stateBuilder().lastConnection(connection(
                    ROBOT,
                    ProtocolVersion.parse("3.1.0"),
                    ConnectionState.ONLINE
                )).build()
            ),
            () -> assertThrows(
                IllegalArgumentException.class,
                () -> stateBuilder().lastConnection(validBroken).build()
            ),
            () -> assertThrows(
                IllegalArgumentException.class,
                () -> stateBuilder().connectionLastWill(validOnline).build()
            ),
            () -> assertThrows(
                IllegalArgumentException.class,
                () -> stateBuilder().connectionLastWill(connection(
                    otherRobot,
                    ProtocolVersion.parse("3.0.0"),
                    ConnectionState.CONNECTION_BROKEN
                )).build()
            ),
            () -> assertThrows(
                IllegalArgumentException.class,
                () -> stateBuilder().connectionLastWill(connection(
                    ROBOT,
                    ProtocolVersion.parse("3.1.0"),
                    ConnectionState.CONNECTION_BROKEN
                )).build()
            ),
            () -> assertEquals(
                validBroken,
                stateBuilder().connectionLastWill(validBroken).build()
                    .connectionLastWill()
            )
        );
    }

    @Test
    void comparesStatesByTheirCompleteStronglyTypedContent() {
        MobileRobotState first = stateBuilder().build();
        MobileRobotState same = first.toBuilder().build();
        MobileRobotState advanced = first.toBuilder()
            .nextConnectionHeaderId(1L)
            .build();

        assertAll(
            () -> assertEquals(first, same),
            () -> assertEquals(first.hashCode(), same.hashCode()),
            () -> assertNotEquals(first, advanced),
            () -> assertNotEquals(first, null)
        );
    }

    private static MobileRobotState.Builder stateBuilder() {
        return MobileRobotState.builder()
            .robotIdentity(ROBOT)
            .versionProfile(ProtocolVersionProfile.V3_0_0);
    }

    private static Connection connection(
        RobotIdentity robotIdentity,
        ProtocolVersion version,
        ConnectionState connectionState
    ) {
        ProtocolHeader header = ProtocolHeader.builder()
            .headerId(7L)
            .timestamp(ProtocolTimestamp.from(
                Instant.parse("2026-08-10T06:00:00.123Z")
            ))
            .version(version)
            .robotIdentity(robotIdentity)
            .build();
        return Connection.builder()
            .header(header)
            .connectionState(connectionState)
            .build();
    }
}
