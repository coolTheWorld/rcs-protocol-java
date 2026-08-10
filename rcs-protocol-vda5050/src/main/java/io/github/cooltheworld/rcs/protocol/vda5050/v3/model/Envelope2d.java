package io.github.cooltheworld.rcs.protocol.vda5050.v3.model;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.ExtensionFields;
import java.util.List;
import java.util.Objects;

/** Factsheet 中机器人二维包络的不可变定义。 */
public final class Envelope2d {
    private final String envelope2dId;
    private final List<Envelope2dVertex> vertices;
    private final String description;
    private final ExtensionFields extensionFields;

    private Envelope2d(Builder builder) {
        this.envelope2dId = Objects.requireNonNull(
            builder.envelope2dId,
            "envelope2dId"
        );
        this.vertices = List.copyOf(
            Objects.requireNonNull(builder.vertices, "vertices")
        );
        this.description = builder.description;
        this.extensionFields = builder.extensionFields == null
            ? ExtensionFields.empty()
            : builder.extensionFields;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String envelope2dId() {
        return envelope2dId;
    }

    public List<Envelope2dVertex> vertices() {
        return vertices;
    }

    public String description() {
        return description;
    }

    public ExtensionFields extensionFields() {
        return extensionFields;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
            || other instanceof Envelope2d that
                && envelope2dId.equals(that.envelope2dId)
                && vertices.equals(that.vertices)
                && Objects.equals(description, that.description)
                && extensionFields.equals(that.extensionFields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            envelope2dId,
            vertices,
            description,
            extensionFields
        );
    }

    public static final class Builder {
        private String envelope2dId;
        private List<Envelope2dVertex> vertices;
        private String description;
        private ExtensionFields extensionFields;

        private Builder() {}

        public Builder envelope2dId(String envelope2dId) {
            this.envelope2dId = envelope2dId;
            return this;
        }

        public Builder vertices(List<Envelope2dVertex> vertices) {
            this.vertices = vertices;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder extensionFields(ExtensionFields extensionFields) {
            this.extensionFields = extensionFields;
            return this;
        }

        public Envelope2d build() {
            return new Envelope2d(this);
        }
    }
}
