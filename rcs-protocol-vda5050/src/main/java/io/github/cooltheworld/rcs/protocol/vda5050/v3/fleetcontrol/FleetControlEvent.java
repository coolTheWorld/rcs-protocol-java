package io.github.cooltheworld.rcs.protocol.vda5050.v3.fleetcontrol;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.Connection;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.ValidatedMessage;
import java.time.Instant;
import java.util.Objects;

/** Fleet Control 状态机可以接收的封闭事件集合。 */
public sealed interface FleetControlEvent
    permits FleetControlEvent.ConnectionReceived {
    /** @return 外部采集的事件发生时间 */
    Instant occurredAt();

    /** 已通过前三层校验的 Connection 入站事件。 */
    record ConnectionReceived(
        ValidatedMessage<Connection> message,
        Instant occurredAt
    ) implements FleetControlEvent {
        public ConnectionReceived {
            message = Objects.requireNonNull(message, "message");
            occurredAt = Objects.requireNonNull(occurredAt, "occurredAt");
        }
    }
}
