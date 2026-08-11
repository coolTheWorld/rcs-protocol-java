package io.github.cooltheworld.rcs.protocol.vda5050.v3.validation;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.internal.Unsigned32;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.trajectory.Trajectory;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.trajectory.TrajectoryControlPoint;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** 对共享 NURBS {@link Trajectory} 执行上下文无关语义校验。 */
public final class TrajectoryValidator {
    private static final String REQUIREMENT_ID = "VDA3-SHARED-013";
    private static final long DEFAULT_DEGREE = 1L;

    private TrajectoryValidator() {}

    /** @return 可缓存复用且线程安全的 Trajectory Validator */
    public static TrajectoryValidator create() {
        return new TrajectoryValidator();
    }

    /**
     * 校验 degree、控制点、权重、knot 数值、顺序、基数与重数。
     *
     * @param trajectory 已完成强类型绑定的共享 Trajectory
     * @return 按字段和数组顺序排列的不可变问题列表
     */
    public List<ValidationIssue> validate(Trajectory trajectory) {
        Objects.requireNonNull(trajectory, "trajectory");
        List<ValidationIssue> issues = new ArrayList<>();
        long degree = trajectory.degree() == null
            ? DEFAULT_DEGREE
            : trajectory.degree();
        boolean degreeValid = degree >= 1L && Unsigned32.isValid(degree);
        if (!degreeValid) {
            issues.add(issue(
                "INVALID_TRAJECTORY_DEGREE",
                "/degree",
                "Trajectory degree must be in the supported uint32 range"
            ));
        }
        validateControlPoints(trajectory.controlPoints(), issues);
        boolean knotValuesValid = validateKnotValues(
            trajectory.knotVector(),
            issues
        );
        if (!degreeValid) {
            return List.copyOf(issues);
        }
        if (trajectory.controlPoints().size() < degree + 1L) {
            issues.add(issue(
                "INSUFFICIENT_TRAJECTORY_CONTROL_POINTS",
                "/controlPoints",
                "Trajectory has too few control points for its degree"
            ));
        }
        List<Double> knots = trajectory.knotVector();
        if (knots == null) {
            return List.copyOf(issues);
        }
        long expectedKnotCount = trajectory.controlPoints().size()
            + degree
            + 1L;
        boolean knotCountValid = knots.size() == expectedKnotCount;
        if (!knotCountValid) {
            issues.add(issue(
                "INVALID_TRAJECTORY_KNOT_COUNT",
                "/knotVector",
                "Trajectory knot count does not match its control points and degree"
            ));
        }
        if (!knotValuesValid || !knotCountValid) {
            return List.copyOf(issues);
        }
        int descendingIndex = firstDescendingIndex(knots);
        if (descendingIndex >= 0) {
            issues.add(issue(
                "NON_MONOTONIC_TRAJECTORY_KNOT_VECTOR",
                "/knotVector/" + descendingIndex,
                "Trajectory knot vector must be non-decreasing"
            ));
            return List.copyOf(issues);
        }
        validateMultiplicities(knots, degree, issues);
        return List.copyOf(issues);
    }

    private static void validateControlPoints(
        List<TrajectoryControlPoint> points,
        List<ValidationIssue> issues
    ) {
        for (int index = 0; index < points.size(); index++) {
            TrajectoryControlPoint point = points.get(index);
            finite(point.x(), "/controlPoints/" + index + "/x", issues);
            finite(point.y(), "/controlPoints/" + index + "/y", issues);
            if (point.weight() != null) {
                positive(
                    point.weight(),
                    "/controlPoints/" + index + "/weight",
                    issues
                );
            }
        }
    }

    private static boolean validateKnotValues(
        List<Double> knots,
        List<ValidationIssue> issues
    ) {
        if (knots == null) {
            return true;
        }
        boolean valid = true;
        for (int index = 0; index < knots.size(); index++) {
            Double knot = knots.get(index);
            String path = "/knotVector/" + index;
            if (!Double.isFinite(knot)) {
                issues.add(nonFinite(path));
                valid = false;
            } else if (knot < 0.0D || knot > 1.0D) {
                issues.add(outOfRange(path));
                valid = false;
            }
        }
        return valid;
    }

    private static int firstDescendingIndex(List<Double> knots) {
        for (int index = 1; index < knots.size(); index++) {
            if (knots.get(index) < knots.get(index - 1)) {
                return index;
            }
        }
        return -1;
    }

    private static void validateMultiplicities(
        List<Double> knots,
        long degree,
        List<ValidationIssue> issues
    ) {
        long expectedEndpointMultiplicity = degree + 1L;
        int firstMultiplicity = runLengthForward(knots, 0);
        int lastIndex = knots.size() - 1;
        int lastMultiplicity = runLengthBackward(knots, lastIndex);
        if (firstMultiplicity != expectedEndpointMultiplicity) {
            issues.add(issue(
                "INVALID_TRAJECTORY_ENDPOINT_MULTIPLICITY",
                "/knotVector/0",
                "Trajectory first knot has an invalid multiplicity"
            ));
        }
        if (lastMultiplicity != expectedEndpointMultiplicity) {
            issues.add(issue(
                "INVALID_TRAJECTORY_ENDPOINT_MULTIPLICITY",
                "/knotVector/" + lastIndex,
                "Trajectory last knot has an invalid multiplicity"
            ));
        }
        int internalEnd = knots.size() - lastMultiplicity;
        int index = firstMultiplicity;
        while (index < internalEnd) {
            int multiplicity = runLengthForward(knots, index);
            if (multiplicity > degree) {
                issues.add(issue(
                    "INVALID_TRAJECTORY_INTERNAL_MULTIPLICITY",
                    "/knotVector/" + index,
                    "Trajectory internal knot has an invalid multiplicity"
                ));
            }
            index += multiplicity;
        }
    }

    private static int runLengthForward(List<Double> knots, int start) {
        int end = start + 1;
        while (end < knots.size() && sameValue(knots.get(start), knots.get(end))) {
            end++;
        }
        return end - start;
    }

    private static int runLengthBackward(List<Double> knots, int start) {
        int begin = start - 1;
        while (begin >= 0 && sameValue(knots.get(start), knots.get(begin))) {
            begin--;
        }
        return start - begin;
    }

    private static boolean sameValue(Double left, Double right) {
        return left.doubleValue() == right.doubleValue();
    }

    private static void finite(
        Double value,
        String path,
        List<ValidationIssue> issues
    ) {
        if (!Double.isFinite(value)) {
            issues.add(nonFinite(path));
        }
    }

    private static void positive(
        Double value,
        String path,
        List<ValidationIssue> issues
    ) {
        if (!Double.isFinite(value)) {
            issues.add(nonFinite(path));
            return;
        }
        if (value <= 0.0D) {
            issues.add(outOfRange(path));
        }
    }

    private static ValidationIssue nonFinite(String path) {
        return issue(
            "NON_FINITE_TRAJECTORY_VALUE",
            path,
            "Trajectory value must be finite"
        );
    }

    private static ValidationIssue outOfRange(String path) {
        return issue(
            "TRAJECTORY_VALUE_OUT_OF_RANGE",
            path,
            "Trajectory value is outside the allowed range"
        );
    }

    private static ValidationIssue issue(
        String code,
        String path,
        String description
    ) {
        return new ValidationIssue(
            code,
            ValidationSeverity.ERROR,
            path,
            description,
            REQUIREMENT_ID
        );
    }
}
