package io.github.cooltheworld.rcs.protocol.vda5050.v3.model.action;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.ExtensionFields;
import java.util.Objects;

/**
 * 一个动作参数的线路表示。
 *
 * <p>参数键保持原文，值使用 {@link ActionParameterValue} 表达；参数是否受支持及其业务约束由后续准入阶段判断。</p>
 */
public final class ActionParameter {
    private final String key;
    private final ActionParameterValue value;
    private final ExtensionFields extensionFields;

    private ActionParameter(Builder builder) {
        this.key = Objects.requireNonNull(builder.key, "key");
        this.value = Objects.requireNonNull(builder.value, "value");
        this.extensionFields = builder.extensionFields == null
                ? ExtensionFields.empty()
                : builder.extensionFields;
    }

    /**
     * 创建动作参数 Builder。
     *
     * @return 新 Builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /** @return 保持原文的参数键 */
    public String key() {
        return key;
    }

    /** @return 强类型线路值 */
    public ActionParameterValue value() {
        return value;
    }

    /** @return 未知扩展字段 */
    public ExtensionFields extensionFields() {
        return extensionFields;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ActionParameter that)) {
            return false;
        }
        return key.equals(that.key)
                && value.equals(that.value)
                && extensionFields.equals(that.extensionFields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value, extensionFields);
    }

    /** 动作参数 Builder。 */
    public static final class Builder {
        private String key;
        private ActionParameterValue value;
        private ExtensionFields extensionFields;

        private Builder() {
        }

        /** @param key 保持原文的参数键 @return 当前 Builder */
        public Builder key(String key) {
            this.key = key;
            return this;
        }

        /** @param value 强类型线路值 @return 当前 Builder */
        public Builder value(ActionParameterValue value) {
            this.value = value;
            return this;
        }

        /** @param extensionFields 未知扩展字段 @return 当前 Builder */
        public Builder extensionFields(ExtensionFields extensionFields) {
            this.extensionFields = extensionFields;
            return this;
        }

        /** @return 不可变动作参数 */
        public ActionParameter build() {
            return new ActionParameter(this);
        }
    }
}
