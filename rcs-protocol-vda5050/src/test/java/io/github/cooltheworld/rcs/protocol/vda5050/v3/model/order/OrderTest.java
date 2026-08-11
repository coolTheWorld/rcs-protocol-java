package io.github.cooltheworld.rcs.protocol.vda5050.v3.model.order;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.ExtensionFields;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.common.ProtocolHeader;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.common.ProtocolTimestamp;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.common.ProtocolVersion;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.common.RobotIdentity;
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

final class OrderTest {
    @Test
    @DisplayName("[VDA3-ORDER-001] 最小 Order 保留空 Node/Edge 列表和缺失说明")
    void buildsTheMinimalOrderWithEmptyLists() {
        Order order = minimalOrder()
            .orderId(" Order/Main ")
            .build();

        assertAll(
            () -> assertEquals(header(), order.header()),
            () -> assertEquals(" Order/Main ", order.orderId()),
            () -> assertEquals(0L, order.orderUpdateId()),
            () -> assertNull(order.orderDescription()),
            () -> assertEquals(List.of(), order.nodes()),
            () -> assertEquals(List.of(), order.edges()),
            () -> assertTrue(order.extensionFields().isEmpty())
        );
    }

    @Test
    @DisplayName("[VDA3-ORDER-001] 完整 Order 防御性复制并保持图列表顺序")
    void buildsTheCompleteImmutableOrder() throws ReflectiveOperationException {
        Node firstNode = node("n0", 0L);
        Node secondNode = node("n2", 2L);
        Edge edge = edge("e1", 1L);
        List<Node> nodes = new ArrayList<>(List.of(firstNode, secondNode));
        List<Edge> edges = new ArrayList<>(List.of(edge));
        ExtensionFields extensions = extensionFields("{\"vendor\":true}");

        Order order = minimalOrder()
            .orderUpdateId(7L)
            .orderDescription(" human-readable ")
            .nodes(nodes)
            .edges(edges)
            .extensionFields(extensions)
            .build();
        nodes.clear();
        edges.clear();

        assertAll(
            () -> assertEquals(7L, order.orderUpdateId()),
            () -> assertEquals(" human-readable ", order.orderDescription()),
            () -> assertEquals(List.of(firstNode, secondNode), order.nodes()),
            () -> assertEquals(List.of(edge), order.edges()),
            () -> assertEquals(extensions, order.extensionFields()),
            () -> assertThrows(
                UnsupportedOperationException.class,
                () -> order.nodes().clear()
            ),
            () -> assertThrows(
                UnsupportedOperationException.class,
                () -> order.edges().clear()
            )
        );
    }

    @Test
    @DisplayName("[VDA3-ORDER-001] Order 无损保留待 O06 校验的更新号和空图")
    void preservesProgrammaticGraphBoundariesForValidation() {
        Order belowRange = minimalOrder()
            .orderUpdateId(Long.MIN_VALUE)
            .build();
        Order aboveRange = minimalOrder()
            .orderUpdateId(Long.MAX_VALUE)
            .build();

        assertAll(
            () -> assertEquals(Long.MIN_VALUE, belowRange.orderUpdateId()),
            () -> assertEquals(Long.MAX_VALUE, aboveRange.orderUpdateId()),
            () -> assertTrue(belowRange.nodes().isEmpty()),
            () -> assertTrue(belowRange.edges().isEmpty())
        );
    }

    @Test
    @DisplayName("[VDA3-ORDER-001] Order 拒绝缺失必填引用和 null 图元素")
    void rejectsMissingRequiredReferencesAndNullGraphElements() {
        Node node = node("n0", 0L);
        Edge edge = edge("e1", 1L);

        assertAll(
            () -> assertThrows(
                NullPointerException.class,
                () -> minimalOrder().header(null).build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> minimalOrder().orderId(null).build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> minimalOrder().orderUpdateId(null).build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> minimalOrder().nodes(null).build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> minimalOrder().edges(null).build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> minimalOrder()
                    .nodes(Arrays.asList(node, null))
                    .build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> minimalOrder()
                    .edges(Arrays.asList(edge, null))
                    .build()
            ),
            () -> assertTrue(
                minimalOrder()
                    .extensionFields(null)
                    .build()
                    .extensionFields()
                    .isEmpty()
            )
        );
    }

    @Test
    @DisplayName("[VDA3-ORDER-001] Order 值相等覆盖全部线路字段")
    void includesEveryOrderFieldInValueEquality()
        throws ReflectiveOperationException {
        ExtensionFields extensions = extensionFields("{\"vendor\":true}");
        Order equal = fullOrder(extensions).build();
        Order same = fullOrder(extensions).build();

        assertAll(
            () -> assertEquals(equal, equal),
            () -> assertNotEquals(equal, null),
            () -> assertNotEquals(equal, "order"),
            () -> assertNotEquals(
                equal,
                fullOrder(extensions).header(otherHeader()).build()
            ),
            () -> assertNotEquals(
                equal,
                fullOrder(extensions).orderId("other").build()
            ),
            () -> assertNotEquals(
                equal,
                fullOrder(extensions).orderUpdateId(8L).build()
            ),
            () -> assertNotEquals(
                equal,
                fullOrder(extensions).orderDescription("other").build()
            ),
            () -> assertNotEquals(
                equal,
                fullOrder(extensions).nodes(List.of(node("other", 0L))).build()
            ),
            () -> assertNotEquals(
                equal,
                fullOrder(extensions).edges(List.of()).build()
            ),
            () -> assertNotEquals(
                equal,
                fullOrder(extensions)
                    .extensionFields(ExtensionFields.empty())
                    .build()
            ),
            () -> assertEquals(equal, same),
            () -> assertEquals(equal.hashCode(), same.hashCode())
        );
    }

    @Test
    @DisplayName("[VDA3-ORDER-001] Order 只公开正文根字段")
    void exposesOnlyTheSpecifiedWireFields() {
        Set<String> fields = Arrays.stream(Order.class.getDeclaredFields())
            .map(Field::getName)
            .collect(Collectors.toUnmodifiableSet());

        assertEquals(
            Set.of(
                "header",
                "orderId",
                "orderUpdateId",
                "orderDescription",
                "nodes",
                "edges",
                "extensionFields"
            ),
            fields
        );
    }

    private static Order.Builder minimalOrder() {
        return Order.builder()
            .header(header())
            .orderId("order-1")
            .orderUpdateId(0L)
            .nodes(List.of())
            .edges(List.of());
    }

    private static Order.Builder fullOrder(ExtensionFields extensionFields) {
        return minimalOrder()
            .orderUpdateId(7L)
            .orderDescription("Order")
            .nodes(List.of(node("n0", 0L), node("n2", 2L)))
            .edges(List.of(edge("e1", 1L)))
            .extensionFields(extensionFields);
    }

    private static ProtocolHeader header() {
        return ProtocolHeader.builder()
            .headerId(1L)
            .timestamp(ProtocolTimestamp.parse("2026-08-11T01:02:03.004Z"))
            .version(ProtocolVersion.parse("3.0.0"))
            .robotIdentity(new RobotIdentity("ACME", "MR-1"))
            .build();
    }

    private static ProtocolHeader otherHeader() {
        return ProtocolHeader.builder()
            .headerId(2L)
            .timestamp(ProtocolTimestamp.parse("2026-08-11T01:02:03.004Z"))
            .version(ProtocolVersion.parse("3.0.0"))
            .robotIdentity(new RobotIdentity("ACME", "MR-1"))
            .build();
    }

    private static Node node(String nodeId, Long sequenceId) {
        return Node.builder()
            .nodeId(nodeId)
            .sequenceId(sequenceId)
            .released(Boolean.TRUE)
            .actions(List.of())
            .build();
    }

    private static Edge edge(String edgeId, Long sequenceId) {
        return Edge.builder()
            .edgeId(edgeId)
            .sequenceId(sequenceId)
            .released(Boolean.TRUE)
            .actions(List.of())
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
