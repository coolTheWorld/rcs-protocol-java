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
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

final class TrajectoryControlPointTest {
    @Test
    @DisplayName("[VDA3-SHARED-013] 最小控制点保留坐标且不物化默认权重")
    void buildsTheMinimalControlPoint() {
        TrajectoryControlPoint point = minimalPoint().build();

        assertAll(
            () -> assertEquals(1.25D, point.x()),
            () -> assertEquals(-2.5D, point.y()),
            () -> assertNull(point.weight()),
            () -> assertTrue(point.extensionFields().isEmpty())
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-013] 完整控制点保存显式权重和未知扩展")
    void buildsTheCompleteControlPoint() throws ReflectiveOperationException {
        ExtensionFields extensions = extensionFields("{\"vendor\":true}");
        TrajectoryControlPoint point = minimalPoint()
            .weight(2.0D)
            .extensionFields(extensions)
            .build();

        assertAll(
            () -> assertEquals(2.0D, point.weight()),
            () -> assertEquals(extensions, point.extensionFields())
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-013] 模型无损保留待 Validator 检查的数值")
    void preservesProgrammaticNumericBoundariesForValidation() {
        TrajectoryControlPoint point = TrajectoryControlPoint.builder()
            .x(Double.NaN)
            .y(Double.NEGATIVE_INFINITY)
            .weight(-0.0D)
            .build();

        assertAll(
            () -> assertTrue(Double.isNaN(point.x())),
            () -> assertEquals(Double.NEGATIVE_INFINITY, point.y()),
            () -> assertEquals(-0.0D, point.weight())
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-013] 控制点只拒绝缺失必填引用")
    void rejectsOnlyMissingRequiredReferences() {
        assertAll(
            () -> assertThrows(
                NullPointerException.class,
                () -> TrajectoryControlPoint.builder().y(0.0D).build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> TrajectoryControlPoint.builder().x(0.0D).build()
            ),
            () -> assertTrue(
                minimalPoint()
                    .extensionFields(null)
                    .build()
                    .extensionFields()
                    .isEmpty()
            )
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-013] 控制点值相等与字段所有权覆盖全部线路值")
    void includesEveryFieldInValueEquality() throws ReflectiveOperationException {
        ExtensionFields extensions = extensionFields("{\"vendor\":true}");
        TrajectoryControlPoint equal = fullPoint(extensions).build();
        TrajectoryControlPoint same = fullPoint(extensions).build();

        assertAll(
            () -> assertEquals(equal, equal),
            () -> assertNotEquals(equal, null),
            () -> assertNotEquals(equal, "point"),
            () -> assertNotEquals(equal, fullPoint(extensions).x(9.0D).build()),
            () -> assertNotEquals(equal, fullPoint(extensions).y(9.0D).build()),
            () -> assertNotEquals(
                equal,
                fullPoint(extensions).weight(3.0D).build()
            ),
            () -> assertNotEquals(
                equal,
                fullPoint(ExtensionFields.empty()).build()
            ),
            () -> assertEquals(equal, same),
            () -> assertEquals(equal.hashCode(), same.hashCode()),
            () -> assertEquals(
                Set.of("x", "y", "weight", "extensionFields"),
                fieldNames(TrajectoryControlPoint.class)
            )
        );
    }

    private static TrajectoryControlPoint.Builder minimalPoint() {
        return TrajectoryControlPoint.builder().x(1.25D).y(-2.5D);
    }

    private static TrajectoryControlPoint.Builder fullPoint(
        ExtensionFields extensionFields
    ) {
        return minimalPoint().weight(2.0D).extensionFields(extensionFields);
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
