package io.github.cooltheworld.rcs.protocol.vda5050.v3.model;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

final class ProtocolLimitsTest {
    @Test
    @DisplayName("[VDA3-FACTSHEET-001] Protocol Limits 公开全部标准字段")
    void exposesEveryStandardLimit() {
        MaximumStringLengths strings = fullStrings().build();
        MaximumArrayLengths arrays = fullArrays().build();
        ProtocolTiming timing = fullTiming().build();
        ProtocolLimits limits = ProtocolLimits.builder()
            .maximumStringLengths(strings)
            .maximumArrayLengths(arrays)
            .timing(timing)
            .build();
        ProtocolLimits equalLimits = ProtocolLimits.builder()
            .maximumStringLengths(fullStrings().build())
            .maximumArrayLengths(fullArrays().build())
            .timing(fullTiming().build())
            .build();

        assertAll(
            () -> assertEquals(1L, strings.maximumMessageLength()),
            () -> assertEquals(2L, strings.maximumTopicSerialLength()),
            () -> assertEquals(3L, strings.maximumTopicElementLength()),
            () -> assertEquals(4L, strings.maximumIdLength()),
            () -> assertEquals(Boolean.TRUE, strings.idNumericalOnly()),
            () -> assertEquals(5L, strings.maximumLoadIdLength()),
            () -> assertEquals(6L, arrays.orderNodes()),
            () -> assertEquals(7L, arrays.orderEdges()),
            () -> assertEquals(8L, arrays.nodeActions()),
            () -> assertEquals(9L, arrays.edgeActions()),
            () -> assertEquals(10L, arrays.actionParameters()),
            () -> assertEquals(11L, arrays.instantActions()),
            () -> assertEquals(12L, arrays.trajectoryKnotVector()),
            () -> assertEquals(13L, arrays.trajectoryControlPoints()),
            () -> assertEquals(14L, arrays.zoneSetZones()),
            () -> assertEquals(15L, arrays.stateNodeStates()),
            () -> assertEquals(16L, arrays.stateEdgeStates()),
            () -> assertEquals(17L, arrays.stateLoads()),
            () -> assertEquals(18L, arrays.stateActionStates()),
            () -> assertEquals(19L, arrays.stateInstantActionStates()),
            () -> assertEquals(20L, arrays.stateZoneActionStates()),
            () -> assertEquals(21L, arrays.stateErrors()),
            () -> assertEquals(22L, arrays.stateInformation()),
            () -> assertEquals(23L, arrays.errorErrorReferences()),
            () -> assertEquals(24L, arrays.informationInfoReferences()),
            () -> assertEquals(0.25D, timing.minimumOrderInterval()),
            () -> assertEquals(0.5D, timing.minimumStateInterval()),
            () -> assertEquals(1.0D, timing.defaultStateInterval()),
            () -> assertEquals(0.1D, timing.visualizationInterval()),
            () -> assertEquals(strings, limits.maximumStringLengths()),
            () -> assertEquals(arrays, limits.maximumArrayLengths()),
            () -> assertEquals(timing, limits.timing()),
            () -> assertTrue(limits.extensionFields().isEmpty()),
            () -> assertEquals(limits, equalLimits),
            () -> assertEquals(limits.hashCode(), equalLimits.hashCode())
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] 原始模型区分缺失声明和显式零值")
    void distinguishesMissingAndExplicitZeroDeclarations() {
        MaximumStringLengths missingStrings = MaximumStringLengths.builder()
            .build();
        MaximumStringLengths zeroStrings = MaximumStringLengths.builder()
            .maximumMessageLength(0L)
            .build();
        MaximumArrayLengths missingArrays = MaximumArrayLengths.builder().build();
        MaximumArrayLengths zeroArrays = MaximumArrayLengths.builder()
            .orderNodes(0L)
            .build();
        ProtocolTiming zeroTiming = ProtocolTiming.builder()
            .minimumOrderInterval(0.0D)
            .minimumStateInterval(0.0D)
            .build();

        assertAll(
            () -> assertNull(missingStrings.maximumMessageLength()),
            () -> assertEquals(0L, zeroStrings.maximumMessageLength()),
            () -> assertNotEquals(missingStrings, zeroStrings),
            () -> assertNull(missingArrays.orderNodes()),
            () -> assertEquals(0L, zeroArrays.orderNodes()),
            () -> assertNotEquals(missingArrays, zeroArrays),
            () -> assertNull(zeroTiming.defaultStateInterval()),
            () -> assertNull(zeroTiming.visualizationInterval())
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] Protocol Limits 拒绝缺失必填子对象和 timing 字段")
    void rejectsMissingRequiredObjectsAndTimingFields() {
        assertAll(
            () -> assertThrows(
                NullPointerException.class,
                () -> ProtocolLimits.builder()
                    .maximumArrayLengths(MaximumArrayLengths.builder().build())
                    .timing(fullTiming().build())
                    .build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> ProtocolLimits.builder()
                    .maximumStringLengths(MaximumStringLengths.builder().build())
                    .timing(fullTiming().build())
                    .build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> ProtocolLimits.builder()
                    .maximumStringLengths(MaximumStringLengths.builder().build())
                    .maximumArrayLengths(MaximumArrayLengths.builder().build())
                    .build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> ProtocolTiming.builder()
                    .minimumStateInterval(1.0D)
                    .build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> ProtocolTiming.builder()
                    .minimumOrderInterval(1.0D)
                    .build()
            )
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] 值语义包含每类限制和独立子对象")
    void valueSemanticsIncludeIndependentLimitGroups() {
        ProtocolLimits limits = ProtocolLimits.builder()
            .maximumStringLengths(fullStrings().build())
            .maximumArrayLengths(fullArrays().build())
            .timing(fullTiming().build())
            .build();

        assertAll(
            () -> assertEquals(limits, limits),
            () -> assertNotEquals(limits, null),
            () -> assertNotEquals(
                limits,
                ProtocolLimits.builder()
                    .maximumStringLengths(fullStrings().maximumIdLength(99L).build())
                    .maximumArrayLengths(fullArrays().build())
                    .timing(fullTiming().build())
                    .build()
            ),
            () -> assertNotEquals(
                fullArrays().build(),
                fullArrays().stateErrors(99L).build()
            ),
            () -> assertNotEquals(
                fullTiming().build(),
                fullTiming().visualizationInterval(9.0D).build()
            )
        );
    }

    private static MaximumStringLengths.Builder fullStrings() {
        return MaximumStringLengths.builder()
            .maximumMessageLength(1L)
            .maximumTopicSerialLength(2L)
            .maximumTopicElementLength(3L)
            .maximumIdLength(4L)
            .idNumericalOnly(true)
            .maximumLoadIdLength(5L);
    }

    private static MaximumArrayLengths.Builder fullArrays() {
        return MaximumArrayLengths.builder()
            .orderNodes(6L)
            .orderEdges(7L)
            .nodeActions(8L)
            .edgeActions(9L)
            .actionParameters(10L)
            .instantActions(11L)
            .trajectoryKnotVector(12L)
            .trajectoryControlPoints(13L)
            .zoneSetZones(14L)
            .stateNodeStates(15L)
            .stateEdgeStates(16L)
            .stateLoads(17L)
            .stateActionStates(18L)
            .stateInstantActionStates(19L)
            .stateZoneActionStates(20L)
            .stateErrors(21L)
            .stateInformation(22L)
            .errorErrorReferences(23L)
            .informationInfoReferences(24L);
    }

    private static ProtocolTiming.Builder fullTiming() {
        return ProtocolTiming.builder()
            .minimumOrderInterval(0.25D)
            .minimumStateInterval(0.5D)
            .defaultStateInterval(1.0D)
            .visualizationInterval(0.1D);
    }
}
