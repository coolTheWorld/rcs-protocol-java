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
import io.github.cooltheworld.rcs.protocol.vda5050.v3.topic.TopicAddress;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.topic.TopicLayout;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.topic.TopicName;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.ConnectionValidator;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.RejectedInboundMessage;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.ValidatedMessage;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.ValidationIssue;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.ValidationResult;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
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

    @Test
    @DisplayName("[VDA3-CONNECTION-001] 相同连接状态重发只更新最近消息")
    void updatesRepeatedConnectionWithoutDuplicateStateChangeEffect() {
        FleetControlTransition first = transition(
            recoveringState(),
            validatedConnection(
                ConnectionState.ONLINE,
                7L,
                "2026-08-10T04:59:59.123Z"
            )
        );
        ValidatedMessage<Connection> repeated = validatedConnection(
            ConnectionState.ONLINE,
            8L,
            "2026-08-10T05:00:01.123Z"
        );

        FleetControlTransition second = transition(first.state(), repeated);

        assertAll(
            () -> assertSame(repeated.message(), second.state().lastConnection()),
            () -> assertEquals(8L, second.state().lastConnection().header().headerId()),
            () -> assertTrue(second.effects().isEmpty()),
            () -> assertTrue(second.issues().isEmpty())
        );
    }

    @Test
    @DisplayName("[VDA3-CONNECTION-001] Last Will 的陈旧消息头仍切换 CONNECTION_BROKEN")
    void acceptsBrokenConnectionWithStaleLastWillHeader() {
        FleetControlTransition online = transition(
            recoveringState(),
            validatedConnection(
                ConnectionState.ONLINE,
                99L,
                "2026-08-10T05:00:00.123Z"
            )
        );
        ValidatedMessage<Connection> staleLastWill = validatedConnection(
            ConnectionState.CONNECTION_BROKEN,
            1L,
            "2026-08-09T05:00:00.123Z"
        );

        FleetControlTransition broken = transition(
            online.state(),
            staleLastWill
        );

        FleetControlEffect.ConnectionStateChanged effect = assertInstanceOf(
            FleetControlEffect.ConnectionStateChanged.class,
            broken.effects().getFirst()
        );
        assertAll(
            () -> assertEquals(
                ConnectionState.CONNECTION_BROKEN,
                broken.state().connectionState()
            ),
            () -> assertSame(staleLastWill.message(), broken.state().lastConnection()),
            () -> assertEquals(ConnectionState.ONLINE, effect.previousState()),
            () -> assertTrue(broken.issues().isEmpty())
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-008] 前三层拒绝输入保持 State 并产生安全诊断 Effect")
    void preservesStateWhenARejectedConnectionArrives() {
        FleetControlState initial = recoveringState();
        RejectedInboundMessage<Connection> rejected = rejectedConnection();
        FleetControlEvent event = new FleetControlEvent.ConnectionRejected(
            rejected,
            OCCURRED_AT
        );

        FleetControlTransition transition = stateMachine.transition(initial, event);

        FleetControlEffect.InboundMessageRejected effect = assertInstanceOf(
            FleetControlEffect.InboundMessageRejected.class,
            transition.effects().getFirst()
        );
        assertAll(
            () -> assertSame(initial, transition.state()),
            () -> assertEquals(rejected.issues(), transition.issues()),
            () -> assertEquals(TopicName.CONNECTION, effect.topic()),
            () -> assertEquals(rejected.robotIdentity(), effect.robotIdentity()),
            () -> assertEquals(rejected.headerId(), effect.headerId()),
            () -> assertEquals(rejected.issues(), effect.issues()),
            () -> assertEquals(OCCURRED_AT, effect.occurredAt())
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-004] 有效消息误投其他 Robot Session 时执行第四层拒绝")
    void rejectsAValidatedConnectionForAnotherRobotSession() {
        FleetControlState initial = recoveringState();
        RobotIdentity otherRobot = new RobotIdentity("Other", "R-002");
        ValidatedMessage<Connection> misrouted = validatedConnection(
            ConnectionState.ONLINE,
            7L,
            "2026-08-10T04:59:59.123Z",
            otherRobot,
            false
        );

        FleetControlTransition transition = transition(initial, misrouted);

        ValidationIssue issue = transition.issues().getFirst();
        FleetControlEffect.InboundMessageRejected effect = assertInstanceOf(
            FleetControlEffect.InboundMessageRejected.class,
            transition.effects().getFirst()
        );
        assertAll(
            () -> assertSame(initial, transition.state()),
            () -> assertEquals(1, transition.issues().size()),
            () -> assertEquals("SESSION_ROBOT_IDENTITY_MISMATCH", issue.code()),
            () -> assertEquals("VDA3-SHARED-004", issue.requirementId()),
            () -> assertEquals(otherRobot, effect.robotIdentity()),
            () -> assertEquals(7L, effect.headerId())
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-007] Connection 未知扩展只产生不含值的诊断 Effect")
    void reportsUnknownConnectionExtensionsWithoutExposingValues() {
        ValidatedMessage<Connection> connection = validatedConnection(
            ConnectionState.ONLINE,
            7L,
            "2026-08-10T04:59:59.123Z",
            ROBOT,
            true
        );

        FleetControlTransition transition = transition(
            recoveringState(),
            connection
        );

        FleetControlEffect.UnknownExtensionObserved effect = assertInstanceOf(
            FleetControlEffect.UnknownExtensionObserved.class,
            transition.effects().get(1)
        );
        assertAll(
            () -> assertEquals(2, transition.effects().size()),
            () -> assertEquals(TopicName.CONNECTION, effect.topic()),
            () -> assertEquals(ROBOT, effect.robotIdentity()),
            () -> assertEquals(7L, effect.headerId()),
            () -> assertEquals(OCCURRED_AT, effect.occurredAt())
        );
    }

    @Test
    @DisplayName("[VDA3-CONNECTION-001] 相同 State 与 Event 的重放结果完全确定")
    void replayIsDeterministicAndIndependentFromLogging() {
        FleetControlState state = recoveringState();
        FleetControlEvent event = new FleetControlEvent.ConnectionReceived(
            validatedConnection(
                ConnectionState.HIBERNATING,
                7L,
                "2026-08-10T04:59:59.123Z"
            ),
            OCCURRED_AT
        );

        FleetControlTransition first = stateMachine.transition(state, event);
        FleetControlTransition second = stateMachine.transition(state, event);

        assertEquals(first, second);
    }

    @Test
    void rejectsMissingProgrammingArguments() {
        ValidatedMessage<Connection> connection = validatedConnection(
            ConnectionState.ONLINE,
            7L,
            "2026-08-10T04:59:59.123Z"
        );
        RejectedInboundMessage<Connection> rejected = rejectedConnection();

        assertAll(
            () -> assertThrows(
                NullPointerException.class,
                () -> stateMachine.transition(null, new FleetControlEvent.ConnectionReceived(
                    connection,
                    OCCURRED_AT
                ))
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> stateMachine.transition(recoveringState(), null)
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> new FleetControlEvent.ConnectionReceived(null, OCCURRED_AT)
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> new FleetControlEvent.ConnectionRejected(null, OCCURRED_AT)
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> new FleetControlEvent.ConnectionRejected(rejected, null)
            )
        );
    }

    private static FleetControlState recoveringState() {
        return FleetControlState.recovering(
            ROBOT,
            ProtocolVersionProfile.V3_0_0
        );
    }

    private FleetControlTransition transition(
        FleetControlState state,
        ValidatedMessage<Connection> message
    ) {
        return stateMachine.transition(
            state,
            new FleetControlEvent.ConnectionReceived(message, OCCURRED_AT)
        );
    }

    @SuppressWarnings("unchecked")
    private static ValidatedMessage<Connection> validatedConnection(
        ConnectionState connectionState,
        long headerId,
        String timestamp
    ) {
        return validatedConnection(
            connectionState,
            headerId,
            timestamp,
            ROBOT,
            false
        );
    }

    @SuppressWarnings("unchecked")
    private static ValidatedMessage<Connection> validatedConnection(
        ConnectionState connectionState,
        long headerId,
        String timestamp,
        RobotIdentity robotIdentity,
        boolean withExtension
    ) {
        String extension = withExtension
            ? ",\n  \"vendorConnection\": {\"sensitiveValue\": \"must-not-log\"}"
            : "";
        String payload = """
            {
              "headerId": %d,
              "timestamp": "%s",
              "version": "3.0.0",
              "manufacturer": "%s",
              "serialNumber": "%s",
              "connectionState": "%s"%s
            }
            """.formatted(
                headerId,
                timestamp,
                robotIdentity.manufacturer(),
                robotIdentity.serialNumber(),
                connectionState.name(),
                extension
            );
        String topicPath = TopicLayout.format(
            DefaultTopicLayout.standard(),
            new TopicAddress(robotIdentity, TopicName.CONNECTION)
        );
        ValidationResult<Connection> result = ConnectionValidator.createDefault()
            .validate(
                DefaultTopicLayout.standard(),
                topicPath,
                payload.getBytes(StandardCharsets.UTF_8)
            );
        return (ValidatedMessage<Connection>) assertInstanceOf(
            ValidatedMessage.class,
            result
        );
    }

    @SuppressWarnings("unchecked")
    private static RejectedInboundMessage<Connection> rejectedConnection() {
        String payload = """
            {
              "headerId": 7,
              "timestamp": "2026-08-10T04:59:59.123Z",
              "version": "3.1.0",
              "manufacturer": "Acme",
              "serialNumber": "R-001",
              "connectionState": "ONLINE"
            }
            """;
        ValidationResult<Connection> result = ConnectionValidator.createDefault()
            .validate(
                DefaultTopicLayout.standard(),
                "vda5050/v3/Acme/R-001/connection",
                payload.getBytes(StandardCharsets.UTF_8)
            );
        return (RejectedInboundMessage<Connection>) assertInstanceOf(
            RejectedInboundMessage.class,
            result
        );
    }
}
