package io.github.cooltheworld.rcs.protocol.vda5050.v3.model;

import java.util.Objects;

/**
 * 八个标准 VDA 5050 Topic 共享的不可变协议消息头。
 *
 * <p>该 Java 类型组合公共字段；线路 JSON 中这些字段仍由各消息根对象平铺承载，Header
 * 不是嵌套 JSON 对象。{@code headerId} 的 {@code uint32} 范围由协议 Validator 检查。</p>
 */
public final class ProtocolHeader {
    private final Long headerId;
    private final ProtocolTimestamp timestamp;
    private final ProtocolVersion version;
    private final RobotIdentity robotIdentity;

    private ProtocolHeader(Builder builder) {
        this.headerId = Objects.requireNonNull(builder.headerId, "headerId");
        this.timestamp = Objects.requireNonNull(builder.timestamp, "timestamp");
        this.version = Objects.requireNonNull(builder.version, "version");
        this.robotIdentity = Objects.requireNonNull(
            builder.robotIdentity,
            "robotIdentity"
        );
    }

    /**
     * 创建空的消息头 Builder。
     *
     * @return 消息头 Builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /** @return 消息在对应 Topic 上的 {@code uint32} 编号 */
    public Long headerId() {
        return headerId;
    }

    /** @return 协议事件时间戳 */
    public ProtocolTimestamp timestamp() {
        return timestamp;
    }

    /** @return 消息声明的协议版本 */
    public ProtocolVersion version() {
        return version;
    }

    /** @return 移动机器人协议身份 */
    public RobotIdentity robotIdentity() {
        return robotIdentity;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
            || other instanceof ProtocolHeader that
                && headerId.equals(that.headerId)
                && timestamp.equals(that.timestamp)
                && version.equals(that.version)
                && robotIdentity.equals(that.robotIdentity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(headerId, timestamp, version, robotIdentity);
    }

    /** 用于构造必填字段完整的 {@link ProtocolHeader}。 */
    public static final class Builder {
        private Long headerId;
        private ProtocolTimestamp timestamp;
        private ProtocolVersion version;
        private RobotIdentity robotIdentity;

        private Builder() {}

        /**
         * @param headerId 消息在对应 Topic 上的编号
         * @return 当前 Builder
         */
        public Builder headerId(Long headerId) {
            this.headerId = headerId;
            return this;
        }

        /**
         * @param timestamp 协议事件时间戳
         * @return 当前 Builder
         */
        public Builder timestamp(ProtocolTimestamp timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        /**
         * @param version 消息声明的协议版本
         * @return 当前 Builder
         */
        public Builder version(ProtocolVersion version) {
            this.version = version;
            return this;
        }

        /**
         * @param robotIdentity 移动机器人协议身份
         * @return 当前 Builder
         */
        public Builder robotIdentity(RobotIdentity robotIdentity) {
            this.robotIdentity = robotIdentity;
            return this;
        }

        /**
         * 构造不可变消息头。
         *
         * @return 必填字段完整的消息头
         * @throws NullPointerException 任一必填字段未设置时
         */
        public ProtocolHeader build() {
            return new ProtocolHeader(this);
        }
    }
}
