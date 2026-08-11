package io.github.cooltheworld.rcs.protocol.vda5050.v3.validation;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.PhysicalParameters;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

final class PhysicalParametersValidatorTest {
    private static final PhysicalParametersValidator VALIDATOR =
        PhysicalParametersValidator.create();

    @Test
    @DisplayName("[VDA3-FACTSHEET-007] 接受合法边界并保持输入不变")
    void acceptsValidBoundariesAndDoesNotMutateInput() {
        PhysicalParameters parameters = parameters(
            0.0D,
            Double.MAX_VALUE,
            null,
            null,
            -0.0D,
            -1.0D,
            -5.0D,
            -4.0D,
            -2.0D,
            -3.0D
        );
        PhysicalParameters snapshot = parameters(
            0.0D,
            Double.MAX_VALUE,
            null,
            null,
            -0.0D,
            -1.0D,
            -5.0D,
            -4.0D,
            -2.0D,
            -3.0D
        );

        List<ValidationIssue> issues = VALIDATOR.validate(parameters);

        assertAll(
            () -> assertEquals(List.of(), issues),
            () -> assertEquals(issues, VALIDATOR.validate(parameters)),
            () -> assertEquals(snapshot, parameters),
            () -> assertThrows(UnsupportedOperationException.class, issues::clear),
            () -> assertThrows(
                NullPointerException.class,
                () -> VALIDATOR.validate(null)
            )
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-007] 按字段顺序报告全部非有限物理参数")
    void rejectsEveryNonFinitePhysicalParameter() {
        List<ValidationIssue> issues = VALIDATOR.validate(parameters(
            Double.NaN,
            Double.POSITIVE_INFINITY,
            Double.NEGATIVE_INFINITY,
            Double.NaN,
            Double.POSITIVE_INFINITY,
            Double.NEGATIVE_INFINITY,
            Double.NaN,
            Double.POSITIVE_INFINITY,
            Double.NEGATIVE_INFINITY,
            Double.NaN
        ));

        assertAll(
            () -> assertEquals(10, issues.size()),
            () -> assertEquals(
                List.of(
                    "/physicalParameters/minimumSpeed",
                    "/physicalParameters/maximumSpeed",
                    "/physicalParameters/minimumAngularSpeed",
                    "/physicalParameters/maximumAngularSpeed",
                    "/physicalParameters/maximumAcceleration",
                    "/physicalParameters/maximumDeceleration",
                    "/physicalParameters/minimumHeight",
                    "/physicalParameters/maximumHeight",
                    "/physicalParameters/width",
                    "/physicalParameters/length"
                ),
                paths(issues)
            ),
            () -> assertTrue(issues.stream().allMatch(issue ->
                issue.code().equals("NON_FINITE_PHYSICAL_PARAMETER")
                    && issue.description().equals(
                        "Physical parameter must be finite"
                    )
                    && issue.requirementId().equals("VDA3-FACTSHEET-007")
                    && issue.severity() == ValidationSeverity.ERROR
            ))
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-007] 只对 Schema 明示字段执行非负校验")
    void rejectsOnlySchemaDeclaredNegativeParameters() {
        List<ValidationIssue> issues = VALIDATOR.validate(parameters(
            -2.0D,
            -1.0D,
            -2.0D,
            -1.0D,
            -1.0D,
            -1.0D,
            -5.0D,
            -4.0D,
            -2.0D,
            -3.0D
        ));

        assertAll(
            () -> assertEquals(
                List.of(
                    "NEGATIVE_PHYSICAL_PARAMETER",
                    "NEGATIVE_PHYSICAL_PARAMETER",
                    "NEGATIVE_PHYSICAL_PARAMETER",
                    "NEGATIVE_PHYSICAL_PARAMETER",
                    "NEGATIVE_PHYSICAL_PARAMETER"
                ),
                codes(issues)
            ),
            () -> assertEquals(
                List.of(
                    "/physicalParameters/minimumSpeed",
                    "/physicalParameters/maximumSpeed",
                    "/physicalParameters/minimumAngularSpeed",
                    "/physicalParameters/maximumAngularSpeed",
                    "/physicalParameters/maximumAcceleration"
                ),
                paths(issues)
            ),
            () -> assertTrue(issues.stream().allMatch(issue ->
                issue.description().equals(
                    "Schema-declared physical parameter must not be negative"
                )
            ))
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-007] 报告线速度、角速度和高度倒置")
    void rejectsInvertedRanges() {
        List<ValidationIssue> issues = VALIDATOR.validate(parameters(
            2.0D,
            1.0D,
            2.0D,
            1.0D,
            1.0D,
            1.0D,
            2.0D,
            1.0D,
            1.0D,
            1.0D
        ));

        assertAll(
            () -> assertEquals(
                List.of(
                    "INVALID_LINEAR_SPEED_RANGE",
                    "INVALID_ANGULAR_SPEED_RANGE",
                    "INVALID_HEIGHT_RANGE"
                ),
                codes(issues)
            ),
            () -> assertEquals(
                List.of(
                    "/physicalParameters/maximumSpeed",
                    "/physicalParameters/maximumAngularSpeed",
                    "/physicalParameters/maximumHeight"
                ),
                paths(issues)
            )
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-007] 可选角速度仅在成对有限时比较")
    void comparesOptionalAngularRangeOnlyWhenBothValuesAreFinite() {
        PhysicalParameters minimumOnly = parameters(
            0.0D, 1.0D, 0.5D, null, 1.0D, 1.0D, 1.0D, 2.0D, 1.0D, 1.0D
        );
        PhysicalParameters maximumOnly = parameters(
            0.0D, 1.0D, null, 0.5D, 1.0D, 1.0D, 1.0D, 2.0D, 1.0D, 1.0D
        );
        PhysicalParameters finiteMinimum = parameters(
            0.0D, 1.0D, 0.5D, Double.NaN, 1.0D, 1.0D, 1.0D, 2.0D, 1.0D, 1.0D
        );
        PhysicalParameters finiteMaximum = parameters(
            0.0D, 1.0D, Double.NaN, 0.5D, 1.0D, 1.0D, 1.0D, 2.0D, 1.0D, 1.0D
        );
        PhysicalParameters equal = parameters(
            0.0D, 1.0D, 0.5D, 0.5D, 1.0D, 1.0D, 1.0D, 2.0D, 1.0D, 1.0D
        );

        assertAll(
            () -> assertEquals(List.of(), VALIDATOR.validate(minimumOnly)),
            () -> assertEquals(List.of(), VALIDATOR.validate(maximumOnly)),
            () -> assertEquals(
                List.of("NON_FINITE_PHYSICAL_PARAMETER"),
                codes(VALIDATOR.validate(finiteMinimum))
            ),
            () -> assertEquals(
                List.of("NON_FINITE_PHYSICAL_PARAMETER"),
                codes(VALIDATOR.validate(finiteMaximum))
            ),
            () -> assertEquals(List.of(), VALIDATOR.validate(equal))
        );
    }

    private static PhysicalParameters parameters(
        Double minimumSpeed,
        Double maximumSpeed,
        Double minimumAngularSpeed,
        Double maximumAngularSpeed,
        Double maximumAcceleration,
        Double maximumDeceleration,
        Double minimumHeight,
        Double maximumHeight,
        Double width,
        Double length
    ) {
        return PhysicalParameters.builder()
            .minimumSpeed(minimumSpeed)
            .maximumSpeed(maximumSpeed)
            .minimumAngularSpeed(minimumAngularSpeed)
            .maximumAngularSpeed(maximumAngularSpeed)
            .maximumAcceleration(maximumAcceleration)
            .maximumDeceleration(maximumDeceleration)
            .minimumHeight(minimumHeight)
            .maximumHeight(maximumHeight)
            .width(width)
            .length(length)
            .build();
    }

    private static List<String> codes(List<ValidationIssue> issues) {
        return issues.stream().map(ValidationIssue::code).toList();
    }

    private static List<String> paths(List<ValidationIssue> issues) {
        return issues.stream().map(ValidationIssue::path).toList();
    }
}
