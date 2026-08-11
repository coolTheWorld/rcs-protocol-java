package io.github.cooltheworld.rcs.protocol.vda5050.v3;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.codec.Vda5050JsonCodec;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.fleetcontrol.FleetControlState;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.fleetcontrol.FleetControlStateMachine;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.fleetcontrol.FleetControlTransition;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.fleetcontrol.effect.FleetControlEffect;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.fleetcontrol.event.FleetControlEvent;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.mobilerobot.MobileRobotState;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.mobilerobot.MobileRobotStateMachine;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.mobilerobot.MobileRobotTransition;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.mobilerobot.effect.MobileRobotEffect;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.mobilerobot.event.MobileRobotEvent;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.common.ProtocolVersionProfile;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.common.RobotIdentity;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.Factsheet;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.FactsheetContent;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.MobileRobotConfiguration;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.NetworkConfiguration;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.topic.DefaultTopicLayout;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.topic.LastWillPolicy;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.topic.RetainedPolicy;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.topic.TopicAddress;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.topic.TopicDescriptor;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.topic.TopicLayout;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.topic.TopicName;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.topic.TopicParticipant;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.topic.TopicQos;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.FactsheetValidator;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.RejectedInboundMessage;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.ValidatedMessage;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.ValidationResult;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

final class FactsheetDialogueTest {
    private static final String FIXTURE =
        "/vda5050/v3.0.0/fixtures/factsheet/dialogue/capability.json";
    private static final RobotIdentity ROBOT = new RobotIdentity("Acme", "R-001");
    private static final ProtocolVersionProfile VERSION =
        ProtocolVersionProfile.V3_0_0;
    private static final Instant PUBLISHED_AT = Instant.parse(
        "2026-08-11T07:00:00.123Z"
    );
    private static final TopicLayout TOPIC_LAYOUT = DefaultTopicLayout.standard();
    private static final String FACTSHEET_TOPIC = TopicLayout.format(
        TOPIC_LAYOUT,
        new TopicAddress(ROBOT, TopicName.FACTSHEET)
    );
    private static final Vda5050JsonCodec CODEC =
        Vda5050JsonCodec.createDefault();
    private static final FactsheetValidator VALIDATOR =
        FactsheetValidator.createDefault();

    private final MobileRobotStateMachine mobileRobot =
        MobileRobotStateMachine.createDefault();
    private final FleetControlStateMachine fleetControl =
        FleetControlStateMachine.createDefault();

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] 双角色经 Codec、Topic 与 Validator 精确保留能力")
    void preservesMobileRobotFactsheetAcrossTheCompleteInboundBoundary()
        throws IOException {
        Factsheet expected = fixture();
        MobileRobotTransition publication = publish(
            openedMobileRobotState(),
            expected.content(),
            PUBLISHED_AT
        );
        MobileRobotEffect.PublishFactsheet publishEffect = assertInstanceOf(
            MobileRobotEffect.PublishFactsheet.class,
            publication.effects().getFirst()
        );
        Factsheet generated = publishEffect.factsheet();
        byte[] firstPayload = CODEC.encode(generated);
        byte[] retryPayload = CODEC.encode(generated);
        ValidatedMessage<Factsheet> inbound = accepted(
            FACTSHEET_TOPIC,
            firstPayload
        );
        FleetControlTransition observed = fleetControl.transition(
            fleetControlState(),
            new FleetControlEvent.FactsheetReceived(
                inbound,
                PUBLISHED_AT.plusMillis(10)
            )
        );
        FleetControlTransition duplicate = fleetControl.transition(
            observed.state(),
            new FleetControlEvent.FactsheetReceived(
                inbound,
                PUBLISHED_AT.plusMillis(20)
            )
        );
        FleetControlEffect.FactsheetChanged changed = assertInstanceOf(
            FleetControlEffect.FactsheetChanged.class,
            observed.effects().getFirst()
        );
        TopicDescriptor descriptor = TopicDescriptor.forTopic(
            TopicName.FACTSHEET
        );

        assertAll(
            () -> assertEquals(expected, generated),
            () -> assertArrayEquals(firstPayload, retryPayload),
            () -> assertEquals(generated, inbound.message()),
            () -> assertEquals(
                TopicName.FACTSHEET,
                TopicLayout.parseForRobot(TOPIC_LAYOUT, FACTSHEET_TOPIC, ROBOT)
            ),
            () -> assertEquals(
                List.of(TopicParticipant.MOBILE_ROBOT),
                descriptor.publishers().stream().toList()
            ),
            () -> assertTrue(
                descriptor.subscribers().contains(TopicParticipant.FLEET_CONTROL)
            ),
            () -> assertEquals(TopicQos.AT_MOST_ONCE, descriptor.qos()),
            () -> assertEquals(RetainedPolicy.REQUIRED, descriptor.retainedPolicy()),
            () -> assertEquals(LastWillPolicy.NOT_REQUIRED, descriptor.lastWillPolicy()),
            () -> assertSame(generated, publication.state().lastFactsheet()),
            () -> assertEquals(1L, publication.state().nextFactsheetHeaderId()),
            () -> assertEquals(generated, observed.state().lastFactsheet()),
            () -> assertEquals(generated, changed.factsheet()),
            () -> assertTrue(observed.issues().isEmpty()),
            () -> assertTrue(duplicate.effects().isEmpty()),
            () -> assertTrue(duplicate.issues().isEmpty()),
            () -> assertEquals(generated, duplicate.state().lastFactsheet())
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-005] 双角色确定拒绝网络变化与 Topic 身份错误")
    void deterministicallyRejectsNetworkChangesAndInvalidDeliveries()
        throws IOException {
        Factsheet fixture = fixture();
        MobileRobotTransition baselinePublication = publish(
            openedMobileRobotState(),
            fixture.content(),
            PUBLISHED_AT
        );
        Factsheet baseline = publishedFactsheet(baselinePublication);
        FleetControlTransition observed = receive(
            fleetControlState(),
            baseline,
            PUBLISHED_AT.plusMillis(10)
        );

        MobileRobotTransition changedPublication = publish(
            baselinePublication.state(),
            withChangedNetwork(fixture.content()),
            PUBLISHED_AT.plusSeconds(1)
        );
        Factsheet changed = publishedFactsheet(changedPublication);
        FleetControlEvent.FactsheetReceived changedEvent =
            new FleetControlEvent.FactsheetReceived(
                accepted(FACTSHEET_TOPIC, CODEC.encode(changed)),
                PUBLISHED_AT.plusSeconds(1).plusMillis(10)
            );
        FleetControlTransition networkRejected = fleetControl.transition(
            observed.state(),
            changedEvent
        );
        FleetControlTransition networkReplay = fleetControl.transition(
            observed.state(),
            changedEvent
        );

        String wrongIdentityTopic = TopicLayout.format(
            TOPIC_LAYOUT,
            new TopicAddress(
                new RobotIdentity("Acme", "R-002"),
                TopicName.FACTSHEET
            )
        );
        RejectedInboundMessage<Factsheet> rejected = rejected(
            wrongIdentityTopic,
            CODEC.encode(baseline)
        );
        FleetControlEvent.FactsheetRejected rejectedEvent =
            new FleetControlEvent.FactsheetRejected(
                rejected,
                PUBLISHED_AT.plusSeconds(2)
            );
        FleetControlTransition invalidDelivery = fleetControl.transition(
            observed.state(),
            rejectedEvent
        );
        FleetControlTransition invalidReplay = fleetControl.transition(
            observed.state(),
            rejectedEvent
        );

        FleetControlEffect.InboundMessageRejected networkEffect =
            assertInstanceOf(
                FleetControlEffect.InboundMessageRejected.class,
                networkRejected.effects().getFirst()
            );
        FleetControlEffect.InboundMessageRejected invalidEffect =
            assertInstanceOf(
                FleetControlEffect.InboundMessageRejected.class,
                invalidDelivery.effects().getFirst()
            );
        assertAll(
            () -> assertEquals(1L, changed.header().headerId()),
            () -> assertSame(observed.state(), networkRejected.state()),
            () -> assertEquals(networkRejected, networkReplay),
            () -> assertEquals(
                "FACTSHEET_NETWORK_BASELINE_CHANGED",
                networkRejected.issues().getFirst().code()
            ),
            () -> assertEquals(
                "VDA3-FACTSHEET-005",
                networkRejected.issues().getFirst().requirementId()
            ),
            () -> assertEquals(TopicName.FACTSHEET, networkEffect.topic()),
            () -> assertSame(observed.state(), invalidDelivery.state()),
            () -> assertEquals(invalidDelivery, invalidReplay),
            () -> assertEquals(
                "TOPIC_HEADER_MISMATCH",
                invalidDelivery.issues().getFirst().code()
            ),
            () -> assertEquals(rejected.issues(), invalidDelivery.issues()),
            () -> assertEquals(TopicName.FACTSHEET, invalidEffect.topic()),
            () -> assertEquals(baseline, invalidDelivery.state().lastFactsheet())
        );
    }

    private MobileRobotState openedMobileRobotState() {
        return mobileRobot.transition(
            MobileRobotState.recovering(ROBOT, VERSION),
            new MobileRobotEvent.ConnectionOpeningRequested(
                PUBLISHED_AT.minusSeconds(1)
            )
        ).state();
    }

    private MobileRobotTransition publish(
        MobileRobotState state,
        FactsheetContent content,
        Instant occurredAt
    ) {
        return mobileRobot.transition(
            state,
            new MobileRobotEvent.FactsheetPublicationRequested(content, occurredAt)
        );
    }

    private FleetControlTransition receive(
        FleetControlState state,
        Factsheet factsheet,
        Instant occurredAt
    ) {
        return fleetControl.transition(
            state,
            new FleetControlEvent.FactsheetReceived(
                accepted(FACTSHEET_TOPIC, CODEC.encode(factsheet)),
                occurredAt
            )
        );
    }

    private static Factsheet publishedFactsheet(
        MobileRobotTransition transition
    ) {
        return ((MobileRobotEffect.PublishFactsheet)
            transition.effects().getFirst()).factsheet();
    }

    private static FactsheetContent withChangedNetwork(
        FactsheetContent content
    ) {
        MobileRobotConfiguration current = content.mobileRobotConfiguration();
        NetworkConfiguration network = current.network();
        NetworkConfiguration changedNetwork = NetworkConfiguration.builder()
            .dnsServers(List.of("10.0.0.99"))
            .ntpServers(network.ntpServers())
            .localIpAddress(network.localIpAddress())
            .netmask(network.netmask())
            .defaultGateway(network.defaultGateway())
            .extensionFields(network.extensionFields())
            .build();
        MobileRobotConfiguration changedConfiguration =
            MobileRobotConfiguration.builder()
                .versions(current.versions())
                .network(changedNetwork)
                .batteryCharging(current.batteryCharging())
                .extensionFields(current.extensionFields())
                .build();
        return FactsheetContent.builder()
            .typeSpecification(content.typeSpecification())
            .physicalParameters(content.physicalParameters())
            .protocolLimits(content.protocolLimits())
            .protocolFeatures(content.protocolFeatures())
            .mobileRobotGeometry(content.mobileRobotGeometry())
            .loadSpecification(content.loadSpecification())
            .mobileRobotConfiguration(changedConfiguration)
            .build();
    }

    private static FleetControlState fleetControlState() {
        return FleetControlState.recovering(ROBOT, VERSION);
    }

    private static Factsheet fixture() throws IOException {
        try (InputStream resource = FactsheetDialogueTest.class
            .getResourceAsStream(FIXTURE)) {
            if (resource == null) {
                throw new IOException("Missing Factsheet dialogue fixture");
            }
            return accepted(FACTSHEET_TOPIC, resource.readAllBytes()).message();
        }
    }

    @SuppressWarnings("unchecked")
    private static ValidatedMessage<Factsheet> accepted(
        String topic,
        byte[] payload
    ) {
        ValidationResult<Factsheet> result = VALIDATOR.validate(
            TOPIC_LAYOUT,
            topic,
            payload
        );
        return (ValidatedMessage<Factsheet>) assertInstanceOf(
            ValidatedMessage.class,
            result,
            () -> "Unexpected validation result: " + result.issues()
        );
    }

    @SuppressWarnings("unchecked")
    private static RejectedInboundMessage<Factsheet> rejected(
        String topic,
        byte[] payload
    ) {
        ValidationResult<Factsheet> result = VALIDATOR.validate(
            TOPIC_LAYOUT,
            topic,
            payload
        );
        return (RejectedInboundMessage<Factsheet>) assertInstanceOf(
            RejectedInboundMessage.class,
            result
        );
    }
}
