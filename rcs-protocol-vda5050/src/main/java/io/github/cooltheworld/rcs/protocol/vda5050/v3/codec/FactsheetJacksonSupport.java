package io.github.cooltheworld.rcs.protocol.vda5050.v3.codec;

import static io.github.cooltheworld.rcs.protocol.vda5050.v3.codec.FactsheetFragmentJacksonSupport.readObject;
import static io.github.cooltheworld.rcs.protocol.vda5050.v3.codec.FactsheetFragmentJacksonSupport.readOptional;
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
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.common.ProtocolHeader;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.common.ProtocolTimestamp;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.common.ProtocolVersion;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.common.RobotIdentity;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.Factsheet;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.FactsheetContent;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.LoadSpecification;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.MobileRobotConfiguration;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.MobileRobotGeometry;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.PhysicalParameters;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.ProtocolFeatures;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.ProtocolLimits;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.TypeSpecification;
import java.io.IOException;
import java.util.Set;

/** 注册 Factsheet 根对象的平铺 Jackson 线路映射。 */
final class FactsheetJacksonSupport {
    private static final Set<String> FACTSHEET_FIELDS = Set.of(
        "headerId",
        "timestamp",
        "version",
        "manufacturer",
        "serialNumber",
        "typeSpecification",
        "physicalParameters",
        "protocolLimits",
        "protocolFeatures",
        "mobileRobotGeometry",
        "loadSpecification",
        "mobileRobotConfiguration"
    );

    private FactsheetJacksonSupport() {}

    static void register(SimpleModule module) {
        module.addSerializer(Factsheet.class, new FactsheetSerializer());
        module.addDeserializer(Factsheet.class, new FactsheetDeserializer());
    }

    private static final class FactsheetSerializer
        extends StdSerializer<Factsheet> {
        private FactsheetSerializer() {
            super(Factsheet.class);
        }

        @Override
        public void serialize(
            Factsheet value,
            JsonGenerator generator,
            SerializerProvider provider
        ) throws IOException {
            ObjectMapper mapper = requireObjectMapper(generator);
            ProtocolHeader header = value.header();
            FactsheetContent content = value.content();
            ObjectNode target = mapper.createObjectNode();
            target.put("headerId", header.headerId());
            target.set("timestamp", mapper.valueToTree(header.timestamp()));
            target.set("version", mapper.valueToTree(header.version()));
            target.put("manufacturer", header.robotIdentity().manufacturer());
            target.put("serialNumber", header.robotIdentity().serialNumber());
            target.set(
                "typeSpecification",
                mapper.valueToTree(content.typeSpecification())
            );
            target.set(
                "physicalParameters",
                mapper.valueToTree(content.physicalParameters())
            );
            target.set(
                "protocolLimits",
                mapper.valueToTree(content.protocolLimits())
            );
            target.set(
                "protocolFeatures",
                mapper.valueToTree(content.protocolFeatures())
            );
            target.set(
                "mobileRobotGeometry",
                mapper.valueToTree(content.mobileRobotGeometry())
            );
            target.set(
                "loadSpecification",
                mapper.valueToTree(content.loadSpecification())
            );
            if (content.mobileRobotConfiguration() != null) {
                target.set(
                    "mobileRobotConfiguration",
                    mapper.valueToTree(content.mobileRobotConfiguration())
                );
            }
            ExtensionFieldsJacksonSupport.merge(
                mapper,
                target,
                value.extensionFields(),
                FACTSHEET_FIELDS
            );
            generator.writeTree(target);
        }
    }

    private static final class FactsheetDeserializer
        extends StdDeserializer<Factsheet> {
        private FactsheetDeserializer() {
            super(Factsheet.class);
        }

        @Override
        public Factsheet deserialize(
            JsonParser parser,
            DeserializationContext context
        ) throws IOException {
            ObjectMapper mapper = requireObjectMapper(parser);
            ObjectNode object = readObject(
                mapper,
                parser,
                context,
                Factsheet.class,
                "Factsheet"
            );
            Long headerId = readRequired(
                object,
                "headerId",
                Long.class,
                Factsheet.class,
                context
            );
            ProtocolTimestamp timestamp = readRequired(
                object,
                "timestamp",
                ProtocolTimestamp.class,
                Factsheet.class,
                context
            );
            ProtocolVersion version = readRequired(
                object,
                "version",
                ProtocolVersion.class,
                Factsheet.class,
                context
            );
            String manufacturer = readRequired(
                object,
                "manufacturer",
                String.class,
                Factsheet.class,
                context
            );
            String serialNumber = readRequired(
                object,
                "serialNumber",
                String.class,
                Factsheet.class,
                context
            );
            TypeSpecification typeSpecification = readRequired(
                object,
                "typeSpecification",
                TypeSpecification.class,
                Factsheet.class,
                context
            );
            PhysicalParameters physicalParameters = readRequired(
                object,
                "physicalParameters",
                PhysicalParameters.class,
                Factsheet.class,
                context
            );
            ProtocolLimits protocolLimits = readRequired(
                object,
                "protocolLimits",
                ProtocolLimits.class,
                Factsheet.class,
                context
            );
            ProtocolFeatures protocolFeatures = readRequired(
                object,
                "protocolFeatures",
                ProtocolFeatures.class,
                Factsheet.class,
                context
            );
            MobileRobotGeometry mobileRobotGeometry = readRequired(
                object,
                "mobileRobotGeometry",
                MobileRobotGeometry.class,
                Factsheet.class,
                context
            );
            LoadSpecification loadSpecification = readRequired(
                object,
                "loadSpecification",
                LoadSpecification.class,
                Factsheet.class,
                context
            );
            MobileRobotConfiguration mobileRobotConfiguration = readOptional(
                object,
                "mobileRobotConfiguration",
                MobileRobotConfiguration.class,
                Factsheet.class,
                context
            );
            ExtensionFields extensionFields = ExtensionFieldsJacksonSupport.capture(
                mapper,
                object,
                FACTSHEET_FIELDS
            );
            try {
                ProtocolHeader header = ProtocolHeader.builder()
                    .headerId(headerId)
                    .timestamp(timestamp)
                    .version(version)
                    .robotIdentity(new RobotIdentity(manufacturer, serialNumber))
                    .build();
                FactsheetContent content = FactsheetContent.builder()
                    .typeSpecification(typeSpecification)
                    .physicalParameters(physicalParameters)
                    .protocolLimits(protocolLimits)
                    .protocolFeatures(protocolFeatures)
                    .mobileRobotGeometry(mobileRobotGeometry)
                    .loadSpecification(loadSpecification)
                    .mobileRobotConfiguration(mobileRobotConfiguration)
                    .build();
                return Factsheet.builder()
                    .header(header)
                    .content(content)
                    .extensionFields(extensionFields)
                    .build();
            } catch (IllegalArgumentException | NullPointerException exception) {
                return context.reportInputMismatch(
                    Factsheet.class,
                    "Factsheet fields do not satisfy model constraints"
                );
            }
        }
    }
}
