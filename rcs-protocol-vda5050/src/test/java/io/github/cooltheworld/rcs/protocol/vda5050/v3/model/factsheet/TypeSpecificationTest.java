package io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

final class TypeSpecificationTest {
    @Test
    @DisplayName("[VDA3-FACTSHEET-001] 类型说明保留强类型能力与不可变列表")
    void buildsAnImmutableTypeSpecification() {
        List<LocalizationType> localizationTypes = new ArrayList<>(List.of(
            LocalizationType.NATURAL,
            LocalizationType.of("ULTRA_WIDEBAND")
        ));
        List<NavigationType> navigationTypes = new ArrayList<>(List.of(
            NavigationType.FREELY_NAVIGATING
        ));
        List<ZoneType> supportedZones = new ArrayList<>(List.of(
            ZoneType.BLOCKED,
            ZoneType.SPEED_LIMIT
        ));

        TypeSpecification specification = TypeSpecification.builder()
            .seriesName("Forkbot X")
            .seriesDescription("Warehouse forklift series")
            .mobileRobotKinematics(MobileRobotKinematics.DIFFERENTIAL)
            .mobileRobotClass(MobileRobotClass.FORKLIFT)
            .maximumLoadMass(1_250.5D)
            .localizationTypes(localizationTypes)
            .navigationTypes(navigationTypes)
            .supportedZones(supportedZones)
            .build();
        TypeSpecification equalSpecification = TypeSpecification.builder()
            .seriesName("Forkbot X")
            .seriesDescription("Warehouse forklift series")
            .mobileRobotKinematics(MobileRobotKinematics.DIFFERENTIAL)
            .mobileRobotClass(MobileRobotClass.FORKLIFT)
            .maximumLoadMass(1_250.5D)
            .localizationTypes(localizationTypes)
            .navigationTypes(navigationTypes)
            .supportedZones(supportedZones)
            .build();

        localizationTypes.clear();
        navigationTypes.clear();
        supportedZones.clear();

        assertAll(
            () -> assertEquals("Forkbot X", specification.seriesName()),
            () -> assertEquals(
                "Warehouse forklift series",
                specification.seriesDescription()
            ),
            () -> assertEquals(
                MobileRobotKinematics.DIFFERENTIAL,
                specification.mobileRobotKinematics()
            ),
            () -> assertEquals(
                MobileRobotClass.FORKLIFT,
                specification.mobileRobotClass()
            ),
            () -> assertEquals(1_250.5D, specification.maximumLoadMass()),
            () -> assertEquals(
                List.of(
                    LocalizationType.NATURAL,
                    LocalizationType.of("ULTRA_WIDEBAND")
                ),
                specification.localizationTypes()
            ),
            () -> assertEquals(
                List.of(NavigationType.FREELY_NAVIGATING),
                specification.navigationTypes()
            ),
            () -> assertEquals(
                List.of(ZoneType.BLOCKED, ZoneType.SPEED_LIMIT),
                specification.supportedZones()
            ),
            () -> assertThrows(
                UnsupportedOperationException.class,
                () -> specification.localizationTypes().clear()
            ),
            () -> assertTrue(specification.extensionFields().isEmpty()),
            () -> assertEquals(specification, equalSpecification),
            () -> assertEquals(
                specification.hashCode(),
                equalSpecification.hashCode()
            ),
            () -> assertNotEquals(
                specification,
                TypeSpecification.builder()
                    .seriesName("Forkbot Y")
                    .mobileRobotKinematics(MobileRobotKinematics.DIFFERENTIAL)
                    .mobileRobotClass(MobileRobotClass.FORKLIFT)
                    .maximumLoadMass(1_250.5D)
                    .localizationTypes(List.of())
                    .navigationTypes(List.of())
                    .build()
            )
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] 可选 Zone 列表区分缺失与空数组")
    void distinguishesMissingAndEmptyOptionalZones() {
        TypeSpecification missing = minimalBuilder().build();
        TypeSpecification empty = minimalBuilder().supportedZones(List.of()).build();

        assertAll(
            () -> assertNull(missing.seriesDescription()),
            () -> assertNull(missing.supportedZones()),
            () -> assertEquals(List.of(), empty.supportedZones()),
            () -> assertNotEquals(missing, empty)
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] 可扩展枚举保留未知值并公开规范常量")
    void preservesUnknownExtensibleValuesAndExposesStandardConstants() {
        assertAll(
            () -> assertEquals(
                Set.of("DIFFERENTIAL", "OMNIDIRECTIONAL", "THREE_WHEEL"),
                Set.of(
                    MobileRobotKinematics.DIFFERENTIAL.value(),
                    MobileRobotKinematics.OMNIDIRECTIONAL.value(),
                    MobileRobotKinematics.THREE_WHEEL.value()
                )
            ),
            () -> assertEquals(
                Set.of("FORKLIFT", "CONVEYOR", "TUGGER", "CARRIER"),
                Set.of(
                    MobileRobotClass.FORKLIFT.value(),
                    MobileRobotClass.CONVEYOR.value(),
                    MobileRobotClass.TUGGER.value(),
                    MobileRobotClass.CARRIER.value()
                )
            ),
            () -> assertEquals(
                "ULTRA_WIDEBAND",
                LocalizationType.of("ULTRA_WIDEBAND").value()
            ),
            () -> assertEquals(
                Set.of("NATURAL", "REFLECTOR", "RFID", "DMC", "SPOT", "GRID"),
                Set.of(
                    LocalizationType.NATURAL.value(),
                    LocalizationType.REFLECTOR.value(),
                    LocalizationType.RFID.value(),
                    LocalizationType.DMC.value(),
                    LocalizationType.SPOT.value(),
                    LocalizationType.GRID.value()
                )
            ),
            () -> assertEquals(
                "AUTONOMOUS_FOLLOWING",
                NavigationType.of("AUTONOMOUS_FOLLOWING").toString()
            ),
            () -> assertEquals(
                Set.of(
                    "PHYSICAL_LINE_GUIDED",
                    "VIRTUAL_LINE_GUIDED",
                    "FREELY_NAVIGATING"
                ),
                Set.of(
                    NavigationType.PHYSICAL_LINE_GUIDED.value(),
                    NavigationType.VIRTUAL_LINE_GUIDED.value(),
                    NavigationType.FREELY_NAVIGATING.value()
                )
            ),
            () -> assertEquals(
                Set.of(
                    ZoneType.BLOCKED,
                    ZoneType.LINE_GUIDED,
                    ZoneType.RELEASE,
                    ZoneType.COORDINATED_REPLANNING,
                    ZoneType.SPEED_LIMIT,
                    ZoneType.ACTION,
                    ZoneType.PRIORITY,
                    ZoneType.PENALTY,
                    ZoneType.DIRECTED,
                    ZoneType.BIDIRECTED
                ),
                Set.of(ZoneType.values())
            )
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] 类型说明值相等覆盖每个标准字段")
    void includesEveryStandardFieldInValueEquality() {
        TypeSpecification specification = fullBuilder().build();

        assertAll(
            () -> assertEquals(specification, specification),
            () -> assertNotEquals(specification, null),
            () -> assertNotEquals(
                specification,
                fullBuilder().seriesName("Forkbot Y").build()
            ),
            () -> assertNotEquals(
                specification,
                fullBuilder().seriesDescription("Different").build()
            ),
            () -> assertNotEquals(
                specification,
                fullBuilder()
                    .mobileRobotKinematics(MobileRobotKinematics.OMNIDIRECTIONAL)
                    .build()
            ),
            () -> assertNotEquals(
                specification,
                fullBuilder().mobileRobotClass(MobileRobotClass.CARRIER).build()
            ),
            () -> assertNotEquals(
                specification,
                fullBuilder().maximumLoadMass(1.0D).build()
            ),
            () -> assertNotEquals(
                specification,
                fullBuilder().localizationTypes(List.of(LocalizationType.GRID)).build()
            ),
            () -> assertNotEquals(
                specification,
                fullBuilder()
                    .navigationTypes(List.of(NavigationType.PHYSICAL_LINE_GUIDED))
                    .build()
            ),
            () -> assertNotEquals(
                specification,
                fullBuilder().supportedZones(List.of(ZoneType.ACTION)).build()
            )
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] 类型说明拒绝缺失必填字段和含 null 的列表")
    void rejectsMissingRequiredFieldsAndNullListElements() {
        assertAll(
            () -> assertThrows(
                NullPointerException.class,
                () -> minimalBuilder().seriesName(null).build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> minimalBuilder().mobileRobotKinematics(null).build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> minimalBuilder().mobileRobotClass(null).build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> minimalBuilder().maximumLoadMass(null).build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> minimalBuilder().localizationTypes(null).build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> minimalBuilder().navigationTypes(null).build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> minimalBuilder()
                    .localizationTypes(java.util.Arrays.asList(
                        LocalizationType.NATURAL,
                        null
                    ))
                    .build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> MobileRobotKinematics.of(null)
            )
        );
    }

    private static TypeSpecification.Builder minimalBuilder() {
        return TypeSpecification.builder()
            .seriesName("Forkbot X")
            .mobileRobotKinematics(MobileRobotKinematics.DIFFERENTIAL)
            .mobileRobotClass(MobileRobotClass.FORKLIFT)
            .maximumLoadMass(0.0D)
            .localizationTypes(List.of(LocalizationType.NATURAL))
            .navigationTypes(List.of(NavigationType.FREELY_NAVIGATING));
    }

    private static TypeSpecification.Builder fullBuilder() {
        return minimalBuilder()
            .seriesDescription("Warehouse forklift series")
            .supportedZones(List.of(ZoneType.BLOCKED));
    }
}
