package io.github.cooltheworld.rcs.protocol.vda5050.v3.codec;

import static io.github.cooltheworld.rcs.protocol.vda5050.v3.codec.FactsheetFragmentJacksonSupport.putOptional;
import static io.github.cooltheworld.rcs.protocol.vda5050.v3.codec.FactsheetFragmentJacksonSupport.readObject;
import static io.github.cooltheworld.rcs.protocol.vda5050.v3.codec.FactsheetFragmentJacksonSupport.readOptional;
import static io.github.cooltheworld.rcs.protocol.vda5050.v3.codec.FactsheetFragmentJacksonSupport.readOptionalList;
import static io.github.cooltheworld.rcs.protocol.vda5050.v3.codec.FactsheetFragmentJacksonSupport.readRequired;
import static io.github.cooltheworld.rcs.protocol.vda5050.v3.codec.FactsheetFragmentJacksonSupport.requireObjectMapper;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.ExtensionFields;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.internal.ExtensionFieldsJacksonSupport;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.BatteryCharging;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.MobileRobotConfiguration;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.NetworkConfiguration;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.VersionInfo;
import java.io.IOException;
import java.util.Set;

/** 注册 Factsheet {@code mobileRobotConfiguration} 子树的 Jackson 线路映射。 */
final class MobileRobotConfigurationJacksonSupport {
    private static final Set<String> CONFIGURATION_FIELDS = Set.of(
        "versions",
        "network",
        "batteryCharging"
    );
    private static final Set<String> VERSION_FIELDS = Set.of("key", "value");
    private static final Set<String> NETWORK_FIELDS = Set.of(
        "dnsServers",
        "ntpServers",
        "localIpAddress",
        "netmask",
        "defaultGateway"
    );
    private static final Set<String> BATTERY_CHARGING_FIELDS = Set.of(
        "criticalLowChargingLevel",
        "minimumDesiredChargingLevel",
        "maximumDesiredChargingLevel",
        "minimumChargingTime"
    );

    private MobileRobotConfigurationJacksonSupport() {}

    static void register(SimpleModule module) {
        module.addSerializer(
            MobileRobotConfiguration.class,
            new MobileRobotConfigurationSerializer()
        );
        module.addDeserializer(
            MobileRobotConfiguration.class,
            new MobileRobotConfigurationDeserializer()
        );
        module.addSerializer(VersionInfo.class, new VersionInfoSerializer());
        module.addDeserializer(VersionInfo.class, new VersionInfoDeserializer());
        module.addSerializer(
            NetworkConfiguration.class,
            new NetworkConfigurationSerializer()
        );
        module.addDeserializer(
            NetworkConfiguration.class,
            new NetworkConfigurationDeserializer()
        );
        module.addSerializer(BatteryCharging.class, new BatteryChargingSerializer());
        module.addDeserializer(
            BatteryCharging.class,
            new BatteryChargingDeserializer()
        );
    }

    private static final class MobileRobotConfigurationSerializer
        extends StdSerializer<MobileRobotConfiguration> {
        private MobileRobotConfigurationSerializer() {
            super(MobileRobotConfiguration.class);
        }

        @Override
        public void serialize(
            MobileRobotConfiguration value,
            JsonGenerator generator,
            SerializerProvider provider
        ) throws IOException {
            ObjectMapper mapper = requireObjectMapper(generator);
            ObjectNode target = mapper.createObjectNode();
            putOptionalTree(mapper, target, "versions", value.versions());
            putOptionalTree(mapper, target, "network", value.network());
            putOptionalTree(
                mapper,
                target,
                "batteryCharging",
                value.batteryCharging()
            );
            merge(
                mapper,
                target,
                value.extensionFields(),
                CONFIGURATION_FIELDS
            );
            generator.writeTree(target);
        }
    }

    private static final class MobileRobotConfigurationDeserializer
        extends StdDeserializer<MobileRobotConfiguration> {
        private MobileRobotConfigurationDeserializer() {
            super(MobileRobotConfiguration.class);
        }

        @Override
        public MobileRobotConfiguration deserialize(
            JsonParser parser,
            DeserializationContext context
        ) throws IOException {
            ObjectMapper mapper = requireObjectMapper(parser);
            ObjectNode object = readObject(
                mapper,
                parser,
                context,
                MobileRobotConfiguration.class,
                "Mobile Robot configuration"
            );
            return MobileRobotConfiguration.builder()
                .versions(readOptionalList(
                    object,
                    "versions",
                    VersionInfo.class,
                    MobileRobotConfiguration.class,
                    context
                ))
                .network(readOptional(
                    object,
                    "network",
                    NetworkConfiguration.class,
                    MobileRobotConfiguration.class,
                    context
                ))
                .batteryCharging(readOptional(
                    object,
                    "batteryCharging",
                    BatteryCharging.class,
                    MobileRobotConfiguration.class,
                    context
                ))
                .extensionFields(capture(mapper, object, CONFIGURATION_FIELDS))
                .build();
        }
    }

    private static final class VersionInfoSerializer
        extends StdSerializer<VersionInfo> {
        private VersionInfoSerializer() {
            super(VersionInfo.class);
        }

        @Override
        public void serialize(
            VersionInfo value,
            JsonGenerator generator,
            SerializerProvider provider
        ) throws IOException {
            ObjectMapper mapper = requireObjectMapper(generator);
            ObjectNode target = mapper.createObjectNode();
            target.put("key", value.key());
            target.put("value", value.value());
            merge(mapper, target, value.extensionFields(), VERSION_FIELDS);
            generator.writeTree(target);
        }
    }

    private static final class VersionInfoDeserializer
        extends StdDeserializer<VersionInfo> {
        private VersionInfoDeserializer() {
            super(VersionInfo.class);
        }

        @Override
        public VersionInfo deserialize(
            JsonParser parser,
            DeserializationContext context
        ) throws IOException {
            ObjectMapper mapper = requireObjectMapper(parser);
            ObjectNode object = readObject(
                mapper,
                parser,
                context,
                VersionInfo.class,
                "Version information"
            );
            return VersionInfo.builder()
                .key(readRequired(
                    object,
                    "key",
                    String.class,
                    VersionInfo.class,
                    context
                ))
                .value(readRequired(
                    object,
                    "value",
                    String.class,
                    VersionInfo.class,
                    context
                ))
                .extensionFields(capture(mapper, object, VERSION_FIELDS))
                .build();
        }
    }

    private static final class NetworkConfigurationSerializer
        extends StdSerializer<NetworkConfiguration> {
        private NetworkConfigurationSerializer() {
            super(NetworkConfiguration.class);
        }

        @Override
        public void serialize(
            NetworkConfiguration value,
            JsonGenerator generator,
            SerializerProvider provider
        ) throws IOException {
            ObjectMapper mapper = requireObjectMapper(generator);
            ObjectNode target = mapper.createObjectNode();
            putOptionalTree(mapper, target, "dnsServers", value.dnsServers());
            putOptionalTree(mapper, target, "ntpServers", value.ntpServers());
            putOptional(target, "localIpAddress", value.localIpAddress());
            putOptional(target, "netmask", value.netmask());
            putOptional(target, "defaultGateway", value.defaultGateway());
            merge(mapper, target, value.extensionFields(), NETWORK_FIELDS);
            generator.writeTree(target);
        }
    }

    private static final class NetworkConfigurationDeserializer
        extends StdDeserializer<NetworkConfiguration> {
        private NetworkConfigurationDeserializer() {
            super(NetworkConfiguration.class);
        }

        @Override
        public NetworkConfiguration deserialize(
            JsonParser parser,
            DeserializationContext context
        ) throws IOException {
            ObjectMapper mapper = requireObjectMapper(parser);
            ObjectNode object = readObject(
                mapper,
                parser,
                context,
                NetworkConfiguration.class,
                "Network configuration"
            );
            return NetworkConfiguration.builder()
                .dnsServers(readOptionalList(
                    object,
                    "dnsServers",
                    String.class,
                    NetworkConfiguration.class,
                    context
                ))
                .ntpServers(readOptionalList(
                    object,
                    "ntpServers",
                    String.class,
                    NetworkConfiguration.class,
                    context
                ))
                .localIpAddress(readOptional(
                    object,
                    "localIpAddress",
                    String.class,
                    NetworkConfiguration.class,
                    context
                ))
                .netmask(readOptional(
                    object,
                    "netmask",
                    String.class,
                    NetworkConfiguration.class,
                    context
                ))
                .defaultGateway(readOptional(
                    object,
                    "defaultGateway",
                    String.class,
                    NetworkConfiguration.class,
                    context
                ))
                .extensionFields(capture(mapper, object, NETWORK_FIELDS))
                .build();
        }
    }

    private static final class BatteryChargingSerializer
        extends StdSerializer<BatteryCharging> {
        private BatteryChargingSerializer() {
            super(BatteryCharging.class);
        }

        @Override
        public void serialize(
            BatteryCharging value,
            JsonGenerator generator,
            SerializerProvider provider
        ) throws IOException {
            ObjectMapper mapper = requireObjectMapper(generator);
            ObjectNode target = mapper.createObjectNode();
            putOptional(
                target,
                "criticalLowChargingLevel",
                value.criticalLowChargingLevel()
            );
            putOptional(
                target,
                "minimumDesiredChargingLevel",
                value.minimumDesiredChargingLevel()
            );
            putOptional(
                target,
                "maximumDesiredChargingLevel",
                value.maximumDesiredChargingLevel()
            );
            putOptionalLong(
                target,
                "minimumChargingTime",
                value.minimumChargingTime()
            );
            merge(
                mapper,
                target,
                value.extensionFields(),
                BATTERY_CHARGING_FIELDS
            );
            generator.writeTree(target);
        }
    }

    private static final class BatteryChargingDeserializer
        extends StdDeserializer<BatteryCharging> {
        private BatteryChargingDeserializer() {
            super(BatteryCharging.class);
        }

        @Override
        public BatteryCharging deserialize(
            JsonParser parser,
            DeserializationContext context
        ) throws IOException {
            ObjectMapper mapper = requireObjectMapper(parser);
            ObjectNode object = readObject(
                mapper,
                parser,
                context,
                BatteryCharging.class,
                "Battery charging parameters"
            );
            return BatteryCharging.builder()
                .criticalLowChargingLevel(readOptional(
                    object,
                    "criticalLowChargingLevel",
                    Double.class,
                    BatteryCharging.class,
                    context
                ))
                .minimumDesiredChargingLevel(readOptional(
                    object,
                    "minimumDesiredChargingLevel",
                    Double.class,
                    BatteryCharging.class,
                    context
                ))
                .maximumDesiredChargingLevel(readOptional(
                    object,
                    "maximumDesiredChargingLevel",
                    Double.class,
                    BatteryCharging.class,
                    context
                ))
                .minimumChargingTime(readOptional(
                    object,
                    "minimumChargingTime",
                    Long.class,
                    BatteryCharging.class,
                    context
                ))
                .extensionFields(capture(
                    mapper,
                    object,
                    BATTERY_CHARGING_FIELDS
                ))
                .build();
        }
    }

    private static ExtensionFields capture(
        ObjectMapper mapper,
        ObjectNode source,
        Set<String> standardFields
    ) throws IOException {
        return ExtensionFieldsJacksonSupport.capture(
            mapper,
            source,
            standardFields
        );
    }

    private static void merge(
        ObjectMapper mapper,
        ObjectNode target,
        ExtensionFields extensionFields,
        Set<String> standardFields
    ) {
        ExtensionFieldsJacksonSupport.merge(
            mapper,
            target,
            extensionFields,
            standardFields
        );
    }

    private static void putOptionalLong(
        ObjectNode target,
        String fieldName,
        Long value
    ) {
        if (value != null) {
            target.put(fieldName, value);
        }
    }

    private static void putOptionalTree(
        ObjectMapper mapper,
        ObjectNode target,
        String fieldName,
        Object value
    ) {
        if (value != null) {
            target.set(fieldName, mapper.valueToTree(value));
        }
    }
}
