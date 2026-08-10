package io.github.cooltheworld.rcs.protocol.vda5050.v3.fleetcontrol.internal;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.fleetcontrol.FleetControlEffect;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.fleetcontrol.FleetControlEvent;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.fleetcontrol.FleetControlState;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.fleetcontrol.FleetControlStateMachine;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.fleetcontrol.FleetControlTransition;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.Connection;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.ConnectionState;
import java.util.List;
import java.util.Objects;

/** 默认 Fleet Control 纯状态机实现。 */
public final class DefaultFleetControlStateMachine
    implements FleetControlStateMachine {
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
        FleetControlEvent.ConnectionReceived received =
            (FleetControlEvent.ConnectionReceived) event;
        Connection connection = received.message().message();
        ConnectionState previousState = state.connectionState();
        FleetControlState nextState = state.toBuilder()
            .lastConnection(connection)
            .build();
        FleetControlEffect effect = new FleetControlEffect.ConnectionStateChanged(
            previousState,
            connection,
            received.occurredAt()
        );
        return new FleetControlTransition(nextState, List.of(effect), List.of());
    }
}
