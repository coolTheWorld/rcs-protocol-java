package io.github.cooltheworld.rcs.protocol.vda5050.v3.codec;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

final class JsonCodecLimitsTest {
    @Test
    @DisplayName("[VDA3-SHARED-009] 默认限制与部署安全基线逐项一致")
    void exposesDeploymentSafetyDefaults() {
        JsonCodecLimits limits = JsonCodecLimits.defaults();

        assertAll(
            () -> assertEquals(8 * 1024 * 1024, limits.maxPayloadBytes()),
            () -> assertEquals(64, limits.maxNestingDepth()),
            () -> assertEquals(256 * 1024, limits.maxStringCharacters()),
            () -> assertEquals(256, limits.maxNameCharacters()),
            () -> assertEquals(128, limits.maxNumberCharacters()),
            () -> assertEquals(10_000, limits.maxArrayElements()),
            () -> assertEquals(1_024, limits.maxObjectProperties()),
            () -> assertEquals(1_000_000L, limits.maxTokens())
        );
    }

    @Test
    void buildsStartupFixedCustomLimits() {
        JsonCodecLimits limits = JsonCodecLimits.builder()
            .maxPayloadBytes(128)
            .maxNestingDepth(2)
            .maxStringCharacters(16)
            .maxNameCharacters(8)
            .maxNumberCharacters(4)
            .maxArrayElements(3)
            .maxObjectProperties(3)
            .maxTokens(20L)
            .build();

        assertAll(
            () -> assertEquals(128, limits.maxPayloadBytes()),
            () -> assertEquals(2, limits.maxNestingDepth()),
            () -> assertEquals(16, limits.maxStringCharacters()),
            () -> assertEquals(8, limits.maxNameCharacters()),
            () -> assertEquals(4, limits.maxNumberCharacters()),
            () -> assertEquals(3, limits.maxArrayElements()),
            () -> assertEquals(3, limits.maxObjectProperties()),
            () -> assertEquals(20L, limits.maxTokens())
        );
    }

    @Test
    void rejectsEveryNonPositiveLimitAtStartup() {
        assertAll(
            () -> assertThrows(
                IllegalArgumentException.class,
                () -> JsonCodecLimits.builder().maxPayloadBytes(0).build()
            ),
            () -> assertThrows(
                IllegalArgumentException.class,
                () -> JsonCodecLimits.builder().maxNestingDepth(0).build()
            ),
            () -> assertThrows(
                IllegalArgumentException.class,
                () -> JsonCodecLimits.builder().maxStringCharacters(0).build()
            ),
            () -> assertThrows(
                IllegalArgumentException.class,
                () -> JsonCodecLimits.builder().maxNameCharacters(0).build()
            ),
            () -> assertThrows(
                IllegalArgumentException.class,
                () -> JsonCodecLimits.builder().maxNumberCharacters(0).build()
            ),
            () -> assertThrows(
                IllegalArgumentException.class,
                () -> JsonCodecLimits.builder().maxArrayElements(0).build()
            ),
            () -> assertThrows(
                IllegalArgumentException.class,
                () -> JsonCodecLimits.builder().maxObjectProperties(0).build()
            ),
            () -> assertThrows(
                IllegalArgumentException.class,
                () -> JsonCodecLimits.builder().maxTokens(0L).build()
            )
        );
    }
}
