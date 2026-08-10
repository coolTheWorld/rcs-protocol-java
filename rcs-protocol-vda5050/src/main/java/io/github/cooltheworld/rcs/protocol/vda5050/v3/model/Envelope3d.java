package io.github.cooltheworld.rcs.protocol.vda5050.v3.model;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.ExtensionFields;
import java.util.Objects;

/** Factsheet 中机器人三维包络的不可变定义。 */
public final class Envelope3d {
    private final String envelope3dId;
    private final String format;
    private final Envelope3dData data;
    private final String url;
    private final String description;
    private final ExtensionFields extensionFields;

    private Envelope3d(Builder builder) {
        this.envelope3dId = Objects.requireNonNull(
            builder.envelope3dId,
            "envelope3dId"
        );
        this.format = Objects.requireNonNull(builder.format, "format");
        this.data = builder.data;
        this.url = builder.url;
        this.description = builder.description;
        this.extensionFields = builder.extensionFields == null
            ? ExtensionFields.empty()
            : builder.extensionFields;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String envelope3dId() {
        return envelope3dId;
    }

    public String format() {
        return format;
    }

    public Envelope3dData data() {
        return data;
    }

    public String url() {
        return url;
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
            || other instanceof Envelope3d that
                && envelope3dId.equals(that.envelope3dId)
                && format.equals(that.format)
                && Objects.equals(data, that.data)
                && Objects.equals(url, that.url)
                && Objects.equals(description, that.description)
                && extensionFields.equals(that.extensionFields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            envelope3dId,
            format,
            data,
            url,
            description,
            extensionFields
        );
    }

    public static final class Builder {
        private String envelope3dId;
        private String format;
        private Envelope3dData data;
        private String url;
        private String description;
        private ExtensionFields extensionFields;

        private Builder() {}

        public Builder envelope3dId(String envelope3dId) {
            this.envelope3dId = envelope3dId;
            return this;
        }

        public Builder format(String format) {
            this.format = format;
            return this;
        }

        public Builder data(Envelope3dData data) {
            this.data = data;
            return this;
        }

        public Builder url(String url) {
            this.url = url;
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

        public Envelope3d build() {
            return new Envelope3d(this);
        }
    }
}
