package io.github.cooltheworld.rcs.protocol.vda5050.v3.model.order;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.ExtensionFields;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.action.Action;
import java.util.List;
import java.util.Objects;

/** VDA 5050 Order 中不可变的 Node（节点）聚合。 */
public final class Node {
    private final String nodeId;
    private final Long sequenceId;
    private final String nodeDescriptor;
    private final Boolean released;
    private final NodePosition nodePosition;
    private final List<Action> actions;
    private final ExtensionFields extensionFields;

    private Node(Builder builder) {
        this.nodeId = Objects.requireNonNull(builder.nodeId, "nodeId");
        this.sequenceId = Objects.requireNonNull(
            builder.sequenceId,
            "sequenceId"
        );
        this.nodeDescriptor = builder.nodeDescriptor;
        this.released = Objects.requireNonNull(builder.released, "released");
        this.nodePosition = builder.nodePosition;
        this.actions = List.copyOf(Objects.requireNonNull(
            builder.actions,
            "actions"
        ));
        this.extensionFields = builder.extensionFields == null
            ? ExtensionFields.empty()
            : builder.extensionFields;
    }

    /** @return 新的 Node Builder */
    public static Builder builder() {
        return new Builder();
    }

    /** @return 保持原文的节点标识 */
    public String nodeId() {
        return nodeId;
    }

    /** @return 节点在 Order 图中的序列号 */
    public Long sequenceId() {
        return sequenceId;
    }

    /** @return 可选的人类可读节点说明 */
    public String nodeDescriptor() {
        return nodeDescriptor;
    }

    /** @return 节点是否已经进入 Base */
    public Boolean released() {
        return released;
    }

    /** @return 可选的节点位置 */
    public NodePosition nodePosition() {
        return nodePosition;
    }

    /** @return 不可变的节点 Action 列表，允许为空 */
    public List<Action> actions() {
        return actions;
    }

    /** @return 不透明保存的未知扩展字段 */
    public ExtensionFields extensionFields() {
        return extensionFields;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
            || other instanceof Node that
                && nodeId.equals(that.nodeId)
                && sequenceId.equals(that.sequenceId)
                && Objects.equals(nodeDescriptor, that.nodeDescriptor)
                && released.equals(that.released)
                && Objects.equals(nodePosition, that.nodePosition)
                && actions.equals(that.actions)
                && extensionFields.equals(that.extensionFields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            nodeId,
            sequenceId,
            nodeDescriptor,
            released,
            nodePosition,
            actions,
            extensionFields
        );
    }

    /** Node Builder。 */
    public static final class Builder {
        private String nodeId;
        private Long sequenceId;
        private String nodeDescriptor;
        private Boolean released;
        private NodePosition nodePosition;
        private List<Action> actions;
        private ExtensionFields extensionFields;

        private Builder() {}

        /** @param nodeId 保持原文的节点标识 @return 当前 Builder */
        public Builder nodeId(String nodeId) {
            this.nodeId = nodeId;
            return this;
        }

        /** @param sequenceId 节点序列号 @return 当前 Builder */
        public Builder sequenceId(Long sequenceId) {
            this.sequenceId = sequenceId;
            return this;
        }

        /** @param nodeDescriptor 可选的人类可读节点说明 @return 当前 Builder */
        public Builder nodeDescriptor(String nodeDescriptor) {
            this.nodeDescriptor = nodeDescriptor;
            return this;
        }

        /** @param released 节点是否已经进入 Base @return 当前 Builder */
        public Builder released(Boolean released) {
            this.released = released;
            return this;
        }

        /** @param nodePosition 可选的节点位置 @return 当前 Builder */
        public Builder nodePosition(NodePosition nodePosition) {
            this.nodePosition = nodePosition;
            return this;
        }

        /** @param actions 必填且允许为空的节点 Action 列表 @return 当前 Builder */
        public Builder actions(List<Action> actions) {
            this.actions = actions;
            return this;
        }

        /** @param extensionFields 未知扩展字段 @return 当前 Builder */
        public Builder extensionFields(ExtensionFields extensionFields) {
            this.extensionFields = extensionFields;
            return this;
        }

        /** @return 必填字段完整的不可变 Node */
        public Node build() {
            return new Node(this);
        }
    }
}
