package io.github.cooltheworld.rcs.protocol.vda5050.v3.validation;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.order.AllowedDeviationXY;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.order.Node;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.order.NodePosition;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

final class NodeValidatorTest {
    private static final long UINT32_MAX = 4_294_967_295L;

    private final NodeValidator validator = NodeValidator.create();

    @Test
    @DisplayName("[VDA3-ORDER-002] 接受 sequenceId 和位置数值的全部闭区间端点")
    void acceptsEveryClosedNumericBoundary() {
        Node minimum = node(
            0L,
            position(
                -Math.PI,
                ellipse(0.0D, 0.0D, -Math.PI / 2.0D),
                0.0D
            )
        );
        Node maximum = node(
            UINT32_MAX,
            position(
                Math.PI,
                ellipse(2.0D, 1.0D, Math.PI / 2.0D),
                Math.PI
            )
        );

        assertAll(
            () -> assertEquals(List.of(), validator.validate(minimum)),
            () -> assertEquals(List.of(), validator.validate(maximum)),
            () -> assertEquals(List.of(), validator.validate(node(1L, null))),
            () -> assertEquals(
                List.of(),
                validator.validate(node(
                    1L,
                    NodePosition.builder()
                        .x(0.0D)
                        .y(0.0D)
                        .mapId("map")
                        .build()
                ))
            )
        );
    }

    @Test
    @DisplayName("[VDA3-ORDER-002] 拒绝 sequenceId 的两个 uint32 越界端")
    void rejectsSequenceIdsOutsideUint32() {
        List<ValidationIssue> below = validator.validate(node(-1L, null));
        List<ValidationIssue> above = validator.validate(
            node(UINT32_MAX + 1L, null)
        );

        assertAll(
            () -> assertSingleIssue(
                below,
                "INVALID_NODE_SEQUENCE_ID",
                "/sequenceId"
            ),
            () -> assertSingleIssue(
                above,
                "INVALID_NODE_SEQUENCE_ID",
                "/sequenceId"
            )
        );
    }

    @Test
    @DisplayName("[VDA3-ORDER-002] 按字段顺序报告全部非有限位置数值")
    void rejectsEveryNonFinitePositionValueInFieldOrder() {
        NodePosition position = NodePosition.builder()
            .x(Double.NaN)
            .y(Double.POSITIVE_INFINITY)
            .theta(Double.NEGATIVE_INFINITY)
            .allowedDeviationXY(ellipse(
                Double.NaN,
                Double.POSITIVE_INFINITY,
                Double.NEGATIVE_INFINITY
            ))
            .allowedDeviationTheta(Double.NaN)
            .mapId("secret-map")
            .build();

        List<ValidationIssue> issues = validator.validate(
            Node.builder()
                .nodeId("secret-node")
                .sequenceId(0L)
                .released(Boolean.TRUE)
                .nodePosition(position)
                .actions(List.of())
                .build()
        );

        assertAll(
            () -> assertEquals(7, issues.size()),
            () -> assertEquals(
                List.of(
                    "/nodePosition/x",
                    "/nodePosition/y",
                    "/nodePosition/theta",
                    "/nodePosition/allowedDeviationXY/a",
                    "/nodePosition/allowedDeviationXY/b",
                    "/nodePosition/allowedDeviationXY/theta",
                    "/nodePosition/allowedDeviationTheta"
                ),
                issues.stream().map(ValidationIssue::path).toList()
            ),
            () -> assertTrue(issues.stream().allMatch(issue ->
                issue.code().equals("NON_FINITE_NODE_POSITION_VALUE")
            )),
            () -> assertTrue(issues.stream().allMatch(issue ->
                issue.requirementId().equals("VDA3-ORDER-002")
            )),
            () -> assertTrue(issues.stream().allMatch(issue ->
                issue.severity() == ValidationSeverity.ERROR
            )),
            () -> assertTrue(issues.stream().noneMatch(issue ->
                issue.description().contains("secret")
            ))
        );
    }

    @Test
    @DisplayName("[VDA3-ORDER-002] 拒绝每个位置字段的下界和上界越界")
    void rejectsEveryOutOfRangePositionValue() {
        NodePosition below = position(
            Math.nextDown(-Math.PI),
            ellipse(-1.0D, -Double.MIN_VALUE, Math.nextDown(-Math.PI / 2.0D)),
            -Double.MIN_VALUE
        );
        NodePosition above = position(
            Math.nextUp(Math.PI),
            ellipse(1.0D, 0.5D, Math.nextUp(Math.PI / 2.0D)),
            Math.nextUp(Math.PI)
        );

        List<ValidationIssue> belowIssues = validator.validate(node(0L, below));
        List<ValidationIssue> aboveIssues = validator.validate(node(0L, above));

        assertAll(
            () -> assertEquals(
                List.of(
                    "/nodePosition/theta",
                    "/nodePosition/allowedDeviationXY/a",
                    "/nodePosition/allowedDeviationXY/b",
                    "/nodePosition/allowedDeviationXY/theta",
                    "/nodePosition/allowedDeviationTheta"
                ),
                belowIssues.stream().map(ValidationIssue::path).toList()
            ),
            () -> assertEquals(
                List.of(
                    "/nodePosition/theta",
                    "/nodePosition/allowedDeviationXY/theta",
                    "/nodePosition/allowedDeviationTheta"
                ),
                aboveIssues.stream().map(ValidationIssue::path).toList()
            ),
            () -> assertTrue(belowIssues.stream().allMatch(issue ->
                issue.code().equals("NODE_POSITION_VALUE_OUT_OF_RANGE")
            )),
            () -> assertTrue(aboveIssues.stream().allMatch(issue ->
                issue.code().equals("NODE_POSITION_VALUE_OUT_OF_RANGE")
            ))
        );
    }

    @Test
    @DisplayName("[VDA3-ORDER-002] 不增加 a≥b 或 allowedDeviationTheta 依赖 theta 的规则")
    void doesNotInventUnspecifiedPositionRelationships() {
        NodePosition position = NodePosition.builder()
            .x(1.0D)
            .y(2.0D)
            .allowedDeviationXY(ellipse(1.0D, 2.0D, 0.0D))
            .allowedDeviationTheta(1.0D)
            .mapId("map")
            .build();

        assertEquals(List.of(), validator.validate(node(1L, position)));
    }

    @Test
    @DisplayName("[VDA3-ORDER-002] 校验输入不变、结果不可变且重放确定")
    void isDeterministicImmutableAndLeavesTheInputUntouched() {
        NodePosition position = position(
            Math.nextUp(Math.PI),
            ellipse(-1.0D, 1.0D, 0.0D),
            0.0D
        );
        Node node = node(-1L, position);
        Node equalBefore = node(-1L, position);

        List<ValidationIssue> first = validator.validate(node);
        List<ValidationIssue> replay = validator.validate(node);

        assertAll(
            () -> assertEquals(equalBefore, node),
            () -> assertEquals(first, replay),
            () -> assertThrows(
                UnsupportedOperationException.class,
                () -> first.add(first.getFirst())
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> validator.validate(null)
            )
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
                ValidationSeverity.ERROR,
                issues.getFirst().severity()
            ),
            () -> assertEquals(
                "VDA3-ORDER-002",
                issues.getFirst().requirementId()
            ),
            () -> assertFalse(issues.getFirst().description().isBlank())
        );
    }

    private static Node node(Long sequenceId, NodePosition position) {
        return Node.builder()
            .nodeId("node")
            .sequenceId(sequenceId)
            .released(Boolean.FALSE)
            .nodePosition(position)
            .actions(List.of())
            .build();
    }

    private static NodePosition position(
        Double theta,
        AllowedDeviationXY allowedDeviationXY,
        Double allowedDeviationTheta
    ) {
        return NodePosition.builder()
            .x(1.0D)
            .y(2.0D)
            .theta(theta)
            .allowedDeviationXY(allowedDeviationXY)
            .allowedDeviationTheta(allowedDeviationTheta)
            .mapId("map")
            .build();
    }

    private static AllowedDeviationXY ellipse(Double a, Double b, Double theta) {
        return AllowedDeviationXY.builder()
            .a(a)
            .b(b)
            .theta(theta)
            .build();
    }
}
