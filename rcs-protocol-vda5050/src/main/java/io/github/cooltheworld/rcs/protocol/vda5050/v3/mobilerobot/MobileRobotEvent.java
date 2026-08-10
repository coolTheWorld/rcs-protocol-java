package io.github.cooltheworld.rcs.protocol.vda5050.v3.mobilerobot;

import java.time.Instant;
import java.util.Objects;

/** Mobile Robot 状态机可以接收的封闭事件集合。 */
public sealed interface MobileRobotEvent
    permits MobileRobotEvent.ConnectionOpeningRequested {
    /** @return 外部采集的事件发生时间 */
    Instant occurredAt();

    /** 请求为新的 Broker 连接配置 Last Will 并发布 ONLINE。 */
    record ConnectionOpeningRequested(Instant occurredAt)
        implements MobileRobotEvent {
        public ConnectionOpeningRequested {
            occurredAt = Objects.requireNonNull(occurredAt, "occurredAt");
        }
    }
}
