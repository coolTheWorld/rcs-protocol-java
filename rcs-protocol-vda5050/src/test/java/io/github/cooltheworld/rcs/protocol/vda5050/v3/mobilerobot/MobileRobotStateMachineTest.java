package io.github.cooltheworld.rcs.protocol.vda5050.v3.mobilerobot;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.Connection;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.ConnectionState;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.ProtocolTimestamp;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.ProtocolVersionProfile;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.RobotIdentity;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

final class MobileRobotStateMachineTest {
    private static final RobotIdentity ROBOT = new RobotIdentity("Acme", "R-001");
    private static final Instant OCCURRED_AT = Instant.parse(
        "2026-08-10T06:00:00.123456Z"
    );
    private final MobileRobotStateMachine stateMachine =
        MobileRobotStateMachine.createDefault();

    @Test
    void startsRecoveringWithTheFirstConnectionHeaderId() {
        MobileRobotState state = recoveringState();

        assertAll(
            () -> assertTrue(state.isRecovering()),
            () -> assertEquals(0L, state.nextConnectionHeaderId()),
            () -> assertNull(state.lastConnection()),
            () -> assertNull(state.connectionLastWill())
        );
    }

    @Test
    @DisplayName("[VDA3-CONNECTION-001] 上线先配置 Last Will 再发布 ONLINE")
    void configuresBrokenLastWillBeforePublishingOnline() {
        MobileRobotState initial = recoveringState();

        MobileRobotTransition transition = stateMachine.transition(
            initial,
            new MobileRobotEvent.ConnectionOpeningRequested(OCCURRED_AT)
        );

        MobileRobotEffect.ConfigureConnectionLastWill configure = assertInstanceOf(
            MobileRobotEffect.ConfigureConnectionLastWill.class,
            transition.effects().get(0)
        );
        MobileRobotEffect.PublishConnection publish = assertInstanceOf(
            MobileRobotEffect.PublishConnection.class,
            transition.effects().get(1)
        );
        Connection lastWill = configure.connection();
        Connection online = publish.connection();
        ProtocolTimestamp expectedTimestamp = ProtocolTimestamp.from(OCCURRED_AT);
        assertAll(
            () -> assertEquals(2, transition.effects().size()),
            () -> assertTrue(transition.issues().isEmpty()),
            () -> assertEquals(ConnectionState.CONNECTION_BROKEN, lastWill.connectionState()),
            () -> assertEquals(0L, lastWill.header().headerId()),
            () -> assertEquals(expectedTimestamp, lastWill.header().timestamp()),
            () -> assertEquals(ConnectionState.ONLINE, online.connectionState()),
            () -> assertEquals(1L, online.header().headerId()),
            () -> assertEquals(expectedTimestamp, online.header().timestamp()),
            () -> assertEquals(2L, transition.state().nextConnectionHeaderId()),
            () -> assertSame(lastWill, transition.state().connectionLastWill()),
            () -> assertSame(online, transition.state().lastConnection()),
            () -> assertTrue(transition.state().isRecovering())
        );
    }

    private static MobileRobotState recoveringState() {
        return MobileRobotState.recovering(
            ROBOT,
            ProtocolVersionProfile.V3_0_0
        );
    }
}
