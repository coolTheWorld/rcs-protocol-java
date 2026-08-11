package io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.ExtensionFields;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.internal.ExtensionFieldsJacksonSupport;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.common.ProtocolHeader;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.common.ProtocolTimestamp;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.common.ProtocolVersion;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.common.RobotIdentity;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

final class FactsheetTest {
    @Test
    @DisplayName("[VDA3-FACTSHEET-001] Factsheet 强类型组合 Header 与内容")
    void buildsFactsheetFromHeaderAndContent() {
        Factsheet factsheet = completeBuilder().build();
        Factsheet equalFactsheet = completeBuilder().build();
        Factsheet defaultExtensions = Factsheet.builder()
            .header(header())
            .content(content())
            .build();

        assertAll(
            () -> assertEquals(header(), factsheet.header()),
            () -> assertEquals(content(), factsheet.content()),
            () -> assertTrue(factsheet.extensionFields().isEmpty()),
            () -> assertTrue(defaultExtensions.extensionFields().isEmpty()),
            () -> assertEquals(factsheet, factsheet),
            () -> assertEquals(factsheet, equalFactsheet),
            () -> assertNotEquals(factsheet, null),
            () -> assertNotEquals(factsheet, "factsheet"),
            () -> assertEquals(factsheet.hashCode(), equalFactsheet.hashCode())
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] Factsheet 拒绝缺失 Header 或内容")
    void rejectsMissingRequiredRootFields() {
        assertAll(
            () -> assertThrows(
                NullPointerException.class,
                () -> completeBuilder().header(null).build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> completeBuilder().content(null).build()
            )
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] 根扩展和标准字段参与值语义")
    void includesEveryRootFieldInValueSemantics() throws Exception {
        Factsheet factsheet = completeBuilder().build();
        ObjectMapper mapper = new ObjectMapper();
        ExtensionFields extensions = ExtensionFieldsJacksonSupport.capture(
            mapper,
            mapper.createObjectNode().put("vendorFactsheet", true),
            Set.of()
        );

        assertAll(
            () -> assertNotEquals(
                factsheet,
                completeBuilder().header(ProtocolHeader.builder()
                    .headerId(2L)
                    .timestamp(ProtocolTimestamp.from(Instant.EPOCH))
                    .version(ProtocolVersion.parse("3.0.0"))
                    .robotIdentity(new RobotIdentity("ACME", "R1"))
                    .build()).build()
            ),
            () -> assertNotEquals(
                factsheet,
                completeBuilder().content(contentBuilder()
                    .mobileRobotConfiguration(null)
                    .build()).build()
            ),
            () -> assertNotEquals(
                factsheet,
                completeBuilder().extensionFields(extensions).build()
            )
        );
    }

    private static Factsheet.Builder completeBuilder() {
        return Factsheet.builder()
            .header(header())
            .content(content())
            .extensionFields(ExtensionFields.empty());
    }

    private static ProtocolHeader header() {
        return ProtocolHeader.builder()
            .headerId(1L)
            .timestamp(ProtocolTimestamp.from(Instant.EPOCH))
            .version(ProtocolVersion.parse("3.0.0"))
            .robotIdentity(new RobotIdentity("ACME", "R1"))
            .build();
    }

    private static FactsheetContent content() {
        return contentBuilder().build();
    }

    private static FactsheetContent.Builder contentBuilder() {
        return FactsheetContent.builder()
            .typeSpecification(TypeSpecification.builder()
                .seriesName("SERIES")
                .mobileRobotKinematics(MobileRobotKinematics.DIFFERENTIAL)
                .mobileRobotClass(MobileRobotClass.CARRIER)
                .maximumLoadMass(100.0D)
                .localizationTypes(List.of(LocalizationType.NATURAL))
                .navigationTypes(List.of(NavigationType.FREELY_NAVIGATING))
                .build())
            .physicalParameters(PhysicalParameters.builder()
                .minimumSpeed(0.1D)
                .maximumSpeed(2.0D)
                .maximumAcceleration(1.0D)
                .maximumDeceleration(1.0D)
                .minimumHeight(0.2D)
                .maximumHeight(1.0D)
                .width(0.8D)
                .length(1.2D)
                .build())
            .protocolLimits(ProtocolLimits.builder()
                .maximumStringLengths(MaximumStringLengths.builder().build())
                .maximumArrayLengths(MaximumArrayLengths.builder().build())
                .timing(ProtocolTiming.builder()
                    .minimumOrderInterval(0.0D)
                    .minimumStateInterval(0.0D)
                    .build())
                .build())
            .protocolFeatures(ProtocolFeatures.builder()
                .optionalParameters(List.of())
                .mobileRobotActions(List.of())
                .build())
            .mobileRobotGeometry(MobileRobotGeometry.builder().build())
            .loadSpecification(LoadSpecification.builder().build())
            .mobileRobotConfiguration(MobileRobotConfiguration.builder().build());
    }
}
