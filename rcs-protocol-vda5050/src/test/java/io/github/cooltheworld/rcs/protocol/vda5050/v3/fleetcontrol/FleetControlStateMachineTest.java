package io.github.cooltheworld.rcs.protocol.vda5050.v3.fleetcontrol;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.Connection;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.ConnectionState;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.ProtocolVersionProfile;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.RobotIdentity;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.topic.DefaultTopicLayout;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.ConnectionValidator;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.ValidatedMessage;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.ValidationResult;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

final class FleetControlStateMachineTest {
    private static final RobotIdentity ROBOT = new RobotIdentity("Acme", "R-001");
    private static final Instant OCCURRED_AT = Instant.parse(
        "2026-08-10T05:00:00.456Z"
    );

    private final FleetControlStateMachine stateMachine =
        FleetControlStateMachine.createDefault();

    @Test
    @DisplayName("[VDA3-CONNECTION-001] Fleet Control 初态显式恢复且不假定连接状态")
    void startsRecoveringWithoutAssumingAConnectionState() {
        FleetControlState state = recoveringState();

        assertAll(
            () -> assertTrue(state.isRecovering()),
            () -> assertEquals(ROBOT, state.robotIdentity()),
            () -> assertEquals(
                ProtocolVersionProfile.V3_0_0,
                state.versionProfile()
            ),
            () -> assertNull(state.lastConnection()),
            () -> assertNull(state.connectionState())
        );
    }

    @ParameterizedTest(name = "[VDA3-CONNECTION-001] Fleet Control 接收 {0}")
    @EnumSource(ConnectionState.class)
    void recordsEveryNormativeConnectionState(ConnectionState connectionState) {
        FleetControlState initial = recoveringState();
        ValidatedMessage<Connection> validated = validatedConnection(
            connectionState,
            7L,
            "2026-08-10T04:59:59.123Z"
        );
        FleetControlEvent event = new FleetControlEvent.ConnectionReceived(
            validated,
            OCCURRED_AT
        );

        FleetControlTransition transition = stateMachine.transition(initial, event);

        FleetControlEffect.ConnectionStateChanged effect = assertInstanceOf(
            FleetControlEffect.ConnectionStateChanged.class,
            transition.effects().getFirst()
        );
        assertAll(
            () -> assertTrue(transition.state().isRecovering()),
            () -> assertSame(
                validated.message(),
                transition.state().lastConnection()
            ),
            () -> assertEquals(
                connectionState,
                transition.state().connectionState()
            ),
            () -> assertTrue(transition.issues().isEmpty()),
            () -> assertEquals(1, transition.effects().size()),
            () -> assertNull(effect.previousState()),
            () -> assertSame(validated.message(), effect.connection()),
            () -> assertEquals(OCCURRED_AT, effect.occurredAt()),
            () -> assertThrows(
                UnsupportedOperationException.class,
                () -> transition.effects().add(effect)
            )
        );
    }

    private static FleetControlState recoveringState() {
        return FleetControlState.recovering(
            ROBOT,
            ProtocolVersionProfile.V3_0_0
        );
    }

    @SuppressWarnings("unchecked")
    private static ValidatedMessage<Connection> validatedConnection(
        ConnectionState connectionState,
        long headerId,
        String timestamp
    ) {
        String payload = """
            {
              "headerId": %d,
              "timestamp": "%s",
              "version": "3.0.0",
              "manufacturer": "Acme",
              "serialNumber": "R-001",
              "connectionState": "%s"
            }
            """.formatted(headerId, timestamp, connectionState.name());
        ValidationResult<Connection> result = ConnectionValidator.createDefault()
            .validate(
                DefaultTopicLayout.standard(),
                "vda5050/v3/Acme/R-001/connection",
                payload.getBytes(StandardCharsets.UTF_8)
            );
        return (ValidatedMessage<Connection>) assertInstanceOf(
            ValidatedMessage.class,
            result
        );
    }
}
