package io.github.cooltheworld.rcs.protocol.vda5050.v3.model.order;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.ExtensionFields;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.action.Action;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.action.BlockingType;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.trajectory.Trajectory;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.trajectory.TrajectoryControlPoint;
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

final class EdgeTest {
    @Test
    @DisplayName("[VDA3-ORDER-001] 最小 Edge 保留原文和空 Action 集合")
    void buildsTheMinimalEdge() {
        Edge edge = minimalEdge()
            .edgeId(" Edge/Main ")
            .actions(List.of())
            .build();

        assertAll(
            () -> assertEquals(" Edge/Main ", edge.edgeId()),
            () -> assertEquals(1L, edge.sequenceId()),
            () -> assertEquals(Boolean.FALSE, edge.released()),
            () -> assertEquals(List.of(), edge.actions()),
            () -> assertNull(edge.edgeDescriptor()),
            () -> assertNull(edge.maximumSpeed()),
            () -> assertNull(edge.maximumMobileRobotHeight()),
            () -> assertNull(edge.minimumLoadHandlingDeviceHeight()),
            () -> assertNull(edge.orientation()),
            () -> assertNull(edge.orientationType()),
            () -> assertNull(edge.direction()),
            () -> assertNull(edge.reachOrientationBeforeEntering()),
            () -> assertNull(edge.maximumRotationSpeed()),
            () -> assertNull(edge.trajectory()),
            () -> assertNull(edge.length()),
            () -> assertNull(edge.corridor()),
            () -> assertTrue(edge.extensionFields().isEmpty())
        );
    }

    @Test
    @DisplayName("[VDA3-ORDER-001][VDA3-ORDER-004][VDA3-SHARED-013] 完整 Edge 保存全部字段")
    void buildsTheCompleteEdge() throws ReflectiveOperationException {
        ExtensionFields extensions = extensionFields("{\"vendor\":true}");
        Action first = action("first");
        Action second = action("second");
        List<Action> input = new ArrayList<>(List.of(first, second));
        Edge edge = fullEdge(extensions).actions(input).build();
        input.clear();

        assertAll(
            () -> assertEquals("edge", edge.edgeId()),
            () -> assertEquals(3L, edge.sequenceId()),
            () -> assertEquals(" descriptor ", edge.edgeDescriptor()),
            () -> assertEquals(Boolean.TRUE, edge.released()),
            () -> assertEquals(2.5D, edge.maximumSpeed()),
            () -> assertEquals(3.5D, edge.maximumMobileRobotHeight()),
            () -> assertEquals(0.5D, edge.minimumLoadHandlingDeviceHeight()),
            () -> assertEquals(Math.PI / 2.0D, edge.orientation()),
            () -> assertEquals(
                EdgeOrientationType.GLOBAL,
                edge.orientationType()
            ),
            () -> assertEquals(" left ", edge.direction()),
            () -> assertEquals(
                Boolean.FALSE,
                edge.reachOrientationBeforeEntering()
            ),
            () -> assertEquals(0.75D, edge.maximumRotationSpeed()),
            () -> assertEquals(trajectory(), edge.trajectory()),
            () -> assertEquals(10.0D, edge.length()),
            () -> assertEquals(corridor(), edge.corridor()),
            () -> assertEquals(List.of(first, second), edge.actions()),
            () -> assertThrows(
                UnsupportedOperationException.class,
                () -> edge.actions().add(first)
            ),
            () -> assertEquals(extensions, edge.extensionFields())
        );
    }

    @Test
    @DisplayName("[VDA3-ORDER-003] 模型无损保留待 Validator 检查的程序化数值")
    void preservesProgrammaticNumericBoundariesForValidation() {
        Edge edge = minimalEdge()
            .sequenceId(-1L)
            .maximumSpeed(Double.NaN)
            .maximumMobileRobotHeight(Double.POSITIVE_INFINITY)
            .minimumLoadHandlingDeviceHeight(Double.NEGATIVE_INFINITY)
            .orientation(Math.nextUp(Math.PI))
            .maximumRotationSpeed(-1.0D)
            .length(-2.0D)
            .actions(List.of())
            .build();

        assertAll(
            () -> assertEquals(-1L, edge.sequenceId()),
            () -> assertTrue(Double.isNaN(edge.maximumSpeed())),
            () -> assertEquals(
                Double.POSITIVE_INFINITY,
                edge.maximumMobileRobotHeight()
            ),
            () -> assertEquals(
                Double.NEGATIVE_INFINITY,
                edge.minimumLoadHandlingDeviceHeight()
            ),
            () -> assertEquals(Math.nextUp(Math.PI), edge.orientation()),
            () -> assertEquals(-1.0D, edge.maximumRotationSpeed()),
            () -> assertEquals(-2.0D, edge.length())
        );
    }

    @Test
    @DisplayName("[VDA3-ORDER-001] Edge 拒绝缺失必填字段和空 Action 元素")
    void rejectsMissingRequiredFieldsAndNullActionElements() {
        assertAll(
            () -> assertThrows(
                NullPointerException.class,
                () -> Edge.builder()
                    .sequenceId(1L)
                    .released(Boolean.TRUE)
                    .actions(List.of())
                    .build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> Edge.builder()
                    .edgeId("edge")
                    .released(Boolean.TRUE)
                    .actions(List.of())
                    .build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> Edge.builder()
                    .edgeId("edge")
                    .sequenceId(1L)
                    .actions(List.of())
                    .build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> minimalEdge().build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> minimalEdge()
                    .actions(Arrays.asList(action("valid"), null))
                    .build()
            ),
            () -> assertTrue(
                minimalEdge()
                    .actions(List.of())
                    .extensionFields(null)
                    .build()
                    .extensionFields()
                    .isEmpty()
            )
        );
    }

    @Test
    @DisplayName("[VDA3-ORDER-001][VDA3-SHARED-013] Edge 值相等覆盖全部字段")
    void includesEveryFieldInValueEquality() throws ReflectiveOperationException {
        ExtensionFields extensions = extensionFields("{\"vendor\":true}");
        Edge equal = fullEdge(extensions).build();
        Edge same = fullEdge(extensions).build();

        assertAll(
            () -> assertEquals(equal, equal),
            () -> assertNotEquals(equal, null),
            () -> assertNotEquals(equal, "edge"),
            () -> assertNotEquals(equal, fullEdge(extensions).edgeId("other").build()),
            () -> assertNotEquals(equal, fullEdge(extensions).sequenceId(5L).build()),
            () -> assertNotEquals(
                equal,
                fullEdge(extensions).edgeDescriptor("other").build()
            ),
            () -> assertNotEquals(
                equal,
                fullEdge(extensions).released(Boolean.FALSE).build()
            ),
            () -> assertNotEquals(
                equal,
                fullEdge(extensions).maximumSpeed(9.0D).build()
            ),
            () -> assertNotEquals(
                equal,
                fullEdge(extensions).maximumMobileRobotHeight(9.0D).build()
            ),
            () -> assertNotEquals(
                equal,
                fullEdge(extensions)
                    .minimumLoadHandlingDeviceHeight(9.0D)
                    .build()
            ),
            () -> assertNotEquals(
                equal,
                fullEdge(extensions).orientation(0.0D).build()
            ),
            () -> assertNotEquals(
                equal,
                fullEdge(extensions)
                    .orientationType(EdgeOrientationType.TANGENTIAL)
                    .build()
            ),
            () -> assertNotEquals(
                equal,
                fullEdge(extensions).direction("right").build()
            ),
            () -> assertNotEquals(
                equal,
                fullEdge(extensions)
                    .reachOrientationBeforeEntering(Boolean.TRUE)
                    .build()
            ),
            () -> assertNotEquals(
                equal,
                fullEdge(extensions).maximumRotationSpeed(2.0D).build()
            ),
            () -> assertNotEquals(
                equal,
                fullEdge(extensions).trajectory(null).build()
            ),
            () -> assertNotEquals(
                equal,
                fullEdge(extensions).length(11.0D).build()
            ),
            () -> assertNotEquals(
                equal,
                fullEdge(extensions)
                    .corridor(Corridor.builder()
                        .leftWidth(9.0D)
                        .rightWidth(2.0D)
                        .build())
                    .build()
            ),
            () -> assertNotEquals(
                equal,
                fullEdge(extensions).actions(List.of(action("other"))).build()
            ),
            () -> assertNotEquals(
                equal,
                fullEdge(ExtensionFields.empty()).build()
            ),
            () -> assertEquals(equal, same),
            () -> assertEquals(equal.hashCode(), same.hashCode())
        );
    }

    @Test
    @DisplayName("[VDA3-ORDER-001][VDA3-ORDER-004] 字段和词汇匹配正文所有权")
    void exposesOnlyTheCurrentSpecifiedWireFieldsAndVocabulary() {
        Set<String> fields = fieldNames(Edge.class);

        assertAll(
            () -> assertEquals(
                Set.of("GLOBAL", "TANGENTIAL"),
                enumNames(EdgeOrientationType.class)
            ),
            () -> assertEquals(
                Set.of(
                    "edgeId",
                    "sequenceId",
                    "edgeDescriptor",
                    "released",
                    "maximumSpeed",
                    "maximumMobileRobotHeight",
                    "minimumLoadHandlingDeviceHeight",
                    "orientation",
                    "orientationType",
                    "direction",
                    "reachOrientationBeforeEntering",
                    "maximumRotationSpeed",
                    "trajectory",
                    "length",
                    "corridor",
                    "actions",
                    "extensionFields"
                ),
                fields
            ),
            () -> assertFalse(fields.contains("startNodeId")),
            () -> assertFalse(fields.contains("endNodeId")),
            () -> assertFalse(fields.contains("maxRotationSpeed"))
        );
    }

    private static Edge.Builder minimalEdge() {
        return Edge.builder()
            .edgeId("edge")
            .sequenceId(1L)
            .released(Boolean.FALSE);
    }

    private static Edge.Builder fullEdge(ExtensionFields extensionFields) {
        return Edge.builder()
            .edgeId("edge")
            .sequenceId(3L)
            .edgeDescriptor(" descriptor ")
            .released(Boolean.TRUE)
            .maximumSpeed(2.5D)
            .maximumMobileRobotHeight(3.5D)
            .minimumLoadHandlingDeviceHeight(0.5D)
            .orientation(Math.PI / 2.0D)
            .orientationType(EdgeOrientationType.GLOBAL)
            .direction(" left ")
            .reachOrientationBeforeEntering(Boolean.FALSE)
            .maximumRotationSpeed(0.75D)
            .trajectory(trajectory())
            .length(10.0D)
            .corridor(corridor())
            .actions(List.of(action("first"), action("second")))
            .extensionFields(extensionFields);
    }

    private static Corridor corridor() {
        return Corridor.builder()
            .leftWidth(1.0D)
            .rightWidth(2.0D)
            .build();
    }

    private static Trajectory trajectory() {
        return Trajectory.builder()
            .degree(1L)
            .knotVector(List.of(0.0D, 0.0D, 1.0D, 1.0D))
            .controlPoints(List.of(
                TrajectoryControlPoint.builder().x(0.0D).y(0.0D).build(),
                TrajectoryControlPoint.builder().x(1.0D).y(1.0D).build()
            ))
            .build();
    }

    private static Action action(String actionId) {
        return Action.builder()
            .actionType("vendor.action")
            .actionId(actionId)
            .blockingType(BlockingType.NONE)
            .build();
    }

    private static Set<String> fieldNames(Class<?> type) {
        return Arrays.stream(type.getDeclaredFields())
            .map(Field::getName)
            .collect(Collectors.toUnmodifiableSet());
    }

    private static Set<String> enumNames(Class<? extends Enum<?>> type) {
        return Arrays.stream(type.getEnumConstants())
            .map(Enum::name)
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
