package io.github.cooltheworld.rcs.protocol.vda5050.v3.validation;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.order.Corridor;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.order.CorridorReleaseLossBehavior;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.order.Edge;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.order.EdgeOrientationType;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

final class EdgeValidatorTest {
    private static final long UINT32_MAX = 4_294_967_295L;

    private final EdgeValidator validator = EdgeValidator.create();

    @Test
    @DisplayName("[VDA3-ORDER-003] 接受 sequenceId、orientation 和 Corridor 端点")
    void acceptsEveryClosedNumericBoundary() {
        Edge minimum = edge(0L)
            .orientation(-Math.PI)
            .corridor(corridor(0.0D, 1.0D))
            .build();
        Edge maximum = edge(UINT32_MAX)
            .orientation(Math.PI)
            .corridor(corridor(1.0D, 0.0D))
            .build();

        assertAll(
            () -> assertEquals(List.of(), validator.validate(minimum)),
            () -> assertEquals(List.of(), validator.validate(maximum)),
            () -> assertEquals(
                List.of(),
                validator.validate(edge(1L).build())
            )
        );
    }

    @Test
    @DisplayName("[VDA3-ORDER-003] 拒绝 sequenceId 的两个 uint32 越界端")
    void rejectsSequenceIdsOutsideUint32() {
        List<ValidationIssue> below = validator.validate(edge(-1L).build());
        List<ValidationIssue> above = validator.validate(
            edge(UINT32_MAX + 1L).build()
        );

        assertAll(
            () -> assertSingleIssue(
                below,
                "INVALID_EDGE_SEQUENCE_ID",
                "/sequenceId"
            ),
            () -> assertSingleIssue(
                above,
                "INVALID_EDGE_SEQUENCE_ID",
                "/sequenceId"
            )
        );
    }

    @Test
    @DisplayName("[VDA3-ORDER-003] 按字段顺序报告全部非有限 Edge 数值")
    void rejectsEveryNonFiniteValueInFieldOrder() {
        Edge edge = edge(0L)
            .maximumSpeed(Double.NaN)
            .maximumMobileRobotHeight(Double.POSITIVE_INFINITY)
            .minimumLoadHandlingDeviceHeight(Double.NEGATIVE_INFINITY)
            .orientation(Double.NaN)
            .maximumRotationSpeed(Double.POSITIVE_INFINITY)
            .length(Double.NEGATIVE_INFINITY)
            .corridor(corridor(Double.NaN, Double.POSITIVE_INFINITY))
            .build();

        List<ValidationIssue> issues = validator.validate(edge);

        assertAll(
            () -> assertEquals(8, issues.size()),
            () -> assertEquals(
                List.of(
                    "/maximumSpeed",
                    "/maximumMobileRobotHeight",
                    "/minimumLoadHandlingDeviceHeight",
                    "/orientation",
                    "/maximumRotationSpeed",
                    "/length",
                    "/corridor/leftWidth",
                    "/corridor/rightWidth"
                ),
                issues.stream().map(ValidationIssue::path).toList()
            ),
            () -> assertTrue(issues.stream().allMatch(issue ->
                issue.code().equals("NON_FINITE_EDGE_VALUE")
            )),
            () -> assertTrue(issues.stream().allMatch(issue ->
                issue.requirementId().equals("VDA3-ORDER-003")
            )),
            () -> assertTrue(issues.stream().allMatch(issue ->
                issue.severity() == ValidationSeverity.ERROR
            )),
            () -> assertTrue(issues.stream().noneMatch(issue ->
                issue.description().contains("NaN")
                    || issue.description().contains("Infinity")
            ))
        );
    }

    @Test
    @DisplayName("[VDA3-ORDER-003] 拒绝 orientation 越界和非法 Corridor 宽度")
    void rejectsEveryOutOfRangeAndEmptyCorridorValue() {
        List<ValidationIssue> below = validator.validate(
            edge(0L)
                .orientation(Math.nextDown(-Math.PI))
                .corridor(corridor(-1.0D, 1.0D))
                .build()
        );
        List<ValidationIssue> above = validator.validate(
            edge(0L)
                .orientation(Math.nextUp(Math.PI))
                .corridor(corridor(1.0D, -Double.MIN_VALUE))
                .build()
        );
        List<ValidationIssue> empty = validator.validate(
            edge(0L).corridor(corridor(0.0D, -0.0D)).build()
        );

        assertAll(
            () -> assertEquals(
                List.of("/orientation", "/corridor/leftWidth"),
                below.stream().map(ValidationIssue::path).toList()
            ),
            () -> assertEquals(
                List.of("/orientation", "/corridor/rightWidth"),
                above.stream().map(ValidationIssue::path).toList()
            ),
            () -> assertTrue(below.stream().allMatch(issue ->
                issue.code().equals("EDGE_VALUE_OUT_OF_RANGE")
            )),
            () -> assertTrue(above.stream().allMatch(issue ->
                issue.code().equals("EDGE_VALUE_OUT_OF_RANGE")
            )),
            () -> assertSingleIssue(
                empty,
                "EMPTY_EDGE_CORRIDOR",
                "/corridor"
            )
        );
    }

    @Test
    @DisplayName("[VDA3-ORDER-003] 不增加未声明范围和可选字段依赖")
    void doesNotInventUnspecifiedRangesOrOptionalDependencies() {
        Edge edge = edge(1L)
            .maximumSpeed(-1.0D)
            .maximumMobileRobotHeight(-2.0D)
            .minimumLoadHandlingDeviceHeight(-3.0D)
            .orientationType(EdgeOrientationType.GLOBAL)
            .reachOrientationBeforeEntering(Boolean.TRUE)
            .maximumRotationSpeed(-4.0D)
            .length(-5.0D)
            .corridor(Corridor.builder()
                .leftWidth(1.0D)
                .rightWidth(1.0D)
                .releaseRequired(Boolean.FALSE)
                .releaseLossBehavior(CorridorReleaseLossBehavior.RETURN)
                .build())
            .build();

        assertEquals(List.of(), validator.validate(edge));
    }

    @Test
    @DisplayName("[VDA3-ORDER-003] 校验输入不变、结果不可变且重放确定")
    void isDeterministicImmutableAndLeavesTheInputUntouched() {
        Edge edge = edge(-1L)
            .orientation(Math.nextUp(Math.PI))
            .corridor(corridor(0.0D, 0.0D))
            .build();
        Edge equalBefore = edge(-1L)
            .orientation(Math.nextUp(Math.PI))
            .corridor(corridor(0.0D, 0.0D))
            .build();

        List<ValidationIssue> first = validator.validate(edge);
        List<ValidationIssue> replay = validator.validate(edge);

        assertAll(
            () -> assertEquals(equalBefore, edge),
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
                "VDA3-ORDER-003",
                issues.getFirst().requirementId()
            ),
            () -> assertFalse(issues.getFirst().description().isBlank())
        );
    }

    private static Edge.Builder edge(Long sequenceId) {
        return Edge.builder()
            .edgeId("secret-edge")
            .sequenceId(sequenceId)
            .released(Boolean.FALSE)
            .actions(List.of());
    }

    private static Corridor corridor(Double leftWidth, Double rightWidth) {
        return Corridor.builder()
            .leftWidth(leftWidth)
            .rightWidth(rightWidth)
            .build();
    }
}
