package io.github.cooltheworld.rcs.protocol.vda5050.v3.model.trajectory;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.ExtensionFields;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

final class TrajectoryTest {
    @Test
    @DisplayName("[VDA3-SHARED-013] 最小 Trajectory 保留缺失默认字段")
    void buildsTheMinimalTrajectory() {
        Trajectory trajectory = Trajectory.builder()
            .controlPoints(List.of(point(0.0D, 0.0D)))
            .build();

        assertAll(
            () -> assertNull(trajectory.degree()),
            () -> assertNull(trajectory.knotVector()),
            () -> assertEquals(1, trajectory.controlPoints().size()),
            () -> assertTrue(trajectory.extensionFields().isEmpty())
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-013] 完整 Trajectory 防御性复制两个列表")
    void buildsTheCompleteImmutableTrajectory() throws ReflectiveOperationException {
        ExtensionFields extensions = extensionFields("{\"vendor\":true}");
        List<Double> knots = new ArrayList<>(List.of(
            0.0D,
            0.0D,
            1.0D,
            1.0D
        ));
        List<TrajectoryControlPoint> points = new ArrayList<>(List.of(
            point(0.0D, 0.0D),
            point(1.0D, 1.0D)
        ));
        Trajectory trajectory = Trajectory.builder()
            .degree(1L)
            .knotVector(knots)
            .controlPoints(points)
            .extensionFields(extensions)
            .build();
        knots.clear();
        points.clear();

        assertAll(
            () -> assertEquals(1L, trajectory.degree()),
            () -> assertEquals(
                List.of(0.0D, 0.0D, 1.0D, 1.0D),
                trajectory.knotVector()
            ),
            () -> assertEquals(2, trajectory.controlPoints().size()),
            () -> assertThrows(
                UnsupportedOperationException.class,
                () -> trajectory.knotVector().add(0.5D)
            ),
            () -> assertThrows(
                UnsupportedOperationException.class,
                () -> trajectory.controlPoints().clear()
            ),
            () -> assertEquals(extensions, trajectory.extensionFields())
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-013] 显式空列表与缺失 knotVector 保持不同语义")
    void preservesEmptyAndMissingCollectionSemantics() {
        Trajectory missingKnots = Trajectory.builder()
            .controlPoints(List.of())
            .build();
        Trajectory emptyKnots = Trajectory.builder()
            .knotVector(List.of())
            .controlPoints(List.of())
            .build();

        assertAll(
            () -> assertNull(missingKnots.knotVector()),
            () -> assertEquals(List.of(), missingKnots.controlPoints()),
            () -> assertEquals(List.of(), emptyKnots.knotVector()),
            () -> assertNotEquals(missingKnots, emptyKnots)
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-013] 聚合无损保留待 Validator 检查的数值")
    void preservesProgrammaticNumericBoundariesForValidation() {
        Trajectory trajectory = Trajectory.builder()
            .degree(-1L)
            .knotVector(List.of(Double.NaN, Double.POSITIVE_INFINITY))
            .controlPoints(List.of(point(Double.NaN, Double.NEGATIVE_INFINITY)))
            .build();

        assertAll(
            () -> assertEquals(-1L, trajectory.degree()),
            () -> assertTrue(Double.isNaN(trajectory.knotVector().getFirst())),
            () -> assertEquals(
                Double.POSITIVE_INFINITY,
                trajectory.knotVector().get(1)
            ),
            () -> assertTrue(
                Double.isNaN(trajectory.controlPoints().getFirst().x())
            )
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-013] Trajectory 拒绝缺失必填集合和 null 元素")
    void rejectsMissingControlPointsAndNullElements() {
        TrajectoryControlPoint point = point(0.0D, 0.0D);

        assertAll(
            () -> assertThrows(
                NullPointerException.class,
                () -> Trajectory.builder().build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> Trajectory.builder()
                    .knotVector(Arrays.asList(0.0D, null))
                    .controlPoints(List.of(point))
                    .build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> Trajectory.builder()
                    .controlPoints(Arrays.asList(point, null))
                    .build()
            ),
            () -> assertTrue(
                Trajectory.builder()
                    .controlPoints(List.of())
                    .extensionFields(null)
                    .build()
                    .extensionFields()
                    .isEmpty()
            )
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-013] Trajectory 值相等覆盖全部线路字段")
    void includesEveryFieldInValueEquality() throws ReflectiveOperationException {
        ExtensionFields extensions = extensionFields("{\"vendor\":true}");
        Trajectory equal = fullTrajectory(extensions).build();
        Trajectory same = fullTrajectory(extensions).build();

        assertAll(
            () -> assertEquals(equal, equal),
            () -> assertNotEquals(equal, null),
            () -> assertNotEquals(equal, "trajectory"),
            () -> assertNotEquals(equal, fullTrajectory(extensions).degree(2L).build()),
            () -> assertNotEquals(
                equal,
                fullTrajectory(extensions)
                    .knotVector(List.of(0.0D, 0.0D, 0.5D, 1.0D))
                    .build()
            ),
            () -> assertNotEquals(
                equal,
                fullTrajectory(extensions)
                    .controlPoints(List.of(point(0.0D, 0.0D)))
                    .build()
            ),
            () -> assertNotEquals(
                equal,
                fullTrajectory(ExtensionFields.empty()).build()
            ),
            () -> assertEquals(equal, same),
            () -> assertEquals(equal.hashCode(), same.hashCode()),
            () -> assertEquals(
                Set.of(
                    "degree",
                    "knotVector",
                    "controlPoints",
                    "extensionFields"
                ),
                fieldNames(Trajectory.class)
            )
        );
    }

    private static Trajectory.Builder fullTrajectory(
        ExtensionFields extensionFields
    ) {
        return Trajectory.builder()
            .degree(1L)
            .knotVector(List.of(0.0D, 0.0D, 1.0D, 1.0D))
            .controlPoints(List.of(
                point(0.0D, 0.0D),
                point(1.0D, 1.0D)
            ))
            .extensionFields(extensionFields);
    }

    private static TrajectoryControlPoint point(Double x, Double y) {
        return TrajectoryControlPoint.builder().x(x).y(y).build();
    }

    private static Set<String> fieldNames(Class<?> type) {
        return Arrays.stream(type.getDeclaredFields())
            .map(Field::getName)
            .collect(Collectors.toUnmodifiableSet());
    }

    private static ExtensionFields extensionFields(String json)
        throws ReflectiveOperationException {
        Method factory = ExtensionFields.class.getDeclaredMethod(
            "fromJsonBytes",
            byte[].class,
            byte[].class
        );
        factory.setAccessible(true);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        return (ExtensionFields) factory.invoke(null, bytes, bytes);
    }
}
