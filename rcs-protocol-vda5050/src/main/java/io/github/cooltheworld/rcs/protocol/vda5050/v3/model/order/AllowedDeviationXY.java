package io.github.cooltheworld.rcs.protocol.vda5050.v3.model.order;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.ExtensionFields;
import java.util.Objects;

/** Node Position（节点位置）允许的二维偏差椭圆。 */
public final class AllowedDeviationXY {
    private final Double a;
    private final Double b;
    private final Double theta;
    private final ExtensionFields extensionFields;

    private AllowedDeviationXY(Builder builder) {
        this.a = Objects.requireNonNull(builder.a, "a");
        this.b = Objects.requireNonNull(builder.b, "b");
        this.theta = Objects.requireNonNull(builder.theta, "theta");
        this.extensionFields = builder.extensionFields == null
            ? ExtensionFields.empty()
            : builder.extensionFields;
    }

    /** @return 新的偏差椭圆 Builder */
    public static Builder builder() {
        return new Builder();
    }

    /** @return 椭圆半长轴长度 */
    public Double a() {
        return a;
    }

    /** @return 椭圆半短轴长度 */
    public Double b() {
        return b;
    }

    /** @return 椭圆旋转角 */
    public Double theta() {
        return theta;
    }

    /** @return 不透明保存的未知扩展字段 */
    public ExtensionFields extensionFields() {
        return extensionFields;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
            || other instanceof AllowedDeviationXY that
                && a.equals(that.a)
                && b.equals(that.b)
                && theta.equals(that.theta)
                && extensionFields.equals(that.extensionFields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(a, b, theta, extensionFields);
    }

    /** 允许二维偏差椭圆 Builder。 */
    public static final class Builder {
        private Double a;
        private Double b;
        private Double theta;
        private ExtensionFields extensionFields;

        private Builder() {}

        /** @param a 椭圆半长轴长度 @return 当前 Builder */
        public Builder a(Double a) {
            this.a = a;
            return this;
        }

        /** @param b 椭圆半短轴长度 @return 当前 Builder */
        public Builder b(Double b) {
            this.b = b;
            return this;
        }

        /** @param theta 椭圆旋转角 @return 当前 Builder */
        public Builder theta(Double theta) {
            this.theta = theta;
            return this;
        }

        /** @param extensionFields 未知扩展字段 @return 当前 Builder */
        public Builder extensionFields(ExtensionFields extensionFields) {
            this.extensionFields = extensionFields;
            return this;
        }

        /** @return 必填字段完整的偏差椭圆 */
        public AllowedDeviationXY build() {
            return new AllowedDeviationXY(this);
        }
    }
}
