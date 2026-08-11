package io.github.cooltheworld.rcs.protocol.vda5050.v3.validation;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.internal.Unsigned32;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.order.AllowedDeviationXY;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.order.Node;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.order.NodePosition;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** 对单个 Order {@link Node} 执行上下文无关数值语义校验。 */
public final class NodeValidator {
    private static final String REQUIREMENT_ID = "VDA3-ORDER-002";
    private static final String POSITION_PATH = "/nodePosition";

    private NodeValidator() {}

    /** @return 可缓存复用且线程安全的单节点 Validator */
    public static NodeValidator create() {
        return new NodeValidator();
    }

    /**
     * 校验 sequenceId、位置有限数和规范闭区间。
     *
     * @param node 已完成强类型绑定的单个 Node
     * @return 按字段顺序排列的不可变问题列表
     */
    public List<ValidationIssue> validate(Node node) {
        Objects.requireNonNull(node, "node");
        List<ValidationIssue> issues = new ArrayList<>();
        if (!Unsigned32.isValid(node.sequenceId())) {
            issues.add(issue(
                "INVALID_NODE_SEQUENCE_ID",
                "/sequenceId",
                "Node sequence ID must be in uint32 range"
            ));
        }
        NodePosition position = node.nodePosition();
        if (position == null) {
            return List.copyOf(issues);
        }
        finite(position.x(), "/x", issues);
        finite(position.y(), "/y", issues);
        if (position.theta() != null) {
            range(position.theta(), -Math.PI, Math.PI, "/theta", issues);
        }
        AllowedDeviationXY deviationXY = position.allowedDeviationXY();
        if (deviationXY != null) {
            nonNegative(
                deviationXY.a(),
                "/allowedDeviationXY/a",
                issues
            );
            nonNegative(
                deviationXY.b(),
                "/allowedDeviationXY/b",
                issues
            );
            range(
                deviationXY.theta(),
                -Math.PI / 2.0D,
                Math.PI / 2.0D,
                "/allowedDeviationXY/theta",
                issues
            );
        }
        if (position.allowedDeviationTheta() != null) {
            range(
                position.allowedDeviationTheta(),
                0.0D,
                Math.PI,
                "/allowedDeviationTheta",
                issues
            );
        }
        return List.copyOf(issues);
    }

    private static void finite(
        Double value,
        String relativePath,
        List<ValidationIssue> issues
    ) {
        if (!Double.isFinite(value)) {
            issues.add(nonFinite(relativePath));
        }
    }

    private static void nonNegative(
        Double value,
        String relativePath,
        List<ValidationIssue> issues
    ) {
        if (!Double.isFinite(value)) {
            issues.add(nonFinite(relativePath));
            return;
        }
        if (value < 0.0D) {
            issues.add(outOfRange(relativePath));
        }
    }

    private static void range(
        Double value,
        double minimum,
        double maximum,
        String relativePath,
        List<ValidationIssue> issues
    ) {
        if (!Double.isFinite(value)) {
            issues.add(nonFinite(relativePath));
            return;
        }
        if (value < minimum || value > maximum) {
            issues.add(outOfRange(relativePath));
        }
    }

    private static ValidationIssue nonFinite(String relativePath) {
        return issue(
            "NON_FINITE_NODE_POSITION_VALUE",
            POSITION_PATH + relativePath,
            "Node position value must be finite"
        );
    }

    private static ValidationIssue outOfRange(String relativePath) {
        return issue(
            "NODE_POSITION_VALUE_OUT_OF_RANGE",
            POSITION_PATH + relativePath,
            "Node position value is outside the allowed range"
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
