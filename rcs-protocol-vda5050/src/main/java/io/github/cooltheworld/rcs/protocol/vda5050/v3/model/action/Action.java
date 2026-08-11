package io.github.cooltheworld.rcs.protocol.vda5050.v3.model.action;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.ExtensionFields;
import java.util.List;
import java.util.Objects;

/**
 * VDA 5050 命令线路中的不可变 Action 聚合。
 *
 * <p>该模型保留线路值和字段缺失语义；Action 目录、作用域及状态机语义由后续准入与角色流程判断。</p>
 */
public final class Action {
    private final String actionType;
    private final String actionId;
    private final String actionDescriptor;
    private final BlockingType blockingType;
    private final List<ActionParameter> actionParameters;
    private final Boolean retriable;
    private final ExtensionFields extensionFields;

    private Action(Builder builder) {
        this.actionType = Objects.requireNonNull(
            builder.actionType,
            "actionType"
        );
        this.actionId = Objects.requireNonNull(builder.actionId, "actionId");
        this.actionDescriptor = builder.actionDescriptor;
        this.blockingType = Objects.requireNonNull(
            builder.blockingType,
            "blockingType"
        );
        this.actionParameters = builder.actionParameters == null
            ? null
            : List.copyOf(builder.actionParameters);
        this.retriable = builder.retriable;
        this.extensionFields = builder.extensionFields == null
            ? ExtensionFields.empty()
            : builder.extensionFields;
    }

    /** @return 新的 Action Builder */
    public static Builder builder() {
        return new Builder();
    }

    /** @return 保持原文的 Action 类型 */
    public String actionType() {
        return actionType;
    }

    /** @return 保持原文的 Action 标识 */
    public String actionId() {
        return actionId;
    }

    /** @return 可选的人类可读说明，缺失时为 {@code null} */
    public String actionDescriptor() {
        return actionDescriptor;
    }

    /** @return 必填 Blocking Type（阻塞类型） */
    public BlockingType blockingType() {
        return blockingType;
    }

    /** @return 可选的不可变参数列表，缺失时为 {@code null} */
    public List<ActionParameter> actionParameters() {
        return actionParameters;
    }

    /** @return 可选的可重试标志，缺失时为 {@code null} */
    public Boolean retriable() {
        return retriable;
    }

    /** @return 不透明保存的未知扩展字段 */
    public ExtensionFields extensionFields() {
        return extensionFields;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
            || other instanceof Action that
                && actionType.equals(that.actionType)
                && actionId.equals(that.actionId)
                && Objects.equals(actionDescriptor, that.actionDescriptor)
                && blockingType == that.blockingType
                && Objects.equals(actionParameters, that.actionParameters)
                && Objects.equals(retriable, that.retriable)
                && extensionFields.equals(that.extensionFields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            actionType,
            actionId,
            actionDescriptor,
            blockingType,
            actionParameters,
            retriable,
            extensionFields
        );
    }

    /** Action Builder。 */
    public static final class Builder {
        private String actionType;
        private String actionId;
        private String actionDescriptor;
        private BlockingType blockingType;
        private List<ActionParameter> actionParameters;
        private Boolean retriable;
        private ExtensionFields extensionFields;

        private Builder() {}

        /** @param actionType 保持原文的 Action 类型 @return 当前 Builder */
        public Builder actionType(String actionType) {
            this.actionType = actionType;
            return this;
        }

        /** @param actionId 保持原文的 Action 标识 @return 当前 Builder */
        public Builder actionId(String actionId) {
            this.actionId = actionId;
            return this;
        }

        /** @param actionDescriptor 可选的人类可读说明 @return 当前 Builder */
        public Builder actionDescriptor(String actionDescriptor) {
            this.actionDescriptor = actionDescriptor;
            return this;
        }

        /** @param blockingType 必填 Blocking Type @return 当前 Builder */
        public Builder blockingType(BlockingType blockingType) {
            this.blockingType = blockingType;
            return this;
        }

        /** @param actionParameters 可选参数列表 @return 当前 Builder */
        public Builder actionParameters(List<ActionParameter> actionParameters) {
            this.actionParameters = actionParameters;
            return this;
        }

        /** @param retriable 可选的可重试标志 @return 当前 Builder */
        public Builder retriable(Boolean retriable) {
            this.retriable = retriable;
            return this;
        }

        /** @param extensionFields 未知扩展字段 @return 当前 Builder */
        public Builder extensionFields(ExtensionFields extensionFields) {
            this.extensionFields = extensionFields;
            return this;
        }

        /** @return 必填字段完整的不可变 Action */
        public Action build() {
            return new Action(this);
        }
    }
}
