package io.github.cooltheworld.rcs.protocol.vda5050.v3.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.ValueSource;

final class ProtocolVersionTest {
    @Test
    @DisplayName("[VDA3-SHARED-003] 3.0.0 是唯一显式支持的版本配置")
    void supportsOnlyVersionThreeZeroZero() {
        ProtocolVersion version = ProtocolVersion.parse("3.0.0");

        assertTrue(ProtocolVersionProfile.supports(version));
        assertSame(
            ProtocolVersionProfile.V3_0_0,
            ProtocolVersionProfile.requireSupported(version)
        );
        assertEquals(version, ProtocolVersionProfile.V3_0_0.version());
        assertEquals("3.0.0", ProtocolVersionProfile.V3_0_0.toString());
    }

    @Test
    @DisplayName("[VDA3-SHARED-003] 未知版本可表示但没有支持配置")
    void representsUnknownVersionWithoutTreatingItAsSupported() {
        ProtocolVersion unknownVersion = ProtocolVersion.parse("3.1.0");

        assertEquals("3.1.0", unknownVersion.value());
        assertEquals("3.1.0", unknownVersion.toString());
        assertFalse(ProtocolVersionProfile.supports(unknownVersion));
        assertThrows(
            IllegalArgumentException.class,
            () -> ProtocolVersionProfile.requireSupported(unknownVersion)
        );
    }

    @ParameterizedTest(name = "[VDA3-SHARED-003] 拒绝非法版本格式：{0}")
    @EmptySource
    @ValueSource(strings = {
        "3",
        "3.0",
        "3.0.0.0",
        "3.0.x",
        "03.0.0",
        "3.00.0",
        "3.0.00",
        " 3.0.0",
        "3.0.0 "
    })
    void rejectsInvalidVersionFormats(String value) {
        assertThrows(IllegalArgumentException.class, () -> ProtocolVersion.parse(value));
    }

    @Test
    void rejectsNullVersionInputs() {
        assertThrows(NullPointerException.class, () -> ProtocolVersion.parse(null));
        assertThrows(NullPointerException.class, () -> ProtocolVersionProfile.supports(null));
        assertThrows(
            NullPointerException.class,
            () -> ProtocolVersionProfile.requireSupported(null)
        );
    }
}
