package io.github.cooltheworld.rcs.protocol.vda5050.v3.codec;

/**
 * 在完整协议对象绑定前执行的不可变 JSON 资源上限。
 *
 * <p>所有上限都必须为正数，并在构造 Codec 时固定。Factsheet 限制的收紧逻辑由后续能力
 * 模块负责，不能借此类型放宽部署硬上限。</p>
 */
public final class JsonCodecLimits {
    private static final int DEFAULT_MAX_PAYLOAD_BYTES = 8 * 1024 * 1024;
    private static final int DEFAULT_MAX_NESTING_DEPTH = 64;
    private static final int DEFAULT_MAX_STRING_CHARACTERS = 256 * 1024;
    private static final int DEFAULT_MAX_NAME_CHARACTERS = 256;
    private static final int DEFAULT_MAX_NUMBER_CHARACTERS = 128;
    private static final int DEFAULT_MAX_ARRAY_ELEMENTS = 10_000;
    private static final int DEFAULT_MAX_OBJECT_PROPERTIES = 1_024;
    private static final long DEFAULT_MAX_TOKENS = 1_000_000L;
    private static final JsonCodecLimits DEFAULTS = builder().build();

    private final int maxPayloadBytes;
    private final int maxNestingDepth;
    private final int maxStringCharacters;
    private final int maxNameCharacters;
    private final int maxNumberCharacters;
    private final int maxArrayElements;
    private final int maxObjectProperties;
    private final long maxTokens;

    private JsonCodecLimits(Builder builder) {
        this.maxPayloadBytes = requirePositive(
            builder.maxPayloadBytes,
            "maxPayloadBytes"
        );
        this.maxNestingDepth = requirePositive(
            builder.maxNestingDepth,
            "maxNestingDepth"
        );
        this.maxStringCharacters = requirePositive(
            builder.maxStringCharacters,
            "maxStringCharacters"
        );
        this.maxNameCharacters = requirePositive(
            builder.maxNameCharacters,
            "maxNameCharacters"
        );
        this.maxNumberCharacters = requirePositive(
            builder.maxNumberCharacters,
            "maxNumberCharacters"
        );
        this.maxArrayElements = requirePositive(
            builder.maxArrayElements,
            "maxArrayElements"
        );
        this.maxObjectProperties = requirePositive(
            builder.maxObjectProperties,
            "maxObjectProperties"
        );
        this.maxTokens = requirePositive(builder.maxTokens, "maxTokens");
    }

    /** @return 规格确认的默认部署硬上限 */
    public static JsonCodecLimits defaults() {
        return DEFAULTS;
    }

    /** @return 以默认硬上限为初值的 Builder */
    public static Builder builder() {
        return new Builder();
    }

    /** @return 单条 UTF-8 payload 最大字节数 */
    public int maxPayloadBytes() {
        return maxPayloadBytes;
    }

    /** @return JSON 最大未闭合对象与数组层数 */
    public int maxNestingDepth() {
        return maxNestingDepth;
    }

    /** @return 单个字符串值最大字符数 */
    public int maxStringCharacters() {
        return maxStringCharacters;
    }

    /** @return 单个字段名最大字符数 */
    public int maxNameCharacters() {
        return maxNameCharacters;
    }

    /** @return 单个数值 Token 最大字符数 */
    public int maxNumberCharacters() {
        return maxNumberCharacters;
    }

    /** @return 单个数组最大元素数 */
    public int maxArrayElements() {
        return maxArrayElements;
    }

    /** @return 单个对象最大属性数 */
    public int maxObjectProperties() {
        return maxObjectProperties;
    }

    /** @return 单文档最大 JSON Token 数 */
    public long maxTokens() {
        return maxTokens;
    }

    private static int requirePositive(int value, String name) {
        if (value <= 0) {
            throw new IllegalArgumentException(name + " must be positive");
        }
        return value;
    }

    private static long requirePositive(long value, String name) {
        if (value <= 0L) {
            throw new IllegalArgumentException(name + " must be positive");
        }
        return value;
    }

    /** 用于在启动时构造固定 JSON 资源上限。 */
    public static final class Builder {
        private int maxPayloadBytes = DEFAULT_MAX_PAYLOAD_BYTES;
        private int maxNestingDepth = DEFAULT_MAX_NESTING_DEPTH;
        private int maxStringCharacters = DEFAULT_MAX_STRING_CHARACTERS;
        private int maxNameCharacters = DEFAULT_MAX_NAME_CHARACTERS;
        private int maxNumberCharacters = DEFAULT_MAX_NUMBER_CHARACTERS;
        private int maxArrayElements = DEFAULT_MAX_ARRAY_ELEMENTS;
        private int maxObjectProperties = DEFAULT_MAX_OBJECT_PROPERTIES;
        private long maxTokens = DEFAULT_MAX_TOKENS;

        private Builder() {}

        /** @param value payload 字节上限 @return 当前 Builder */
        public Builder maxPayloadBytes(int value) {
            this.maxPayloadBytes = value;
            return this;
        }

        /** @param value 嵌套深度上限 @return 当前 Builder */
        public Builder maxNestingDepth(int value) {
            this.maxNestingDepth = value;
            return this;
        }

        /** @param value 字符串字符数上限 @return 当前 Builder */
        public Builder maxStringCharacters(int value) {
            this.maxStringCharacters = value;
            return this;
        }

        /** @param value 字段名字符数上限 @return 当前 Builder */
        public Builder maxNameCharacters(int value) {
            this.maxNameCharacters = value;
            return this;
        }

        /** @param value 数值文本字符数上限 @return 当前 Builder */
        public Builder maxNumberCharacters(int value) {
            this.maxNumberCharacters = value;
            return this;
        }

        /** @param value 数组元素上限 @return 当前 Builder */
        public Builder maxArrayElements(int value) {
            this.maxArrayElements = value;
            return this;
        }

        /** @param value 对象属性上限 @return 当前 Builder */
        public Builder maxObjectProperties(int value) {
            this.maxObjectProperties = value;
            return this;
        }

        /** @param value 文档 Token 上限 @return 当前 Builder */
        public Builder maxTokens(long value) {
            this.maxTokens = value;
            return this;
        }

        /** @return 已校验且不可变的资源上限 */
        public JsonCodecLimits build() {
            return new JsonCodecLimits(this);
        }
    }
}
