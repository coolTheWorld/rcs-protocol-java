package io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

/**
 * 三维包络格式相关的内联 JSON 对象。
 *
 * <p>公共 API 只暴露值语义，不允许调用方按动态字段名读取或执行业务分派。非空值由
 * 有资源上限的协议 Codec 绑定。</p>
 */
public final class Envelope3dData {
    private static final byte[] EMPTY_JSON = "{}".getBytes(StandardCharsets.UTF_8);
    private static final Envelope3dData EMPTY = new Envelope3dData(
        EMPTY_JSON,
        EMPTY_JSON
    );

    private final byte[] canonicalJson;
    private final byte[] wireJson;

    private Envelope3dData(byte[] canonicalJson, byte[] wireJson) {
        this.canonicalJson = Objects.requireNonNull(
            canonicalJson,
            "canonicalJson"
        ).clone();
        this.wireJson = Objects.requireNonNull(wireJson, "wireJson").clone();
    }

    /** @return 可用于表达线路上空 JSON 对象的值 */
    public static Envelope3dData empty() {
        return EMPTY;
    }

    public boolean isEmpty() {
        return Arrays.equals(canonicalJson, EMPTY_JSON);
    }

    @Override
    public boolean equals(Object other) {
        return this == other
            || other instanceof Envelope3dData that
            && Arrays.equals(canonicalJson, that.canonicalJson);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(canonicalJson);
    }

    private static Envelope3dData fromJsonBytes(
        byte[] canonicalJson,
        byte[] wireJson
    ) {
        Objects.requireNonNull(canonicalJson, "canonicalJson");
        Objects.requireNonNull(wireJson, "wireJson");
        return Arrays.equals(canonicalJson, EMPTY_JSON)
            ? EMPTY
            : new Envelope3dData(canonicalJson, wireJson);
    }

    private byte[] toJsonBytes() {
        return wireJson.clone();
    }
}
