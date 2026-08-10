package io.github.cooltheworld.rcs.protocol.vda5050.v3.validation;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.codec.JsonCodecLimits;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.MaximumArrayLengths;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.MaximumStringLengths;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.ProtocolLimits;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.ProtocolTiming;
import java.util.Objects;

/**
 * Factsheet 能力声明与不可突破的部署级 JSON 硬上限的不可变交集。
 *
 * <p>长度和数量声明缺失或为零时使用部署上限，非零时与对应部署上限
 * 取最小值。Timing 值不是 JSON 资源上限，仅将零归一为未声明。</p>
 */
public final class EffectiveProtocolLimits {
    private static final long UINT32_MAX = 4_294_967_295L;

    private final ProtocolLimits declared;
    private final JsonCodecLimits deployment;

    private EffectiveProtocolLimits(
        ProtocolLimits declared,
        JsonCodecLimits deployment
    ) {
        this.declared = Objects.requireNonNull(declared, "declared");
        this.deployment = Objects.requireNonNull(deployment, "deployment");
        validate(declared);
    }

    /**
     * 计算不可突破部署硬上限的有效协议限制。
     *
     * @param declared Factsheet 声明的原始 Protocol Limits
     * @param deployment 启动时固定的部署级 JSON 资源上限
     * @return 已取交集并归一零值的有效限制
     * @throws IllegalArgumentException 声明包含非法数值时
     */
    public static EffectiveProtocolLimits resolve(
        ProtocolLimits declared,
        JsonCodecLimits deployment
    ) {
        return new EffectiveProtocolLimits(declared, deployment);
    }

    public Long maximumMessageLength() {
        return cap(
            declared.maximumStringLengths().maximumMessageLength(),
            deployment.maxPayloadBytes()
        );
    }

    public Long maximumTopicSerialLength() {
        return capString(
            declared.maximumStringLengths().maximumTopicSerialLength()
        );
    }

    public Long maximumTopicElementLength() {
        return capString(
            declared.maximumStringLengths().maximumTopicElementLength()
        );
    }

    public Long maximumIdLength() {
        return capString(declared.maximumStringLengths().maximumIdLength());
    }

    public boolean idNumericalOnly() {
        return Boolean.TRUE.equals(
            declared.maximumStringLengths().idNumericalOnly()
        );
    }

    public Long maximumLoadIdLength() {
        return capString(declared.maximumStringLengths().maximumLoadIdLength());
    }

    public Long orderNodes() {
        return capArray(declared.maximumArrayLengths().orderNodes());
    }

    public Long orderEdges() {
        return capArray(declared.maximumArrayLengths().orderEdges());
    }

    public Long nodeActions() {
        return capArray(declared.maximumArrayLengths().nodeActions());
    }

    public Long edgeActions() {
        return capArray(declared.maximumArrayLengths().edgeActions());
    }

    public Long actionParameters() {
        return capArray(declared.maximumArrayLengths().actionParameters());
    }

    public Long instantActions() {
        return capArray(declared.maximumArrayLengths().instantActions());
    }

    public Long trajectoryKnotVector() {
        return capArray(declared.maximumArrayLengths().trajectoryKnotVector());
    }

    public Long trajectoryControlPoints() {
        return capArray(
            declared.maximumArrayLengths().trajectoryControlPoints()
        );
    }

    public Long zoneSetZones() {
        return capArray(declared.maximumArrayLengths().zoneSetZones());
    }

    public Long stateNodeStates() {
        return capArray(declared.maximumArrayLengths().stateNodeStates());
    }

    public Long stateEdgeStates() {
        return capArray(declared.maximumArrayLengths().stateEdgeStates());
    }

    public Long stateLoads() {
        return capArray(declared.maximumArrayLengths().stateLoads());
    }

    public Long stateActionStates() {
        return capArray(declared.maximumArrayLengths().stateActionStates());
    }

    public Long stateInstantActionStates() {
        return capArray(
            declared.maximumArrayLengths().stateInstantActionStates()
        );
    }

    public Long stateZoneActionStates() {
        return capArray(declared.maximumArrayLengths().stateZoneActionStates());
    }

    public Long stateErrors() {
        return capArray(declared.maximumArrayLengths().stateErrors());
    }

    public Long stateInformation() {
        return capArray(declared.maximumArrayLengths().stateInformation());
    }

    public Long errorErrorReferences() {
        return capArray(declared.maximumArrayLengths().errorErrorReferences());
    }

    public Long informationInfoReferences() {
        return capArray(
            declared.maximumArrayLengths().informationInfoReferences()
        );
    }

    /** @return 非零最小 order 发送间隔，未声明时为 {@code null} */
    public Double minimumOrderInterval() {
        return normalizeTiming(declared.timing().minimumOrderInterval());
    }

    /** @return 非零最小 state 发送间隔，未声明时为 {@code null} */
    public Double minimumStateInterval() {
        return normalizeTiming(declared.timing().minimumStateInterval());
    }

    /** @return 非零默认 state 发送间隔，未声明时为 {@code null} */
    public Double defaultStateInterval() {
        return normalizeTiming(declared.timing().defaultStateInterval());
    }

    /** @return 非零 visualization 发送间隔，未声明时为 {@code null} */
    public Double visualizationInterval() {
        return normalizeTiming(declared.timing().visualizationInterval());
    }

    private Long capString(Long declaredLimit) {
        return cap(declaredLimit, deployment.maxStringCharacters());
    }

    private Long capArray(Long declaredLimit) {
        return cap(declaredLimit, deployment.maxArrayElements());
    }

    private static Long cap(Long declaredLimit, long deploymentLimit) {
        return declaredLimit == null || declaredLimit == 0L
            ? deploymentLimit
            : Math.min(declaredLimit, deploymentLimit);
    }

    private static Double normalizeTiming(Double value) {
        return value == null || value == 0.0D ? null : value;
    }

    private static void validate(ProtocolLimits limits) {
        validate(limits.maximumStringLengths());
        validate(limits.maximumArrayLengths());
        validate(limits.timing());
    }

    private static void validate(MaximumStringLengths limits) {
        validateUint32(limits.maximumMessageLength(), "maximumMessageLength");
        validateUint32(
            limits.maximumTopicSerialLength(),
            "maximumTopicSerialLength"
        );
        validateUint32(
            limits.maximumTopicElementLength(),
            "maximumTopicElementLength"
        );
        validateUint32(limits.maximumIdLength(), "maximumIdLength");
        validateUint32(limits.maximumLoadIdLength(), "maximumLoadIdLength");
    }

    private static void validate(MaximumArrayLengths limits) {
        validateUint32(limits.orderNodes(), "order.nodes");
        validateUint32(limits.orderEdges(), "order.edges");
        validateUint32(limits.nodeActions(), "node.actions");
        validateUint32(limits.edgeActions(), "edge.actions");
        validateUint32(
            limits.actionParameters(),
            "actions.actionsParameters"
        );
        validateUint32(limits.instantActions(), "instantActions");
        validateUint32(limits.trajectoryKnotVector(), "trajectory.knotVector");
        validateUint32(
            limits.trajectoryControlPoints(),
            "trajectory.controlPoints"
        );
        validateUint32(limits.zoneSetZones(), "zoneSet.zones");
        validateUint32(limits.stateNodeStates(), "state.nodeStates");
        validateUint32(limits.stateEdgeStates(), "state.edgeStates");
        validateUint32(limits.stateLoads(), "state.loads");
        validateUint32(limits.stateActionStates(), "state.actionStates");
        validateUint32(
            limits.stateInstantActionStates(),
            "state.instantActionStates"
        );
        validateUint32(
            limits.stateZoneActionStates(),
            "state.zoneActionStates"
        );
        validateUint32(limits.stateErrors(), "state.errors");
        validateUint32(limits.stateInformation(), "state.information");
        validateUint32(
            limits.errorErrorReferences(),
            "error.errorReferences"
        );
        validateUint32(
            limits.informationInfoReferences(),
            "information.infoReferences"
        );
    }

    private static void validate(ProtocolTiming timing) {
        validateTiming(
            timing.minimumOrderInterval(),
            "minimumOrderInterval"
        );
        validateTiming(
            timing.minimumStateInterval(),
            "minimumStateInterval"
        );
        validateTiming(
            timing.defaultStateInterval(),
            "defaultStateInterval"
        );
        validateTiming(
            timing.visualizationInterval(),
            "visualizationInterval"
        );
    }

    private static void validateUint32(Long value, String name) {
        if (value != null && (value < 0L || value > UINT32_MAX)) {
            throw new IllegalArgumentException(name + " must be a uint32");
        }
    }

    private static void validateTiming(Double value, String name) {
        if (value != null && (!Double.isFinite(value) || value < 0.0D)) {
            throw new IllegalArgumentException(
                name + " must be a finite non-negative number"
            );
        }
    }
}
