package io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.internal;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.codec.internal.OpaqueJsonJacksonAccess;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.ExtensionFields;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Jackson Codec 用于捕获和安全回写不透明扩展字段的内部支持。 */
public final class ExtensionFieldsJacksonSupport {
    private ExtensionFieldsJacksonSupport() {}

    /** 把不透明扩展值的线路表示注册到协议 Jackson Module。 */
    public static void register(SimpleModule module) {
        Objects.requireNonNull(module, "module");
        module.addSerializer(ExtensionFields.class, new Serializer());
        module.addDeserializer(ExtensionFields.class, new Deserializer());
    }

    /**
     * 从消息对象捕获不属于标准字段集合的属性，不修改输入树。
     *
     * @param mapper 当前协议 Codec 使用的 ObjectMapper
     * @param source 已解析的消息对象
     * @param standardFieldNames 当前消息类型的标准字段名
     * @return 深拷贝后的不透明扩展字段
     * @throws JsonProcessingException Jackson 无法构造扩展值时
     */
    public static ExtensionFields capture(
        ObjectMapper mapper,
        ObjectNode source,
        Set<String> standardFieldNames
    ) throws JsonProcessingException {
        Objects.requireNonNull(mapper, "mapper");
        Objects.requireNonNull(source, "source");
        Set<String> standardNames = Set.copyOf(
            Objects.requireNonNull(standardFieldNames, "standardFieldNames")
        );
        ObjectNode captured = mapper.createObjectNode();
        for (Map.Entry<String, JsonNode> property : source.properties()) {
            if (!standardNames.contains(property.getKey())) {
                captured.set(property.getKey(), property.getValue().deepCopy());
            }
        }
        return OpaqueJsonJacksonAccess.extensionFields(mapper, captured);
    }

    /**
     * 把扩展字段合并到目标对象；发现任一保留名或已有属性冲突时保持目标不变并失败。
     *
     * @param mapper 当前协议 Codec 使用的 ObjectMapper
     * @param target 已写入标准字段的目标消息对象
     * @param extensionFields 待回写的不透明扩展字段
     * @param standardFieldNames 当前消息类型的标准字段名
     */
    public static void merge(
        ObjectMapper mapper,
        ObjectNode target,
        ExtensionFields extensionFields,
        Set<String> standardFieldNames
    ) {
        Objects.requireNonNull(mapper, "mapper");
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(extensionFields, "extensionFields");
        Set<String> standardNames = Set.copyOf(
            Objects.requireNonNull(standardFieldNames, "standardFieldNames")
        );
        ObjectNode extensionObject = OpaqueJsonJacksonAccess.object(
            mapper,
            extensionFields
        );

        for (Map.Entry<String, JsonNode> property : extensionObject.properties()) {
            if (standardNames.contains(property.getKey()) || target.has(property.getKey())) {
                throw new IllegalArgumentException(
                    "Extension field conflicts with a reserved or existing field"
                );
            }
        }
        for (Map.Entry<String, JsonNode> property : extensionObject.properties()) {
            target.set(property.getKey(), property.getValue().deepCopy());
        }
    }

    private static final class Serializer
        extends StdSerializer<ExtensionFields> {
        private Serializer() {
            super(ExtensionFields.class);
        }

        @Override
        public void serialize(
            ExtensionFields value,
            JsonGenerator generator,
            SerializerProvider provider
        ) throws IOException {
            generator.writeTree(OpaqueJsonJacksonAccess.object(
                requireObjectMapper(generator),
                value
            ));
        }
    }

    private static final class Deserializer
        extends StdDeserializer<ExtensionFields> {
        private Deserializer() {
            super(ExtensionFields.class);
        }

        @Override
        public ExtensionFields deserialize(
            JsonParser parser,
            DeserializationContext context
        ) throws IOException {
            ObjectMapper mapper = requireObjectMapper(parser);
            JsonNode value = mapper.readTree(parser);
            if (!value.isObject()) {
                return context.reportInputMismatch(
                    ExtensionFields.class,
                    "Extension fields must be a JSON object"
                );
            }
            ObjectNode object = (ObjectNode) value;
            return OpaqueJsonJacksonAccess.extensionFields(mapper, object);
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
