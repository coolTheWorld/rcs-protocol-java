package io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.ExtensionFields;
import java.util.Objects;

/** Factsheet 中载荷包围盒的不可变尺寸，单位为米。 */
public final class LoadDimensions {
    private final Double length;
    private final Double width;
    private final Double height;
    private final ExtensionFields extensionFields;

    private LoadDimensions(Builder builder) {
        this.length = Objects.requireNonNull(builder.length, "length");
        this.width = Objects.requireNonNull(builder.width, "width");
        this.height = builder.height;
        this.extensionFields = builder.extensionFields == null
            ? ExtensionFields.empty()
            : builder.extensionFields;
    }

    public static Builder builder() {
        return new Builder();
    }

    /** @return 沿机器人坐标系 x 轴的绝对长度，单位 m */
    public Double length() {
        return length;
    }

    /** @return 沿机器人坐标系 y 轴的绝对宽度，单位 m */
    public Double width() {
        return width;
    }

    /** @return 已知时的可选绝对高度，单位 m */
    public Double height() {
        return height;
    }

    public ExtensionFields extensionFields() {
        return extensionFields;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
            || other instanceof LoadDimensions that
                && length.equals(that.length)
                && width.equals(that.width)
                && Objects.equals(height, that.height)
                && extensionFields.equals(that.extensionFields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(length, width, height, extensionFields);
    }

    public static final class Builder {
        private Double length;
        private Double width;
        private Double height;
        private ExtensionFields extensionFields;

        private Builder() {}

        public Builder length(Double length) {
            this.length = length;
            return this;
        }

        public Builder width(Double width) {
            this.width = width;
            return this;
        }

        public Builder height(Double height) {
            this.height = height;
            return this;
        }

        public Builder extensionFields(ExtensionFields extensionFields) {
            this.extensionFields = extensionFields;
            return this;
        }

        public LoadDimensions build() {
            return new LoadDimensions(this);
        }
    }
}
