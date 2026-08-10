package io.github.cooltheworld.rcs.protocol.vda5050.v3.model;

import java.util.Objects;

/** Factsheet 中可扩展的导航类型。 */
public record NavigationType(String value) {
    public static final NavigationType PHYSICAL_LINE_GUIDED = of(
        "PHYSICAL_LINE_GUIDED"
    );
    public static final NavigationType VIRTUAL_LINE_GUIDED = of(
        "VIRTUAL_LINE_GUIDED"
    );
    public static final NavigationType FREELY_NAVIGATING = of(
        "FREELY_NAVIGATING"
    );

    public NavigationType {
        Objects.requireNonNull(value, "value");
    }

    /**
     * @param value 标准值或厂商扩展值
     * @return 精确保留文本的导航类型
     */
    public static NavigationType of(String value) {
        return new NavigationType(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
