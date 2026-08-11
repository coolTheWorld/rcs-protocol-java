package io.github.cooltheworld.rcs.protocol.vda5050.v3.model.order;

/** Corridor 边界的车体参考点。 */
public enum CorridorReferencePoint {
    /** 运动学中心必须位于 Corridor 内。 */
    KINEMATIC_CENTER,

    /** 包含载荷的车体轮廓必须位于 Corridor 内。 */
    CONTOUR
}
