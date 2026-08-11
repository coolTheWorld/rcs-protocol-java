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
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.Factsheet;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.FactsheetContent;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.LoadSpecification;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.LocalizationType;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.MaximumArrayLengths;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.MaximumStringLengths;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.MobileRobotClass;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.MobileRobotGeometry;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.MobileRobotKinematics;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.NavigationType;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.PhysicalParameters;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.ProtocolFeatures;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.ProtocolLimits;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.ProtocolTiming;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.TypeSpecification;
import java.time.Instant;
import java.util.List;
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
    @DisplayName("[VDA3-FACTSHEET-001] 活跃会话确定性生成完整 Factsheet")
    void publishesFactsheetFromStateIdentityVersionCounterAndEventTime() {
        MobileRobotState initial = openedState();
        FactsheetContent content = factsheetContent();

        MobileRobotTransition transition = stateMachine.transition(
            initial,
            new MobileRobotEvent.FactsheetPublicationRequested(
                content,
                OCCURRED_AT
            )
        );

        MobileRobotEffect.PublishFactsheet effect = assertInstanceOf(
            MobileRobotEffect.PublishFactsheet.class,
            transition.effects().getFirst()
        );
        Factsheet factsheet = effect.factsheet();
        assertAll(
            () -> assertEquals(1, transition.effects().size()),
            () -> assertTrue(transition.issues().isEmpty()),
            () -> assertEquals(0L, factsheet.header().headerId()),
            () -> assertEquals(
                ProtocolTimestamp.from(OCCURRED_AT),
                factsheet.header().timestamp()
            ),
            () -> assertEquals(
                ProtocolVersionProfile.V3_0_0.version(),
                factsheet.header().version()
            ),
            () -> assertEquals(ROBOT, factsheet.header().robotIdentity()),
            () -> assertSame(content, factsheet.content()),
            () -> assertEquals(1L, transition.state().nextFactsheetHeaderId()),
            () -> assertSame(factsheet, transition.state().lastFactsheet()),
            () -> assertEquals(
                initial.nextConnectionHeaderId(),
                transition.state().nextConnectionHeaderId()
            ),
            () -> assertSame(
                initial.lastConnection(),
                transition.state().lastConnection()
            ),
            () -> assertSame(
                initial.connectionLastWill(),
                transition.state().connectionLastWill()
            )
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-005] Factsheet 使用独立 uint32 循环计数器")
    void wrapsTheIndependentFactsheetHeaderId() {
        MobileRobotState initial = openedState().toBuilder()
            .nextFactsheetHeaderId(4_294_967_295L)
            .build();

        MobileRobotTransition transition = stateMachine.transition(
            initial,
            factsheetPublication(OCCURRED_AT)
        );

        Factsheet factsheet = ((MobileRobotEffect.PublishFactsheet)
            transition.effects().getFirst()).factsheet();
        assertAll(
            () -> assertEquals(4_294_967_295L, factsheet.header().headerId()),
            () -> assertEquals(0L, transition.state().nextFactsheetHeaderId()),
            () -> assertEquals(
                initial.nextConnectionHeaderId(),
                transition.state().nextConnectionHeaderId()
            )
        );
    }

    @Test
    void permitsFactsheetPublicationWhileTheConnectionIsHibernating() {
        MobileRobotState hibernating = stateMachine.transition(
            openedState(),
            new MobileRobotEvent.ConnectionStatePublicationRequested(
                ConnectionState.HIBERNATING,
                OCCURRED_AT.plusSeconds(1)
            )
        ).state();

        MobileRobotTransition transition = stateMachine.transition(
            hibernating,
            factsheetPublication(OCCURRED_AT.plusSeconds(2))
        );

        assertAll(
            () -> assertEquals(1, transition.effects().size()),
            () -> assertInstanceOf(
                MobileRobotEffect.PublishFactsheet.class,
                transition.effects().getFirst()
            ),
            () -> assertTrue(transition.issues().isEmpty())
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] 未上线或已 OFFLINE 时拒绝发布")
    void rejectsFactsheetPublicationOutsideAnActiveConnectionSession() {
        MobileRobotState unopened = recoveringState();
        MobileRobotState offline = stateMachine.transition(
            openedState(),
            new MobileRobotEvent.ConnectionStatePublicationRequested(
                ConnectionState.OFFLINE,
                OCCURRED_AT.plusSeconds(1)
            )
        ).state();

        MobileRobotTransition unopenedResult = stateMachine.transition(
            unopened,
            factsheetPublication(OCCURRED_AT)
        );
        MobileRobotTransition offlineResult = stateMachine.transition(
            offline,
            factsheetPublication(OCCURRED_AT.plusSeconds(2))
        );

        assertAll(
            () -> assertSame(unopened, unopenedResult.state()),
            () -> assertTrue(unopenedResult.effects().isEmpty()),
            () -> assertEquals(
                "FACTSHEET_PUBLICATION_SESSION_NOT_ACTIVE",
                unopenedResult.issues().getFirst().code()
            ),
            () -> assertEquals(
                "VDA3-FACTSHEET-001",
                unopenedResult.issues().getFirst().requirementId()
            ),
            () -> assertSame(offline, offlineResult.state()),
            () -> assertTrue(offlineResult.effects().isEmpty()),
            () -> assertEquals(
                unopenedResult.issues(),
                offlineResult.issues()
            )
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] 相同 State/Event 重放得到相等结果")
    void deterministicallyReplaysFactsheetPublication() {
        MobileRobotState state = openedState().toBuilder()
            .nextFactsheetHeaderId(17L)
            .build();
        MobileRobotEvent event = factsheetPublication(OCCURRED_AT);

        MobileRobotTransition first = stateMachine.transition(state, event);
        MobileRobotTransition replay = stateMachine.transition(state, event);
        MobileRobotEffect.PublishFactsheet persistedEffect =
            (MobileRobotEffect.PublishFactsheet) first.effects().getFirst();

        assertAll(
            () -> assertEquals(first, replay),
            () -> assertSame(
                persistedEffect.factsheet(),
                deliver(persistedEffect)
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

    private static Factsheet deliver(
        MobileRobotEffect.PublishFactsheet effect
    ) {
        return effect.factsheet();
    }

    private static MobileRobotEvent.FactsheetPublicationRequested
        factsheetPublication(Instant occurredAt) {
        return new MobileRobotEvent.FactsheetPublicationRequested(
            factsheetContent(),
            occurredAt
        );
    }

    private static FactsheetContent factsheetContent() {
        return FactsheetContent.builder()
            .typeSpecification(TypeSpecification.builder()
                .seriesName("SERIES")
                .mobileRobotKinematics(MobileRobotKinematics.DIFFERENTIAL)
                .mobileRobotClass(MobileRobotClass.CARRIER)
                .maximumLoadMass(100.0D)
                .localizationTypes(List.of(LocalizationType.NATURAL))
                .navigationTypes(List.of(NavigationType.FREELY_NAVIGATING))
                .build())
            .physicalParameters(PhysicalParameters.builder()
                .minimumSpeed(0.1D)
                .maximumSpeed(2.0D)
                .maximumAcceleration(1.0D)
                .maximumDeceleration(1.0D)
                .minimumHeight(0.2D)
                .maximumHeight(1.0D)
                .width(0.8D)
                .length(1.2D)
                .build())
            .protocolLimits(ProtocolLimits.builder()
                .maximumStringLengths(MaximumStringLengths.builder().build())
                .maximumArrayLengths(MaximumArrayLengths.builder().build())
                .timing(ProtocolTiming.builder()
                    .minimumOrderInterval(0.0D)
                    .minimumStateInterval(0.0D)
                    .build())
                .build())
            .protocolFeatures(ProtocolFeatures.builder()
                .optionalParameters(List.of())
                .mobileRobotActions(List.of())
                .build())
            .mobileRobotGeometry(MobileRobotGeometry.builder().build())
            .loadSpecification(LoadSpecification.builder().build())
            .build();
    }
}
