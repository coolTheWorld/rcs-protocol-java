package io.github.cooltheworld.rcs.protocol.vda5050.v3;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.codec.Vda5050JsonCodec;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.fleetcontrol.effect.FleetControlEffect;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.fleetcontrol.event.FleetControlEvent;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.fleetcontrol.FleetControlState;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.fleetcontrol.FleetControlStateMachine;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.fleetcontrol.FleetControlTransition;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.mobilerobot.effect.MobileRobotEffect;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.mobilerobot.event.MobileRobotEvent;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.mobilerobot.MobileRobotState;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.mobilerobot.MobileRobotStateMachine;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.mobilerobot.MobileRobotTransition;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.connection.Connection;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.connection.ConnectionState;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.common.ProtocolTimestamp;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.common.ProtocolVersionProfile;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.common.RobotIdentity;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.topic.DefaultTopicLayout;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.topic.TopicAddress;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.topic.TopicLayout;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.topic.TopicName;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.ConnectionValidator;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.ValidatedMessage;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.ValidationResult;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unchecked")
final class ConnectionDialogueTest {
    private static final String FIXTURE_ROOT =
        "/vda5050/v3.0.0/fixtures/connection/dialogue/";
    private static final RobotIdentity ROBOT = new RobotIdentity("Acme", "R-001");
    private static final ProtocolVersionProfile VERSION =
        ProtocolVersionProfile.V3_0_0;
    private static final Instant OPENED_AT = Instant.parse(
        "2026-08-10T08:00:00.123Z"
    );
    private static final TopicLayout TOPIC_LAYOUT = DefaultTopicLayout.standard();
    private static final String CONNECTION_TOPIC = TopicLayout.format(
        TOPIC_LAYOUT,
        new TopicAddress(ROBOT, TopicName.CONNECTION)
    );
    private static final Vda5050JsonCodec CODEC =
        Vda5050JsonCodec.createDefault();
    private static final ConnectionValidator VALIDATOR =
        ConnectionValidator.createDefault();

    private final MobileRobotStateMachine mobileRobot =
        MobileRobotStateMachine.createDefault();
    private final FleetControlStateMachine fleetControl =
        FleetControlStateMachine.createDefault();

    @Test
    @DisplayName("[VDA3-CONNECTION-001] 双角色完成上线与正常下线对话")
    void completesOnlineAndGracefulOfflineDialogueWithoutInfrastructure()
        throws IOException {
        MobileRobotState robotInitial = mobileRobotState();
        FleetControlState fleetInitial = fleetControlState();

        MobileRobotTransition opening = mobileRobot.transition(
            robotInitial,
            new MobileRobotEvent.ConnectionOpeningRequested(OPENED_AT)
        );
        MobileRobotEffect.ConfigureConnectionLastWill configured = assertInstanceOf(
            MobileRobotEffect.ConfigureConnectionLastWill.class,
            opening.effects().get(0)
        );
        MobileRobotEffect.PublishConnection online = assertInstanceOf(
            MobileRobotEffect.PublishConnection.class,
            opening.effects().get(1)
        );
        FleetControlTransition observedOnline = observe(
            fleetInitial,
            online.connection(),
            OPENED_AT.plusMillis(10)
        );

        Instant offlineAt = OPENED_AT.plusSeconds(300);
        MobileRobotTransition offlinePublication = mobileRobot.transition(
            opening.state(),
            new MobileRobotEvent.ConnectionStatePublicationRequested(
                ConnectionState.OFFLINE,
                offlineAt
            )
        );
        MobileRobotEffect.PublishConnection offline = assertInstanceOf(
            MobileRobotEffect.PublishConnection.class,
            offlinePublication.effects().getFirst()
        );
        FleetControlTransition observedOffline = observe(
            observedOnline.state(),
            offline.connection(),
            offlineAt.plusMillis(10)
        );

        FleetControlEffect.ConnectionStateChanged onlineChanged = assertInstanceOf(
            FleetControlEffect.ConnectionStateChanged.class,
            observedOnline.effects().getFirst()
        );
        FleetControlEffect.ConnectionStateChanged offlineChanged = assertInstanceOf(
            FleetControlEffect.ConnectionStateChanged.class,
            observedOffline.effects().getFirst()
        );
        assertAll(
            () -> assertEquals(fixture("last-will.json"), configured.connection()),
            () -> assertEquals(fixture("online.json"), online.connection()),
            () -> assertEquals(fixture("offline.json"), offline.connection()),
            () -> assertEquals(online.connection(), observedOnline.state().lastConnection()),
            () -> assertEquals(offline.connection(), observedOffline.state().lastConnection()),
            () -> assertEquals(ConnectionState.ONLINE, onlineChanged.connection().connectionState()),
            () -> assertEquals(ConnectionState.ONLINE, offlineChanged.previousState()),
            () -> assertEquals(ConnectionState.OFFLINE, observedOffline.state().connectionState()),
            () -> assertEquals(3L, offlinePublication.state().nextConnectionHeaderId()),
            () -> assertTrue(opening.issues().isEmpty()),
            () -> assertTrue(offlinePublication.issues().isEmpty()),
            () -> assertTrue(observedOnline.issues().isEmpty()),
            () -> assertTrue(observedOffline.issues().isEmpty())
        );
    }

    @Test
    @DisplayName("[VDA3-CONNECTION-001] Broker 投递陈旧 Last Will 并容忍重复")
    void observesStaleLastWillAfterUnexpectedDisconnect() {
        MobileRobotTransition opening = mobileRobot.transition(
            mobileRobotState(),
            new MobileRobotEvent.ConnectionOpeningRequested(OPENED_AT)
        );
        Connection lastWill = ((MobileRobotEffect.ConfigureConnectionLastWill)
            opening.effects().get(0)).connection();
        Connection online = ((MobileRobotEffect.PublishConnection)
            opening.effects().get(1)).connection();
        FleetControlTransition observedOnline = observe(
            fleetControlState(),
            online,
            OPENED_AT.plusMillis(10)
        );

        Instant brokerPublishedAt = OPENED_AT.plusSeconds(3600);
        FleetControlTransition broken = observe(
            observedOnline.state(),
            lastWill,
            brokerPublishedAt
        );
        FleetControlTransition duplicate = observe(
            broken.state(),
            lastWill,
            brokerPublishedAt.plusMillis(1)
        );

        FleetControlEffect.ConnectionStateChanged changed = assertInstanceOf(
            FleetControlEffect.ConnectionStateChanged.class,
            broken.effects().getFirst()
        );
        assertAll(
            () -> assertEquals(0L, lastWill.header().headerId()),
            () -> assertEquals(1L, online.header().headerId()),
            () -> assertEquals(
                ProtocolTimestamp.from(OPENED_AT),
                lastWill.header().timestamp()
            ),
            () -> assertEquals(lastWill.header().timestamp(), online.header().timestamp()),
            () -> assertEquals(brokerPublishedAt, changed.occurredAt()),
            () -> assertEquals(ConnectionState.ONLINE, changed.previousState()),
            () -> assertEquals(ConnectionState.CONNECTION_BROKEN, broken.state().connectionState()),
            () -> assertEquals(lastWill, broken.state().lastConnection()),
            () -> assertTrue(broken.issues().isEmpty()),
            () -> assertTrue(duplicate.effects().isEmpty()),
            () -> assertTrue(duplicate.issues().isEmpty()),
            () -> assertEquals(lastWill, duplicate.state().lastConnection())
        );
    }

    @Test
    @DisplayName("[VDA3-CONNECTION-001] 发布重试与重复接收精确保留同一消息")
    void preservesExactMessageAcrossPublicationRetryAndDuplicateDelivery() {
        MobileRobotTransition opening = mobileRobot.transition(
            mobileRobotState(),
            new MobileRobotEvent.ConnectionOpeningRequested(OPENED_AT)
        );
        Connection online = ((MobileRobotEffect.PublishConnection)
            opening.effects().get(1)).connection();
        FleetControlTransition observedOnline = observe(
            fleetControlState(),
            online,
            OPENED_AT.plusMillis(10)
        );
        Instant hibernatingAt = OPENED_AT.plusSeconds(60);
        MobileRobotTransition publication = mobileRobot.transition(
            opening.state(),
            new MobileRobotEvent.ConnectionStatePublicationRequested(
                ConnectionState.HIBERNATING,
                hibernatingAt
            )
        );
        MobileRobotEffect.PublishConnection persistedEffect = assertInstanceOf(
            MobileRobotEffect.PublishConnection.class,
            publication.effects().getFirst()
        );

        byte[] firstPayload = CODEC.encode(persistedEffect.connection());
        byte[] retryPayload = CODEC.encode(persistedEffect.connection());
        ValidatedMessage<Connection> firstDelivery = accepted(firstPayload);
        ValidatedMessage<Connection> retryDelivery = accepted(retryPayload);
        FleetControlTransition firstObserved = fleetControl.transition(
            observedOnline.state(),
            new FleetControlEvent.ConnectionReceived(
                firstDelivery,
                hibernatingAt.plusMillis(10)
            )
        );
        FleetControlTransition retryObserved = fleetControl.transition(
            firstObserved.state(),
            new FleetControlEvent.ConnectionReceived(
                retryDelivery,
                hibernatingAt.plusMillis(20)
            )
        );

        assertAll(
            () -> assertArrayEquals(firstPayload, retryPayload),
            () -> assertEquals(persistedEffect.connection(), firstDelivery.message()),
            () -> assertEquals(firstDelivery.message(), retryDelivery.message()),
            () -> assertEquals(2L, persistedEffect.connection().header().headerId()),
            () -> assertEquals(3L, publication.state().nextConnectionHeaderId()),
            () -> assertEquals(ConnectionState.HIBERNATING, firstObserved.state().connectionState()),
            () -> assertEquals(persistedEffect.connection(), retryObserved.state().lastConnection()),
            () -> assertEquals(1, firstObserved.effects().size()),
            () -> assertTrue(retryObserved.effects().isEmpty()),
            () -> assertTrue(retryObserved.issues().isEmpty())
        );
    }

    private FleetControlTransition observe(
        FleetControlState state,
        Connection connection,
        Instant occurredAt
    ) {
        ValidatedMessage<Connection> inbound = accepted(CODEC.encode(connection));
        assertEquals(connection, inbound.message());
        return fleetControl.transition(
            state,
            new FleetControlEvent.ConnectionReceived(inbound, occurredAt)
        );
    }

    private static ValidatedMessage<Connection> accepted(byte[] payload) {
        ValidationResult<Connection> result = VALIDATOR.validate(
            TOPIC_LAYOUT,
            CONNECTION_TOPIC,
            payload
        );
        return (ValidatedMessage<Connection>) assertInstanceOf(
            ValidatedMessage.class,
            result
        );
    }

    private static Connection fixture(String name) throws IOException {
        try (InputStream resource = ConnectionDialogueTest.class.getResourceAsStream(
            FIXTURE_ROOT + name
        )) {
            if (resource == null) {
                throw new IOException("Missing Connection dialogue fixture: " + name);
            }
            return accepted(resource.readAllBytes()).message();
        }
    }

    private static MobileRobotState mobileRobotState() {
        return MobileRobotState.recovering(ROBOT, VERSION);
    }

    private static FleetControlState fleetControlState() {
        return FleetControlState.recovering(ROBOT, VERSION);
    }
}
