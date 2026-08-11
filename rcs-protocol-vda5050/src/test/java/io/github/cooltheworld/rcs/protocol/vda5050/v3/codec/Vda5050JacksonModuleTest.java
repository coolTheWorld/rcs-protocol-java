package io.github.cooltheworld.rcs.protocol.vda5050.v3.codec;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.DefaultDeserializationContext;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.ExtensionFields;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.action.ActionParameterDefinition;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.action.ActionValueDataType;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.common.ProtocolTimestamp;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.common.ProtocolVersion;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.common.ProtocolHeader;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.common.RobotIdentity;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.connection.Connection;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.connection.ConnectionState;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.Envelope3d;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.Envelope3dData;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.TypeSpecification;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.WheelType;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

final class Vda5050JacksonModuleTest {
    private static final ObjectMapper MAPPER = JsonMapper.builder()
        .addModule(new Vda5050JacksonModule())
        .build();
    private static final ProtocolVersion VERSION = ProtocolVersion.parse("3.0.0");
    private static final ProtocolTimestamp TIMESTAMP = ProtocolTimestamp.from(
        Instant.parse("2026-08-07T05:00:00.123Z")
    );

    @Test
    @DisplayName("[VDA3-SHARED-010] Module 注册协议值类型但不修改调用方 null 策略")
    void registersProtocolValuesWithoutChangingCallerPolicy() throws Exception {
        ModuleProbe probe = new ModuleProbe(VERSION, TIMESTAMP, null);

        byte[] json = MAPPER.writeValueAsBytes(probe);
        JsonNode tree = MAPPER.readTree(json);
        ModuleProbe decoded = MAPPER.readValue(json, ModuleProbe.class);

        assertAll(
            () -> assertEquals("3.0.0", tree.path("version").textValue()),
            () -> assertEquals(
                "2026-08-07T05:00:00.123Z",
                tree.path("timestamp").textValue()
            ),
            () -> assertTrue(tree.has("optional")),
            () -> assertTrue(tree.path("optional").isNull()),
            () -> assertEquals(probe, decoded)
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-010] Module 直接调用时结构化拒绝错误 Token 和缺失字段")
    void rejectsInvalidDirectJacksonInputs() {
        assertAll(
            () -> assertThrows(
                JsonMappingException.class,
                () -> MAPPER.readValue("1", ProtocolVersion.class)
            ),
            () -> assertThrows(
                JsonMappingException.class,
                () -> MAPPER.readValue("1", ProtocolTimestamp.class)
            ),
            () -> assertThrows(
                JsonMappingException.class,
                () -> deserializeWithPrivateDeserializer(
                    "io.github.cooltheworld.rcs.protocol.vda5050.v3.codec."
                        + "Vda5050JacksonModule$ConnectionDeserializer",
                    "[]"
                )
            ),
            () -> assertThrows(
                JsonMappingException.class,
                () -> MAPPER.readValue("{}", Connection.class)
            ),
            () -> assertThrows(
                JsonMappingException.class,
                () -> readFactsheetObject("[]")
            ),
            () -> assertThrows(
                JsonMappingException.class,
                () -> MAPPER.readValue("1", WheelType.class)
            ),
            () -> assertThrows(
                JsonMappingException.class,
                () -> deserializeWithPrivateDeserializer(
                    "io.github.cooltheworld.rcs.protocol.vda5050.v3.codec."
                        + "MobileRobotGeometryJacksonSupport$"
                        + "Envelope3dDataDeserializer",
                    "[]"
                )
            ),
            () -> assertThrows(
                JsonMappingException.class,
                () -> deserializeWithPrivateDeserializer(
                    "io.github.cooltheworld.rcs.protocol.vda5050.v3.extension."
                        + "internal.ExtensionFieldsJacksonSupport$Deserializer",
                    "[]"
                )
            )
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-010] Module 省略缺失的标准可选字段")
    void omitsMissingOptionalFields() {
        ActionParameterDefinition parameter = ActionParameterDefinition.builder()
            .key("height")
            .valueDataType(ActionValueDataType.NUMBER)
            .build();
        Envelope3d envelope = Envelope3d.builder()
            .envelope3dId("body")
            .format("gltf")
            .build();
        JsonNode parameterTree = MAPPER.valueToTree(parameter);
        JsonNode envelopeTree = MAPPER.valueToTree(envelope);

        assertAll(
            () -> assertFalse(parameterTree.has("isOptional")),
            () -> assertFalse(envelopeTree.has("data"))
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-010] Module 防御未绑定 ObjectMapper 的底层调用")
    void requiresObjectMapperCodecForInternalTreeBindings() throws Exception {
        ExtensionFields emptyExtensions = MAPPER.readValue(
            "{}",
            ExtensionFields.class
        );
        ExtensionFields directlyDeserializedExtensions = (ExtensionFields)
            deserializeWithPrivateDeserializer(
                "io.github.cooltheworld.rcs.protocol.vda5050.v3.extension."
                    + "internal.ExtensionFieldsJacksonSupport$Deserializer",
                "{}"
            );
        byte[] serializedExtensions = MAPPER.writeValueAsBytes(emptyExtensions);
        Connection connection = Connection.builder()
            .header(ProtocolHeader.builder()
                .headerId(0L)
                .timestamp(TIMESTAMP)
                .version(VERSION)
                .robotIdentity(new RobotIdentity("Acme", "R-001"))
                .build())
            .connectionState(ConnectionState.ONLINE)
            .build();

        assertAll(
            () -> assertTrue(emptyExtensions.isEmpty()),
            () -> assertTrue(directlyDeserializedExtensions.isEmpty()),
            () -> assertEquals(
                "{}",
                new String(serializedExtensions, StandardCharsets.UTF_8)
            ),
            () -> assertThrows(
                IOException.class,
                () -> serializeWithoutMapper(connection, Connection.class)
            ),
            () -> assertThrows(
                IOException.class,
                () -> deserializeWithoutMapper("{}", Connection.class)
            ),
            () -> assertThrows(
                IOException.class,
                () -> serializeWithoutMapper(
                    ExtensionFields.empty(),
                    ExtensionFields.class
                )
            ),
            () -> assertThrows(
                IOException.class,
                () -> deserializeWithoutMapper("{}", ExtensionFields.class)
            ),
            () -> assertThrows(
                IOException.class,
                Vda5050JacksonModuleTest::requireFactsheetGeneratorMapper
            ),
            () -> assertThrows(
                IOException.class,
                Vda5050JacksonModuleTest::requireFactsheetParserMapper
            )
        );
    }

    private static <T> void serializeWithoutMapper(T value, Class<T> type)
        throws IOException {
        SerializerProvider provider = MAPPER.getSerializerProviderInstance();
        JsonSerializer<Object> serializer = provider.findValueSerializer(type);
        try (
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            JsonGenerator generator = new JsonFactory().createGenerator(output)
        ) {
            serializer.serialize(value, generator, provider);
        }
    }

    private static void deserializeWithoutMapper(String json, Class<?> type)
        throws IOException {
        try (JsonParser parser = new JsonFactory().createParser(json)) {
            parser.nextToken();
            DefaultDeserializationContext context =
                ((DefaultDeserializationContext) MAPPER.getDeserializationContext())
                    .createInstance(
                        MAPPER.getDeserializationConfig(),
                        parser,
                        MAPPER.getInjectableValues()
                    );
            JsonDeserializer<Object> deserializer = context
                .findRootValueDeserializer(MAPPER.constructType(type));
            deserializer.deserialize(parser, context);
        }
    }

    @SuppressWarnings("unchecked")
    private static Object deserializeWithPrivateDeserializer(
        String deserializerClassName,
        String json
    ) throws Exception {
        Class<?> deserializerClass = Class.forName(deserializerClassName);
        var constructor = deserializerClass.getDeclaredConstructor();
        constructor.setAccessible(true);
        JsonDeserializer<Object> deserializer = (JsonDeserializer<Object>)
            constructor.newInstance();
        try (JsonParser parser = MAPPER.createParser(json)) {
            parser.nextToken();
            DefaultDeserializationContext context =
                ((DefaultDeserializationContext) MAPPER.getDeserializationContext())
                    .createInstance(
                        MAPPER.getDeserializationConfig(),
                        parser,
                        MAPPER.getInjectableValues()
                    );
            return deserializer.deserialize(parser, context);
        }
    }

    private static void readFactsheetObject(String json) throws IOException {
        try (JsonParser parser = MAPPER.createParser(json)) {
            parser.nextToken();
            DefaultDeserializationContext context =
                ((DefaultDeserializationContext) MAPPER.getDeserializationContext())
                    .createInstance(
                        MAPPER.getDeserializationConfig(),
                        parser,
                        MAPPER.getInjectableValues()
                    );
            FactsheetFragmentJacksonSupport.readObject(
                MAPPER,
                parser,
                context,
                TypeSpecification.class,
                "TypeSpecification"
            );
        }
    }

    private static void requireFactsheetGeneratorMapper() throws IOException {
        try (
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            JsonGenerator generator = new JsonFactory().createGenerator(output)
        ) {
            FactsheetFragmentJacksonSupport.requireObjectMapper(generator);
        }
    }

    private static void requireFactsheetParserMapper() throws IOException {
        try (JsonParser parser = new JsonFactory().createParser("{}")) {
            parser.nextToken();
            FactsheetFragmentJacksonSupport.requireObjectMapper(parser);
        }
    }

    private record ModuleProbe(
        ProtocolVersion version,
        ProtocolTimestamp timestamp,
        String optional
    ) {}
}
