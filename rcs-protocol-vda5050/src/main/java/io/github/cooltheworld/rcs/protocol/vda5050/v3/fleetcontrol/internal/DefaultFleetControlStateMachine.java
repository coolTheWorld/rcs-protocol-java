package io.github.cooltheworld.rcs.protocol.vda5050.v3.fleetcontrol.internal;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.fleetcontrol.effect.FleetControlEffect;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.fleetcontrol.event.FleetControlEvent;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.fleetcontrol.FleetControlState;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.fleetcontrol.FleetControlStateMachine;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.fleetcontrol.FleetControlTransition;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.action.MobileRobotAction;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.connection.Connection;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.connection.ConnectionState;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.common.RobotIdentity;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.Envelope2d;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.Factsheet;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.LoadSpecification;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.LoadSet;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.MobileRobotConfiguration;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.MobileRobotGeometry;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.NetworkConfiguration;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.ProtocolFeatures;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.ProtocolLimits;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.topic.TopicName;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.RejectedInboundMessage;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.ValidationIssue;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.ValidationSeverity;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
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
        if (event instanceof FleetControlEvent.ConnectionRejected rejected) {
            return transitionRejection(
                state,
                rejected.rejection(),
                rejected.occurredAt()
            );
        }
        if (event instanceof FleetControlEvent.FactsheetReceived received) {
            return transitionFactsheet(state, received);
        }
        FleetControlEvent.FactsheetRejected rejected =
            (FleetControlEvent.FactsheetRejected) event;
        return transitionRejection(
            state,
            rejected.rejection(),
            rejected.occurredAt()
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

    private FleetControlTransition transitionFactsheet(
        FleetControlState state,
        FleetControlEvent.FactsheetReceived received
    ) {
        Factsheet factsheet = received.message().message();
        RobotIdentity incomingIdentity = factsheet.header().robotIdentity();
        LOGGER.debug(
            "event=fleet_control_factsheet_received manufacturer={} "
                + "serialNumber={} headerId={}",
            incomingIdentity.manufacturer(),
            incomingIdentity.serialNumber(),
            factsheet.header().headerId()
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
                TopicName.FACTSHEET,
                incomingIdentity,
                factsheet.header().headerId(),
                List.of(issue),
                received.occurredAt()
            );
        }
        if (
            !state.versionProfile()
                .version()
                .equals(factsheet.header().version())
        ) {
            ValidationIssue issue = new ValidationIssue(
                "SESSION_PROTOCOL_VERSION_MISMATCH",
                ValidationSeverity.ERROR,
                "/version",
                "消息版本与 Fleet Control 会话版本不一致",
                "VDA3-SHARED-003"
            );
            return rejectedTransition(
                state,
                TopicName.FACTSHEET,
                incomingIdentity,
                factsheet.header().headerId(),
                List.of(issue),
                received.occurredAt()
            );
        }

        Factsheet previousFactsheet = state.lastFactsheet();
        NetworkConfiguration baselineNetwork = network(previousFactsheet);
        NetworkConfiguration incomingNetwork = network(factsheet);
        if (baselineNetwork != null && !baselineNetwork.equals(incomingNetwork)) {
            ValidationIssue issue = new ValidationIssue(
                "FACTSHEET_NETWORK_BASELINE_CHANGED",
                ValidationSeverity.ERROR,
                "/mobileRobotConfiguration/network",
                "Factsheet 网络配置与已冻结的会话基线不一致",
                "VDA3-FACTSHEET-005"
            );
            return rejectedTransition(
                state,
                TopicName.FACTSHEET,
                incomingIdentity,
                factsheet.header().headerId(),
                List.of(issue),
                received.occurredAt()
            );
        }

        FleetControlState nextState = state.toBuilder()
            .lastFactsheet(factsheet)
            .build();
        List<FleetControlEffect> effects = new ArrayList<>();
        if (!Objects.equals(previousFactsheet, factsheet)) {
            effects.add(new FleetControlEffect.FactsheetChanged(
                previousFactsheet,
                factsheet,
                received.occurredAt()
            ));
        }
        if (hasUnknownExtension(factsheet)) {
            effects.add(new FleetControlEffect.UnknownExtensionObserved(
                TopicName.FACTSHEET,
                incomingIdentity,
                factsheet.header().headerId(),
                received.occurredAt()
            ));
            LOGGER.debug(
                "event=fleet_control_unknown_extension_observed topic={} "
                    + "manufacturer={} serialNumber={} headerId={}",
                TopicName.FACTSHEET.wireName(),
                incomingIdentity.manufacturer(),
                incomingIdentity.serialNumber(),
                factsheet.header().headerId()
            );
        }
        return new FleetControlTransition(nextState, effects, List.of());
    }

    private FleetControlTransition transitionRejection(
        FleetControlState state,
        RejectedInboundMessage<?> rejection,
        Instant occurredAt
    ) {
        return rejectedTransition(
            state,
            rejection.topic(),
            rejection.robotIdentity(),
            rejection.headerId(),
            rejection.issues(),
            occurredAt
        );
    }

    private static NetworkConfiguration network(Factsheet factsheet) {
        if (factsheet == null) {
            return null;
        }
        MobileRobotConfiguration configuration = factsheet.content()
            .mobileRobotConfiguration();
        return configuration == null ? null : configuration.network();
    }

    private static boolean hasUnknownExtension(Factsheet factsheet) {
        var content = factsheet.content();
        boolean found = !factsheet.extensionFields().isEmpty();
        found |= !content.typeSpecification().extensionFields().isEmpty();
        found |= !content.physicalParameters().extensionFields().isEmpty();
        found |= hasUnknownExtension(content.protocolLimits());
        found |= hasUnknownExtension(content.protocolFeatures());
        found |= hasUnknownExtension(content.mobileRobotGeometry());
        found |= hasUnknownExtension(content.loadSpecification());
        found |= hasUnknownExtension(content.mobileRobotConfiguration());
        return found;
    }

    private static boolean hasUnknownExtension(
        ProtocolLimits limits
    ) {
        boolean found = !limits.extensionFields().isEmpty();
        found |= !limits.maximumStringLengths().extensionFields().isEmpty();
        found |= !limits.maximumArrayLengths().extensionFields().isEmpty();
        found |= !limits.timing().extensionFields().isEmpty();
        return found;
    }

    private static boolean hasUnknownExtension(ProtocolFeatures features) {
        boolean found = !features.extensionFields().isEmpty();
        found |= any(
            features.optionalParameters(),
            value -> !value.extensionFields().isEmpty()
        );
        found |= any(
            features.mobileRobotActions(),
            DefaultFleetControlStateMachine::hasUnknownExtension
        );
        return found;
    }

    private static boolean hasUnknownExtension(MobileRobotAction action) {
        boolean found = !action.extensionFields().isEmpty();
        found |= any(
            action.actionParameters(),
            value -> !value.extensionFields().isEmpty()
        );
        return found;
    }

    private static boolean hasUnknownExtension(MobileRobotGeometry geometry) {
        boolean found = !geometry.extensionFields().isEmpty();
        found |= any(
            geometry.wheelDefinitions(),
            wheel -> !wheel.extensionFields().isEmpty()
                | !wheel.position().extensionFields().isEmpty()
        );
        found |= any(
            geometry.envelopes2d(),
            DefaultFleetControlStateMachine::hasUnknownExtension
        );
        found |= any(
            geometry.envelopes3d(),
            envelope -> !envelope.extensionFields().isEmpty()
        );
        return found;
    }

    private static boolean hasUnknownExtension(Envelope2d envelope) {
        boolean found = !envelope.extensionFields().isEmpty();
        found |= any(
            envelope.vertices(),
            vertex -> !vertex.extensionFields().isEmpty()
        );
        return found;
    }

    private static boolean hasUnknownExtension(
        LoadSpecification specification
    ) {
        boolean found = !specification.extensionFields().isEmpty();
        found |= any(
            specification.loadSets(),
            DefaultFleetControlStateMachine::hasUnknownExtension
        );
        return found;
    }

    private static boolean hasUnknownExtension(LoadSet loadSet) {
        boolean found = !loadSet.extensionFields().isEmpty();
        found |= loadSet.boundingBoxReference() != null
            && !loadSet.boundingBoxReference().extensionFields().isEmpty();
        found |= loadSet.loadDimensions() != null
            && !loadSet.loadDimensions().extensionFields().isEmpty();
        return found;
    }

    private static boolean hasUnknownExtension(
        MobileRobotConfiguration configuration
    ) {
        if (configuration == null) {
            return false;
        }
        boolean found = !configuration.extensionFields().isEmpty();
        found |= any(
            configuration.versions(),
            version -> !version.extensionFields().isEmpty()
        );
        found |= configuration.network() != null
            && !configuration.network().extensionFields().isEmpty();
        found |= configuration.batteryCharging() != null
            && !configuration.batteryCharging().extensionFields().isEmpty();
        return found;
    }

    private static <T> boolean any(
        List<T> values,
        Predicate<T> extensionDetector
    ) {
        List<T> safeValues = values == null ? List.of() : values;
        return safeValues.stream().anyMatch(extensionDetector);
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
