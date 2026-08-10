package io.github.cooltheworld.rcs.protocol.vda5050.v3.fleetcontrol.effect;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.connection.Connection;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.connection.ConnectionState;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.common.RobotIdentity;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.topic.TopicName;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.ValidationIssue;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.ValidationSeverity;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

/** Fleet Control 状态机产生的封闭效果集合。 */
public sealed interface FleetControlEffect
    permits FleetControlEffect.ConnectionStateChanged,
        FleetControlEffect.InboundMessageRejected,
        FleetControlEffect.UnknownExtensionObserved {
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

    /** 向外部报告未通过校验的入站消息，不携带原始 payload。 */
    record InboundMessageRejected(
        TopicName topic,
        RobotIdentity robotIdentity,
        Long headerId,
        List<ValidationIssue> issues,
        Instant occurredAt
    ) implements FleetControlEffect {
        public InboundMessageRejected {
            topic = Objects.requireNonNull(topic, "topic");
            issues = List.copyOf(Objects.requireNonNull(issues, "issues"));
            occurredAt = Objects.requireNonNull(occurredAt, "occurredAt");
            if (issues.stream().noneMatch(InboundMessageRejected::isError)) {
                throw new IllegalArgumentException(
                    "Rejected message effect must contain at least one error issue"
                );
            }
        }

        private static boolean isError(ValidationIssue issue) {
            return issue.severity() == ValidationSeverity.ERROR;
        }
    }

    /** 向外部报告消息含有未知扩展；不暴露扩展名称或值。 */
    record UnknownExtensionObserved(
        TopicName topic,
        RobotIdentity robotIdentity,
        Long headerId,
        Instant occurredAt
    ) implements FleetControlEffect {
        public UnknownExtensionObserved {
            topic = Objects.requireNonNull(topic, "topic");
            robotIdentity = Objects.requireNonNull(
                robotIdentity,
                "robotIdentity"
            );
            headerId = Objects.requireNonNull(headerId, "headerId");
            occurredAt = Objects.requireNonNull(occurredAt, "occurredAt");
        }
    }
}
