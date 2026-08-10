package io.github.cooltheworld.rcs.protocol.vda5050.v3.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Objects;

/**
 * 三维包络格式相关的内联 JSON 对象。
 *
 * <p>公共 API 只暴露值语义，不允许调用方按动态字段名读取或执行业务分派。非空值由
 * 有资源上限的协议 Codec 绑定。</p>
 */
public final class Envelope3dData {
    private static final Envelope3dData EMPTY = new Envelope3dData(
        JsonNodeFactory.instance.objectNode()
    );

    private final ObjectNode value;

    private Envelope3dData(ObjectNode value) {
        this.value = Objects.requireNonNull(value, "value").deepCopy();
    }

    /** @return 可用于表达线路上空 JSON 对象的值 */
    public static Envelope3dData empty() {
        return EMPTY;
    }

    public boolean isEmpty() {
        return value.isEmpty();
    }

    @Override
    public boolean equals(Object other) {
        return this == other
            || other instanceof Envelope3dData that && value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    private static Envelope3dData fromJson(ObjectNode value) {
        Objects.requireNonNull(value, "value");
        return value.isEmpty() ? EMPTY : new Envelope3dData(value);
    }

    @JsonValue
    private ObjectNode toJson() {
        return value.deepCopy();
    }
}
