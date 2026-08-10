package io.github.cooltheworld.rcs.protocol.vda5050.v3.model;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

final class PhysicalParametersTest {
    @Test
    @DisplayName("[VDA3-FACTSHEET-001] 物理参数用 Double 精确表达规范字段")
    void buildsPhysicalParametersWithProtocolNumericWrappers() {
        PhysicalParameters parameters = completeBuilder().build();
        PhysicalParameters equalParameters = completeBuilder().build();
        PhysicalParameters differentParameters = completeBuilder()
            .length(2.5D)
            .build();

        assertAll(
            () -> assertEquals(0.05D, parameters.minimumSpeed()),
            () -> assertEquals(2.0D, parameters.maximumSpeed()),
            () -> assertEquals(0.1D, parameters.minimumAngularSpeed()),
            () -> assertEquals(1.5D, parameters.maximumAngularSpeed()),
            () -> assertEquals(0.8D, parameters.maximumAcceleration()),
            () -> assertEquals(1.2D, parameters.maximumDeceleration()),
            () -> assertEquals(0.2D, parameters.minimumHeight()),
            () -> assertEquals(2.2D, parameters.maximumHeight()),
            () -> assertEquals(1.1D, parameters.width()),
            () -> assertEquals(2.0D, parameters.length()),
            () -> assertTrue(parameters.extensionFields().isEmpty()),
            () -> assertEquals(parameters, equalParameters),
            () -> assertEquals(parameters.hashCode(), equalParameters.hashCode()),
            () -> assertNotEquals(parameters, differentParameters)
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] 可选角速度用 null 表达缺失")
    void representsMissingOptionalAngularSpeedsAsNull() {
        PhysicalParameters parameters = requiredBuilder().build();

        assertAll(
            () -> assertNull(parameters.minimumAngularSpeed()),
            () -> assertNull(parameters.maximumAngularSpeed())
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] 物理参数值相等覆盖每个标准字段")
    void includesEveryStandardFieldInValueEquality() {
        PhysicalParameters parameters = completeBuilder().build();

        assertAll(
            () -> assertEquals(parameters, parameters),
            () -> assertNotEquals(parameters, null),
            () -> assertNotEquals(
                parameters,
                completeBuilder().minimumSpeed(0.06D).build()
            ),
            () -> assertNotEquals(
                parameters,
                completeBuilder().maximumSpeed(2.1D).build()
            ),
            () -> assertNotEquals(
                parameters,
                completeBuilder().minimumAngularSpeed(0.2D).build()
            ),
            () -> assertNotEquals(
                parameters,
                completeBuilder().maximumAngularSpeed(1.6D).build()
            ),
            () -> assertNotEquals(
                parameters,
                completeBuilder().maximumAcceleration(0.9D).build()
            ),
            () -> assertNotEquals(
                parameters,
                completeBuilder().maximumDeceleration(1.3D).build()
            ),
            () -> assertNotEquals(
                parameters,
                completeBuilder().minimumHeight(0.3D).build()
            ),
            () -> assertNotEquals(
                parameters,
                completeBuilder().maximumHeight(2.3D).build()
            ),
            () -> assertNotEquals(
                parameters,
                completeBuilder().width(1.2D).build()
            ),
            () -> assertNotEquals(
                parameters,
                completeBuilder().length(2.1D).build()
            )
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] 物理参数拒绝缺失必填字段")
    void rejectsMissingRequiredFields() {
        assertAll(
            () -> assertThrows(
                NullPointerException.class,
                () -> requiredBuilder().minimumSpeed(null).build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> requiredBuilder().maximumSpeed(null).build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> requiredBuilder().maximumAcceleration(null).build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> requiredBuilder().maximumDeceleration(null).build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> requiredBuilder().minimumHeight(null).build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> requiredBuilder().maximumHeight(null).build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> requiredBuilder().width(null).build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> requiredBuilder().length(null).build()
            )
        );
    }

    private static PhysicalParameters.Builder completeBuilder() {
        return requiredBuilder()
            .minimumAngularSpeed(0.1D)
            .maximumAngularSpeed(1.5D);
    }

    private static PhysicalParameters.Builder requiredBuilder() {
        return PhysicalParameters.builder()
            .minimumSpeed(0.05D)
            .maximumSpeed(2.0D)
            .maximumAcceleration(0.8D)
            .maximumDeceleration(1.2D)
            .minimumHeight(0.2D)
            .maximumHeight(2.2D)
            .width(1.1D)
            .length(2.0D);
    }
}
