package io.github.cooltheworld.rcs.protocol.vda5050.v3.validation;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.codec.JsonCodecLimits;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.topic.TopicName;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.internal.NetworkntVda5050SchemaValidator;
import java.util.List;
import java.util.Objects;

/** 对资源受限 UTF-8 payload 执行 VDA 5050 Draft 2020-12 Schema 校验。 */
public interface Vda5050SchemaValidator {
    /** @return 使用默认 JSON 资源硬上限的 Schema Validator */
    static Vda5050SchemaValidator createDefault() {
        return create(JsonCodecLimits.defaults());
    }

    /**
     * @param limits 在 Schema 解析前执行的 JSON 资源硬上限
     * @return 可缓存复用且线程安全的 Schema Validator
     */
    static Vda5050SchemaValidator create(JsonCodecLimits limits) {
        return new NetworkntVda5050SchemaValidator(
            Objects.requireNonNull(limits, "limits")
        );
    }

    /**
     * 校验不可信 UTF-8 payload。语法、资源与 Schema 失败均以结构化问题返回。
     *
     * @param topic 已识别的标准 Topic
     * @param payload 不可信 UTF-8 JSON 字节
     * @return 不可变问题列表；通过时为空
     */
    List<ValidationIssue> validate(TopicName topic, byte[] payload);
}
