package io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.internal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.ExtensionFields;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Jackson Codec 用于捕获和安全回写不透明扩展字段的内部支持。 */
public final class ExtensionFieldsJacksonSupport {
    private ExtensionFieldsJacksonSupport() {}

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
        return mapper.treeToValue(captured, ExtensionFields.class);
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
        ObjectNode extensionObject = mapper.valueToTree(extensionFields);

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
}
