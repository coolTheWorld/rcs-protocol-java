package io.github.cooltheworld.rcs.protocol.vda5050.v3.mobilerobot.event;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.connection.ConnectionState;
import java.time.Instant;
import java.util.Objects;

/** Mobile Robot 状态机可以接收的封闭事件集合。 */
public sealed interface MobileRobotEvent
    permits MobileRobotEvent.ConnectionOpeningRequested,
        MobileRobotEvent.ConnectionStatePublicationRequested {
    /** @return 外部采集的事件发生时间 */
    Instant occurredAt();

    /** 请求为新的 Broker 连接配置 Last Will 并发布 ONLINE。 */
    record ConnectionOpeningRequested(Instant occurredAt)
        implements MobileRobotEvent {
        public ConnectionOpeningRequested {
            occurredAt = Objects.requireNonNull(occurredAt, "occurredAt");
        }
    }

    /** 请求主动发布 Mobile Robot 允许产生的 Connection 状态。 */
    record ConnectionStatePublicationRequested(
        ConnectionState connectionState,
        Instant occurredAt
    ) implements MobileRobotEvent {
        public ConnectionStatePublicationRequested {
            connectionState = Objects.requireNonNull(
                connectionState,
                "connectionState"
            );
            occurredAt = Objects.requireNonNull(occurredAt, "occurredAt");
            if (connectionState == ConnectionState.CONNECTION_BROKEN) {
                throw new IllegalArgumentException(
                    "Mobile Robot must not actively publish CONNECTION_BROKEN"
                );
            }
        }
    }
}
