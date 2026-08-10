package io.github.cooltheworld.rcs.protocol.vda5050.v3.mobilerobot.internal;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.internal.Unsigned32;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.mobilerobot.MobileRobotEffect;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.mobilerobot.MobileRobotEvent;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.mobilerobot.MobileRobotState;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.mobilerobot.MobileRobotStateMachine;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.mobilerobot.MobileRobotTransition;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.Connection;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.ConnectionState;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.ProtocolHeader;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.ProtocolTimestamp;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 默认 Mobile Robot 纯状态机实现。 */
public final class DefaultMobileRobotStateMachine
    implements MobileRobotStateMachine {
    private static final Logger LOGGER = LoggerFactory.getLogger(
        DefaultMobileRobotStateMachine.class
    );
    private static final DefaultMobileRobotStateMachine INSTANCE =
        new DefaultMobileRobotStateMachine();

    private DefaultMobileRobotStateMachine() {}

    public static DefaultMobileRobotStateMachine instance() {
        return INSTANCE;
    }

    @Override
    public MobileRobotTransition transition(
        MobileRobotState state,
        MobileRobotEvent event
    ) {
        Objects.requireNonNull(state, "state");
        Objects.requireNonNull(event, "event");
        MobileRobotEvent.ConnectionOpeningRequested opening =
            (MobileRobotEvent.ConnectionOpeningRequested) event;
        ProtocolTimestamp timestamp = ProtocolTimestamp.from(opening.occurredAt());
        Long lastWillHeaderId = state.nextConnectionHeaderId();
        Long onlineHeaderId = Unsigned32.next(lastWillHeaderId);
        Connection lastWill = connection(
            state,
            lastWillHeaderId,
            timestamp,
            ConnectionState.CONNECTION_BROKEN
        );
        Connection online = connection(
            state,
            onlineHeaderId,
            timestamp,
            ConnectionState.ONLINE
        );
        MobileRobotState nextState = state.toBuilder()
            .nextConnectionHeaderId(Unsigned32.next(onlineHeaderId))
            .connectionLastWill(lastWill)
            .lastConnection(online)
            .build();
        LOGGER.info(
            "event=mobile_robot_connection_opening manufacturer={} "
                + "serialNumber={} lastWillHeaderId={} onlineHeaderId={}",
            state.robotIdentity().manufacturer(),
            state.robotIdentity().serialNumber(),
            lastWillHeaderId,
            onlineHeaderId
        );
        return new MobileRobotTransition(
            nextState,
            List.of(
                new MobileRobotEffect.ConfigureConnectionLastWill(lastWill),
                new MobileRobotEffect.PublishConnection(online)
            ),
            List.of()
        );
    }

    private static Connection connection(
        MobileRobotState state,
        Long headerId,
        ProtocolTimestamp timestamp,
        ConnectionState connectionState
    ) {
        ProtocolHeader header = ProtocolHeader.builder()
            .headerId(headerId)
            .timestamp(timestamp)
            .version(state.versionProfile().version())
            .robotIdentity(state.robotIdentity())
            .build();
        return Connection.builder()
            .header(header)
            .connectionState(connectionState)
            .build();
    }
}
