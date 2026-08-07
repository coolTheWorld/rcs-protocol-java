package io.github.cooltheworld.rcs.protocol.vda5050.v3.validation;

import java.util.List;

/**
 * JSON 语法与基础类型解码的封闭结果。
 *
 * <p>成功分支仍未经过 Schema 和协议语义校验，不能替代
 * {@link ValidatedMessage}。</p>
 *
 * @param <T> 目标强类型消息
 */
public sealed interface DecodingResult<T>
    permits DecodedMessage, RejectedInboundMessage {
    /** @return 已完成基础解码时返回 {@code true} */
    boolean isDecoded();

    /** @return 不可变问题列表；成功分支为空 */
    List<ValidationIssue> issues();
}
