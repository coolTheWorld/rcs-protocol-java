package io.github.cooltheworld.rcs.protocol.vda5050.v3.validation;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.internal.Unsigned32;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.order.Corridor;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.order.Edge;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** 对单个 Order {@link Edge} 执行上下文无关数值语义校验。 */
public final class EdgeValidator {
    private static final String REQUIREMENT_ID = "VDA3-ORDER-003";

    private EdgeValidator() {}

    /** @return 可缓存复用且线程安全的单 Edge Validator */
    public static EdgeValidator create() {
        return new EdgeValidator();
    }

    /**
     * 校验 sequenceId、当前标量有限性、orientation 和 Corridor 边界。
     *
     * @param edge 已完成强类型绑定的单个 Edge
     * @return 按字段顺序排列的不可变问题列表
     */
    public List<ValidationIssue> validate(Edge edge) {
        Objects.requireNonNull(edge, "edge");
        List<ValidationIssue> issues = new ArrayList<>();
        if (!Unsigned32.isValid(edge.sequenceId())) {
            issues.add(issue(
                "INVALID_EDGE_SEQUENCE_ID",
                "/sequenceId",
                "Edge sequence ID must be in uint32 range"
            ));
        }
        finite(edge.maximumSpeed(), "/maximumSpeed", issues);
        finite(
            edge.maximumMobileRobotHeight(),
            "/maximumMobileRobotHeight",
            issues
        );
        finite(
            edge.minimumLoadHandlingDeviceHeight(),
            "/minimumLoadHandlingDeviceHeight",
            issues
        );
        if (edge.orientation() != null) {
            range(
                edge.orientation(),
                -Math.PI,
                Math.PI,
                "/orientation",
                issues
            );
        }
        finite(
            edge.maximumRotationSpeed(),
            "/maximumRotationSpeed",
            issues
        );
        finite(edge.length(), "/length", issues);
        validateCorridor(edge.corridor(), issues);
        return List.copyOf(issues);
    }

    private static void finite(
        Double value,
        String path,
        List<ValidationIssue> issues
    ) {
        if (value != null && !Double.isFinite(value)) {
            issues.add(nonFinite(path));
        }
    }

    private static void range(
        Double value,
        double minimum,
        double maximum,
        String path,
        List<ValidationIssue> issues
    ) {
        if (!Double.isFinite(value)) {
            issues.add(nonFinite(path));
            return;
        }
        if (value < minimum || value > maximum) {
            issues.add(outOfRange(path));
        }
    }

    private static void validateCorridor(
        Corridor corridor,
        List<ValidationIssue> issues
    ) {
        if (corridor == null) {
            return;
        }
        boolean leftValid = validWidth(
            corridor.leftWidth(),
            "/corridor/leftWidth",
            issues
        );
        boolean rightValid = validWidth(
            corridor.rightWidth(),
            "/corridor/rightWidth",
            issues
        );
        if (leftValid
            && rightValid
            && corridor.leftWidth() == 0.0D
            && corridor.rightWidth() == 0.0D) {
            issues.add(issue(
                "EMPTY_EDGE_CORRIDOR",
                "/corridor",
                "Edge corridor must allow a non-zero deviation"
            ));
        }
    }

    private static boolean validWidth(
        Double value,
        String path,
        List<ValidationIssue> issues
    ) {
        if (!Double.isFinite(value)) {
            issues.add(nonFinite(path));
            return false;
        }
        if (value < 0.0D) {
            issues.add(outOfRange(path));
            return false;
        }
        return true;
    }

    private static ValidationIssue nonFinite(String path) {
        return issue(
            "NON_FINITE_EDGE_VALUE",
            path,
            "Edge value must be finite"
        );
    }

    private static ValidationIssue outOfRange(String path) {
        return issue(
            "EDGE_VALUE_OUT_OF_RANGE",
            path,
            "Edge value is outside the allowed range"
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
