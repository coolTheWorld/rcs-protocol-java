package io.github.cooltheworld.rcs.protocol.vda5050.v3.validation;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.ProtocolVersionProfile;
import java.util.List;
import java.util.Objects;

/**
 * 消息已通过 JSON、Schema 和上下文无关协议语义校验的成功凭证。
 *
 * <p>构造入口仅对核心校验包可见。调用方可以消费凭证，但不能通过公共 API 把裸消息包装成
 * 已校验消息。</p>
 *
 * @param <T> 强类型协议消息类型
 */
public final class ValidatedMessage<T> implements ValidationResult<T> {
    private final T message;
    private final ProtocolVersionProfile versionProfile;
    private final List<ValidationIssue> issues;

    ValidatedMessage(
        T message,
        ProtocolVersionProfile versionProfile,
        List<ValidationIssue> issues
    ) {
        this.message = Objects.requireNonNull(message, "message");
        this.versionProfile = Objects.requireNonNull(
            versionProfile,
            "versionProfile"
        );
        this.issues = List.copyOf(Objects.requireNonNull(issues, "issues"));
        if (this.issues.stream().anyMatch(ValidatedMessage::isError)) {
            throw new IllegalArgumentException(
                "Validated message issues must not contain errors"
            );
        }
    }

    /** @return 已通过前三层校验的原始强类型消息 */
    public T message() {
        return message;
    }

    /** @return 校验消息时使用的显式协议版本配置 */
    public ProtocolVersionProfile versionProfile() {
        return versionProfile;
    }

    @Override
    public boolean isAccepted() {
        return true;
    }

    @Override
    public List<ValidationIssue> issues() {
        return issues;
    }

    private static boolean isError(ValidationIssue issue) {
        return issue.severity() == ValidationSeverity.ERROR;
    }
}
