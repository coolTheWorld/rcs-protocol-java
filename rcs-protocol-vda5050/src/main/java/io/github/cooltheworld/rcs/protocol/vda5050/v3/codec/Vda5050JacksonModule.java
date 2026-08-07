package io.github.cooltheworld.rcs.protocol.vda5050.v3.codec;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.ProtocolTimestamp;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.ProtocolVersion;
import java.io.IOException;

/**
 * 可显式注册到调用方 Jackson 2 {@code ObjectMapper} 的 VDA 5050 v3 Module。
 *
 * <p>该 Module 只注册协议类型的线路表示，不修改调用方的 null、未知字段、资源限制或多态
 * 配置。需要完整默认安全配置时应使用 {@link Vda5050JsonCodec}。</p>
 */
public final class Vda5050JacksonModule extends SimpleModule {
    /** 创建只包含协议类型映射的 Module。 */
    public Vda5050JacksonModule() {
        super("Vda5050JacksonModule");
        addSerializer(ProtocolVersion.class, new ProtocolVersionSerializer());
        addDeserializer(ProtocolVersion.class, new ProtocolVersionDeserializer());
        addSerializer(ProtocolTimestamp.class, new ProtocolTimestampSerializer());
        addDeserializer(ProtocolTimestamp.class, new ProtocolTimestampDeserializer());
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
            return ProtocolVersion.parse(parser.getText());
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
            return ProtocolTimestamp.parse(parser.getText());
        }
    }
}
