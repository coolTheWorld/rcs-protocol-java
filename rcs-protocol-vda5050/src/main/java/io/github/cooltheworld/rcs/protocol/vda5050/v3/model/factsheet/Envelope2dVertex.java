package io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.ExtensionFields;
import java.util.Objects;

/** 二维包络多边形中的一个顶点。 */
public final class Envelope2dVertex {
    private final Double x;
    private final Double y;
    private final ExtensionFields extensionFields;

    private Envelope2dVertex(Builder builder) {
        this.x = Objects.requireNonNull(builder.x, "x");
        this.y = Objects.requireNonNull(builder.y, "y");
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

    public ExtensionFields extensionFields() {
        return extensionFields;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
            || other instanceof Envelope2dVertex that
                && x.equals(that.x)
                && y.equals(that.y)
                && extensionFields.equals(that.extensionFields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, extensionFields);
    }

    public static final class Builder {
        private Double x;
        private Double y;
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

        public Builder extensionFields(ExtensionFields extensionFields) {
            this.extensionFields = extensionFields;
            return this;
        }

        public Envelope2dVertex build() {
            return new Envelope2dVertex(this);
        }
    }
}
