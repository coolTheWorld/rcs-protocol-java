package io.github.cooltheworld.rcs.protocol.vda5050.v3.model.common;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * VDA 5050 协议头中的语义版本标识。
 *
 * <p>该值对象只表达格式正确的版本，不代表该版本已经被当前库支持。</p>
 *
 * @param value {@code Major.Minor.Patch} 格式的版本文本
 */
public record ProtocolVersion(String value) {
    private static final Pattern VERSION_PATTERN = Pattern.compile(
        "(0|[1-9][0-9]*)\\.(0|[1-9][0-9]*)\\.(0|[1-9][0-9]*)"
    );

    public ProtocolVersion {
        Objects.requireNonNull(value, "value");
        if (!VERSION_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException(
                "Protocol version must use Major.Minor.Patch format"
            );
        }
    }

    /**
     * 解析一个协议版本值。
     *
     * @param value 协议版本文本
     * @return 格式正确的协议版本
     */
    public static ProtocolVersion parse(String value) {
        return new ProtocolVersion(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
