package io.github.cooltheworld.rcs.protocol.vda5050.v3.model.trajectory;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.ExtensionFields;
import java.util.List;
import java.util.Objects;

/** Order、State、Visualization 与 Zone 请求共享的 NURBS Trajectory。 */
public final class Trajectory {
    private final Long degree;
    private final List<Double> knotVector;
    private final List<TrajectoryControlPoint> controlPoints;
    private final ExtensionFields extensionFields;

    private Trajectory(Builder builder) {
        this.degree = builder.degree;
        this.knotVector = builder.knotVector == null
            ? null
            : List.copyOf(builder.knotVector);
        this.controlPoints = List.copyOf(Objects.requireNonNull(
            builder.controlPoints,
            "controlPoints"
        ));
        this.extensionFields = builder.extensionFields == null
            ? ExtensionFields.empty()
            : builder.extensionFields;
    }

    /** @return 新的 Trajectory Builder */
    public static Builder builder() {
        return new Builder();
    }

    /** @return 可选的 NURBS degree；缺失时由消费方解释默认值 1 */
    public Long degree() {
        return degree;
    }

    /** @return 可选的不可变 knot vector；缺失时由消费方解释正文默认值 */
    public List<Double> knotVector() {
        return knotVector;
    }

    /** @return 必填且不可变的控制点列表 */
    public List<TrajectoryControlPoint> controlPoints() {
        return controlPoints;
    }

    /** @return 不透明保存的未知扩展字段 */
    public ExtensionFields extensionFields() {
        return extensionFields;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
            || other instanceof Trajectory that
                && Objects.equals(degree, that.degree)
                && Objects.equals(knotVector, that.knotVector)
                && controlPoints.equals(that.controlPoints)
                && extensionFields.equals(that.extensionFields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            degree,
            knotVector,
            controlPoints,
            extensionFields
        );
    }

    /** Trajectory Builder。 */
    public static final class Builder {
        private Long degree;
        private List<Double> knotVector;
        private List<TrajectoryControlPoint> controlPoints;
        private ExtensionFields extensionFields;

        private Builder() {}

        /** @param degree 可选的 NURBS degree @return 当前 Builder */
        public Builder degree(Long degree) {
            this.degree = degree;
            return this;
        }

        /** @param knotVector 可选的 knot vector @return 当前 Builder */
        public Builder knotVector(List<Double> knotVector) {
            this.knotVector = knotVector;
            return this;
        }

        /** @param controlPoints 必填且允许为空的控制点列表 @return 当前 Builder */
        public Builder controlPoints(
            List<TrajectoryControlPoint> controlPoints
        ) {
            this.controlPoints = controlPoints;
            return this;
        }

        /** @param extensionFields 未知扩展字段 @return 当前 Builder */
        public Builder extensionFields(ExtensionFields extensionFields) {
            this.extensionFields = extensionFields;
            return this;
        }

        /** @return 必填控制点集合存在的不可变 Trajectory */
        public Trajectory build() {
            return new Trajectory(this);
        }
    }
}
