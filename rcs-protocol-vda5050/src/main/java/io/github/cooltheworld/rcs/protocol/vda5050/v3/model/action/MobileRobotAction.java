package io.github.cooltheworld.rcs.protocol.vda5050.v3.model.action;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.ExtensionFields;
import java.util.List;
import java.util.Objects;

/** Factsheet 中移动机器人支持的单个 Action 不可变声明。 */
public final class MobileRobotAction {
    private final String actionType;
    private final String actionDescription;
    private final List<ActionScope> actionScopes;
    private final List<ActionParameterDefinition> actionParameters;
    private final String actionResult;
    private final List<BlockingType> blockingTypes;
    private final Boolean pauseAllowed;
    private final Boolean cancelAllowed;
    private final ExtensionFields extensionFields;

    private MobileRobotAction(Builder builder) {
        this.actionType = Objects.requireNonNull(builder.actionType, "actionType");
        this.actionDescription = builder.actionDescription;
        this.actionScopes = List.copyOf(Objects.requireNonNull(
            builder.actionScopes,
            "actionScopes"
        ));
        this.actionParameters = copyOptional(builder.actionParameters);
        this.actionResult = builder.actionResult;
        this.blockingTypes = copyOptional(builder.blockingTypes);
        this.pauseAllowed = Objects.requireNonNull(
            builder.pauseAllowed,
            "pauseAllowed"
        );
        this.cancelAllowed = Objects.requireNonNull(
            builder.cancelAllowed,
            "cancelAllowed"
        );
        this.extensionFields = builder.extensionFields == null
            ? ExtensionFields.empty()
            : builder.extensionFields;
    }

    /** @return 空的受支持 Action Builder */
    public static Builder builder() {
        return new Builder();
    }

    /** @return 与线路 Action 对应的唯一类型 */
    public String actionType() {
        return actionType;
    }

    /** @return 可选的人类可读说明，缺失时为 {@code null} */
    public String actionDescription() {
        return actionDescription;
    }

    /** @return 不可变的允许作用域列表 */
    public List<ActionScope> actionScopes() {
        return actionScopes;
    }

    /** @return 可选的不可变参数定义列表，缺失时为 {@code null} */
    public List<ActionParameterDefinition> actionParameters() {
        return actionParameters;
    }

    /** @return 可选的结果说明，缺失时为 {@code null} */
    public String actionResult() {
        return actionResult;
    }

    /** @return 可选的不可变 Blocking Type 列表，缺失时为 {@code null} */
    public List<BlockingType> blockingTypes() {
        return blockingTypes;
    }

    /** @return 是否允许暂停该 Action */
    public Boolean pauseAllowed() {
        return pauseAllowed;
    }

    /** @return 是否允许取消该 Action */
    public Boolean cancelAllowed() {
        return cancelAllowed;
    }

    /** @return 不透明保存的未知扩展字段 */
    public ExtensionFields extensionFields() {
        return extensionFields;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
            || other instanceof MobileRobotAction that
                && actionType.equals(that.actionType)
                && Objects.equals(actionDescription, that.actionDescription)
                && actionScopes.equals(that.actionScopes)
                && Objects.equals(actionParameters, that.actionParameters)
                && Objects.equals(actionResult, that.actionResult)
                && Objects.equals(blockingTypes, that.blockingTypes)
                && pauseAllowed.equals(that.pauseAllowed)
                && cancelAllowed.equals(that.cancelAllowed)
                && extensionFields.equals(that.extensionFields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            actionType,
            actionDescription,
            actionScopes,
            actionParameters,
            actionResult,
            blockingTypes,
            pauseAllowed,
            cancelAllowed,
            extensionFields
        );
    }

    private static <T> List<T> copyOptional(List<T> values) {
        return values == null ? null : List.copyOf(values);
    }

    /** 用于构造必填字段完整的 {@link MobileRobotAction}。 */
    public static final class Builder {
        private String actionType;
        private String actionDescription;
        private List<ActionScope> actionScopes;
        private List<ActionParameterDefinition> actionParameters;
        private String actionResult;
        private List<BlockingType> blockingTypes;
        private Boolean pauseAllowed;
        private Boolean cancelAllowed;
        private ExtensionFields extensionFields;

        private Builder() {}

        public Builder actionType(String actionType) {
            this.actionType = actionType;
            return this;
        }

        public Builder actionDescription(String actionDescription) {
            this.actionDescription = actionDescription;
            return this;
        }

        public Builder actionScopes(List<ActionScope> actionScopes) {
            this.actionScopes = actionScopes;
            return this;
        }

        public Builder actionParameters(
            List<ActionParameterDefinition> actionParameters
        ) {
            this.actionParameters = actionParameters;
            return this;
        }

        public Builder actionResult(String actionResult) {
            this.actionResult = actionResult;
            return this;
        }

        public Builder blockingTypes(List<BlockingType> blockingTypes) {
            this.blockingTypes = blockingTypes;
            return this;
        }

        public Builder pauseAllowed(Boolean pauseAllowed) {
            this.pauseAllowed = pauseAllowed;
            return this;
        }

        public Builder cancelAllowed(Boolean cancelAllowed) {
            this.cancelAllowed = cancelAllowed;
            return this;
        }

        public Builder extensionFields(ExtensionFields extensionFields) {
            this.extensionFields = extensionFields;
            return this;
        }

        /** @return 必填字段完整的受支持 Action 声明 */
        public MobileRobotAction build() {
            return new MobileRobotAction(this);
        }
    }
}
