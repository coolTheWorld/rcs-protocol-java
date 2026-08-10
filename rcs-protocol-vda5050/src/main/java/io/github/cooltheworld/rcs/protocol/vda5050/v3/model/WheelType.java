package io.github.cooltheworld.rcs.protocol.vda5050.v3.model;

import java.util.Objects;

/** Factsheet 中可扩展的车轮类型。 */
public record WheelType(String value) {
    public static final WheelType DRIVE = of("DRIVE");
    public static final WheelType CASTER = of("CASTER");
    public static final WheelType FIXED = of("FIXED");
    public static final WheelType MECANUM = of("MECANUM");

    public WheelType {
        Objects.requireNonNull(value, "value");
    }

    /** @return 精确保留文本的标准或厂商扩展车轮类型 */
    public static WheelType of(String value) {
        return new WheelType(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
