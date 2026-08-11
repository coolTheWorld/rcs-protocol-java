package io.github.cooltheworld.rcs.protocol.vda5050.v3.model.trajectory;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.ExtensionFields;
import java.util.Objects;

/** 共享 NURBS Trajectory 的二维控制点。 */
public final class TrajectoryControlPoint {
    private final Double x;
    private final Double y;
    private final Double weight;
    private final ExtensionFields extensionFields;

    private TrajectoryControlPoint(Builder builder) {
        this.x = Objects.requireNonNull(builder.x, "x");
        this.y = Objects.requireNonNull(builder.y, "y");
        this.weight = builder.weight;
        this.extensionFields = builder.extensionFields == null
            ? ExtensionFields.empty()
            : builder.extensionFields;
    }

    /** @return 新的 Trajectory Control Point Builder */
    public static Builder builder() {
        return new Builder();
    }

    /** @return 项目坐标系中的 X 坐标 */
    public Double x() {
        return x;
    }

    /** @return 项目坐标系中的 Y 坐标 */
    public Double y() {
        return y;
    }

    /** @return 可选的显式 NURBS 权重；缺失时由消费方解释默认值 1.0 */
    public Double weight() {
        return weight;
    }

    /** @return 不透明保存的未知扩展字段 */
    public ExtensionFields extensionFields() {
        return extensionFields;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
            || other instanceof TrajectoryControlPoint that
                && x.equals(that.x)
                && y.equals(that.y)
                && Objects.equals(weight, that.weight)
                && extensionFields.equals(that.extensionFields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, weight, extensionFields);
    }

    /** Trajectory Control Point Builder。 */
    public static final class Builder {
        private Double x;
        private Double y;
        private Double weight;
        private ExtensionFields extensionFields;

        private Builder() {}

        /** @param x 项目坐标系中的 X 坐标 @return 当前 Builder */
        public Builder x(Double x) {
            this.x = x;
            return this;
        }

        /** @param y 项目坐标系中的 Y 坐标 @return 当前 Builder */
        public Builder y(Double y) {
            this.y = y;
            return this;
        }

        /** @param weight 可选的显式 NURBS 权重 @return 当前 Builder */
        public Builder weight(Double weight) {
            this.weight = weight;
            return this;
        }

        /** @param extensionFields 未知扩展字段 @return 当前 Builder */
        public Builder extensionFields(ExtensionFields extensionFields) {
            this.extensionFields = extensionFields;
            return this;
        }

        /** @return 必填坐标完整的 Trajectory Control Point */
        public TrajectoryControlPoint build() {
            return new TrajectoryControlPoint(this);
        }
    }
}
