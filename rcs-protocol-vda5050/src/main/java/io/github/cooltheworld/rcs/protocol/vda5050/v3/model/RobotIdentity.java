package io.github.cooltheworld.rcs.protocol.vda5050.v3.model;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 由 manufacturer 与 serialNumber 原文共同组成的移动机器人协议身份。
 *
 * <p>该值对象逐字符区分身份，不执行空白裁剪、大小写转换或 Unicode 规范化。</p>
 *
 * @param manufacturer 移动机器人制造商
 * @param serialNumber 移动机器人序列号
 */
public record RobotIdentity(String manufacturer, String serialNumber) {
    private static final Pattern SERIAL_NUMBER_PATTERN = Pattern.compile(
        "[A-Za-z0-9_.:-]+"
    );

    public RobotIdentity {
        Objects.requireNonNull(manufacturer, "manufacturer");
        Objects.requireNonNull(serialNumber, "serialNumber");
        if (manufacturer.isEmpty() || containsUnsafeTopicCharacter(manufacturer)) {
            throw new IllegalArgumentException(
                "Manufacturer must be non-empty and safe for a topic level"
            );
        }
        if (!SERIAL_NUMBER_PATTERN.matcher(serialNumber).matches()) {
            throw new IllegalArgumentException(
                "Serial number contains characters outside the VDA 5050 topic set"
            );
        }
    }

    private static boolean containsUnsafeTopicCharacter(String value) {
        return value.indexOf('/') >= 0
            || value.indexOf('+') >= 0
            || value.indexOf('#') >= 0
            || value.indexOf('$') >= 0
            || value.codePoints().anyMatch(Character::isISOControl);
    }
}
