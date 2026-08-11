package io.github.cooltheworld.rcs.protocol.vda5050.v3.model.order;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.ExtensionFields;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.action.Action;
import java.util.List;
import java.util.Objects;

/** VDA 5050 Order 中不可变的 Edge（有向边）非 Trajectory 聚合。 */
public final class Edge {
    private final String edgeId;
    private final Long sequenceId;
    private final String edgeDescriptor;
    private final Boolean released;
    private final Double maximumSpeed;
    private final Double maximumMobileRobotHeight;
    private final Double minimumLoadHandlingDeviceHeight;
    private final Double orientation;
    private final EdgeOrientationType orientationType;
    private final String direction;
    private final Boolean reachOrientationBeforeEntering;
    private final Double maximumRotationSpeed;
    private final Double length;
    private final Corridor corridor;
    private final List<Action> actions;
    private final ExtensionFields extensionFields;

    private Edge(Builder builder) {
        this.edgeId = Objects.requireNonNull(builder.edgeId, "edgeId");
        this.sequenceId = Objects.requireNonNull(
            builder.sequenceId,
            "sequenceId"
        );
        this.edgeDescriptor = builder.edgeDescriptor;
        this.released = Objects.requireNonNull(builder.released, "released");
        this.maximumSpeed = builder.maximumSpeed;
        this.maximumMobileRobotHeight = builder.maximumMobileRobotHeight;
        this.minimumLoadHandlingDeviceHeight =
            builder.minimumLoadHandlingDeviceHeight;
        this.orientation = builder.orientation;
        this.orientationType = builder.orientationType;
        this.direction = builder.direction;
        this.reachOrientationBeforeEntering =
            builder.reachOrientationBeforeEntering;
        this.maximumRotationSpeed = builder.maximumRotationSpeed;
        this.length = builder.length;
        this.corridor = builder.corridor;
        this.actions = List.copyOf(Objects.requireNonNull(
            builder.actions,
            "actions"
        ));
        this.extensionFields = builder.extensionFields == null
            ? ExtensionFields.empty()
            : builder.extensionFields;
    }

    /** @return 新的 Edge Builder */
    public static Builder builder() {
        return new Builder();
    }

    /** @return 保持原文的边标识 */
    public String edgeId() {
        return edgeId;
    }

    /** @return 边在 Order 图中的序列号 */
    public Long sequenceId() {
        return sequenceId;
    }

    /** @return 可选的人类可读边说明 */
    public String edgeDescriptor() {
        return edgeDescriptor;
    }

    /** @return 边是否已经进入 Base */
    public Boolean released() {
        return released;
    }

    /** @return 可选的最大行驶速度，单位米每秒 */
    public Double maximumSpeed() {
        return maximumSpeed;
    }

    /** @return 可选的车体与载荷最大高度，单位米 */
    public Double maximumMobileRobotHeight() {
        return maximumMobileRobotHeight;
    }

    /** @return 可选的载荷处理装置最小高度，单位米 */
    public Double minimumLoadHandlingDeviceHeight() {
        return minimumLoadHandlingDeviceHeight;
    }

    /** @return 可选的 Edge 方向角，单位弧度 */
    public Double orientation() {
        return orientation;
    }

    /** @return 可选的方向角参考系 */
    public EdgeOrientationType orientationType() {
        return orientationType;
    }

    /** @return 可选的导航分支原文指示 */
    public String direction() {
        return direction;
    }

    /** @return 是否必须在进入 Edge 前达到目标方向 */
    public Boolean reachOrientationBeforeEntering() {
        return reachOrientationBeforeEntering;
    }

    /** @return 可选的最大旋转速度，单位弧度每秒 */
    public Double maximumRotationSpeed() {
        return maximumRotationSpeed;
    }

    /** @return 可选的路径长度，单位米 */
    public Double length() {
        return length;
    }

    /** @return 可选的轨迹偏离 Corridor */
    public Corridor corridor() {
        return corridor;
    }

    /** @return 不可变的边 Action 列表，允许为空 */
    public List<Action> actions() {
        return actions;
    }

    /** @return 不透明保存的未知扩展字段 */
    public ExtensionFields extensionFields() {
        return extensionFields;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
            || other instanceof Edge that
                && edgeId.equals(that.edgeId)
                && sequenceId.equals(that.sequenceId)
                && Objects.equals(edgeDescriptor, that.edgeDescriptor)
                && released.equals(that.released)
                && Objects.equals(maximumSpeed, that.maximumSpeed)
                && Objects.equals(
                    maximumMobileRobotHeight,
                    that.maximumMobileRobotHeight
                )
                && Objects.equals(
                    minimumLoadHandlingDeviceHeight,
                    that.minimumLoadHandlingDeviceHeight
                )
                && Objects.equals(orientation, that.orientation)
                && orientationType == that.orientationType
                && Objects.equals(direction, that.direction)
                && Objects.equals(
                    reachOrientationBeforeEntering,
                    that.reachOrientationBeforeEntering
                )
                && Objects.equals(
                    maximumRotationSpeed,
                    that.maximumRotationSpeed
                )
                && Objects.equals(length, that.length)
                && Objects.equals(corridor, that.corridor)
                && actions.equals(that.actions)
                && extensionFields.equals(that.extensionFields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            edgeId,
            sequenceId,
            edgeDescriptor,
            released,
            maximumSpeed,
            maximumMobileRobotHeight,
            minimumLoadHandlingDeviceHeight,
            orientation,
            orientationType,
            direction,
            reachOrientationBeforeEntering,
            maximumRotationSpeed,
            length,
            corridor,
            actions,
            extensionFields
        );
    }

    /** Edge Builder。 */
    public static final class Builder {
        private String edgeId;
        private Long sequenceId;
        private String edgeDescriptor;
        private Boolean released;
        private Double maximumSpeed;
        private Double maximumMobileRobotHeight;
        private Double minimumLoadHandlingDeviceHeight;
        private Double orientation;
        private EdgeOrientationType orientationType;
        private String direction;
        private Boolean reachOrientationBeforeEntering;
        private Double maximumRotationSpeed;
        private Double length;
        private Corridor corridor;
        private List<Action> actions;
        private ExtensionFields extensionFields;

        private Builder() {}

        /** @param edgeId 保持原文的边标识 @return 当前 Builder */
        public Builder edgeId(String edgeId) {
            this.edgeId = edgeId;
            return this;
        }

        /** @param sequenceId 边序列号 @return 当前 Builder */
        public Builder sequenceId(Long sequenceId) {
            this.sequenceId = sequenceId;
            return this;
        }

        /** @param edgeDescriptor 可选的人类可读边说明 @return 当前 Builder */
        public Builder edgeDescriptor(String edgeDescriptor) {
            this.edgeDescriptor = edgeDescriptor;
            return this;
        }

        /** @param released 边是否已经进入 Base @return 当前 Builder */
        public Builder released(Boolean released) {
            this.released = released;
            return this;
        }

        /** @param maximumSpeed 可选最大行驶速度 @return 当前 Builder */
        public Builder maximumSpeed(Double maximumSpeed) {
            this.maximumSpeed = maximumSpeed;
            return this;
        }

        /** @param maximumMobileRobotHeight 可选最大车体高度 @return 当前 Builder */
        public Builder maximumMobileRobotHeight(
            Double maximumMobileRobotHeight
        ) {
            this.maximumMobileRobotHeight = maximumMobileRobotHeight;
            return this;
        }

        /** @param minimumLoadHandlingDeviceHeight 可选最小装置高度 @return 当前 Builder */
        public Builder minimumLoadHandlingDeviceHeight(
            Double minimumLoadHandlingDeviceHeight
        ) {
            this.minimumLoadHandlingDeviceHeight =
                minimumLoadHandlingDeviceHeight;
            return this;
        }

        /** @param orientation 可选 Edge 方向角 @return 当前 Builder */
        public Builder orientation(Double orientation) {
            this.orientation = orientation;
            return this;
        }

        /** @param orientationType 可选方向角参考系 @return 当前 Builder */
        public Builder orientationType(EdgeOrientationType orientationType) {
            this.orientationType = orientationType;
            return this;
        }

        /** @param direction 可选导航分支原文指示 @return 当前 Builder */
        public Builder direction(String direction) {
            this.direction = direction;
            return this;
        }

        /** @param reachOrientationBeforeEntering 进入前是否达到方向 @return 当前 Builder */
        public Builder reachOrientationBeforeEntering(
            Boolean reachOrientationBeforeEntering
        ) {
            this.reachOrientationBeforeEntering =
                reachOrientationBeforeEntering;
            return this;
        }

        /** @param maximumRotationSpeed 可选最大旋转速度 @return 当前 Builder */
        public Builder maximumRotationSpeed(Double maximumRotationSpeed) {
            this.maximumRotationSpeed = maximumRotationSpeed;
            return this;
        }

        /** @param length 可选路径长度 @return 当前 Builder */
        public Builder length(Double length) {
            this.length = length;
            return this;
        }

        /** @param corridor 可选轨迹偏离 Corridor @return 当前 Builder */
        public Builder corridor(Corridor corridor) {
            this.corridor = corridor;
            return this;
        }

        /** @param actions 必填且允许为空的边 Action 列表 @return 当前 Builder */
        public Builder actions(List<Action> actions) {
            this.actions = actions;
            return this;
        }

        /** @param extensionFields 未知扩展字段 @return 当前 Builder */
        public Builder extensionFields(ExtensionFields extensionFields) {
            this.extensionFields = extensionFields;
            return this;
        }

        /** @return 必填字段完整的不可变 Edge */
        public Edge build() {
            return new Edge(this);
        }
    }
}
