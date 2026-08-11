package io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.ExtensionFields;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.internal.ExtensionFieldsJacksonSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

final class MobileRobotConfigurationTest {
    @Test
    @DisplayName("[VDA3-FACTSHEET-001] 机器人配置聚合版本网络与充电片段")
    void aggregatesImmutableConfigurationFragments() {
        List<VersionInfo> versions = new ArrayList<>(List.of(version()));
        MobileRobotConfiguration configuration = completeBuilder()
            .versions(versions)
            .build();
        MobileRobotConfiguration equalConfiguration = completeBuilder().build();
        versions.clear();

        assertAll(
            () -> assertEquals(List.of(version()), configuration.versions()),
            () -> assertEquals(network(), configuration.network()),
            () -> assertEquals(charging(), configuration.batteryCharging()),
            () -> assertThrows(
                UnsupportedOperationException.class,
                () -> configuration.versions().clear()
            ),
            () -> assertTrue(configuration.extensionFields().isEmpty()),
            () -> assertEquals(configuration, configuration),
            () -> assertEquals(configuration, equalConfiguration),
            () -> assertNotEquals(configuration, null),
            () -> assertNotEquals(configuration, "configuration"),
            () -> assertEquals(
                configuration.hashCode(),
                equalConfiguration.hashCode()
            )
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] 配置片段保持缺失空列表和 null 元素边界")
    void preservesMissingAndEmptyConfigurationFragments() {
        MobileRobotConfiguration missing = MobileRobotConfiguration.builder()
            .build();
        MobileRobotConfiguration empty = MobileRobotConfiguration.builder()
            .versions(List.of())
            .build();

        assertAll(
            () -> assertNull(missing.versions()),
            () -> assertNull(missing.network()),
            () -> assertNull(missing.batteryCharging()),
            () -> assertEquals(List.of(), empty.versions()),
            () -> assertNotEquals(missing, empty),
            () -> assertThrows(
                NullPointerException.class,
                () -> MobileRobotConfiguration.builder()
                    .versions(Arrays.asList(version(), null))
                    .build()
            )
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] 配置聚合全部字段和扩展参与值语义")
    void includesEveryConfigurationFieldInValueSemantics() throws Exception {
        MobileRobotConfiguration configuration = completeBuilder().build();
        ObjectMapper mapper = new ObjectMapper();
        ExtensionFields extensions = ExtensionFieldsJacksonSupport.capture(
            mapper,
            mapper.createObjectNode().put("vendorConfig", true),
            Set.of()
        );

        assertAll(
            () -> assertNotEquals(
                configuration,
                completeBuilder().versions(List.of(VersionInfo.builder()
                    .key("plcVersion")
                    .value("2")
                    .build())).build()
            ),
            () -> assertNotEquals(
                configuration,
                completeBuilder().network(NetworkConfiguration.builder().build()).build()
            ),
            () -> assertNotEquals(
                configuration,
                completeBuilder().batteryCharging(BatteryCharging.builder().build()).build()
            ),
            () -> assertNotEquals(
                configuration,
                completeBuilder().extensionFields(extensions).build()
            )
        );
    }

    private static MobileRobotConfiguration.Builder completeBuilder() {
        return MobileRobotConfiguration.builder()
            .versions(List.of(version()))
            .network(network())
            .batteryCharging(charging())
            .extensionFields(ExtensionFields.empty());
    }

    private static VersionInfo version() {
        return VersionInfo.builder()
            .key("softwareVersion")
            .value("1")
            .build();
    }

    private static NetworkConfiguration network() {
        return NetworkConfiguration.builder()
            .dnsServers(List.of("10.0.0.2"))
            .build();
    }

    private static BatteryCharging charging() {
        return BatteryCharging.builder()
            .minimumDesiredChargingLevel(20.0D)
            .maximumDesiredChargingLevel(80.0D)
            .build();
    }
}
