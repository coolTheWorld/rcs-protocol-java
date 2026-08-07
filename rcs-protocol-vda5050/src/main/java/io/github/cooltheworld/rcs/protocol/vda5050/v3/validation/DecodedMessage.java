package io.github.cooltheworld.rcs.protocol.vda5050.v3.validation;

import java.util.List;
import java.util.Objects;

/**
 * 只完成 JSON 语法与基础类型解码、尚未获得校验凭证的强类型消息。
 *
 * @param message 已解码消息
 * @param <T> 强类型消息类型
 */
public record DecodedMessage<T>(T message) implements DecodingResult<T> {
    /** 验证已解码消息非空。 */
    public DecodedMessage {
        message = Objects.requireNonNull(message, "message");
    }

    @Override
    public boolean isDecoded() {
        return true;
    }

    @Override
    public List<ValidationIssue> issues() {
        return List.of();
    }
}
