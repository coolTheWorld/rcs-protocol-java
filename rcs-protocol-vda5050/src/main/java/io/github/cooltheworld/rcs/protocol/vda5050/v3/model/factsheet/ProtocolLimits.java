package io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.ExtensionFields;
import java.util.Objects;

/** Factsheet 中移动机器人声明的不可变协议限制。 */
public final class ProtocolLimits {
    private final MaximumStringLengths maximumStringLengths;
    private final MaximumArrayLengths maximumArrayLengths;
    private final ProtocolTiming timing;
    private final ExtensionFields extensionFields;

    private ProtocolLimits(Builder builder) {
        this.maximumStringLengths = Objects.requireNonNull(
            builder.maximumStringLengths,
            "maximumStringLengths"
        );
        this.maximumArrayLengths = Objects.requireNonNull(
            builder.maximumArrayLengths,
            "maximumArrayLengths"
        );
        this.timing = Objects.requireNonNull(builder.timing, "timing");
        this.extensionFields = builder.extensionFields == null
            ? ExtensionFields.empty()
            : builder.extensionFields;
    }

    /** @return 空的协议限制 Builder */
    public static Builder builder() {
        return new Builder();
    }

    /** @return 字符串长度能力声明 */
    public MaximumStringLengths maximumStringLengths() {
        return maximumStringLengths;
    }

    /** @return 数组长度能力声明 */
    public MaximumArrayLengths maximumArrayLengths() {
        return maximumArrayLengths;
    }

    /** @return 协议发送与发布时序声明 */
    public ProtocolTiming timing() {
        return timing;
    }

    /** @return 不透明保存的未知扩展字段 */
    public ExtensionFields extensionFields() {
        return extensionFields;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
            || other instanceof ProtocolLimits that
                && maximumStringLengths.equals(that.maximumStringLengths)
                && maximumArrayLengths.equals(that.maximumArrayLengths)
                && timing.equals(that.timing)
                && extensionFields.equals(that.extensionFields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            maximumStringLengths,
            maximumArrayLengths,
            timing,
            extensionFields
        );
    }

    /** 用于构造三个必填子对象完整的协议限制。 */
    public static final class Builder {
        private MaximumStringLengths maximumStringLengths;
        private MaximumArrayLengths maximumArrayLengths;
        private ProtocolTiming timing;
        private ExtensionFields extensionFields;

        private Builder() {}

        public Builder maximumStringLengths(MaximumStringLengths value) {
            this.maximumStringLengths = value;
            return this;
        }

        public Builder maximumArrayLengths(MaximumArrayLengths value) {
            this.maximumArrayLengths = value;
            return this;
        }

        public Builder timing(ProtocolTiming value) {
            this.timing = value;
            return this;
        }

        public Builder extensionFields(ExtensionFields value) {
            this.extensionFields = value;
            return this;
        }

        /** @return 必填子对象完整的不可变协议限制 */
        public ProtocolLimits build() {
            return new ProtocolLimits(this);
        }
    }
}
