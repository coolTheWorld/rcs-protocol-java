package io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.ExtensionFields;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.action.MobileRobotAction;
import java.util.List;
import java.util.Objects;

/** Factsheet 中受支持 VDA 5050 协议能力的不可变声明。 */
public final class ProtocolFeatures {
    private final List<OptionalParameter> optionalParameters;
    private final List<MobileRobotAction> mobileRobotActions;
    private final ExtensionFields extensionFields;

    private ProtocolFeatures(Builder builder) {
        this.optionalParameters = List.copyOf(Objects.requireNonNull(
            builder.optionalParameters,
            "optionalParameters"
        ));
        this.mobileRobotActions = List.copyOf(Objects.requireNonNull(
            builder.mobileRobotActions,
            "mobileRobotActions"
        ));
        this.extensionFields = builder.extensionFields == null
            ? ExtensionFields.empty()
            : builder.extensionFields;
    }

    /** @return 空的协议能力 Builder */
    public static Builder builder() {
        return new Builder();
    }

    /** @return 不可变的可选协议参数支持声明 */
    public List<OptionalParameter> optionalParameters() {
        return optionalParameters;
    }

    /** @return 不可变的移动机器人受支持 Action 声明 */
    public List<MobileRobotAction> mobileRobotActions() {
        return mobileRobotActions;
    }

    /** @return 不透明保存的未知扩展字段 */
    public ExtensionFields extensionFields() {
        return extensionFields;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
            || other instanceof ProtocolFeatures that
                && optionalParameters.equals(that.optionalParameters)
                && mobileRobotActions.equals(that.mobileRobotActions)
                && extensionFields.equals(that.extensionFields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            optionalParameters,
            mobileRobotActions,
            extensionFields
        );
    }

    /** 用于构造必填字段完整的 {@link ProtocolFeatures}。 */
    public static final class Builder {
        private List<OptionalParameter> optionalParameters;
        private List<MobileRobotAction> mobileRobotActions;
        private ExtensionFields extensionFields;

        private Builder() {}

        public Builder optionalParameters(
            List<OptionalParameter> optionalParameters
        ) {
            this.optionalParameters = optionalParameters;
            return this;
        }

        public Builder mobileRobotActions(
            List<MobileRobotAction> mobileRobotActions
        ) {
            this.mobileRobotActions = mobileRobotActions;
            return this;
        }

        public Builder extensionFields(ExtensionFields extensionFields) {
            this.extensionFields = extensionFields;
            return this;
        }

        /** @return 必填字段完整的协议能力声明 */
        public ProtocolFeatures build() {
            return new ProtocolFeatures(this);
        }
    }
}
