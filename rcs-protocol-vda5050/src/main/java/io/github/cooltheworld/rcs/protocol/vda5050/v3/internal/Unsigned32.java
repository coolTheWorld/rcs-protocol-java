package io.github.cooltheworld.rcs.protocol.vda5050.v3.internal;

import java.util.Objects;

/**
 * VDA 5050 {@code uint32} 范围与循环递增工具。
 *
 * <p>这是内部实现类型；公共协议模型仍直接使用 {@link Long}。</p>
 */
public final class Unsigned32 {
    private static final Long ZERO = 0L;
    private static final long MAX_VALUE = 4_294_967_295L;

    private Unsigned32() {}

    /**
     * 返回出站循环计数器的首个值。
     *
     * @return 零
     */
    public static Long initial() {
        return ZERO;
    }

    /**
     * 判断值是否位于 {@code uint32} 闭区间。
     *
     * @param value 待检查值
     * @return 值非空且位于 {@code [0, 4294967295]} 时返回 {@code true}
     */
    public static boolean isValid(Long value) {
        return value != null && value >= 0L && value <= MAX_VALUE;
    }

    /**
     * 返回循环计数器的下一值，最大值之后回绕到零。
     *
     * @param current 当前合法值
     * @return 下一值
     * @throws NullPointerException 当前值为 {@code null} 时
     * @throws IllegalArgumentException 当前值超出 {@code uint32} 范围时
     */
    public static Long next(Long current) {
        Objects.requireNonNull(current, "current");
        if (!isValid(current)) {
            throw new IllegalArgumentException("Current value is outside the uint32 range");
        }
        return current == MAX_VALUE ? ZERO : current + 1L;
    }
}
