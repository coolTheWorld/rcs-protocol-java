package io.github.cooltheworld.rcs.protocol.vda5050.v3.validation;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 可由调用方和状态机处理的单个结构化校验问题。
 *
 * @param code 稳定的全大写错误或警告代码
 * @param severity 严重级别
 * @param path 问题字段路径；空字符串表示消息根
 * @param description 不包含原始 payload 的安全说明
 * @param requirementId 一致性清单中的稳定需求标识
 */
public record ValidationIssue(
    String code,
    ValidationSeverity severity,
    String path,
    String description,
    String requirementId
) {
    private static final Pattern CODE_PATTERN = Pattern.compile(
        "[A-Z][A-Z0-9]*(?:_[A-Z0-9]+)*"
    );
    private static final Pattern REQUIREMENT_ID_PATTERN = Pattern.compile(
        "VDA3-(?:SHARED|CONNECTION|FACTSHEET|INSTANT_ACTIONS|ORDER|RESPONSES|STATE|"
            + "VISUALIZATION|ZONE_SET)-[0-9]{3}"
    );

    /** 验证结构化问题自身的不变量。 */
    public ValidationIssue {
        code = requireMatch(code, "code", CODE_PATTERN);
        severity = Objects.requireNonNull(severity, "severity");
        path = Objects.requireNonNull(path, "path");
        description = requireNonBlank(description, "description");
        requirementId = requireMatch(
            requirementId,
            "requirementId",
            REQUIREMENT_ID_PATTERN
        );
    }

    private static String requireNonBlank(String value, String name) {
        Objects.requireNonNull(value, name);
        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }

    private static String requireMatch(String value, String name, Pattern pattern) {
        requireNonBlank(value, name);
        if (!pattern.matcher(value).matches()) {
            throw new IllegalArgumentException(name + " has an invalid format");
        }
        return value;
    }
}
