package io.github.cooltheworld.rcs.protocol.vda5050.v3.validation;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.trajectory.Trajectory;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.trajectory.TrajectoryControlPoint;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

final class TrajectoryValidatorTest {
    private static final long UINT32_MAX = 4_294_967_295L;

    private final TrajectoryValidator validator = TrajectoryValidator.create();

    @Test
    @DisplayName("[VDA3-SHARED-013] 接受缺失默认、显式 clamped 向量和非零端点")
    void acceptsDefaultsClampedVectorsAndNonZeroEndpoints() {
        Trajectory defaults = Trajectory.builder()
            .controlPoints(List.of(
                point(0.0D, 0.0D, 2.0D),
                point(1.0D, -1.0D, null)
            ))
            .build();
        Trajectory cubic = trajectory(
            3L,
            List.of(0.0D, 0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D, 1.0D),
            points(4)
        );
        Trajectory nonZeroEndpoints = trajectory(
            1L,
            List.of(0.25D, 0.25D, 0.75D, 0.75D),
            points(2)
        );
        Trajectory signedZero = trajectory(
            1L,
            List.of(0.0D, -0.0D, 1.0D, 1.0D),
            points(2)
        );

        assertAll(
            () -> assertEquals(List.of(), validator.validate(defaults)),
            () -> assertEquals(List.of(), validator.validate(cubic)),
            () -> assertEquals(
                List.of(),
                validator.validate(nonZeroEndpoints)
            ),
            () -> assertEquals(List.of(), validator.validate(signedZero))
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-013] degree 必须位于 [1,uint32.max]")
    void rejectsDegreesOutsideTheClosedRange() {
        List<ValidationIssue> zero = validator.validate(
            trajectory(0L, null, List.of())
        );
        List<ValidationIssue> negative = validator.validate(
            trajectory(-1L, null, List.of())
        );
        List<ValidationIssue> above = validator.validate(
            trajectory(UINT32_MAX + 1L, null, List.of())
        );
        List<ValidationIssue> maximum = validator.validate(
            trajectory(UINT32_MAX, null, List.of())
        );

        assertAll(
            () -> assertSingleIssue(
                zero,
                "INVALID_TRAJECTORY_DEGREE",
                "/degree"
            ),
            () -> assertSingleIssue(
                negative,
                "INVALID_TRAJECTORY_DEGREE",
                "/degree"
            ),
            () -> assertSingleIssue(
                above,
                "INVALID_TRAJECTORY_DEGREE",
                "/degree"
            ),
            () -> assertSingleIssue(
                maximum,
                "INSUFFICIENT_TRAJECTORY_CONTROL_POINTS",
                "/controlPoints"
            )
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-013] 按控制点顺序报告坐标和权重数值")
    void reportsEveryInvalidControlPointValueInOrder() {
        Trajectory trajectory = trajectory(
            1L,
            null,
            List.of(
                point(Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY),
                point(0.0D, 0.0D, 0.0D)
            )
        );

        List<ValidationIssue> issues = validator.validate(trajectory);

        assertAll(
            () -> assertEquals(
                List.of(
                    "/controlPoints/0/x",
                    "/controlPoints/0/y",
                    "/controlPoints/0/weight",
                    "/controlPoints/1/weight"
                ),
                paths(issues)
            ),
            () -> assertEquals(
                List.of(
                    "NON_FINITE_TRAJECTORY_VALUE",
                    "NON_FINITE_TRAJECTORY_VALUE",
                    "NON_FINITE_TRAJECTORY_VALUE",
                    "TRAJECTORY_VALUE_OUT_OF_RANGE"
                ),
                codes(issues)
            )
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-013] 报告每个非有限或越界 knot 且不级联顺序错误")
    void reportsEveryInvalidKnotWithoutOrderingCascades() {
        Trajectory trajectory = trajectory(
            1L,
            List.of(
                Double.NaN,
                -Double.MIN_VALUE,
                Math.nextUp(1.0D),
                Double.POSITIVE_INFINITY
            ),
            points(2)
        );

        List<ValidationIssue> issues = validator.validate(trajectory);

        assertAll(
            () -> assertEquals(
                List.of(
                    "/knotVector/0",
                    "/knotVector/1",
                    "/knotVector/2",
                    "/knotVector/3"
                ),
                paths(issues)
            ),
            () -> assertEquals(
                List.of(
                    "NON_FINITE_TRAJECTORY_VALUE",
                    "TRAJECTORY_VALUE_OUT_OF_RANGE",
                    "TRAJECTORY_VALUE_OUT_OF_RANGE",
                    "NON_FINITE_TRAJECTORY_VALUE"
                ),
                codes(issues)
            )
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-013] 校验最小控制点数和显式 knot 精确长度")
    void rejectsInsufficientControlPointsAndIncorrectKnotCount() {
        Trajectory explicitDegree = trajectory(
            2L,
            List.of(0.0D, 0.0D, 0.0D, 0.5D, 1.0D, 1.0D),
            points(2)
        );
        Trajectory defaultDegree = Trajectory.builder()
            .controlPoints(points(1))
            .build();

        assertAll(
            () -> assertEquals(
                List.of(
                    "INSUFFICIENT_TRAJECTORY_CONTROL_POINTS",
                    "INVALID_TRAJECTORY_KNOT_COUNT"
                ),
                codes(validator.validate(explicitDegree))
            ),
            () -> assertSingleIssue(
                validator.validate(defaultDegree),
                "INSUFFICIENT_TRAJECTORY_CONTROL_POINTS",
                "/controlPoints"
            )
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-013] knot vector 必须非递减且不级联重数错误")
    void rejectsTheFirstDescendingKnotWithoutMultiplicityCascades() {
        Trajectory trajectory = trajectory(
            1L,
            List.of(0.0D, 0.0D, 0.75D, 0.5D),
            points(2)
        );

        assertSingleIssue(
            validator.validate(trajectory),
            "NON_MONOTONIC_TRAJECTORY_KNOT_VECTOR",
            "/knotVector/3"
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-013] 首尾 knot 重数必须精确为 degree+1")
    void rejectsBothInvalidEndpointMultiplicities() {
        Trajectory trajectory = trajectory(
            2L,
            List.of(0.0D, 0.0D, 0.25D, 0.75D, 1.0D, 1.0D),
            points(3)
        );
        Trajectory oneRepeatedValue = trajectory(
            1L,
            List.of(0.5D, 0.5D, 0.5D, 0.5D),
            points(2)
        );

        List<ValidationIssue> issues = validator.validate(trajectory);
        List<ValidationIssue> repeatedIssues = validator.validate(
            oneRepeatedValue
        );

        assertAll(
            () -> assertEquals(
                List.of(
                    "/knotVector/0",
                    "/knotVector/5"
                ),
                paths(issues)
            ),
            () -> assertTrue(issues.stream().allMatch(issue ->
                issue.code().equals(
                    "INVALID_TRAJECTORY_ENDPOINT_MULTIPLICITY"
                )
            )),
            () -> assertEquals(
                List.of("/knotVector/0", "/knotVector/3"),
                paths(repeatedIssues)
            )
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-013] 内部 knot 重数不得大于 degree")
    void rejectsExcessiveInternalMultiplicity() {
        Trajectory trajectory = trajectory(
            2L,
            List.of(
                0.0D,
                0.0D,
                0.0D,
                0.5D,
                0.5D,
                0.5D,
                1.0D,
                1.0D,
                1.0D
            ),
            points(6)
        );

        assertSingleIssue(
            validator.validate(trajectory),
            "INVALID_TRAJECTORY_INTERNAL_MULTIPLICITY",
            "/knotVector/3"
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-013] 非法 degree 跳过派生判断但保留独立数值问题")
    void skipsDegreeDependentChecksButKeepsIndependentNumericIssues() {
        Trajectory trajectory = trajectory(
            0L,
            List.of(Double.NaN),
            List.of(point(Double.POSITIVE_INFINITY, 0.0D, null))
        );

        List<ValidationIssue> issues = validator.validate(trajectory);

        assertAll(
            () -> assertEquals(
                List.of(
                    "INVALID_TRAJECTORY_DEGREE",
                    "NON_FINITE_TRAJECTORY_VALUE",
                    "NON_FINITE_TRAJECTORY_VALUE"
                ),
                codes(issues)
            ),
            () -> assertFalse(codes(issues).contains(
                "INSUFFICIENT_TRAJECTORY_CONTROL_POINTS"
            )),
            () -> assertFalse(codes(issues).contains(
                "INVALID_TRAJECTORY_KNOT_COUNT"
            ))
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-013] 多组 degree 与控制点基数保持 clamped 不变量")
    void validatesCardinalityPropertiesAcrossMultipleDegrees() {
        for (int degree = 1; degree <= 8; degree++) {
            for (int pointCount = degree + 1; pointCount <= degree + 4; pointCount++) {
                Trajectory valid = trajectory(
                    (long) degree,
                    clampedKnots(degree, pointCount),
                    points(pointCount)
                );
                List<Double> shortKnots = new ArrayList<>(valid.knotVector());
                shortKnots.removeLast();

                assertEquals(List.of(), validator.validate(valid));
                assertTrue(
                    codes(validator.validate(trajectory(
                        (long) degree,
                        shortKnots,
                        points(pointCount)
                    ))).contains("INVALID_TRAJECTORY_KNOT_COUNT")
                );
            }
        }
    }

    @Test
    @DisplayName("[VDA3-SHARED-013] 校验保持输入不变、结果不可变且重放确定")
    void isDeterministicImmutableAndLeavesTheInputUntouched() {
        Trajectory trajectory = trajectory(
            1L,
            List.of(0.0D, 0.5D, 1.0D, 1.0D),
            points(2)
        );
        Trajectory equalBefore = trajectory(
            1L,
            List.of(0.0D, 0.5D, 1.0D, 1.0D),
            points(2)
        );

        List<ValidationIssue> first = validator.validate(trajectory);
        List<ValidationIssue> replay = validator.validate(trajectory);

        assertAll(
            () -> assertEquals(equalBefore, trajectory),
            () -> assertEquals(first, replay),
            () -> assertThrows(
                UnsupportedOperationException.class,
                () -> first.add(first.getFirst())
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> validator.validate(null)
            ),
            () -> assertTrue(first.stream().allMatch(issue ->
                issue.requirementId().equals("VDA3-SHARED-013")
                    && issue.severity() == ValidationSeverity.ERROR
                    && !issue.description().contains("0.5")
            ))
        );
    }

    private static void assertSingleIssue(
        List<ValidationIssue> issues,
        String code,
        String path
    ) {
        assertAll(
            () -> assertEquals(1, issues.size()),
            () -> assertEquals(code, issues.getFirst().code()),
            () -> assertEquals(path, issues.getFirst().path()),
            () -> assertEquals(
                "VDA3-SHARED-013",
                issues.getFirst().requirementId()
            ),
            () -> assertFalse(issues.getFirst().description().isBlank())
        );
    }

    private static List<String> codes(List<ValidationIssue> issues) {
        return issues.stream().map(ValidationIssue::code).toList();
    }

    private static List<String> paths(List<ValidationIssue> issues) {
        return issues.stream().map(ValidationIssue::path).toList();
    }

    private static Trajectory trajectory(
        Long degree,
        List<Double> knots,
        List<TrajectoryControlPoint> controlPoints
    ) {
        return Trajectory.builder()
            .degree(degree)
            .knotVector(knots)
            .controlPoints(controlPoints)
            .build();
    }

    private static List<TrajectoryControlPoint> points(int count) {
        return IntStream.range(0, count)
            .mapToObj(index -> point((double) index, (double) -index, null))
            .toList();
    }

    private static TrajectoryControlPoint point(
        Double x,
        Double y,
        Double weight
    ) {
        return TrajectoryControlPoint.builder()
            .x(x)
            .y(y)
            .weight(weight)
            .build();
    }

    private static List<Double> clampedKnots(int degree, int pointCount) {
        List<Double> knots = new ArrayList<>();
        for (int index = 0; index <= degree; index++) {
            knots.add(0.0D);
        }
        int internalCount = pointCount - degree - 1;
        for (int index = 1; index <= internalCount; index++) {
            knots.add((double) index / (internalCount + 1.0D));
        }
        for (int index = 0; index <= degree; index++) {
            knots.add(1.0D);
        }
        return List.copyOf(knots);
    }
}
