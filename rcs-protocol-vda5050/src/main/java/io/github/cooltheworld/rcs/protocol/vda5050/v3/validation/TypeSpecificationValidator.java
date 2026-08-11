package io.github.cooltheworld.rcs.protocol.vda5050.v3.validation;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.TypeSpecification;
import java.util.List;
import java.util.Objects;

/** 对 Factsheet {@code typeSpecification} 执行上下文无关语义校验。 */
public final class TypeSpecificationValidator {
    private static final String REQUIREMENT_ID = "VDA3-FACTSHEET-006";
    private static final String MAXIMUM_LOAD_MASS_PATH =
        "/typeSpecification/maximumLoadMass";

    private TypeSpecificationValidator() {}

    /** @return 可缓存复用且线程安全的类型说明 Validator */
    public static TypeSpecificationValidator create() {
        return new TypeSpecificationValidator();
    }

    /**
     * 校验最大载荷质量为有限非负数。
     *
     * @param specification 已完成强类型绑定的类型说明片段
     * @return 不可变问题列表
     */
    public List<ValidationIssue> validate(TypeSpecification specification) {
        Objects.requireNonNull(specification, "specification");
        double maximumLoadMass = specification.maximumLoadMass();
        if (!Double.isFinite(maximumLoadMass)) {
            return List.of(issue(
                "NON_FINITE_MAXIMUM_LOAD_MASS",
                "Maximum load mass must be finite"
            ));
        }
        if (maximumLoadMass < 0.0D) {
            return List.of(issue(
                "NEGATIVE_MAXIMUM_LOAD_MASS",
                "Maximum load mass must not be negative"
            ));
        }
        return List.of();
    }

    private static ValidationIssue issue(String code, String description) {
        return new ValidationIssue(
            code,
            ValidationSeverity.ERROR,
            MAXIMUM_LOAD_MASS_PATH,
            description,
            REQUIREMENT_ID
        );
    }
}
