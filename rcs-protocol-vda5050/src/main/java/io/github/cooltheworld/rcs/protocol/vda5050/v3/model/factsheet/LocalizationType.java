package io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet;

import java.util.Objects;

/** Factsheet 中可扩展的定位类型。 */
public record LocalizationType(String value) {
    public static final LocalizationType NATURAL = of("NATURAL");
    public static final LocalizationType REFLECTOR = of("REFLECTOR");
    public static final LocalizationType RFID = of("RFID");
    public static final LocalizationType DMC = of("DMC");
    public static final LocalizationType SPOT = of("SPOT");
    public static final LocalizationType GRID = of("GRID");

    public LocalizationType {
        Objects.requireNonNull(value, "value");
    }

    /**
     * @param value 标准值或厂商扩展值
     * @return 精确保留文本的定位类型
     */
    public static LocalizationType of(String value) {
        return new LocalizationType(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
