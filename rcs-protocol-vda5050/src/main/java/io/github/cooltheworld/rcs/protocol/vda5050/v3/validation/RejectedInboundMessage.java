package io.github.cooltheworld.rcs.protocol.vda5050.v3.validation;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.internal.Unsigned32;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.common.ProtocolTimestamp;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.common.ProtocolVersion;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.common.RobotIdentity;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.topic.TopicName;
import java.util.List;
import java.util.Objects;

/**
 * 前三层校验未通过的入站消息数据。
 *
 * <p>对象只保留标准 Topic、结构化问题以及能够独立安全解析的强类型 Header 上下文。原始
 * payload、动态 JSON 与任意键值容器均不进入该边界。</p>
 *
 * @param <T> 期望的强类型协议消息类型；拒绝分支不携带该类型的实例
 */
public final class RejectedInboundMessage<T>
    implements ValidationResult<T>, DecodingResult<T> {
    private final TopicName topic;
    private final RobotIdentity robotIdentity;
    private final Long headerId;
    private final ProtocolTimestamp timestamp;
    private final ProtocolVersion version;
    private final List<ValidationIssue> issues;

    private RejectedInboundMessage(Builder<T> builder) {
        this.topic = builder.topic;
        this.robotIdentity = builder.robotIdentity;
        this.headerId = builder.headerId;
        this.timestamp = builder.timestamp;
        this.version = builder.version;
        this.issues = List.copyOf(builder.issues);
        if (this.issues.stream().noneMatch(RejectedInboundMessage::isError)) {
            throw new IllegalArgumentException(
                "Rejected message must contain at least one error issue"
            );
        }
    }

    /**
     * 创建拒绝消息 Builder。
     *
     * @param topic 已识别的标准 Topic 名称
     * @param issues 校验问题；构造结果时必须至少包含一个错误
     * @param <T> 期望的强类型协议消息类型
     * @return 拒绝消息 Builder
     */
    public static <T> Builder<T> builder(
        TopicName topic,
        List<ValidationIssue> issues
    ) {
        return new Builder<>(topic, issues);
    }

    /** @return 入站消息对应的标准 Topic 名称 */
    public TopicName topic() {
        return topic;
    }

    /** @return 可安全提取的机器人身份；不可用时为 {@code null} */
    public RobotIdentity robotIdentity() {
        return robotIdentity;
    }

    /** @return 可安全提取且位于 {@code uint32} 范围的 Header ID；不可用时为 {@code null} */
    public Long headerId() {
        return headerId;
    }

    /** @return 可安全提取的严格协议时间戳；不可用时为 {@code null} */
    public ProtocolTimestamp timestamp() {
        return timestamp;
    }

    /** @return 可安全提取的协议版本；不可用时为 {@code null} */
    public ProtocolVersion version() {
        return version;
    }

    @Override
    public boolean isAccepted() {
        return false;
    }

    @Override
    public boolean isDecoded() {
        return false;
    }

    @Override
    public List<ValidationIssue> issues() {
        return issues;
    }

    private static boolean isError(ValidationIssue issue) {
        return issue.severity() == ValidationSeverity.ERROR;
    }

    /** 用于组合拒绝结果中的可用安全上下文。 */
    public static final class Builder<T> {
        private final TopicName topic;
        private final List<ValidationIssue> issues;
        private RobotIdentity robotIdentity;
        private Long headerId;
        private ProtocolTimestamp timestamp;
        private ProtocolVersion version;

        private Builder(TopicName topic, List<ValidationIssue> issues) {
            this.topic = Objects.requireNonNull(topic, "topic");
            this.issues = List.copyOf(Objects.requireNonNull(issues, "issues"));
        }

        /** @param robotIdentity 可安全提取的机器人身份 @return 当前 Builder */
        public Builder<T> robotIdentity(RobotIdentity robotIdentity) {
            this.robotIdentity = robotIdentity;
            return this;
        }

        /**
         * @param headerId 可安全提取的 Header ID；非空值必须位于 {@code uint32} 范围
         * @return 当前 Builder
         */
        public Builder<T> headerId(Long headerId) {
            if (headerId != null && !Unsigned32.isValid(headerId)) {
                throw new IllegalArgumentException(
                    "Header ID context is outside the uint32 range"
                );
            }
            this.headerId = headerId;
            return this;
        }

        /** @param timestamp 可安全提取的严格协议时间戳 @return 当前 Builder */
        public Builder<T> timestamp(ProtocolTimestamp timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        /** @param version 可安全提取的协议版本 @return 当前 Builder */
        public Builder<T> version(ProtocolVersion version) {
            this.version = version;
            return this;
        }

        /**
         * 构造不可变拒绝结果。
         *
         * @return 结构化拒绝消息
         * @throws IllegalArgumentException 问题列表不包含错误时
         */
        public RejectedInboundMessage<T> build() {
            return new RejectedInboundMessage<>(this);
        }
    }
}
