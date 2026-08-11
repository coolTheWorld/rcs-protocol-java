package io.github.cooltheworld.rcs.protocol.vda5050.v3.model.order;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.ExtensionFields;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.action.Action;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.action.BlockingType;
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

final class NodeTest {
    @Test
    @DisplayName("[VDA3-ORDER-001] 最小 Node 要求 actions 引用但允许空列表")
    void buildsTheMinimalNodeWithAnEmptyActionList() {
        Node node = minimalNode()
            .nodeId(" Node/Main ")
            .build();

        assertAll(
            () -> assertEquals(" Node/Main ", node.nodeId()),
            () -> assertEquals(0L, node.sequenceId()),
            () -> assertEquals(Boolean.TRUE, node.released()),
            () -> assertEquals(List.of(), node.actions()),
            () -> assertNull(node.nodeDescriptor()),
            () -> assertNull(node.nodePosition()),
            () -> assertTrue(node.extensionFields().isEmpty())
        );
    }

    @Test
    @DisplayName("[VDA3-ORDER-001] 完整 Node 防御性复制并保持 Action 顺序")
    void buildsTheCompleteImmutableNode() throws ReflectiveOperationException {
        Action first = action("first");
        Action second = action("second");
        List<Action> actions = new ArrayList<>(List.of(first, second));
        NodePosition position = position();
        ExtensionFields extensions = extensionFields("{\"vendor\":true}");

        Node node = minimalNode()
            .sequenceId(2L)
            .nodeDescriptor(" human-readable ")
            .nodePosition(position)
            .actions(actions)
            .extensionFields(extensions)
            .build();
        actions.clear();

        assertAll(
            () -> assertEquals(2L, node.sequenceId()),
            () -> assertEquals(" human-readable ", node.nodeDescriptor()),
            () -> assertEquals(position, node.nodePosition()),
            () -> assertEquals(List.of(first, second), node.actions()),
            () -> assertEquals(extensions, node.extensionFields()),
            () -> assertThrows(
                UnsupportedOperationException.class,
                () -> node.actions().clear()
            )
        );
    }

    @Test
    @DisplayName("[VDA3-ORDER-002] Node 无损保留待 Validator 检查的 sequenceId")
    void preservesProgrammaticSequenceBoundariesForValidation() {
        Node belowRange = minimalNode().sequenceId(Long.MIN_VALUE).build();
        Node aboveRange = minimalNode().sequenceId(Long.MAX_VALUE).build();
        Node horizon = minimalNode().released(Boolean.FALSE).build();

        assertAll(
            () -> assertEquals(Long.MIN_VALUE, belowRange.sequenceId()),
            () -> assertEquals(Long.MAX_VALUE, aboveRange.sequenceId()),
            () -> assertEquals(Boolean.FALSE, horizon.released())
        );
    }

    @Test
    @DisplayName("[VDA3-ORDER-001] Node 拒绝缺失必填引用和 null Action 元素")
    void rejectsMissingRequiredReferencesAndNullActionElements() {
        Action action = action("action-1");

        assertAll(
            () -> assertThrows(
                NullPointerException.class,
                () -> Node.builder()
                    .sequenceId(0L)
                    .released(Boolean.TRUE)
                    .actions(List.of())
                    .build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> Node.builder()
                    .nodeId("node")
                    .released(Boolean.TRUE)
                    .actions(List.of())
                    .build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> Node.builder()
                    .nodeId("node")
                    .sequenceId(0L)
                    .actions(List.of())
                    .build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> Node.builder()
                    .nodeId("node")
                    .sequenceId(0L)
                    .released(Boolean.TRUE)
                    .build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> minimalNode().actions(Arrays.asList(action, null)).build()
            ),
            () -> assertTrue(
                minimalNode().extensionFields(null).build().extensionFields().isEmpty()
            )
        );
    }

    @Test
    @DisplayName("[VDA3-ORDER-001] Node 值相等覆盖全部线路字段")
    void includesEveryNodeFieldInValueEquality() throws ReflectiveOperationException {
        ExtensionFields extensions = extensionFields("{\"vendor\":true}");
        Node equal = fullNode(extensions).build();
        Node same = fullNode(extensions).build();

        assertAll(
            () -> assertEquals(equal, equal),
            () -> assertNotEquals(equal, null),
            () -> assertNotEquals(equal, "node"),
            () -> assertNotEquals(equal, fullNode(extensions).nodeId("other").build()),
            () -> assertNotEquals(equal, fullNode(extensions).sequenceId(4L).build()),
            () -> assertNotEquals(
                equal,
                fullNode(extensions).nodeDescriptor("other").build()
            ),
            () -> assertNotEquals(equal, fullNode(extensions).released(Boolean.FALSE).build()),
            () -> assertNotEquals(
                equal,
                fullNode(extensions)
                    .nodePosition(NodePosition.builder()
                        .x(9.0D)
                        .y(2.0D)
                        .mapId("map")
                        .build())
                    .build()
            ),
            () -> assertNotEquals(equal, fullNode(extensions).actions(List.of()).build()),
            () -> assertNotEquals(
                equal,
                fullNode(extensions)
                    .extensionFields(ExtensionFields.empty())
                    .build()
            ),
            () -> assertEquals(equal, same),
            () -> assertEquals(equal.hashCode(), same.hashCode())
        );
    }

    @Test
    @DisplayName("[VDA3-ORDER-001] Node 只公开规范字段")
    void exposesOnlyTheSpecifiedWireFields() {
        Set<String> fields = Arrays.stream(Node.class.getDeclaredFields())
            .map(Field::getName)
            .collect(Collectors.toUnmodifiableSet());

        assertEquals(
            Set.of(
                "nodeId",
                "sequenceId",
                "nodeDescriptor",
                "released",
                "nodePosition",
                "actions",
                "extensionFields"
            ),
            fields
        );
    }

    private static Node.Builder minimalNode() {
        return Node.builder()
            .nodeId("node-1")
            .sequenceId(0L)
            .released(Boolean.TRUE)
            .actions(List.of());
    }

    private static Node.Builder fullNode(ExtensionFields extensionFields) {
        return minimalNode()
            .sequenceId(2L)
            .nodeDescriptor("Node")
            .nodePosition(position())
            .actions(List.of(action("action-1")))
            .extensionFields(extensionFields);
    }

    private static NodePosition position() {
        return NodePosition.builder()
            .x(1.0D)
            .y(2.0D)
            .mapId("map")
            .build();
    }

    private static Action action(String actionId) {
        return Action.builder()
            .actionType("pick")
            .actionId(actionId)
            .blockingType(BlockingType.SOFT)
            .build();
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
