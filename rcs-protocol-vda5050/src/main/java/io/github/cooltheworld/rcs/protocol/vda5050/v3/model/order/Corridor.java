package io.github.cooltheworld.rcs.protocol.vda5050.v3.model.order;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.ExtensionFields;
import java.util.Objects;

/** Edge 轨迹左右两侧可允许偏离的 Corridor（廊道）边界。 */
public final class Corridor {
    private final Double leftWidth;
    private final Double rightWidth;
    private final CorridorReferencePoint corridorReferencePoint;
    private final Boolean releaseRequired;
    private final CorridorReleaseLossBehavior releaseLossBehavior;
    private final ExtensionFields extensionFields;

    private Corridor(Builder builder) {
        this.leftWidth = Objects.requireNonNull(
            builder.leftWidth,
            "leftWidth"
        );
        this.rightWidth = Objects.requireNonNull(
            builder.rightWidth,
            "rightWidth"
        );
        this.corridorReferencePoint = builder.corridorReferencePoint;
        this.releaseRequired = builder.releaseRequired;
        this.releaseLossBehavior = builder.releaseLossBehavior;
        this.extensionFields = builder.extensionFields == null
            ? ExtensionFields.empty()
            : builder.extensionFields;
    }

    /** @return 新的 Corridor Builder */
    public static Builder builder() {
        return new Builder();
    }

    /** @return 相对轨迹左侧的允许宽度，单位米 */
    public Double leftWidth() {
        return leftWidth;
    }

    /** @return 相对轨迹右侧的允许宽度，单位米 */
    public Double rightWidth() {
        return rightWidth;
    }

    /** @return 可选的车体参考点；缺失时不物化默认值 */
    public CorridorReferencePoint corridorReferencePoint() {
        return corridorReferencePoint;
    }

    /** @return 是否必须请求 Fleet Control 授权；可为缺失 */
    public Boolean releaseRequired() {
        return releaseRequired;
    }

    /** @return 可选的授权丢失行为；缺失时不物化默认值 */
    public CorridorReleaseLossBehavior releaseLossBehavior() {
        return releaseLossBehavior;
    }

    /** @return 不透明保存的未知扩展字段 */
    public ExtensionFields extensionFields() {
        return extensionFields;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
            || other instanceof Corridor that
                && leftWidth.equals(that.leftWidth)
                && rightWidth.equals(that.rightWidth)
                && corridorReferencePoint == that.corridorReferencePoint
                && Objects.equals(releaseRequired, that.releaseRequired)
                && releaseLossBehavior == that.releaseLossBehavior
                && extensionFields.equals(that.extensionFields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            leftWidth,
            rightWidth,
            corridorReferencePoint,
            releaseRequired,
            releaseLossBehavior,
            extensionFields
        );
    }

    /** Corridor Builder。 */
    public static final class Builder {
        private Double leftWidth;
        private Double rightWidth;
        private CorridorReferencePoint corridorReferencePoint;
        private Boolean releaseRequired;
        private CorridorReleaseLossBehavior releaseLossBehavior;
        private ExtensionFields extensionFields;

        private Builder() {}

        /** @param leftWidth 轨迹左侧允许宽度 @return 当前 Builder */
        public Builder leftWidth(Double leftWidth) {
            this.leftWidth = leftWidth;
            return this;
        }

        /** @param rightWidth 轨迹右侧允许宽度 @return 当前 Builder */
        public Builder rightWidth(Double rightWidth) {
            this.rightWidth = rightWidth;
            return this;
        }

        /** @param corridorReferencePoint 车体参考点 @return 当前 Builder */
        public Builder corridorReferencePoint(
            CorridorReferencePoint corridorReferencePoint
        ) {
            this.corridorReferencePoint = corridorReferencePoint;
            return this;
        }

        /** @param releaseRequired 是否必须请求授权 @return 当前 Builder */
        public Builder releaseRequired(Boolean releaseRequired) {
            this.releaseRequired = releaseRequired;
            return this;
        }

        /** @param releaseLossBehavior 授权丢失行为 @return 当前 Builder */
        public Builder releaseLossBehavior(
            CorridorReleaseLossBehavior releaseLossBehavior
        ) {
            this.releaseLossBehavior = releaseLossBehavior;
            return this;
        }

        /** @param extensionFields 未知扩展字段 @return 当前 Builder */
        public Builder extensionFields(ExtensionFields extensionFields) {
            this.extensionFields = extensionFields;
            return this;
        }

        /** @return 必填字段完整的 Corridor */
        public Corridor build() {
            return new Corridor(this);
        }
    }
}
