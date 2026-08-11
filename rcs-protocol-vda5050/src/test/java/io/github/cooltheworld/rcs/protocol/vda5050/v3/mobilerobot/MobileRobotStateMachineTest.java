package io.github.cooltheworld.rcs.protocol.vda5050.v3.mobilerobot;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.mobilerobot.effect.MobileRobotEffect;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.mobilerobot.event.MobileRobotEvent;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.connection.Connection;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.connection.ConnectionState;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.common.ProtocolTimestamp;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.common.ProtocolVersionProfile;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.common.RobotIdentity;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

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
            () -> assertEquals(ROBOT, lastWill.header().robotIdentity()),
            () -> assertEquals(
                ProtocolVersionProfile.V3_0_0.version(),
                lastWill.header().version()
            ),
            () -> assertEquals(ConnectionState.ONLINE, online.connectionState()),
            () -> assertEquals(1L, online.header().headerId()),
            () -> assertEquals(expectedTimestamp, online.header().timestamp()),
            () -> assertEquals(ROBOT, online.header().robotIdentity()),
            () -> assertEquals(
                ProtocolVersionProfile.V3_0_0.version(),
                online.header().version()
            ),
            () -> assertEquals(2L, transition.state().nextConnectionHeaderId()),
            () -> assertSame(lastWill, transition.state().connectionLastWill()),
            () -> assertSame(online, transition.state().lastConnection()),
            () -> assertTrue(transition.state().isRecovering())
        );
    }

    @ParameterizedTest(name = "主动发布 {0}")
    @EnumSource(
        value = ConnectionState.class,
        names = {"ONLINE", "OFFLINE", "HIBERNATING"}
    )
    @DisplayName("[VDA3-CONNECTION-001] Mobile Robot 发布允许的连接状态")
    void publishesAnAllowedConnectionState(ConnectionState connectionState) {
        MobileRobotState initial = openedState().toBuilder()
            .nextConnectionHeaderId(7L)
            .build();

        MobileRobotTransition transition = stateMachine.transition(
            initial,
            new MobileRobotEvent.ConnectionStatePublicationRequested(
                connectionState,
                OCCURRED_AT
            )
        );

        MobileRobotEffect.PublishConnection effect = assertInstanceOf(
            MobileRobotEffect.PublishConnection.class,
            transition.effects().getFirst()
        );
        Connection connection = effect.connection();
        assertAll(
            () -> assertEquals(1, transition.effects().size()),
            () -> assertTrue(transition.issues().isEmpty()),
            () -> assertEquals(connectionState, connection.connectionState()),
            () -> assertEquals(7L, connection.header().headerId()),
            () -> assertEquals(
                ProtocolTimestamp.from(OCCURRED_AT),
                connection.header().timestamp()
            ),
            () -> assertEquals(8L, transition.state().nextConnectionHeaderId()),
            () -> assertSame(connection, transition.state().lastConnection()),
            () -> assertSame(
                initial.connectionLastWill(),
                transition.state().connectionLastWill()
            )
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-005] 上线时两个 Connection headerId 跨最大值回绕")
    void wrapsConnectionHeaderIdsWhileOpening() {
        MobileRobotState initial = recoveringState().toBuilder()
            .nextConnectionHeaderId(4_294_967_295L)
            .build();

        MobileRobotTransition transition = stateMachine.transition(
            initial,
            new MobileRobotEvent.ConnectionOpeningRequested(OCCURRED_AT)
        );

        Connection lastWill = ((MobileRobotEffect.ConfigureConnectionLastWill)
            transition.effects().get(0)).connection();
        Connection online = ((MobileRobotEffect.PublishConnection)
            transition.effects().get(1)).connection();
        assertAll(
            () -> assertEquals(4_294_967_295L, lastWill.header().headerId()),
            () -> assertEquals(0L, online.header().headerId()),
            () -> assertEquals(1L, transition.state().nextConnectionHeaderId())
        );
    }

    @Test
    @DisplayName("[VDA3-CONNECTION-001] 重放转换与重交付 Effect 复用消息头和时间")
    void replayAndEffectRedeliveryReuseTheExactConnectionMessage() {
        MobileRobotState state = openedState();
        MobileRobotEvent event = new MobileRobotEvent.ConnectionStatePublicationRequested(
            ConnectionState.OFFLINE,
            OCCURRED_AT
        );

        MobileRobotTransition first = stateMachine.transition(state, event);
        MobileRobotTransition replay = stateMachine.transition(state, event);
        MobileRobotEffect.PublishConnection persistedEffect =
            (MobileRobotEffect.PublishConnection) first.effects().getFirst();
        Connection firstDelivery = deliver(persistedEffect);
        Connection retryDelivery = deliver(persistedEffect);

        assertAll(
            () -> assertEquals(first, replay),
            () -> assertSame(firstDelivery, retryDelivery),
            () -> assertEquals(
                firstDelivery.header().headerId(),
                retryDelivery.header().headerId()
            ),
            () -> assertEquals(
                firstDelivery.header().timestamp(),
                retryDelivery.header().timestamp()
            )
        );
    }

    @Test
    @DisplayName("[VDA3-CONNECTION-001] 未配置 Last Will 时拒绝主动发布")
    void rejectsPublicationOutsideAnActiveConnectionSession() {
        MobileRobotState initial = recoveringState();

        MobileRobotTransition transition = stateMachine.transition(
            initial,
            new MobileRobotEvent.ConnectionStatePublicationRequested(
                ConnectionState.ONLINE,
                OCCURRED_AT
            )
        );

        assertAll(
            () -> assertSame(initial, transition.state()),
            () -> assertTrue(transition.effects().isEmpty()),
            () -> assertEquals(1, transition.issues().size()),
            () -> assertEquals(
                "CONNECTION_SESSION_NOT_ACTIVE",
                transition.issues().getFirst().code()
            ),
            () -> assertEquals(
                "VDA3-CONNECTION-001",
                transition.issues().getFirst().requirementId()
            )
        );
    }

    @Test
    void rejectsPublicationWhenLastWillExistsWithoutALastConnection() {
        Connection lastWill = stateMachine.transition(
            recoveringState(),
            new MobileRobotEvent.ConnectionOpeningRequested(OCCURRED_AT)
        ).state().connectionLastWill();
        MobileRobotState incompleteSession = recoveringState().toBuilder()
            .connectionLastWill(lastWill)
            .build();

        MobileRobotTransition transition = stateMachine.transition(
            incompleteSession,
            new MobileRobotEvent.ConnectionStatePublicationRequested(
                ConnectionState.ONLINE,
                OCCURRED_AT.plusSeconds(1)
            )
        );

        assertAll(
            () -> assertSame(incompleteSession, transition.state()),
            () -> assertTrue(transition.effects().isEmpty()),
            () -> assertEquals(
                "CONNECTION_SESSION_NOT_ACTIVE",
                transition.issues().getFirst().code()
            )
        );
    }

    @Test
    @DisplayName("[VDA3-CONNECTION-001] 发布 OFFLINE 后必须重新执行上线序列")
    void requiresReopeningAfterPublishingOffline() {
        MobileRobotTransition offline = stateMachine.transition(
            openedState(),
            new MobileRobotEvent.ConnectionStatePublicationRequested(
                ConnectionState.OFFLINE,
                OCCURRED_AT.plusSeconds(1)
            )
        );

        MobileRobotTransition invalid = stateMachine.transition(
            offline.state(),
            new MobileRobotEvent.ConnectionStatePublicationRequested(
                ConnectionState.HIBERNATING,
                OCCURRED_AT.plusSeconds(2)
            )
        );

        assertAll(
            () -> assertSame(offline.state(), invalid.state()),
            () -> assertTrue(invalid.effects().isEmpty()),
            () -> assertEquals(
                "CONNECTION_SESSION_NOT_ACTIVE",
                invalid.issues().getFirst().code()
            )
        );
    }

    @Test
    void rejectsBrokenActivePublicationAndMissingProgrammingArguments() {
        MobileRobotState opened = openedState();
        Connection online = opened.lastConnection();
        Connection broken = opened.connectionLastWill();

        assertAll(
            () -> assertThrows(
                IllegalArgumentException.class,
                () -> new MobileRobotEvent.ConnectionStatePublicationRequested(
                    ConnectionState.CONNECTION_BROKEN,
                    OCCURRED_AT
                )
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> new MobileRobotEvent.ConnectionStatePublicationRequested(
                    null,
                    OCCURRED_AT
                )
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> new MobileRobotEvent.ConnectionStatePublicationRequested(
                    ConnectionState.ONLINE,
                    null
                )
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> new MobileRobotEvent.ConnectionOpeningRequested(null)
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> stateMachine.transition(null, new MobileRobotEvent.ConnectionOpeningRequested(
                    OCCURRED_AT
                ))
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> stateMachine.transition(recoveringState(), null)
            ),
            () -> assertThrows(
                IllegalArgumentException.class,
                () -> new MobileRobotEffect.PublishConnection(broken)
            ),
            () -> assertThrows(
                IllegalArgumentException.class,
                () -> new MobileRobotEffect.ConfigureConnectionLastWill(online)
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> new MobileRobotEffect.PublishConnection(null)
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> new MobileRobotEffect.ConfigureConnectionLastWill(null)
            )
        );
    }

    private static MobileRobotState recoveringState() {
        return MobileRobotState.recovering(
            ROBOT,
            ProtocolVersionProfile.V3_0_0
        );
    }

    private MobileRobotState openedState() {
        return stateMachine.transition(
            recoveringState(),
            new MobileRobotEvent.ConnectionOpeningRequested(OCCURRED_AT)
        ).state();
    }

    private static Connection deliver(
        MobileRobotEffect.PublishConnection effect
    ) {
        return effect.connection();
    }
}
