package io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet;

import java.util.Objects;

/** Factsheet 中可扩展的移动机器人运动学类型。 */
public record MobileRobotKinematics(String value) {
    public static final MobileRobotKinematics DIFFERENTIAL = of("DIFFERENTIAL");
    public static final MobileRobotKinematics OMNIDIRECTIONAL = of(
        "OMNIDIRECTIONAL"
    );
    public static final MobileRobotKinematics THREE_WHEEL = of("THREE_WHEEL");

    public MobileRobotKinematics {
        Objects.requireNonNull(value, "value");
    }

    /**
     * @param value 标准值或厂商扩展值
     * @return 精确保留文本的运动学类型
     */
    public static MobileRobotKinematics of(String value) {
        return new MobileRobotKinematics(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
