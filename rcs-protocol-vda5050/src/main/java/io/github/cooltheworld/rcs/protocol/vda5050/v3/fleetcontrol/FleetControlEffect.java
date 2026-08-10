package io.github.cooltheworld.rcs.protocol.vda5050.v3.fleetcontrol;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.Connection;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.ConnectionState;
import java.time.Instant;
import java.util.Objects;

/** Fleet Control 状态机产生的封闭效果集合。 */
public sealed interface FleetControlEffect
    permits FleetControlEffect.ConnectionStateChanged {
    /** 向外部报告已观察到的连接状态变化。 */
    record ConnectionStateChanged(
        ConnectionState previousState,
        Connection connection,
        Instant occurredAt
    ) implements FleetControlEffect {
        public ConnectionStateChanged {
            connection = Objects.requireNonNull(connection, "connection");
            occurredAt = Objects.requireNonNull(occurredAt, "occurredAt");
        }
    }
}
