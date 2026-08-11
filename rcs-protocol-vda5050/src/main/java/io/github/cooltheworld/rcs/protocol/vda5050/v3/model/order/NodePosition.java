package io.github.cooltheworld.rcs.protocol.vda5050.v3.model.order;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.ExtensionFields;
import java.util.Objects;

/** VDA 5050 Order 中不可变的 Node Position（节点位置）。 */
public final class NodePosition {
    private final Double x;
    private final Double y;
    private final Double theta;
    private final AllowedDeviationXY allowedDeviationXY;
    private final Double allowedDeviationTheta;
    private final String mapId;
    private final ExtensionFields extensionFields;

    private NodePosition(Builder builder) {
        this.x = Objects.requireNonNull(builder.x, "x");
        this.y = Objects.requireNonNull(builder.y, "y");
        this.theta = builder.theta;
        this.allowedDeviationXY = builder.allowedDeviationXY;
        this.allowedDeviationTheta = builder.allowedDeviationTheta;
        this.mapId = Objects.requireNonNull(builder.mapId, "mapId");
        this.extensionFields = builder.extensionFields == null
            ? ExtensionFields.empty()
            : builder.extensionFields;
    }

    /** @return 新的 Node Position Builder */
    public static Builder builder() {
        return new Builder();
    }

    /** @return 地图坐标 X */
    public Double x() {
        return x;
    }

    /** @return 地图坐标 Y */
    public Double y() {
        return y;
    }

    /** @return 可选的节点绝对方向 */
    public Double theta() {
        return theta;
    }

    /** @return 可选的二维偏差椭圆 */
    public AllowedDeviationXY allowedDeviationXY() {
        return allowedDeviationXY;
    }

    /** @return 可选的方向允许偏差 */
    public Double allowedDeviationTheta() {
        return allowedDeviationTheta;
    }

    /** @return 保持原文的地图标识 */
    public String mapId() {
        return mapId;
    }

    /** @return 不透明保存的未知扩展字段 */
    public ExtensionFields extensionFields() {
        return extensionFields;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
            || other instanceof NodePosition that
                && x.equals(that.x)
                && y.equals(that.y)
                && Objects.equals(theta, that.theta)
                && Objects.equals(
                    allowedDeviationXY,
                    that.allowedDeviationXY
                )
                && Objects.equals(
                    allowedDeviationTheta,
                    that.allowedDeviationTheta
                )
                && mapId.equals(that.mapId)
                && extensionFields.equals(that.extensionFields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            x,
            y,
            theta,
            allowedDeviationXY,
            allowedDeviationTheta,
            mapId,
            extensionFields
        );
    }

    /** Node Position Builder。 */
    public static final class Builder {
        private Double x;
        private Double y;
        private Double theta;
        private AllowedDeviationXY allowedDeviationXY;
        private Double allowedDeviationTheta;
        private String mapId;
        private ExtensionFields extensionFields;

        private Builder() {}

        /** @param x 地图坐标 X @return 当前 Builder */
        public Builder x(Double x) {
            this.x = x;
            return this;
        }

        /** @param y 地图坐标 Y @return 当前 Builder */
        public Builder y(Double y) {
            this.y = y;
            return this;
        }

        /** @param theta 可选的节点绝对方向 @return 当前 Builder */
        public Builder theta(Double theta) {
            this.theta = theta;
            return this;
        }

        /** @param allowedDeviationXY 可选的二维偏差椭圆 @return 当前 Builder */
        public Builder allowedDeviationXY(
            AllowedDeviationXY allowedDeviationXY
        ) {
            this.allowedDeviationXY = allowedDeviationXY;
            return this;
        }

        /** @param allowedDeviationTheta 可选的方向允许偏差 @return 当前 Builder */
        public Builder allowedDeviationTheta(Double allowedDeviationTheta) {
            this.allowedDeviationTheta = allowedDeviationTheta;
            return this;
        }

        /** @param mapId 保持原文的地图标识 @return 当前 Builder */
        public Builder mapId(String mapId) {
            this.mapId = mapId;
            return this;
        }

        /** @param extensionFields 未知扩展字段 @return 当前 Builder */
        public Builder extensionFields(ExtensionFields extensionFields) {
            this.extensionFields = extensionFields;
            return this;
        }

        /** @return 必填字段完整的 Node Position */
        public NodePosition build() {
            return new NodePosition(this);
        }
    }
}
