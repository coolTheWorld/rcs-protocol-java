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

final class NetworkConfigurationTest {
    @Test
    @DisplayName("[VDA3-FACTSHEET-001] 网络配置保留原始元数据与不可变列表")
    void preservesRawImmutableNetworkMetadata() {
        List<String> dnsServers = new ArrayList<>(List.of(" 10.0.0.2 "));
        List<String> ntpServers = new ArrayList<>(List.of("ntp.example.test"));
        NetworkConfiguration configuration = NetworkConfiguration.builder()
            .dnsServers(dnsServers)
            .ntpServers(ntpServers)
            .localIpAddress("10.0.0.5")
            .netmask("255.255.255.0")
            .defaultGateway("10.0.0.1")
            .build();
        NetworkConfiguration equalConfiguration = completeBuilder().build();
        dnsServers.clear();
        ntpServers.clear();

        assertAll(
            () -> assertEquals(List.of(" 10.0.0.2 "), configuration.dnsServers()),
            () -> assertEquals(List.of("ntp.example.test"), configuration.ntpServers()),
            () -> assertEquals("10.0.0.5", configuration.localIpAddress()),
            () -> assertEquals("255.255.255.0", configuration.netmask()),
            () -> assertEquals("10.0.0.1", configuration.defaultGateway()),
            () -> assertThrows(
                UnsupportedOperationException.class,
                () -> configuration.dnsServers().clear()
            ),
            () -> assertTrue(configuration.extensionFields().isEmpty()),
            () -> assertEquals(configuration, configuration),
            () -> assertEquals(configuration, equalConfiguration),
            () -> assertNotEquals(configuration, null),
            () -> assertNotEquals(configuration, "network"),
            () -> assertEquals(
                configuration.hashCode(),
                equalConfiguration.hashCode()
            )
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] 网络列表区分缺失与空数组并拒绝 null 元素")
    void distinguishesMissingAndEmptyServerLists() {
        NetworkConfiguration missing = NetworkConfiguration.builder().build();
        NetworkConfiguration empty = NetworkConfiguration.builder()
            .dnsServers(List.of())
            .ntpServers(List.of())
            .build();

        assertAll(
            () -> assertNull(missing.dnsServers()),
            () -> assertNull(missing.ntpServers()),
            () -> assertNull(missing.localIpAddress()),
            () -> assertNull(missing.netmask()),
            () -> assertNull(missing.defaultGateway()),
            () -> assertEquals(List.of(), empty.dnsServers()),
            () -> assertEquals(List.of(), empty.ntpServers()),
            () -> assertNotEquals(missing, empty),
            () -> assertThrows(
                NullPointerException.class,
                () -> NetworkConfiguration.builder()
                    .dnsServers(Arrays.asList("10.0.0.2", null))
                    .build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> NetworkConfiguration.builder()
                    .ntpServers(Arrays.asList("ntp.example.test", null))
                    .build()
            )
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] 网络配置全部字段和扩展参与值语义")
    void includesEveryNetworkFieldInValueSemantics() throws Exception {
        NetworkConfiguration configuration = completeBuilder().build();
        ObjectMapper mapper = new ObjectMapper();
        ExtensionFields extensions = ExtensionFieldsJacksonSupport.capture(
            mapper,
            mapper.createObjectNode().put("interface", "wlan0"),
            Set.of()
        );

        assertAll(
            () -> assertNotEquals(
                configuration,
                completeBuilder().dnsServers(List.of("1.1.1.1")).build()
            ),
            () -> assertNotEquals(
                configuration,
                completeBuilder().ntpServers(List.of("time.example.test")).build()
            ),
            () -> assertNotEquals(
                configuration,
                completeBuilder().localIpAddress("10.0.0.6").build()
            ),
            () -> assertNotEquals(
                configuration,
                completeBuilder().netmask("255.255.0.0").build()
            ),
            () -> assertNotEquals(
                configuration,
                completeBuilder().defaultGateway("10.0.0.254").build()
            ),
            () -> assertNotEquals(
                configuration,
                completeBuilder().extensionFields(extensions).build()
            )
        );
    }

    private static NetworkConfiguration.Builder completeBuilder() {
        return NetworkConfiguration.builder()
            .dnsServers(List.of(" 10.0.0.2 "))
            .ntpServers(List.of("ntp.example.test"))
            .localIpAddress("10.0.0.5")
            .netmask("255.255.255.0")
            .defaultGateway("10.0.0.1")
            .extensionFields(ExtensionFields.empty());
    }
}
