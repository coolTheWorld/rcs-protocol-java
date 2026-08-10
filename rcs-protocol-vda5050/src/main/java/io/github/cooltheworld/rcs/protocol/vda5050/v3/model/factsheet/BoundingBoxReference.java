package io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.ExtensionFields;
import java.util.Objects;

/** 载荷包围盒底面中心在机器人坐标系中的不可变引用点。 */
public final class BoundingBoxReference {
    private final Double x;
    private final Double y;
    private final Double z;
    private final Double theta;
    private final ExtensionFields extensionFields;

    private BoundingBoxReference(Builder builder) {
        this.x = Objects.requireNonNull(builder.x, "x");
        this.y = Objects.requireNonNull(builder.y, "y");
        this.z = Objects.requireNonNull(builder.z, "z");
        this.theta = builder.theta;
        this.extensionFields = builder.extensionFields == null
            ? ExtensionFields.empty()
            : builder.extensionFields;
    }

    public static Builder builder() {
        return new Builder();
    }

    /** @return 引用点的 x 坐标 */
    public Double x() {
        return x;
    }

    /** @return 引用点的 y 坐标 */
    public Double y() {
        return y;
    }

    /** @return 引用点的 z 坐标 */
    public Double z() {
        return z;
    }

    /** @return 可选的载荷包围盒方向，单位 rad */
    public Double theta() {
        return theta;
    }

    public ExtensionFields extensionFields() {
        return extensionFields;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
            || other instanceof BoundingBoxReference that
                && x.equals(that.x)
                && y.equals(that.y)
                && z.equals(that.z)
                && Objects.equals(theta, that.theta)
                && extensionFields.equals(that.extensionFields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z, theta, extensionFields);
    }

    public static final class Builder {
        private Double x;
        private Double y;
        private Double z;
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

        public Builder z(Double z) {
            this.z = z;
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

        public BoundingBoxReference build() {
            return new BoundingBoxReference(this);
        }
    }
}
