package io.github.cooltheworld.rcs.protocol.vda5050.v3.model;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.ExtensionFields;
import java.util.Objects;

/** Factsheet Protocol Limits 中可选的不可变数组长度声明。 */
public final class MaximumArrayLengths {
    private final Long orderNodes;
    private final Long orderEdges;
    private final Long nodeActions;
    private final Long edgeActions;
    private final Long actionParameters;
    private final Long instantActions;
    private final Long trajectoryKnotVector;
    private final Long trajectoryControlPoints;
    private final Long zoneSetZones;
    private final Long stateNodeStates;
    private final Long stateEdgeStates;
    private final Long stateLoads;
    private final Long stateActionStates;
    private final Long stateInstantActionStates;
    private final Long stateZoneActionStates;
    private final Long stateErrors;
    private final Long stateInformation;
    private final Long errorErrorReferences;
    private final Long informationInfoReferences;
    private final ExtensionFields extensionFields;

    private MaximumArrayLengths(Builder builder) {
        this.orderNodes = builder.orderNodes;
        this.orderEdges = builder.orderEdges;
        this.nodeActions = builder.nodeActions;
        this.edgeActions = builder.edgeActions;
        this.actionParameters = builder.actionParameters;
        this.instantActions = builder.instantActions;
        this.trajectoryKnotVector = builder.trajectoryKnotVector;
        this.trajectoryControlPoints = builder.trajectoryControlPoints;
        this.zoneSetZones = builder.zoneSetZones;
        this.stateNodeStates = builder.stateNodeStates;
        this.stateEdgeStates = builder.stateEdgeStates;
        this.stateLoads = builder.stateLoads;
        this.stateActionStates = builder.stateActionStates;
        this.stateInstantActionStates = builder.stateInstantActionStates;
        this.stateZoneActionStates = builder.stateZoneActionStates;
        this.stateErrors = builder.stateErrors;
        this.stateInformation = builder.stateInformation;
        this.errorErrorReferences = builder.errorErrorReferences;
        this.informationInfoReferences = builder.informationInfoReferences;
        this.extensionFields = builder.extensionFields == null
            ? ExtensionFields.empty()
            : builder.extensionFields;
    }

    /** @return 空的数组长度 Builder */
    public static Builder builder() {
        return new Builder();
    }

    public Long orderNodes() {
        return orderNodes;
    }

    public Long orderEdges() {
        return orderEdges;
    }

    public Long nodeActions() {
        return nodeActions;
    }

    public Long edgeActions() {
        return edgeActions;
    }

    /** @return 每个 Action 的 Action Parameter 数量上限 */
    public Long actionParameters() {
        return actionParameters;
    }

    public Long instantActions() {
        return instantActions;
    }

    public Long trajectoryKnotVector() {
        return trajectoryKnotVector;
    }

    public Long trajectoryControlPoints() {
        return trajectoryControlPoints;
    }

    public Long zoneSetZones() {
        return zoneSetZones;
    }

    public Long stateNodeStates() {
        return stateNodeStates;
    }

    public Long stateEdgeStates() {
        return stateEdgeStates;
    }

    public Long stateLoads() {
        return stateLoads;
    }

    public Long stateActionStates() {
        return stateActionStates;
    }

    public Long stateInstantActionStates() {
        return stateInstantActionStates;
    }

    public Long stateZoneActionStates() {
        return stateZoneActionStates;
    }

    public Long stateErrors() {
        return stateErrors;
    }

    public Long stateInformation() {
        return stateInformation;
    }

    public Long errorErrorReferences() {
        return errorErrorReferences;
    }

    public Long informationInfoReferences() {
        return informationInfoReferences;
    }

    /** @return 不透明保存的未知扩展字段 */
    public ExtensionFields extensionFields() {
        return extensionFields;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
            || other instanceof MaximumArrayLengths that
                && Objects.equals(orderNodes, that.orderNodes)
                && Objects.equals(orderEdges, that.orderEdges)
                && Objects.equals(nodeActions, that.nodeActions)
                && Objects.equals(edgeActions, that.edgeActions)
                && Objects.equals(actionParameters, that.actionParameters)
                && Objects.equals(instantActions, that.instantActions)
                && Objects.equals(
                    trajectoryKnotVector,
                    that.trajectoryKnotVector
                )
                && Objects.equals(
                    trajectoryControlPoints,
                    that.trajectoryControlPoints
                )
                && Objects.equals(zoneSetZones, that.zoneSetZones)
                && Objects.equals(stateNodeStates, that.stateNodeStates)
                && Objects.equals(stateEdgeStates, that.stateEdgeStates)
                && Objects.equals(stateLoads, that.stateLoads)
                && Objects.equals(stateActionStates, that.stateActionStates)
                && Objects.equals(
                    stateInstantActionStates,
                    that.stateInstantActionStates
                )
                && Objects.equals(
                    stateZoneActionStates,
                    that.stateZoneActionStates
                )
                && Objects.equals(stateErrors, that.stateErrors)
                && Objects.equals(stateInformation, that.stateInformation)
                && Objects.equals(
                    errorErrorReferences,
                    that.errorErrorReferences
                )
                && Objects.equals(
                    informationInfoReferences,
                    that.informationInfoReferences
                )
                && extensionFields.equals(that.extensionFields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            orderNodes,
            orderEdges,
            nodeActions,
            edgeActions,
            actionParameters,
            instantActions,
            trajectoryKnotVector,
            trajectoryControlPoints,
            zoneSetZones,
            stateNodeStates,
            stateEdgeStates,
            stateLoads,
            stateActionStates,
            stateInstantActionStates,
            stateZoneActionStates,
            stateErrors,
            stateInformation,
            errorErrorReferences,
            informationInfoReferences,
            extensionFields
        );
    }

    /** 用于构造数组长度声明。 */
    public static final class Builder {
        private Long orderNodes;
        private Long orderEdges;
        private Long nodeActions;
        private Long edgeActions;
        private Long actionParameters;
        private Long instantActions;
        private Long trajectoryKnotVector;
        private Long trajectoryControlPoints;
        private Long zoneSetZones;
        private Long stateNodeStates;
        private Long stateEdgeStates;
        private Long stateLoads;
        private Long stateActionStates;
        private Long stateInstantActionStates;
        private Long stateZoneActionStates;
        private Long stateErrors;
        private Long stateInformation;
        private Long errorErrorReferences;
        private Long informationInfoReferences;
        private ExtensionFields extensionFields;

        private Builder() {}

        public Builder orderNodes(Long value) {
            this.orderNodes = value;
            return this;
        }

        public Builder orderEdges(Long value) {
            this.orderEdges = value;
            return this;
        }

        public Builder nodeActions(Long value) {
            this.nodeActions = value;
            return this;
        }

        public Builder edgeActions(Long value) {
            this.edgeActions = value;
            return this;
        }

        public Builder actionParameters(Long value) {
            this.actionParameters = value;
            return this;
        }

        public Builder instantActions(Long value) {
            this.instantActions = value;
            return this;
        }

        public Builder trajectoryKnotVector(Long value) {
            this.trajectoryKnotVector = value;
            return this;
        }

        public Builder trajectoryControlPoints(Long value) {
            this.trajectoryControlPoints = value;
            return this;
        }

        public Builder zoneSetZones(Long value) {
            this.zoneSetZones = value;
            return this;
        }

        public Builder stateNodeStates(Long value) {
            this.stateNodeStates = value;
            return this;
        }

        public Builder stateEdgeStates(Long value) {
            this.stateEdgeStates = value;
            return this;
        }

        public Builder stateLoads(Long value) {
            this.stateLoads = value;
            return this;
        }

        public Builder stateActionStates(Long value) {
            this.stateActionStates = value;
            return this;
        }

        public Builder stateInstantActionStates(Long value) {
            this.stateInstantActionStates = value;
            return this;
        }

        public Builder stateZoneActionStates(Long value) {
            this.stateZoneActionStates = value;
            return this;
        }

        public Builder stateErrors(Long value) {
            this.stateErrors = value;
            return this;
        }

        public Builder stateInformation(Long value) {
            this.stateInformation = value;
            return this;
        }

        public Builder errorErrorReferences(Long value) {
            this.errorErrorReferences = value;
            return this;
        }

        public Builder informationInfoReferences(Long value) {
            this.informationInfoReferences = value;
            return this;
        }

        public Builder extensionFields(ExtensionFields value) {
            this.extensionFields = value;
            return this;
        }

        /** @return 不可变数组长度声明 */
        public MaximumArrayLengths build() {
            return new MaximumArrayLengths(this);
        }
    }
}
