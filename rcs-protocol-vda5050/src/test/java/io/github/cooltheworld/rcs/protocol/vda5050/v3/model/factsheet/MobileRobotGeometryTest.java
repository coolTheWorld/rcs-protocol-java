package io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

final class MobileRobotGeometryTest {
    @Test
    @DisplayName("[VDA3-FACTSHEET-001] 车轮与位置形成不可变强类型几何图")
    void buildsAnImmutableWheelGeometryGraph() {
        WheelPosition position = WheelPosition.builder()
            .x(0.8D)
            .y(-0.4D)
            .theta(1.57D)
            .build();
        WheelDefinition wheel = WheelDefinition.builder()
            .type(WheelType.FIXED)
            .isActiveDriven(true)
            .isActiveSteered(false)
            .position(position)
            .diameter(0.32D)
            .width(0.1D)
            .centerDisplacement(0.0D)
            .constraints("rear axle")
            .build();
        List<WheelDefinition> wheels = new ArrayList<>(List.of(wheel));

        MobileRobotGeometry geometry = MobileRobotGeometry.builder()
            .wheelDefinitions(wheels)
            .build();
        MobileRobotGeometry equalGeometry = MobileRobotGeometry.builder()
            .wheelDefinitions(List.of(wheel))
            .build();
        wheels.clear();

        assertAll(
            () -> assertEquals(List.of(wheel), geometry.wheelDefinitions()),
            () -> assertNull(geometry.envelopes2d()),
            () -> assertNull(geometry.envelopes3d()),
            () -> assertThrows(
                UnsupportedOperationException.class,
                () -> geometry.wheelDefinitions().clear()
            ),
            () -> assertTrue(position.extensionFields().isEmpty()),
            () -> assertTrue(wheel.extensionFields().isEmpty()),
            () -> assertTrue(geometry.extensionFields().isEmpty()),
            () -> assertEquals(geometry, equalGeometry),
            () -> assertEquals(geometry.hashCode(), equalGeometry.hashCode())
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] 车轮类型保留标准值与厂商扩展值")
    void preservesStandardAndVendorWheelTypes() {
        assertAll(
            () -> assertEquals("DRIVE", WheelType.DRIVE.value()),
            () -> assertEquals("CASTER", WheelType.CASTER.value()),
            () -> assertEquals("FIXED", WheelType.FIXED.value()),
            () -> assertEquals("MECANUM", WheelType.MECANUM.value()),
            () -> assertEquals(
                "SPHERICAL",
                WheelType.of("SPHERICAL").value()
            )
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] 车轮几何值相等覆盖每个标准字段")
    void includesEveryStandardWheelGeometryFieldInValueEquality() {
        WheelPosition position = fullPosition().build();
        WheelDefinition wheel = fullWheel().build();
        Envelope2d envelope2d = Envelope2d.builder()
            .envelope2dId("footprint")
            .vertices(List.of())
            .build();
        Envelope3d envelope3d = Envelope3d.builder()
            .envelope3dId("body")
            .format("gltf")
            .build();
        MobileRobotGeometry geometry = MobileRobotGeometry.builder()
            .wheelDefinitions(List.of(wheel))
            .envelopes2d(List.of(envelope2d))
            .envelopes3d(List.of(envelope3d))
            .build();

        assertAll(
            () -> assertEquals(position, position),
            () -> assertNotEquals(position, null),
            () -> assertNotEquals(position, "position"),
            () -> assertNotEquals(position, fullPosition().x(0.9D).build()),
            () -> assertNotEquals(position, fullPosition().y(-0.5D).build()),
            () -> assertNotEquals(position, fullPosition().theta(0.0D).build()),
            () -> assertEquals(wheel, wheel),
            () -> assertNotEquals(wheel, null),
            () -> assertNotEquals(wheel, "wheel"),
            () -> assertNotEquals(wheel, fullWheel().type(WheelType.DRIVE).build()),
            () -> assertNotEquals(wheel, fullWheel().isActiveDriven(false).build()),
            () -> assertNotEquals(wheel, fullWheel().isActiveSteered(true).build()),
            () -> assertNotEquals(
                wheel,
                fullWheel().position(fullPosition().x(0.9D).build()).build()
            ),
            () -> assertNotEquals(wheel, fullWheel().diameter(0.4D).build()),
            () -> assertNotEquals(wheel, fullWheel().width(0.2D).build()),
            () -> assertNotEquals(
                wheel,
                fullWheel().centerDisplacement(0.1D).build()
            ),
            () -> assertNotEquals(
                wheel,
                fullWheel().constraints("front axle").build()
            ),
            () -> assertEquals(geometry, geometry),
            () -> assertNotEquals(geometry, null),
            () -> assertNotEquals(geometry, "geometry"),
            () -> assertNotEquals(
                geometry,
                MobileRobotGeometry.builder()
                    .wheelDefinitions(List.of())
                    .envelopes2d(List.of(envelope2d))
                    .envelopes3d(List.of(envelope3d))
                    .build()
            ),
            () -> assertNotEquals(
                geometry,
                MobileRobotGeometry.builder()
                    .wheelDefinitions(List.of(wheel))
                    .envelopes2d(List.of())
                    .envelopes3d(List.of(envelope3d))
                    .build()
            ),
            () -> assertNotEquals(
                geometry,
                MobileRobotGeometry.builder()
                    .wheelDefinitions(List.of(wheel))
                    .envelopes2d(List.of(envelope2d))
                    .envelopes3d(List.of())
                    .build()
            )
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] 几何构造拒绝缺失必填字段和 null 列表元素")
    void rejectsMissingRequiredFieldsAndNullListElements() {
        WheelPosition position = WheelPosition.builder().x(0.0D).y(0.0D).build();
        WheelDefinition.Builder wheel = WheelDefinition.builder()
            .type(WheelType.DRIVE)
            .isActiveDriven(true)
            .isActiveSteered(false)
            .position(position)
            .diameter(0.3D)
            .width(0.1D);

        assertAll(
            () -> assertThrows(
                NullPointerException.class,
                () -> WheelPosition.builder().y(0.0D).build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> WheelPosition.builder().x(0.0D).build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> wheel.type(null).build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> wheel.type(WheelType.DRIVE).diameter(null).build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> MobileRobotGeometry.builder()
                    .wheelDefinitions(Arrays.asList(wheel.diameter(0.3D).build(), null))
                    .build()
            )
        );
    }

    private static WheelPosition.Builder fullPosition() {
        return WheelPosition.builder()
            .x(0.8D)
            .y(-0.4D)
            .theta(1.57D);
    }

    private static WheelDefinition.Builder fullWheel() {
        return WheelDefinition.builder()
            .type(WheelType.FIXED)
            .isActiveDriven(true)
            .isActiveSteered(false)
            .position(fullPosition().build())
            .diameter(0.32D)
            .width(0.1D)
            .centerDisplacement(0.0D)
            .constraints("rear axle");
    }
}
