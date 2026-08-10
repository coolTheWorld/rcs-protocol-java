package io.github.cooltheworld.rcs.protocol.vda5050.v3.model;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.ExtensionFields;
import java.util.Objects;

/** Factsheet Protocol Limits 中可选的不可变字符串长度声明。 */
public final class MaximumStringLengths {
    private final Long maximumMessageLength;
    private final Long maximumTopicSerialLength;
    private final Long maximumTopicElementLength;
    private final Long maximumIdLength;
    private final Boolean idNumericalOnly;
    private final Long maximumLoadIdLength;
    private final ExtensionFields extensionFields;

    private MaximumStringLengths(Builder builder) {
        this.maximumMessageLength = builder.maximumMessageLength;
        this.maximumTopicSerialLength = builder.maximumTopicSerialLength;
        this.maximumTopicElementLength = builder.maximumTopicElementLength;
        this.maximumIdLength = builder.maximumIdLength;
        this.idNumericalOnly = builder.idNumericalOnly;
        this.maximumLoadIdLength = builder.maximumLoadIdLength;
        this.extensionFields = builder.extensionFields == null
            ? ExtensionFields.empty()
            : builder.extensionFields;
    }

    /** @return 空的字符串长度 Builder */
    public static Builder builder() {
        return new Builder();
    }

    /** @return 可选的 MQTT 消息字节数上限 */
    public Long maximumMessageLength() {
        return maximumMessageLength;
    }

    /** @return 可选的 MQTT Topic 序列号部分长度上限 */
    public Long maximumTopicSerialLength() {
        return maximumTopicSerialLength;
    }

    /** @return 可选的 MQTT Topic 其他元素长度上限 */
    public Long maximumTopicElementLength() {
        return maximumTopicElementLength;
    }

    /** @return 可选的协议 ID 字符串长度上限 */
    public Long maximumIdLength() {
        return maximumIdLength;
    }

    /** @return ID 是否只能由数字组成，未声明时为 {@code null} */
    public Boolean idNumericalOnly() {
        return idNumericalOnly;
    }

    /** @return 可选的载荷 ID 字符串长度上限 */
    public Long maximumLoadIdLength() {
        return maximumLoadIdLength;
    }

    /** @return 不透明保存的未知扩展字段 */
    public ExtensionFields extensionFields() {
        return extensionFields;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
            || other instanceof MaximumStringLengths that
                && Objects.equals(maximumMessageLength, that.maximumMessageLength)
                && Objects.equals(
                    maximumTopicSerialLength,
                    that.maximumTopicSerialLength
                )
                && Objects.equals(
                    maximumTopicElementLength,
                    that.maximumTopicElementLength
                )
                && Objects.equals(maximumIdLength, that.maximumIdLength)
                && Objects.equals(idNumericalOnly, that.idNumericalOnly)
                && Objects.equals(maximumLoadIdLength, that.maximumLoadIdLength)
                && extensionFields.equals(that.extensionFields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            maximumMessageLength,
            maximumTopicSerialLength,
            maximumTopicElementLength,
            maximumIdLength,
            idNumericalOnly,
            maximumLoadIdLength,
            extensionFields
        );
    }

    /** 用于构造字符串长度声明。 */
    public static final class Builder {
        private Long maximumMessageLength;
        private Long maximumTopicSerialLength;
        private Long maximumTopicElementLength;
        private Long maximumIdLength;
        private Boolean idNumericalOnly;
        private Long maximumLoadIdLength;
        private ExtensionFields extensionFields;

        private Builder() {}

        public Builder maximumMessageLength(Long value) {
            this.maximumMessageLength = value;
            return this;
        }

        public Builder maximumTopicSerialLength(Long value) {
            this.maximumTopicSerialLength = value;
            return this;
        }

        public Builder maximumTopicElementLength(Long value) {
            this.maximumTopicElementLength = value;
            return this;
        }

        public Builder maximumIdLength(Long value) {
            this.maximumIdLength = value;
            return this;
        }

        public Builder idNumericalOnly(Boolean value) {
            this.idNumericalOnly = value;
            return this;
        }

        public Builder maximumLoadIdLength(Long value) {
            this.maximumLoadIdLength = value;
            return this;
        }

        public Builder extensionFields(ExtensionFields value) {
            this.extensionFields = value;
            return this;
        }

        /** @return 不可变字符串长度声明 */
        public MaximumStringLengths build() {
            return new MaximumStringLengths(this);
        }
    }
}
