package io.github.cooltheworld.rcs.protocol.vda5050.v3.extension;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Objects;

/**
 * 不透明保存当前协议版本未知的 JSON 扩展字段。
 *
 * <p>公共 API 只提供空值和值语义，不允许调用方按字段名动态读取扩展内容。扩展的
 * Jackson 表示由协议内部支持代码使用，并在边界处执行深拷贝。</p>
 */
public final class ExtensionFields {
    private static final ExtensionFields EMPTY = new ExtensionFields(
        JsonNodeFactory.instance.objectNode()
    );

    private final ObjectNode fields;

    private ExtensionFields(ObjectNode fields) {
        this.fields = Objects.requireNonNull(fields, "fields").deepCopy();
    }

    /**
     * 返回不含扩展字段的值。
     *
     * @return 空扩展字段
     */
    public static ExtensionFields empty() {
        return EMPTY;
    }

    /** @return 不含任何扩展字段时返回 {@code true} */
    public boolean isEmpty() {
        return fields.isEmpty();
    }

    @Override
    public boolean equals(Object other) {
        return this == other
            || other instanceof ExtensionFields that && fields.equals(that.fields);
    }

    @Override
    public int hashCode() {
        return fields.hashCode();
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    private static ExtensionFields fromJson(ObjectNode fields) {
        Objects.requireNonNull(fields, "fields");
        return fields.isEmpty() ? EMPTY : new ExtensionFields(fields);
    }

    @JsonValue
    private ObjectNode toJson() {
        return fields.deepCopy();
    }
}
