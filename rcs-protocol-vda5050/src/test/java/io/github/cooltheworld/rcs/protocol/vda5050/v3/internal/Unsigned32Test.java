package io.github.cooltheworld.rcs.protocol.vda5050.v3.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

final class Unsigned32Test {
    @Test
    @DisplayName("[VDA3-SHARED-005] 出站计数器从零开始")
    void startsAtZero() {
        assertEquals(0L, Unsigned32.initial());
    }

    @Test
    @DisplayName("[VDA3-SHARED-002] uint32 接受闭区间边界")
    void acceptsClosedUnsigned32Range() {
        assertTrue(Unsigned32.isValid(0L));
        assertTrue(Unsigned32.isValid(4_294_967_295L));
    }

    @ParameterizedTest(name = "[VDA3-SHARED-002] 拒绝 uint32 范围外数值：{0}")
    @ValueSource(longs = {
        Long.MIN_VALUE,
        -1L,
        4_294_967_296L,
        Long.MAX_VALUE
    })
    void rejectsValuesOutsideUnsigned32Range(Long value) {
        assertFalse(Unsigned32.isValid(value));
        assertThrows(IllegalArgumentException.class, () -> Unsigned32.next(value));
    }

    @Test
    @DisplayName("[VDA3-SHARED-005] 计数器递增并在最大值后回绕")
    void incrementsAndWrapsAfterMaximum() {
        assertEquals(1L, Unsigned32.next(0L));
        assertEquals(4_294_967_295L, Unsigned32.next(4_294_967_294L));
        assertEquals(0L, Unsigned32.next(4_294_967_295L));
    }

    @Test
    void treatsNullAsInvalidAndRejectsIncrement() {
        assertFalse(Unsigned32.isValid(null));
        assertThrows(NullPointerException.class, () -> Unsigned32.next(null));
    }
}
