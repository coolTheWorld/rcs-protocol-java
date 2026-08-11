package io.github.cooltheworld.rcs.protocol.vda5050.v3.model.order;

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

final class NodePositionTest {
    @Test
    @DisplayName("[VDA3-ORDER-001] 最小 NodePosition 保留坐标和 mapId 原文")
    void buildsTheMinimalNodePosition() {
        NodePosition position = minimalPosition()
            .mapId(" Map/Main ")
            .build();

        assertAll(
            () -> assertEquals(1.25D, position.x()),
            () -> assertEquals(-2.5D, position.y()),
            () -> assertEquals(" Map/Main ", position.mapId()),
            () -> assertNull(position.theta()),
            () -> assertNull(position.allowedDeviationXY()),
            () -> assertNull(position.allowedDeviationTheta()),
            () -> assertTrue(position.extensionFields().isEmpty())
        );
    }

    @Test
    @DisplayName("[VDA3-ORDER-001] 完整 NodePosition 聚合偏差椭圆和未知扩展")
    void buildsTheCompleteNodePosition() throws ReflectiveOperationException {
        ExtensionFields ellipseExtensions = extensionFields(
            "{\"ellipseVendor\":true}"
        );
        ExtensionFields positionExtensions = extensionFields(
            "{\"positionVendor\":1}"
        );
        AllowedDeviationXY ellipse = ellipse(ellipseExtensions).build();
        NodePosition position = minimalPosition()
            .theta(Math.PI)
            .allowedDeviationXY(ellipse)
            .allowedDeviationTheta(Math.PI / 4.0D)
            .extensionFields(positionExtensions)
            .build();

        assertAll(
            () -> assertEquals(2.0D, ellipse.a()),
            () -> assertEquals(1.0D, ellipse.b()),
            () -> assertEquals(0.5D, ellipse.theta()),
            () -> assertEquals(ellipseExtensions, ellipse.extensionFields()),
            () -> assertEquals(Math.PI, position.theta()),
            () -> assertEquals(ellipse, position.allowedDeviationXY()),
            () -> assertEquals(Math.PI / 4.0D, position.allowedDeviationTheta()),
            () -> assertEquals(positionExtensions, position.extensionFields())
        );
    }

    @Test
    @DisplayName("[VDA3-ORDER-002] 模型无损保留待 Validator 检查的程序化数值")
    void preservesProgrammaticNumericBoundariesForValidation() {
        AllowedDeviationXY ellipse = AllowedDeviationXY.builder()
            .a(-1.0D)
            .b(Double.POSITIVE_INFINITY)
            .theta(Math.PI)
            .build();
        NodePosition position = NodePosition.builder()
            .x(Double.NaN)
            .y(Double.NEGATIVE_INFINITY)
            .theta(Math.nextUp(Math.PI))
            .allowedDeviationXY(ellipse)
            .allowedDeviationTheta(-1.0D)
            .mapId("")
            .build();

        assertAll(
            () -> assertEquals(-1.0D, ellipse.a()),
            () -> assertEquals(Double.POSITIVE_INFINITY, ellipse.b()),
            () -> assertTrue(Double.isNaN(position.x())),
            () -> assertEquals(Double.NEGATIVE_INFINITY, position.y()),
            () -> assertEquals(Math.nextUp(Math.PI), position.theta()),
            () -> assertEquals(-1.0D, position.allowedDeviationTheta()),
            () -> assertEquals("", position.mapId())
        );
    }

    @Test
    @DisplayName("[VDA3-ORDER-001] 位置对象图只拒绝缺失必填引用")
    void rejectsMissingRequiredReferences() {
        assertAll(
            () -> assertThrows(
                NullPointerException.class,
                () -> AllowedDeviationXY.builder().b(1.0D).theta(0.0D).build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> AllowedDeviationXY.builder().a(1.0D).theta(0.0D).build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> AllowedDeviationXY.builder().a(1.0D).b(1.0D).build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> NodePosition.builder().y(0.0D).mapId("map").build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> NodePosition.builder().x(0.0D).mapId("map").build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> NodePosition.builder().x(0.0D).y(0.0D).build()
            ),
            () -> assertTrue(
                ellipse(null).build().extensionFields().isEmpty()
            ),
            () -> assertTrue(
                minimalPosition().extensionFields(null).build().extensionFields().isEmpty()
            )
        );
    }

    @Test
    @DisplayName("[VDA3-ORDER-001] AllowedDeviationXY 值相等覆盖全部字段")
    void includesEveryEllipseFieldInValueEquality() throws ReflectiveOperationException {
        ExtensionFields extensions = extensionFields("{\"vendor\":true}");
        AllowedDeviationXY equal = ellipse(extensions).build();
        AllowedDeviationXY same = ellipse(extensions).build();

        assertAll(
            () -> assertEquals(equal, equal),
            () -> assertNotEquals(equal, null),
            () -> assertNotEquals(equal, "ellipse"),
            () -> assertNotEquals(equal, ellipse(extensions).a(3.0D).build()),
            () -> assertNotEquals(equal, ellipse(extensions).b(0.5D).build()),
            () -> assertNotEquals(equal, ellipse(extensions).theta(0.25D).build()),
            () -> assertNotEquals(equal, ellipse(ExtensionFields.empty()).build()),
            () -> assertEquals(equal, same),
            () -> assertEquals(equal.hashCode(), same.hashCode())
        );
    }

    @Test
    @DisplayName("[VDA3-ORDER-001] NodePosition 值相等覆盖全部字段")
    void includesEveryNodePositionFieldInValueEquality()
        throws ReflectiveOperationException {
        ExtensionFields extensions = extensionFields("{\"vendor\":true}");
        NodePosition equal = fullPosition(extensions).build();
        NodePosition same = fullPosition(extensions).build();

        assertAll(
            () -> assertEquals(equal, equal),
            () -> assertNotEquals(equal, null),
            () -> assertNotEquals(equal, "position"),
            () -> assertNotEquals(equal, fullPosition(extensions).x(9.0D).build()),
            () -> assertNotEquals(equal, fullPosition(extensions).y(9.0D).build()),
            () -> assertNotEquals(equal, fullPosition(extensions).theta(0.0D).build()),
            () -> assertNotEquals(
                equal,
                fullPosition(extensions)
                    .allowedDeviationXY(ellipse(extensions).a(3.0D).build())
                    .build()
            ),
            () -> assertNotEquals(
                equal,
                fullPosition(extensions).allowedDeviationTheta(0.25D).build()
            ),
            () -> assertNotEquals(equal, fullPosition(extensions).mapId("other").build()),
            () -> assertNotEquals(
                equal,
                fullPosition(extensions)
                    .extensionFields(ExtensionFields.empty())
                    .build()
            ),
            () -> assertEquals(equal, same),
            () -> assertEquals(equal.hashCode(), same.hashCode())
        );
    }

    @Test
    @DisplayName("[VDA3-ORDER-001] 位置类型只公开规范字段")
    void exposesOnlyTheSpecifiedWireFields() {
        assertAll(
            () -> assertEquals(
                Set.of("a", "b", "theta", "extensionFields"),
                fieldNames(AllowedDeviationXY.class)
            ),
            () -> assertEquals(
                Set.of(
                    "x",
                    "y",
                    "theta",
                    "allowedDeviationXY",
                    "allowedDeviationTheta",
                    "mapId",
                    "extensionFields"
                ),
                fieldNames(NodePosition.class)
            )
        );
    }

    private static AllowedDeviationXY.Builder ellipse(
        ExtensionFields extensionFields
    ) {
        return AllowedDeviationXY.builder()
            .a(2.0D)
            .b(1.0D)
            .theta(0.5D)
            .extensionFields(extensionFields);
    }

    private static NodePosition.Builder minimalPosition() {
        return NodePosition.builder()
            .x(1.25D)
            .y(-2.5D)
            .mapId("map-1");
    }

    private static NodePosition.Builder fullPosition(
        ExtensionFields extensionFields
    ) {
        return minimalPosition()
            .theta(0.5D)
            .allowedDeviationXY(ellipse(extensionFields).build())
            .allowedDeviationTheta(0.75D)
            .extensionFields(extensionFields);
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
