package io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

final class FactsheetContentTest {
    @Test
    @DisplayName("[VDA3-FACTSHEET-001] 内容聚合六个必填片段与可选配置")
    void aggregatesRequiredFactsheetFragmentsAndOptionalConfiguration() {
        FactsheetContent content = completeBuilder().build();
        FactsheetContent equalContent = completeBuilder().build();
        FactsheetContent missingConfiguration = completeBuilder()
            .mobileRobotConfiguration(null)
            .build();

        assertAll(
            () -> assertEquals(typeSpecification(), content.typeSpecification()),
            () -> assertEquals(physicalParameters(), content.physicalParameters()),
            () -> assertEquals(protocolLimits(), content.protocolLimits()),
            () -> assertEquals(protocolFeatures(), content.protocolFeatures()),
            () -> assertEquals(mobileRobotGeometry(), content.mobileRobotGeometry()),
            () -> assertEquals(loadSpecification(), content.loadSpecification()),
            () -> assertEquals(configuration(), content.mobileRobotConfiguration()),
            () -> assertNull(missingConfiguration.mobileRobotConfiguration()),
            () -> assertEquals(content, content),
            () -> assertEquals(content, equalContent),
            () -> assertNotEquals(content, null),
            () -> assertNotEquals(content, "factsheet"),
            () -> assertEquals(content.hashCode(), equalContent.hashCode())
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] 内容聚合拒绝任一必填片段缺失")
    void rejectsEveryMissingRequiredFragment() {
        assertAll(
            () -> assertThrows(
                NullPointerException.class,
                () -> completeBuilder().typeSpecification(null).build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> completeBuilder().physicalParameters(null).build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> completeBuilder().protocolLimits(null).build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> completeBuilder().protocolFeatures(null).build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> completeBuilder().mobileRobotGeometry(null).build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> completeBuilder().loadSpecification(null).build()
            )
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] 内容聚合全部字段参与值语义")
    void includesEveryContentFieldInValueSemantics() {
        FactsheetContent content = completeBuilder().build();

        assertAll(
            () -> assertNotEquals(
                content,
                completeBuilder()
                    .typeSpecification(typeSpecificationBuilder()
                        .seriesName("OTHER")
                        .build())
                    .build()
            ),
            () -> assertNotEquals(
                content,
                completeBuilder()
                    .physicalParameters(physicalParametersBuilder()
                        .maximumSpeed(3.0D)
                        .build())
                    .build()
            ),
            () -> assertNotEquals(
                content,
                completeBuilder()
                    .protocolLimits(ProtocolLimits.builder()
                        .maximumStringLengths(MaximumStringLengths.builder()
                            .maximumIdLength(10L)
                            .build())
                        .maximumArrayLengths(MaximumArrayLengths.builder().build())
                        .timing(ProtocolTiming.builder()
                            .minimumOrderInterval(0.0D)
                            .minimumStateInterval(0.0D)
                            .build())
                        .build())
                    .build()
            ),
            () -> assertNotEquals(
                content,
                completeBuilder()
                    .protocolFeatures(ProtocolFeatures.builder()
                        .optionalParameters(List.of())
                        .mobileRobotActions(List.of())
                        .build())
                    .build()
            ),
            () -> assertNotEquals(
                content,
                completeBuilder()
                    .mobileRobotGeometry(MobileRobotGeometry.builder().build())
                    .build()
            ),
            () -> assertNotEquals(
                content,
                completeBuilder()
                    .loadSpecification(LoadSpecification.builder().build())
                    .build()
            ),
            () -> assertNotEquals(
                content,
                completeBuilder().mobileRobotConfiguration(null).build()
            )
        );
    }

    private static FactsheetContent.Builder completeBuilder() {
        return FactsheetContent.builder()
            .typeSpecification(typeSpecification())
            .physicalParameters(physicalParameters())
            .protocolLimits(protocolLimits())
            .protocolFeatures(protocolFeatures())
            .mobileRobotGeometry(mobileRobotGeometry())
            .loadSpecification(loadSpecification())
            .mobileRobotConfiguration(configuration());
    }

    private static TypeSpecification typeSpecification() {
        return typeSpecificationBuilder().build();
    }

    private static TypeSpecification.Builder typeSpecificationBuilder() {
        return TypeSpecification.builder()
            .seriesName("SERIES")
            .mobileRobotKinematics(MobileRobotKinematics.DIFFERENTIAL)
            .mobileRobotClass(MobileRobotClass.CARRIER)
            .maximumLoadMass(100.0D)
            .localizationTypes(List.of(LocalizationType.NATURAL))
            .navigationTypes(List.of(NavigationType.FREELY_NAVIGATING));
    }

    private static PhysicalParameters physicalParameters() {
        return physicalParametersBuilder().build();
    }

    private static PhysicalParameters.Builder physicalParametersBuilder() {
        return PhysicalParameters.builder()
            .minimumSpeed(0.1D)
            .maximumSpeed(2.0D)
            .maximumAcceleration(1.0D)
            .maximumDeceleration(1.0D)
            .minimumHeight(0.2D)
            .maximumHeight(1.0D)
            .width(0.8D)
            .length(1.2D);
    }

    private static ProtocolLimits protocolLimits() {
        return ProtocolLimits.builder()
            .maximumStringLengths(MaximumStringLengths.builder().build())
            .maximumArrayLengths(MaximumArrayLengths.builder().build())
            .timing(ProtocolTiming.builder()
                .minimumOrderInterval(0.0D)
                .minimumStateInterval(0.0D)
                .build())
            .build();
    }

    private static ProtocolFeatures protocolFeatures() {
        return ProtocolFeatures.builder()
            .optionalParameters(List.of(OptionalParameter.builder()
                .parameter("order.nodes")
                .support(OptionalParameterSupport.SUPPORTED)
                .build()))
            .mobileRobotActions(List.of())
            .build();
    }

    private static MobileRobotGeometry mobileRobotGeometry() {
        return MobileRobotGeometry.builder()
            .envelopes2d(List.of())
            .build();
    }

    private static LoadSpecification loadSpecification() {
        return LoadSpecification.builder()
            .loadPositions(List.of())
            .build();
    }

    private static MobileRobotConfiguration configuration() {
        return MobileRobotConfiguration.builder()
            .versions(List.of(VersionInfo.builder()
                .key("softwareVersion")
                .value("1")
                .build()))
            .build();
    }
}
