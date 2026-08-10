package io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.ExtensionFields;
import java.util.Objects;

/** Factsheet Protocol Limits 中的不可变发送与发布时序声明。 */
public final class ProtocolTiming {
    private final Double minimumOrderInterval;
    private final Double minimumStateInterval;
    private final Double defaultStateInterval;
    private final Double visualizationInterval;
    private final ExtensionFields extensionFields;

    private ProtocolTiming(Builder builder) {
        this.minimumOrderInterval = Objects.requireNonNull(
            builder.minimumOrderInterval,
            "minimumOrderInterval"
        );
        this.minimumStateInterval = Objects.requireNonNull(
            builder.minimumStateInterval,
            "minimumStateInterval"
        );
        this.defaultStateInterval = builder.defaultStateInterval;
        this.visualizationInterval = builder.visualizationInterval;
        this.extensionFields = builder.extensionFields == null
            ? ExtensionFields.empty()
            : builder.extensionFields;
    }

    /** @return 空的协议时序 Builder */
    public static Builder builder() {
        return new Builder();
    }

    /** @return 发送 order 消息的最小间隔，单位秒 */
    public Double minimumOrderInterval() {
        return minimumOrderInterval;
    }

    /** @return 发送 state 消息的最小间隔，单位秒 */
    public Double minimumStateInterval() {
        return minimumStateInterval;
    }

    /** @return 可选的 state 默认发送间隔，单位秒 */
    public Double defaultStateInterval() {
        return defaultStateInterval;
    }

    /** @return 可选的 visualization 发送间隔，单位秒 */
    public Double visualizationInterval() {
        return visualizationInterval;
    }

    /** @return 不透明保存的未知扩展字段 */
    public ExtensionFields extensionFields() {
        return extensionFields;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
            || other instanceof ProtocolTiming that
                && minimumOrderInterval.equals(that.minimumOrderInterval)
                && minimumStateInterval.equals(that.minimumStateInterval)
                && Objects.equals(
                    defaultStateInterval,
                    that.defaultStateInterval
                )
                && Objects.equals(
                    visualizationInterval,
                    that.visualizationInterval
                )
                && extensionFields.equals(that.extensionFields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            minimumOrderInterval,
            minimumStateInterval,
            defaultStateInterval,
            visualizationInterval,
            extensionFields
        );
    }

    /** 用于构造必填最小间隔完整的协议时序。 */
    public static final class Builder {
        private Double minimumOrderInterval;
        private Double minimumStateInterval;
        private Double defaultStateInterval;
        private Double visualizationInterval;
        private ExtensionFields extensionFields;

        private Builder() {}

        public Builder minimumOrderInterval(Double value) {
            this.minimumOrderInterval = value;
            return this;
        }

        public Builder minimumStateInterval(Double value) {
            this.minimumStateInterval = value;
            return this;
        }

        public Builder defaultStateInterval(Double value) {
            this.defaultStateInterval = value;
            return this;
        }

        public Builder visualizationInterval(Double value) {
            this.visualizationInterval = value;
            return this;
        }

        public Builder extensionFields(ExtensionFields value) {
            this.extensionFields = value;
            return this;
        }

        /** @return 必填字段完整的不可变协议时序 */
        public ProtocolTiming build() {
            return new ProtocolTiming(this);
        }
    }
}
