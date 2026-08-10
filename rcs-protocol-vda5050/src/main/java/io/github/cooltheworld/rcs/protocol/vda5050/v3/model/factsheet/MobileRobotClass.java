package io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet;

import java.util.Objects;

/** Factsheet 中可扩展的移动机器人类别。 */
public record MobileRobotClass(String value) {
    public static final MobileRobotClass FORKLIFT = of("FORKLIFT");
    public static final MobileRobotClass CONVEYOR = of("CONVEYOR");
    public static final MobileRobotClass TUGGER = of("TUGGER");
    public static final MobileRobotClass CARRIER = of("CARRIER");

    public MobileRobotClass {
        Objects.requireNonNull(value, "value");
    }

    /**
     * @param value 标准值或厂商扩展值
     * @return 精确保留文本的机器人类别
     */
    public static MobileRobotClass of(String value) {
        return new MobileRobotClass(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
