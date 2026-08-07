package io.github.cooltheworld.rcs.protocol.vda5050.v3.validation;

import java.util.List;

/**
 * 前三层入站校验的封闭结果边界。
 *
 * <p>成功结果是不可由公共 API 构造的 {@link ValidatedMessage}；失败结果是只包含安全上下文的
 * {@link RejectedInboundMessage}。第四层会话语义仍由角色状态机处理。</p>
 *
 * @param <T> 强类型协议消息类型
 */
public sealed interface ValidationResult<T>
    permits ValidatedMessage, RejectedInboundMessage {
    /**
     * 判断前三层校验是否已产生成功凭证。
     *
     * @return 成功分支返回 {@code true}
     */
    boolean isAccepted();

    /**
     * 返回不可变的结构化问题列表。
     *
     * @return 成功分支可包含警告，失败分支至少包含一个错误
     */
    List<ValidationIssue> issues();
}
