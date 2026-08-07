package io.github.cooltheworld.rcs.protocol.vda5050.v3.codec;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.codec.internal.JacksonVda5050JsonCodec;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.topic.TopicName;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.DecodingResult;
import java.util.Objects;

/** 有资源上限的确定性 UTF-8 VDA 5050 JSON 编解码边界。 */
public interface Vda5050JsonCodec {
    /** @return 使用规格默认部署硬上限的 Codec */
    static Vda5050JsonCodec createDefault() {
        return create(JsonCodecLimits.defaults());
    }

    /**
     * @param limits 启动时固定的部署硬上限
     * @return 独立且线程安全的 Codec
     */
    static Vda5050JsonCodec create(JsonCodecLimits limits) {
        return new JacksonVda5050JsonCodec(Objects.requireNonNull(limits, "limits"));
    }

    /**
     * 解码不可信 UTF-8 payload。普通语法、类型和资源错误作为拒绝数据返回。
     *
     * @param topic 已识别的标准 Topic
     * @param payload UTF-8 JSON 字节
     * @param messageType 目标强类型消息
     * @param <T> 目标消息类型
     * @return 解码成功或结构化拒绝结果
     */
    <T> DecodingResult<T> decode(
        TopicName topic,
        byte[] payload,
        Class<T> messageType
    );

    /**
     * 编码强类型协议对象为紧凑 UTF-8 JSON。
     *
     * @param message 待编码消息
     * @return UTF-8 JSON 字节
     * @throws IllegalArgumentException 消息不能由协议 Module 编码时
     */
    byte[] encode(Object message);
}
