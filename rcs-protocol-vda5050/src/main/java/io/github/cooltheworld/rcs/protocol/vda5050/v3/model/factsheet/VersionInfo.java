package io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.ExtensionFields;
import java.util.Objects;

/** Factsheet 中软件或硬件版本的不可变键值声明。 */
public final class VersionInfo {
    private final String key;
    private final String value;
    private final ExtensionFields extensionFields;

    private VersionInfo(Builder builder) {
        this.key = Objects.requireNonNull(builder.key, "key");
        this.value = Objects.requireNonNull(builder.value, "value");
        this.extensionFields = builder.extensionFields == null
            ? ExtensionFields.empty()
            : builder.extensionFields;
    }

    /** @return 空的版本信息 Builder */
    public static Builder builder() {
        return new Builder();
    }

    /** @return 软件或硬件版本的原始键 */
    public String key() {
        return key;
    }

    /** @return 与键对应的原始版本值 */
    public String value() {
        return value;
    }

    /** @return 不透明保存的未知扩展字段 */
    public ExtensionFields extensionFields() {
        return extensionFields;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
            || other instanceof VersionInfo that
                && key.equals(that.key)
                && value.equals(that.value)
                && extensionFields.equals(that.extensionFields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value, extensionFields);
    }

    /** 用于构造必填键和值完整的版本信息。 */
    public static final class Builder {
        private String key;
        private String value;
        private ExtensionFields extensionFields;

        private Builder() {}

        public Builder key(String key) {
            this.key = key;
            return this;
        }

        public Builder value(String value) {
            this.value = value;
            return this;
        }

        public Builder extensionFields(ExtensionFields extensionFields) {
            this.extensionFields = extensionFields;
            return this;
        }

        /** @return 必填字段完整的不可变版本信息 */
        public VersionInfo build() {
            return new VersionInfo(this);
        }
    }
}
