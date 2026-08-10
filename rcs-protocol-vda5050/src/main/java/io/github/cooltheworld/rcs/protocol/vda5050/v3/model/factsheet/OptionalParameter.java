package io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.ExtensionFields;
import java.util.Objects;

/** Factsheet 中单个可选协议参数的不可变支持声明。 */
public final class OptionalParameter {
    private final String parameter;
    private final OptionalParameterSupport support;
    private final String description;
    private final ExtensionFields extensionFields;

    private OptionalParameter(Builder builder) {
        this.parameter = Objects.requireNonNull(builder.parameter, "parameter");
        this.support = Objects.requireNonNull(builder.support, "support");
        this.description = builder.description;
        this.extensionFields = builder.extensionFields == null
            ? ExtensionFields.empty()
            : builder.extensionFields;
    }

    /** @return 空的可选参数 Builder */
    public static Builder builder() {
        return new Builder();
    }

    /** @return 可选参数的完整线路字段名 */
    public String parameter() {
        return parameter;
    }

    /** @return 移动机器人对该参数的支持级别 */
    public OptionalParameterSupport support() {
        return support;
    }

    /** @return 可选说明，缺失时为 {@code null} */
    public String description() {
        return description;
    }

    /** @return 不透明保存的未知扩展字段 */
    public ExtensionFields extensionFields() {
        return extensionFields;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
            || other instanceof OptionalParameter that
                && parameter.equals(that.parameter)
                && support == that.support
                && Objects.equals(description, that.description)
                && extensionFields.equals(that.extensionFields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parameter, support, description, extensionFields);
    }

    /** 用于构造必填字段完整的 {@link OptionalParameter}。 */
    public static final class Builder {
        private String parameter;
        private OptionalParameterSupport support;
        private String description;
        private ExtensionFields extensionFields;

        private Builder() {}

        public Builder parameter(String parameter) {
            this.parameter = parameter;
            return this;
        }

        public Builder support(OptionalParameterSupport support) {
            this.support = support;
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

        /** @return 必填字段完整的可选参数声明 */
        public OptionalParameter build() {
            return new OptionalParameter(this);
        }
    }
}
