package io.github.cooltheworld.rcs.protocol.vda5050.v3.codec;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.ExtensionFields;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.internal.ExtensionFieldsJacksonSupport;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.connection.Connection;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.connection.ConnectionState;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.common.ProtocolHeader;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.common.ProtocolTimestamp;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.common.ProtocolVersion;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.common.RobotIdentity;
import java.io.IOException;
import java.util.Set;

/**
 * 可显式注册到调用方 Jackson 2 {@code ObjectMapper} 的 VDA 5050 v3 Module。
 *
 * <p>该 Module 只注册协议类型的线路表示，不修改调用方的 null、未知字段、资源限制或多态
 * 配置。需要完整默认安全配置时应使用 {@link Vda5050JsonCodec}。</p>
 */
public final class Vda5050JacksonModule extends SimpleModule {
    private static final Set<String> CONNECTION_FIELDS = Set.of(
        "headerId",
        "timestamp",
        "version",
        "manufacturer",
        "serialNumber",
        "connectionState"
    );

    /** 创建只包含协议类型映射的 Module。 */
    public Vda5050JacksonModule() {
        super("Vda5050JacksonModule");
        addSerializer(ProtocolVersion.class, new ProtocolVersionSerializer());
        addDeserializer(ProtocolVersion.class, new ProtocolVersionDeserializer());
        addSerializer(ProtocolTimestamp.class, new ProtocolTimestampSerializer());
        addDeserializer(ProtocolTimestamp.class, new ProtocolTimestampDeserializer());
        addSerializer(Connection.class, new ConnectionSerializer());
        addDeserializer(Connection.class, new ConnectionDeserializer());
        ExtensionFieldsJacksonSupport.register(this);
        FactsheetFragmentJacksonSupport.register(this);
        ProtocolLimitsJacksonSupport.register(this);
        ProtocolFeaturesJacksonSupport.register(this);
        MobileRobotGeometryJacksonSupport.register(this);
        LoadSpecificationJacksonSupport.register(this);
    }

    private static final class ProtocolVersionSerializer
        extends StdScalarSerializer<ProtocolVersion> {
        private ProtocolVersionSerializer() {
            super(ProtocolVersion.class);
        }

        @Override
        public void serialize(
            ProtocolVersion value,
            JsonGenerator generator,
            SerializerProvider provider
        ) throws IOException {
            generator.writeString(value.toString());
        }
    }

    private static final class ProtocolVersionDeserializer
        extends StdDeserializer<ProtocolVersion> {
        private ProtocolVersionDeserializer() {
            super(ProtocolVersion.class);
        }

        @Override
        public ProtocolVersion deserialize(
            JsonParser parser,
            DeserializationContext context
        ) throws IOException {
            if (!parser.hasToken(JsonToken.VALUE_STRING)) {
                return (ProtocolVersion) context.handleUnexpectedToken(
                    ProtocolVersion.class,
                    parser
                );
            }
            try {
                return ProtocolVersion.parse(parser.getText());
            } catch (IllegalArgumentException exception) {
                return context.reportInputMismatch(
                    ProtocolVersion.class,
                    "Protocol version does not satisfy model constraints"
                );
            }
        }
    }

    private static final class ProtocolTimestampSerializer
        extends StdScalarSerializer<ProtocolTimestamp> {
        private ProtocolTimestampSerializer() {
            super(ProtocolTimestamp.class);
        }

        @Override
        public void serialize(
            ProtocolTimestamp value,
            JsonGenerator generator,
            SerializerProvider provider
        ) throws IOException {
            generator.writeString(value.toString());
        }
    }

    private static final class ProtocolTimestampDeserializer
        extends StdDeserializer<ProtocolTimestamp> {
        private ProtocolTimestampDeserializer() {
            super(ProtocolTimestamp.class);
        }

        @Override
        public ProtocolTimestamp deserialize(
            JsonParser parser,
            DeserializationContext context
        ) throws IOException {
            if (!parser.hasToken(JsonToken.VALUE_STRING)) {
                return (ProtocolTimestamp) context.handleUnexpectedToken(
                    ProtocolTimestamp.class,
                    parser
                );
            }
            try {
                return ProtocolTimestamp.parse(parser.getText());
            } catch (IllegalArgumentException exception) {
                return context.reportInputMismatch(
                    ProtocolTimestamp.class,
                    "Protocol timestamp does not satisfy model constraints"
                );
            }
        }
    }

    private static final class ConnectionSerializer extends StdSerializer<Connection> {
        private ConnectionSerializer() {
            super(Connection.class);
        }

        @Override
        public void serialize(
            Connection value,
            JsonGenerator generator,
            SerializerProvider provider
        ) throws IOException {
            ObjectMapper mapper = requireObjectMapper(generator);
            ProtocolHeader header = value.header();
            ObjectNode target = mapper.createObjectNode();
            target.put("headerId", header.headerId());
            target.set("timestamp", mapper.valueToTree(header.timestamp()));
            target.set("version", mapper.valueToTree(header.version()));
            target.put("manufacturer", header.robotIdentity().manufacturer());
            target.put("serialNumber", header.robotIdentity().serialNumber());
            target.put("connectionState", value.connectionState().name());
            ExtensionFieldsJacksonSupport.merge(
                mapper,
                target,
                value.extensionFields(),
                CONNECTION_FIELDS
            );
            generator.writeTree(target);
        }
    }

    private static final class ConnectionDeserializer
        extends StdDeserializer<Connection> {
        private ConnectionDeserializer() {
            super(Connection.class);
        }

        @Override
        public Connection deserialize(
            JsonParser parser,
            DeserializationContext context
        ) throws IOException {
            ObjectMapper mapper = requireObjectMapper(parser);
            JsonNode tree = mapper.readTree(parser);
            if (!tree.isObject()) {
                return context.reportInputMismatch(
                    Connection.class,
                    "Connection message must be a JSON object"
                );
            }
            ObjectNode object = (ObjectNode) tree;

            Long headerId = readRequired(object, "headerId", Long.class, context);
            ProtocolTimestamp timestamp = readRequired(
                object,
                "timestamp",
                ProtocolTimestamp.class,
                context
            );
            ProtocolVersion version = readRequired(
                object,
                "version",
                ProtocolVersion.class,
                context
            );
            String manufacturer = readRequired(
                object,
                "manufacturer",
                String.class,
                context
            );
            String serialNumber = readRequired(
                object,
                "serialNumber",
                String.class,
                context
            );
            ConnectionState connectionState = readRequired(
                object,
                "connectionState",
                ConnectionState.class,
                context
            );
            ExtensionFields extensionFields = ExtensionFieldsJacksonSupport.capture(
                mapper,
                object,
                CONNECTION_FIELDS
            );

            try {
                ProtocolHeader header = ProtocolHeader.builder()
                    .headerId(headerId)
                    .timestamp(timestamp)
                    .version(version)
                    .robotIdentity(new RobotIdentity(manufacturer, serialNumber))
                    .build();
                return Connection.builder()
                    .header(header)
                    .connectionState(connectionState)
                    .extensionFields(extensionFields)
                    .build();
            } catch (IllegalArgumentException | NullPointerException exception) {
                return context.reportInputMismatch(
                    Connection.class,
                    "Connection fields do not satisfy model constraints"
                );
            }
        }

        private static <T> T readRequired(
            ObjectNode object,
            String fieldName,
            Class<T> fieldType,
            DeserializationContext context
        ) throws IOException {
            JsonNode value = object.get(fieldName);
            if (value == null) {
                return context.reportInputMismatch(
                    Connection.class,
                    "Connection message is missing a required field"
                );
            }
            try {
                return context.readTreeAsValue(value, fieldType);
            } catch (JsonMappingException exception) {
                exception.prependPath(Connection.class, fieldName);
                throw exception;
            }
        }
    }

    private static ObjectMapper requireObjectMapper(JsonGenerator generator)
        throws IOException {
        if (generator.getCodec() instanceof ObjectMapper mapper) {
            return mapper;
        }
        throw new IOException("VDA 5050 Module requires an ObjectMapper codec");
    }

    private static ObjectMapper requireObjectMapper(JsonParser parser)
        throws IOException {
        if (parser.getCodec() instanceof ObjectMapper mapper) {
            return mapper;
        }
        throw new IOException("VDA 5050 Module requires an ObjectMapper codec");
    }
}
