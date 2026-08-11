package io.github.cooltheworld.rcs.protocol.vda5050.v3.model.order;

/** Corridor 授权过期或被撤销后的行为。 */
public enum CorridorReleaseLossBehavior {
    /** 停车并等待人工干预。 */
    STOP,

    /** 按原路返回 Edge 的预定义轨迹。 */
    RETURN
}
