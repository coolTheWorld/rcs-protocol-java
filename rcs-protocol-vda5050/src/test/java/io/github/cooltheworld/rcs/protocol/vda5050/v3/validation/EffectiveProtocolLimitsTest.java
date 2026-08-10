package io.github.cooltheworld.rcs.protocol.vda5050.v3.validation;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.codec.JsonCodecLimits;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.MaximumArrayLengths;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.MaximumStringLengths;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.ProtocolLimits;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.ProtocolTiming;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

final class EffectiveProtocolLimitsTest {
    private static final long UINT32_MAX = 4_294_967_295L;

    @Test
    @DisplayName("[VDA3-SHARED-009] 有效限制与对应部署硬上限取交集")
    void intersectsDeclaredCapabilitiesWithDeploymentLimits() {
        JsonCodecLimits deployment = JsonCodecLimits.builder()
            .maxPayloadBytes(100)
            .maxStringCharacters(50)
            .maxArrayElements(20)
            .build();
        ProtocolLimits declared = ProtocolLimits.builder()
            .maximumStringLengths(MaximumStringLengths.builder()
                .maximumMessageLength(80L)
                .maximumTopicSerialLength(0L)
                .maximumTopicElementLength(null)
                .maximumIdLength(75L)
                .idNumericalOnly(true)
                .maximumLoadIdLength(25L)
                .build())
            .maximumArrayLengths(MaximumArrayLengths.builder()
                .orderNodes(10L)
                .orderEdges(30L)
                .nodeActions(0L)
                .edgeActions(null)
                .actionParameters(5L)
                .instantActions(25L)
                .trajectoryKnotVector(6L)
                .trajectoryControlPoints(26L)
                .zoneSetZones(7L)
                .stateNodeStates(27L)
                .stateEdgeStates(8L)
                .stateLoads(28L)
                .stateActionStates(9L)
                .stateInstantActionStates(29L)
                .stateZoneActionStates(10L)
                .stateErrors(30L)
                .stateInformation(11L)
                .errorErrorReferences(31L)
                .informationInfoReferences(12L)
                .build())
            .timing(ProtocolTiming.builder()
                .minimumOrderInterval(0.0D)
                .minimumStateInterval(0.5D)
                .defaultStateInterval(null)
                .visualizationInterval(0.0D)
                .build())
            .build();

        EffectiveProtocolLimits effective = EffectiveProtocolLimits.resolve(
            declared,
            deployment
        );

        assertAll(
            () -> assertEquals(80L, effective.maximumMessageLength()),
            () -> assertEquals(50L, effective.maximumTopicSerialLength()),
            () -> assertEquals(50L, effective.maximumTopicElementLength()),
            () -> assertEquals(50L, effective.maximumIdLength()),
            () -> assertTrue(effective.idNumericalOnly()),
            () -> assertEquals(25L, effective.maximumLoadIdLength()),
            () -> assertEquals(10L, effective.orderNodes()),
            () -> assertEquals(20L, effective.orderEdges()),
            () -> assertEquals(20L, effective.nodeActions()),
            () -> assertEquals(20L, effective.edgeActions()),
            () -> assertEquals(5L, effective.actionParameters()),
            () -> assertEquals(20L, effective.instantActions()),
            () -> assertEquals(6L, effective.trajectoryKnotVector()),
            () -> assertEquals(20L, effective.trajectoryControlPoints()),
            () -> assertEquals(7L, effective.zoneSetZones()),
            () -> assertEquals(20L, effective.stateNodeStates()),
            () -> assertEquals(8L, effective.stateEdgeStates()),
            () -> assertEquals(20L, effective.stateLoads()),
            () -> assertEquals(9L, effective.stateActionStates()),
            () -> assertEquals(20L, effective.stateInstantActionStates()),
            () -> assertEquals(10L, effective.stateZoneActionStates()),
            () -> assertEquals(20L, effective.stateErrors()),
            () -> assertEquals(11L, effective.stateInformation()),
            () -> assertEquals(20L, effective.errorErrorReferences()),
            () -> assertEquals(12L, effective.informationInfoReferences()),
            () -> assertNull(effective.minimumOrderInterval()),
            () -> assertEquals(0.5D, effective.minimumStateInterval()),
            () -> assertNull(effective.defaultStateInterval()),
            () -> assertNull(effective.visualizationInterval())
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-009] 缺失或零值声明均不放宽部署上限")
    void usesDeploymentLimitsForMissingAndZeroDeclarations() {
        JsonCodecLimits deployment = JsonCodecLimits.builder()
            .maxPayloadBytes(101)
            .maxStringCharacters(51)
            .maxArrayElements(21)
            .build();
        ProtocolLimits missing = limits(
            MaximumStringLengths.builder().build(),
            MaximumArrayLengths.builder().build(),
            ProtocolTiming.builder()
                .minimumOrderInterval(0.0D)
                .minimumStateInterval(0.0D)
                .build()
        );
        ProtocolLimits zero = limits(
            MaximumStringLengths.builder()
                .maximumMessageLength(0L)
                .maximumTopicSerialLength(0L)
                .maximumTopicElementLength(0L)
                .maximumIdLength(0L)
                .idNumericalOnly(false)
                .maximumLoadIdLength(0L)
                .build(),
            arrays(0L),
            ProtocolTiming.builder()
                .minimumOrderInterval(0.0D)
                .minimumStateInterval(0.0D)
                .defaultStateInterval(0.0D)
                .visualizationInterval(0.0D)
                .build()
        );

        EffectiveProtocolLimits missingEffective = EffectiveProtocolLimits.resolve(
            missing,
            deployment
        );
        EffectiveProtocolLimits zeroEffective = EffectiveProtocolLimits.resolve(
            zero,
            deployment
        );

        assertAll(
            () -> assertStringLimits(missingEffective, 101L, 51L),
            () -> assertStringLimits(zeroEffective, 101L, 51L),
            () -> assertArrayLimits(missingEffective, 21L),
            () -> assertArrayLimits(zeroEffective, 21L),
            () -> assertFalse(missingEffective.idNumericalOnly()),
            () -> assertFalse(zeroEffective.idNumericalOnly()),
            () -> assertNull(zeroEffective.defaultStateInterval())
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-009] 交集算法对任意非负 uint32 声明都不突破部署上限")
    void intersectionPropertyNeverExceedsDeploymentLimits() {
        Random random = new Random(50_50L);

        for (int sample = 0; sample < 1_000; sample++) {
            int payloadLimit = random.nextInt(1, 100_001);
            int stringLimit = random.nextInt(1, 100_001);
            int arrayLimit = random.nextInt(1, 100_001);
            long declaration = random.nextLong(UINT32_MAX + 1L);
            JsonCodecLimits deployment = JsonCodecLimits.builder()
                .maxPayloadBytes(payloadLimit)
                .maxStringCharacters(stringLimit)
                .maxArrayElements(arrayLimit)
                .build();
            ProtocolLimits limits = limits(
                strings(declaration),
                arrays(declaration),
                ProtocolTiming.builder()
                    .minimumOrderInterval(0.0D)
                    .minimumStateInterval(0.0D)
                    .build()
            );

            EffectiveProtocolLimits effective = EffectiveProtocolLimits.resolve(
                limits,
                deployment
            );
            long expectedPayload = declaration == 0L
                ? payloadLimit
                : Math.min(declaration, payloadLimit);
            long expectedString = declaration == 0L
                ? stringLimit
                : Math.min(declaration, stringLimit);
            long expectedArray = declaration == 0L
                ? arrayLimit
                : Math.min(declaration, arrayLimit);

            assertStringLimits(effective, expectedPayload, expectedString);
            assertArrayLimits(effective, expectedArray);
        }
    }

    @Test
    @DisplayName("[VDA3-SHARED-009] 非法限制值 fail closed")
    void rejectsInvalidDeclaredLimits() {
        JsonCodecLimits deployment = JsonCodecLimits.defaults();

        assertAll(
            () -> assertThrows(
                IllegalArgumentException.class,
                () -> EffectiveProtocolLimits.resolve(
                    limits(
                        MaximumStringLengths.builder()
                            .maximumIdLength(-1L)
                            .build(),
                        MaximumArrayLengths.builder().build(),
                        timing()
                    ),
                    deployment
                )
            ),
            () -> assertThrows(
                IllegalArgumentException.class,
                () -> EffectiveProtocolLimits.resolve(
                    limits(
                        MaximumStringLengths.builder().build(),
                        MaximumArrayLengths.builder().build(),
                        ProtocolTiming.builder()
                            .minimumOrderInterval(0.0D)
                            .minimumStateInterval(0.0D)
                            .defaultStateInterval(-0.1D)
                            .build()
                    ),
                    deployment
                )
            ),
            () -> assertThrows(
                IllegalArgumentException.class,
                () -> EffectiveProtocolLimits.resolve(
                    limits(
                        MaximumStringLengths.builder().build(),
                        MaximumArrayLengths.builder()
                            .stateErrors(UINT32_MAX + 1L)
                            .build(),
                        timing()
                    ),
                    deployment
                )
            ),
            () -> assertThrows(
                IllegalArgumentException.class,
                () -> EffectiveProtocolLimits.resolve(
                    limits(
                        MaximumStringLengths.builder().build(),
                        MaximumArrayLengths.builder().build(),
                        ProtocolTiming.builder()
                            .minimumOrderInterval(Double.NaN)
                            .minimumStateInterval(0.0D)
                            .build()
                    ),
                    deployment
                )
            ),
            () -> assertThrows(
                IllegalArgumentException.class,
                () -> EffectiveProtocolLimits.resolve(
                    limits(
                        MaximumStringLengths.builder().build(),
                        MaximumArrayLengths.builder().build(),
                        ProtocolTiming.builder()
                            .minimumOrderInterval(0.0D)
                            .minimumStateInterval(Double.POSITIVE_INFINITY)
                            .build()
                    ),
                    deployment
                )
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> EffectiveProtocolLimits.resolve(null, deployment)
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> EffectiveProtocolLimits.resolve(
                    limits(
                        MaximumStringLengths.builder().build(),
                        MaximumArrayLengths.builder().build(),
                        timing()
                    ),
                    null
                )
            )
        );
    }

    private static ProtocolLimits limits(
        MaximumStringLengths strings,
        MaximumArrayLengths arrays,
        ProtocolTiming timing
    ) {
        return ProtocolLimits.builder()
            .maximumStringLengths(strings)
            .maximumArrayLengths(arrays)
            .timing(timing)
            .build();
    }

    private static MaximumStringLengths strings(long value) {
        return MaximumStringLengths.builder()
            .maximumMessageLength(value)
            .maximumTopicSerialLength(value)
            .maximumTopicElementLength(value)
            .maximumIdLength(value)
            .maximumLoadIdLength(value)
            .build();
    }

    private static MaximumArrayLengths arrays(long value) {
        return MaximumArrayLengths.builder()
            .orderNodes(value)
            .orderEdges(value)
            .nodeActions(value)
            .edgeActions(value)
            .actionParameters(value)
            .instantActions(value)
            .trajectoryKnotVector(value)
            .trajectoryControlPoints(value)
            .zoneSetZones(value)
            .stateNodeStates(value)
            .stateEdgeStates(value)
            .stateLoads(value)
            .stateActionStates(value)
            .stateInstantActionStates(value)
            .stateZoneActionStates(value)
            .stateErrors(value)
            .stateInformation(value)
            .errorErrorReferences(value)
            .informationInfoReferences(value)
            .build();
    }

    private static ProtocolTiming timing() {
        return ProtocolTiming.builder()
            .minimumOrderInterval(0.0D)
            .minimumStateInterval(0.0D)
            .build();
    }

    private static void assertStringLimits(
        EffectiveProtocolLimits effective,
        long expectedPayload,
        long expectedString
    ) {
        assertEquals(expectedPayload, effective.maximumMessageLength());
        assertEquals(
            List.of(expectedString, expectedString, expectedString, expectedString),
            List.of(
                effective.maximumTopicSerialLength(),
                effective.maximumTopicElementLength(),
                effective.maximumIdLength(),
                effective.maximumLoadIdLength()
            )
        );
    }

    private static void assertArrayLimits(
        EffectiveProtocolLimits effective,
        long expected
    ) {
        List<Long> actual = List.of(
            effective.orderNodes(),
            effective.orderEdges(),
            effective.nodeActions(),
            effective.edgeActions(),
            effective.actionParameters(),
            effective.instantActions(),
            effective.trajectoryKnotVector(),
            effective.trajectoryControlPoints(),
            effective.zoneSetZones(),
            effective.stateNodeStates(),
            effective.stateEdgeStates(),
            effective.stateLoads(),
            effective.stateActionStates(),
            effective.stateInstantActionStates(),
            effective.stateZoneActionStates(),
            effective.stateErrors(),
            effective.stateInformation(),
            effective.errorErrorReferences(),
            effective.informationInfoReferences()
        );

        assertEquals(19, actual.size());
        actual.forEach(value -> assertEquals(expected, value));
    }
}
