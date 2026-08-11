package io.github.cooltheworld.rcs.protocol.vda5050.v3.validation;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.PhysicalParameters;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** 对 Factsheet {@code physicalParameters} 执行上下文无关语义校验。 */
public final class PhysicalParametersValidator {
    private static final String REQUIREMENT_ID = "VDA3-FACTSHEET-007";
    private static final String BASE_PATH = "/physicalParameters";

    private PhysicalParametersValidator() {}

    /** @return 可缓存复用且线程安全的物理参数 Validator */
    public static PhysicalParametersValidator create() {
        return new PhysicalParametersValidator();
    }

    /**
     * 校验有限数、Schema 明示的非负约束以及成对数值关系。
     *
     * @param parameters 已完成强类型绑定的物理参数片段
     * @return 按字段顺序排列的不可变问题列表
     */
    public List<ValidationIssue> validate(PhysicalParameters parameters) {
        Objects.requireNonNull(parameters, "parameters");
        List<ValidationIssue> issues = new ArrayList<>();
        number(parameters.minimumSpeed(), "/minimumSpeed", true, issues);
        number(parameters.maximumSpeed(), "/maximumSpeed", true, issues);
        number(
            parameters.minimumAngularSpeed(),
            "/minimumAngularSpeed",
            true,
            issues
        );
        number(
            parameters.maximumAngularSpeed(),
            "/maximumAngularSpeed",
            true,
            issues
        );
        number(
            parameters.maximumAcceleration(),
            "/maximumAcceleration",
            true,
            issues
        );
        number(
            parameters.maximumDeceleration(),
            "/maximumDeceleration",
            false,
            issues
        );
        number(parameters.minimumHeight(), "/minimumHeight", false, issues);
        number(parameters.maximumHeight(), "/maximumHeight", false, issues);
        number(parameters.width(), "/width", false, issues);
        number(parameters.length(), "/length", false, issues);
        range(
            parameters.minimumSpeed(),
            parameters.maximumSpeed(),
            "INVALID_LINEAR_SPEED_RANGE",
            "/maximumSpeed",
            "Minimum speed must not exceed maximum speed",
            issues
        );
        range(
            parameters.minimumAngularSpeed(),
            parameters.maximumAngularSpeed(),
            "INVALID_ANGULAR_SPEED_RANGE",
            "/maximumAngularSpeed",
            "Minimum angular speed must not exceed maximum angular speed",
            issues
        );
        range(
            parameters.minimumHeight(),
            parameters.maximumHeight(),
            "INVALID_HEIGHT_RANGE",
            "/maximumHeight",
            "Minimum height must not exceed maximum height",
            issues
        );
        return List.copyOf(issues);
    }

    private static void number(
        Double value,
        String relativePath,
        boolean nonNegative,
        List<ValidationIssue> issues
    ) {
        if (value == null) {
            return;
        }
        if (!Double.isFinite(value)) {
            issues.add(issue(
                "NON_FINITE_PHYSICAL_PARAMETER",
                relativePath,
                "Physical parameter must be finite"
            ));
            return;
        }
        if (nonNegative && value < 0.0D) {
            issues.add(issue(
                "NEGATIVE_PHYSICAL_PARAMETER",
                relativePath,
                "Schema-declared physical parameter must not be negative"
            ));
        }
    }

    private static void range(
        Double minimum,
        Double maximum,
        String code,
        String maximumPath,
        String description,
        List<ValidationIssue> issues
    ) {
        if (minimum == null) {
            return;
        }
        if (maximum == null) {
            return;
        }
        if (!Double.isFinite(minimum)) {
            return;
        }
        if (!Double.isFinite(maximum)) {
            return;
        }
        if (minimum <= maximum) {
            return;
        }
        issues.add(issue(code, maximumPath, description));
    }

    private static ValidationIssue issue(
        String code,
        String relativePath,
        String description
    ) {
        return new ValidationIssue(
            code,
            ValidationSeverity.ERROR,
            BASE_PATH + relativePath,
            description,
            REQUIREMENT_ID
        );
    }
}
