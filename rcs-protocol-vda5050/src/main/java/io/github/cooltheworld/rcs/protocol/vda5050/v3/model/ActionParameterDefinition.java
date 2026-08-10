package io.github.cooltheworld.rcs.protocol.vda5050.v3.model;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.ExtensionFields;
import java.util.Objects;

/** Factsheet 中单个受支持 Action 参数的不可变定义。 */
public final class ActionParameterDefinition {
    private final String key;
    private final ActionValueDataType valueDataType;
    private final String description;
    private final Boolean isOptional;
    private final ExtensionFields extensionFields;

    private ActionParameterDefinition(Builder builder) {
        this.key = Objects.requireNonNull(builder.key, "key");
        this.valueDataType = Objects.requireNonNull(
            builder.valueDataType,
            "valueDataType"
        );
        this.description = builder.description;
        this.isOptional = builder.isOptional;
        this.extensionFields = builder.extensionFields == null
            ? ExtensionFields.empty()
            : builder.extensionFields;
    }

    /** @return 空的 Action 参数定义 Builder */
    public static Builder builder() {
        return new Builder();
    }

    /** @return Action 参数键 */
    public String key() {
        return key;
    }

    /** @return Action 参数值的 JSON 数据类型 */
    public ActionValueDataType valueDataType() {
        return valueDataType;
    }

    /** @return 可选说明，缺失时为 {@code null} */
    public String description() {
        return description;
    }

    /** @return 是否为可选参数；声明缺失时为 {@code null} */
    public Boolean isOptional() {
        return isOptional;
    }

    /** @return 不透明保存的未知扩展字段 */
    public ExtensionFields extensionFields() {
        return extensionFields;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
            || other instanceof ActionParameterDefinition that
                && key.equals(that.key)
                && valueDataType == that.valueDataType
                && Objects.equals(description, that.description)
                && Objects.equals(isOptional, that.isOptional)
                && extensionFields.equals(that.extensionFields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            key,
            valueDataType,
            description,
            isOptional,
            extensionFields
        );
    }

    /** 用于构造必填字段完整的 {@link ActionParameterDefinition}。 */
    public static final class Builder {
        private String key;
        private ActionValueDataType valueDataType;
        private String description;
        private Boolean isOptional;
        private ExtensionFields extensionFields;

        private Builder() {}

        public Builder key(String key) {
            this.key = key;
            return this;
        }

        public Builder valueDataType(ActionValueDataType valueDataType) {
            this.valueDataType = valueDataType;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder isOptional(Boolean isOptional) {
            this.isOptional = isOptional;
            return this;
        }

        public Builder extensionFields(ExtensionFields extensionFields) {
            this.extensionFields = extensionFields;
            return this;
        }

        /** @return 必填字段完整的 Action 参数定义 */
        public ActionParameterDefinition build() {
            return new ActionParameterDefinition(this);
        }
    }
}
