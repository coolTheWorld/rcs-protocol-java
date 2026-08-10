package io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.ExtensionFields;
import java.util.Objects;

/** Factsheet 中移动机器人的不可变基础物理参数。 */
public final class PhysicalParameters {
    private final Double minimumSpeed;
    private final Double maximumSpeed;
    private final Double minimumAngularSpeed;
    private final Double maximumAngularSpeed;
    private final Double maximumAcceleration;
    private final Double maximumDeceleration;
    private final Double minimumHeight;
    private final Double maximumHeight;
    private final Double width;
    private final Double length;
    private final ExtensionFields extensionFields;

    private PhysicalParameters(Builder builder) {
        this.minimumSpeed = Objects.requireNonNull(
            builder.minimumSpeed,
            "minimumSpeed"
        );
        this.maximumSpeed = Objects.requireNonNull(
            builder.maximumSpeed,
            "maximumSpeed"
        );
        this.minimumAngularSpeed = builder.minimumAngularSpeed;
        this.maximumAngularSpeed = builder.maximumAngularSpeed;
        this.maximumAcceleration = Objects.requireNonNull(
            builder.maximumAcceleration,
            "maximumAcceleration"
        );
        this.maximumDeceleration = Objects.requireNonNull(
            builder.maximumDeceleration,
            "maximumDeceleration"
        );
        this.minimumHeight = Objects.requireNonNull(
            builder.minimumHeight,
            "minimumHeight"
        );
        this.maximumHeight = Objects.requireNonNull(
            builder.maximumHeight,
            "maximumHeight"
        );
        this.width = Objects.requireNonNull(builder.width, "width");
        this.length = Objects.requireNonNull(builder.length, "length");
        this.extensionFields = builder.extensionFields == null
            ? ExtensionFields.empty()
            : builder.extensionFields;
    }

    /** @return 空的物理参数 Builder */
    public static Builder builder() {
        return new Builder();
    }

    /** @return 最小可控连续线速度，单位 m/s */
    public Double minimumSpeed() {
        return minimumSpeed;
    }

    /** @return 最大线速度，单位 m/s */
    public Double maximumSpeed() {
        return maximumSpeed;
    }

    /** @return 可选的最小可控连续角速度，单位 rad/s */
    public Double minimumAngularSpeed() {
        return minimumAngularSpeed;
    }

    /** @return 可选的最大角速度，单位 rad/s */
    public Double maximumAngularSpeed() {
        return maximumAngularSpeed;
    }

    /** @return 最大线加速度，单位 m/s² */
    public Double maximumAcceleration() {
        return maximumAcceleration;
    }

    /** @return 最大线减速度，单位 m/s² */
    public Double maximumDeceleration() {
        return maximumDeceleration;
    }

    /** @return 最小高度，单位 m */
    public Double minimumHeight() {
        return minimumHeight;
    }

    /** @return 最大高度，单位 m */
    public Double maximumHeight() {
        return maximumHeight;
    }

    /** @return 机器人宽度，单位 m */
    public Double width() {
        return width;
    }

    /** @return 机器人长度，单位 m */
    public Double length() {
        return length;
    }

    /** @return 不透明保存的未知扩展字段 */
    public ExtensionFields extensionFields() {
        return extensionFields;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
            || other instanceof PhysicalParameters that
                && minimumSpeed.equals(that.minimumSpeed)
                && maximumSpeed.equals(that.maximumSpeed)
                && Objects.equals(
                    minimumAngularSpeed,
                    that.minimumAngularSpeed
                )
                && Objects.equals(
                    maximumAngularSpeed,
                    that.maximumAngularSpeed
                )
                && maximumAcceleration.equals(that.maximumAcceleration)
                && maximumDeceleration.equals(that.maximumDeceleration)
                && minimumHeight.equals(that.minimumHeight)
                && maximumHeight.equals(that.maximumHeight)
                && width.equals(that.width)
                && length.equals(that.length)
                && extensionFields.equals(that.extensionFields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            minimumSpeed,
            maximumSpeed,
            minimumAngularSpeed,
            maximumAngularSpeed,
            maximumAcceleration,
            maximumDeceleration,
            minimumHeight,
            maximumHeight,
            width,
            length,
            extensionFields
        );
    }

    /** 用于构造必填字段完整的 {@link PhysicalParameters}。 */
    public static final class Builder {
        private Double minimumSpeed;
        private Double maximumSpeed;
        private Double minimumAngularSpeed;
        private Double maximumAngularSpeed;
        private Double maximumAcceleration;
        private Double maximumDeceleration;
        private Double minimumHeight;
        private Double maximumHeight;
        private Double width;
        private Double length;
        private ExtensionFields extensionFields;

        private Builder() {}

        public Builder minimumSpeed(Double minimumSpeed) {
            this.minimumSpeed = minimumSpeed;
            return this;
        }

        public Builder maximumSpeed(Double maximumSpeed) {
            this.maximumSpeed = maximumSpeed;
            return this;
        }

        public Builder minimumAngularSpeed(Double minimumAngularSpeed) {
            this.minimumAngularSpeed = minimumAngularSpeed;
            return this;
        }

        public Builder maximumAngularSpeed(Double maximumAngularSpeed) {
            this.maximumAngularSpeed = maximumAngularSpeed;
            return this;
        }

        public Builder maximumAcceleration(Double maximumAcceleration) {
            this.maximumAcceleration = maximumAcceleration;
            return this;
        }

        public Builder maximumDeceleration(Double maximumDeceleration) {
            this.maximumDeceleration = maximumDeceleration;
            return this;
        }

        public Builder minimumHeight(Double minimumHeight) {
            this.minimumHeight = minimumHeight;
            return this;
        }

        public Builder maximumHeight(Double maximumHeight) {
            this.maximumHeight = maximumHeight;
            return this;
        }

        public Builder width(Double width) {
            this.width = width;
            return this;
        }

        public Builder length(Double length) {
            this.length = length;
            return this;
        }

        public Builder extensionFields(ExtensionFields extensionFields) {
            this.extensionFields = extensionFields;
            return this;
        }

        /** @return 必填字段完整的物理参数 */
        public PhysicalParameters build() {
            return new PhysicalParameters(this);
        }
    }
}
