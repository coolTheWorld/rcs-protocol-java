package io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.ExtensionFields;
import java.util.Objects;

/** 车轮相对于机器人坐标系原点的位置。 */
public final class WheelPosition {
    private final Double x;
    private final Double y;
    private final Double theta;
    private final ExtensionFields extensionFields;

    private WheelPosition(Builder builder) {
        this.x = Objects.requireNonNull(builder.x, "x");
        this.y = Objects.requireNonNull(builder.y, "y");
        this.theta = builder.theta;
        this.extensionFields = builder.extensionFields == null
            ? ExtensionFields.empty()
            : builder.extensionFields;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Double x() {
        return x;
    }

    public Double y() {
        return y;
    }

    /** @return 相对于机器人坐标系的可选朝向，单位 rad */
    public Double theta() {
        return theta;
    }

    public ExtensionFields extensionFields() {
        return extensionFields;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
            || other instanceof WheelPosition that
                && x.equals(that.x)
                && y.equals(that.y)
                && Objects.equals(theta, that.theta)
                && extensionFields.equals(that.extensionFields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, theta, extensionFields);
    }

    public static final class Builder {
        private Double x;
        private Double y;
        private Double theta;
        private ExtensionFields extensionFields;

        private Builder() {}

        public Builder x(Double x) {
            this.x = x;
            return this;
        }

        public Builder y(Double y) {
            this.y = y;
            return this;
        }

        public Builder theta(Double theta) {
            this.theta = theta;
            return this;
        }

        public Builder extensionFields(ExtensionFields extensionFields) {
            this.extensionFields = extensionFields;
            return this;
        }

        public WheelPosition build() {
            return new WheelPosition(this);
        }
    }
}
