package io.github.cooltheworld.rcs.protocol.vda5050.v3.extension;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

/**
 * 不透明保存当前协议版本未知的 JSON 扩展字段。
 *
 * <p>公共 API 只提供空值和值语义，不允许调用方按字段名动态读取扩展内容。扩展的
 * JSON 表示由协议 Codec 内部支持代码使用，并在边界处执行防御性复制。</p>
 */
public final class ExtensionFields {
    private static final byte[] EMPTY_JSON = "{}".getBytes(StandardCharsets.UTF_8);
    private static final ExtensionFields EMPTY = new ExtensionFields(
        EMPTY_JSON,
        EMPTY_JSON
    );

    private final byte[] canonicalJson;
    private final byte[] wireJson;

    private ExtensionFields(byte[] canonicalJson, byte[] wireJson) {
        this.canonicalJson = Objects.requireNonNull(
            canonicalJson,
            "canonicalJson"
        ).clone();
        this.wireJson = Objects.requireNonNull(wireJson, "wireJson").clone();
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
        return Arrays.equals(canonicalJson, EMPTY_JSON);
    }

    @Override
    public boolean equals(Object other) {
        return this == other
            || other instanceof ExtensionFields that
            && Arrays.equals(canonicalJson, that.canonicalJson);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(canonicalJson);
    }

    private static ExtensionFields fromJsonBytes(
        byte[] canonicalJson,
        byte[] wireJson
    ) {
        Objects.requireNonNull(canonicalJson, "canonicalJson");
        Objects.requireNonNull(wireJson, "wireJson");
        return Arrays.equals(canonicalJson, EMPTY_JSON)
            ? EMPTY
            : new ExtensionFields(canonicalJson, wireJson);
    }

    private byte[] toJsonBytes() {
        return wireJson.clone();
    }
}
