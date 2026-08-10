package io.github.cooltheworld.rcs.protocol.vda5050.v3.fleetcontrol.internal;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.fleetcontrol.effect.FleetControlEffect;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.fleetcontrol.event.FleetControlEvent;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.fleetcontrol.FleetControlState;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.fleetcontrol.FleetControlStateMachine;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.fleetcontrol.FleetControlTransition;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.connection.Connection;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.connection.ConnectionState;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.common.RobotIdentity;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.topic.TopicName;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.RejectedInboundMessage;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.ValidationIssue;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.ValidationSeverity;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 默认 Fleet Control 纯状态机实现。 */
public final class DefaultFleetControlStateMachine
    implements FleetControlStateMachine {
    private static final Logger LOGGER = LoggerFactory.getLogger(
        DefaultFleetControlStateMachine.class
    );
    private static final DefaultFleetControlStateMachine INSTANCE =
        new DefaultFleetControlStateMachine();

    private DefaultFleetControlStateMachine() {}

    public static DefaultFleetControlStateMachine instance() {
        return INSTANCE;
    }

    @Override
    public FleetControlTransition transition(
        FleetControlState state,
        FleetControlEvent event
    ) {
        Objects.requireNonNull(state, "state");
        Objects.requireNonNull(event, "event");
        if (event instanceof FleetControlEvent.ConnectionReceived received) {
            return transitionConnection(state, received);
        }
        return transitionRejection(
            state,
            (FleetControlEvent.ConnectionRejected) event
        );
    }

    private FleetControlTransition transitionConnection(
        FleetControlState state,
        FleetControlEvent.ConnectionReceived received
    ) {
        Connection connection = received.message().message();
        RobotIdentity incomingIdentity = connection.header().robotIdentity();
        LOGGER.debug(
            "event=fleet_control_connection_received manufacturer={} "
                + "serialNumber={} connectionState={} headerId={}",
            incomingIdentity.manufacturer(),
            incomingIdentity.serialNumber(),
            connection.connectionState(),
            connection.header().headerId()
        );
        if (!state.robotIdentity().equals(incomingIdentity)) {
            ValidationIssue issue = new ValidationIssue(
                "SESSION_ROBOT_IDENTITY_MISMATCH",
                ValidationSeverity.ERROR,
                "",
                "消息身份与 Fleet Control 会话身份不一致",
                "VDA3-SHARED-004"
            );
            return rejectedTransition(
                state,
                TopicName.CONNECTION,
                incomingIdentity,
                connection.header().headerId(),
                List.of(issue),
                received.occurredAt()
            );
        }

        ConnectionState previousState = state.connectionState();
        FleetControlState nextState = state.toBuilder()
            .lastConnection(connection)
            .build();
        List<FleetControlEffect> effects = new ArrayList<>();
        if (previousState != connection.connectionState()) {
            effects.add(new FleetControlEffect.ConnectionStateChanged(
                previousState,
                connection,
                received.occurredAt()
            ));
            LOGGER.info(
                "event=fleet_control_connection_state_changed manufacturer={} "
                    + "serialNumber={} previousState={} currentState={} headerId={}",
                incomingIdentity.manufacturer(),
                incomingIdentity.serialNumber(),
                previousState,
                connection.connectionState(),
                connection.header().headerId()
            );
        }
        if (!connection.extensionFields().isEmpty()) {
            effects.add(new FleetControlEffect.UnknownExtensionObserved(
                TopicName.CONNECTION,
                incomingIdentity,
                connection.header().headerId(),
                received.occurredAt()
            ));
            LOGGER.debug(
                "event=fleet_control_unknown_extension_observed topic={} "
                    + "manufacturer={} serialNumber={} headerId={}",
                TopicName.CONNECTION.wireName(),
                incomingIdentity.manufacturer(),
                incomingIdentity.serialNumber(),
                connection.header().headerId()
            );
        }
        return new FleetControlTransition(nextState, effects, List.of());
    }

    private FleetControlTransition transitionRejection(
        FleetControlState state,
        FleetControlEvent.ConnectionRejected event
    ) {
        RejectedInboundMessage<Connection> rejection = event.rejection();
        return rejectedTransition(
            state,
            rejection.topic(),
            rejection.robotIdentity(),
            rejection.headerId(),
            rejection.issues(),
            event.occurredAt()
        );
    }

    private FleetControlTransition rejectedTransition(
        FleetControlState state,
        TopicName topic,
        RobotIdentity robotIdentity,
        Long headerId,
        List<ValidationIssue> issues,
        java.time.Instant occurredAt
    ) {
        String firstIssueCode = issues.getFirst().code();
        LOGGER.warn(
            "event=fleet_control_inbound_message_rejected topic={} "
                + "manufacturer={} serialNumber={} headerId={} issueCount={} "
                + "firstIssueCode={}",
            topic.wireName(),
            robotIdentity == null ? null : robotIdentity.manufacturer(),
            robotIdentity == null ? null : robotIdentity.serialNumber(),
            headerId,
            issues.size(),
            firstIssueCode
        );
        FleetControlEffect effect = new FleetControlEffect.InboundMessageRejected(
            topic,
            robotIdentity,
            headerId,
            issues,
            occurredAt
        );
        return new FleetControlTransition(state, List.of(effect), issues);
    }
}
