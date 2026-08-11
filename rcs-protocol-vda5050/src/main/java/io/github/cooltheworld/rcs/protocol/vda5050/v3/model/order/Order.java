package io.github.cooltheworld.rcs.protocol.vda5050.v3.model.order;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.ExtensionFields;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.common.ProtocolHeader;
import java.util.List;
import java.util.Objects;

/** VDA 5050 v3.0.0 不可变 Order（订单）根消息。 */
public final class Order {
    private final ProtocolHeader header;
    private final String orderId;
    private final Long orderUpdateId;
    private final String orderDescription;
    private final List<Node> nodes;
    private final List<Edge> edges;
    private final ExtensionFields extensionFields;

    private Order(Builder builder) {
        this.header = Objects.requireNonNull(builder.header, "header");
        this.orderId = Objects.requireNonNull(builder.orderId, "orderId");
        this.orderUpdateId = Objects.requireNonNull(
            builder.orderUpdateId,
            "orderUpdateId"
        );
        this.orderDescription = builder.orderDescription;
        this.nodes = List.copyOf(Objects.requireNonNull(
            builder.nodes,
            "nodes"
        ));
        this.edges = List.copyOf(Objects.requireNonNull(
            builder.edges,
            "edges"
        ));
        this.extensionFields = builder.extensionFields == null
            ? ExtensionFields.empty()
            : builder.extensionFields;
    }

    /** @return 新的 Order Builder */
    public static Builder builder() {
        return new Builder();
    }

    /** @return 必填的公共协议 Header */
    public ProtocolHeader header() {
        return header;
    }

    /** @return 保持原文的订单标识 */
    public String orderId() {
        return orderId;
    }

    /** @return 订单更新号 */
    public Long orderUpdateId() {
        return orderUpdateId;
    }

    /** @return 可选的人类可读订单说明 */
    public String orderDescription() {
        return orderDescription;
    }

    /** @return 不可变的节点列表 */
    public List<Node> nodes() {
        return nodes;
    }

    /** @return 不可变的边列表 */
    public List<Edge> edges() {
        return edges;
    }

    /** @return 不透明保存的根级未知扩展字段 */
    public ExtensionFields extensionFields() {
        return extensionFields;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
            || other instanceof Order that
                && header.equals(that.header)
                && orderId.equals(that.orderId)
                && orderUpdateId.equals(that.orderUpdateId)
                && Objects.equals(orderDescription, that.orderDescription)
                && nodes.equals(that.nodes)
                && edges.equals(that.edges)
                && extensionFields.equals(that.extensionFields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            header,
            orderId,
            orderUpdateId,
            orderDescription,
            nodes,
            edges,
            extensionFields
        );
    }

    /** Order Builder。 */
    public static final class Builder {
        private ProtocolHeader header;
        private String orderId;
        private Long orderUpdateId;
        private String orderDescription;
        private List<Node> nodes;
        private List<Edge> edges;
        private ExtensionFields extensionFields;

        private Builder() {}

        /** @param header 必填的公共协议 Header @return 当前 Builder */
        public Builder header(ProtocolHeader header) {
            this.header = header;
            return this;
        }

        /** @param orderId 保持原文的订单标识 @return 当前 Builder */
        public Builder orderId(String orderId) {
            this.orderId = orderId;
            return this;
        }

        /** @param orderUpdateId 订单更新号 @return 当前 Builder */
        public Builder orderUpdateId(Long orderUpdateId) {
            this.orderUpdateId = orderUpdateId;
            return this;
        }

        /** @param orderDescription 可选的人类可读订单说明 @return 当前 Builder */
        public Builder orderDescription(String orderDescription) {
            this.orderDescription = orderDescription;
            return this;
        }

        /** @param nodes 必填且允许为空的节点列表 @return 当前 Builder */
        public Builder nodes(List<Node> nodes) {
            this.nodes = nodes;
            return this;
        }

        /** @param edges 必填且允许为空的边列表 @return 当前 Builder */
        public Builder edges(List<Edge> edges) {
            this.edges = edges;
            return this;
        }

        /** @param extensionFields 未知扩展字段 @return 当前 Builder */
        public Builder extensionFields(ExtensionFields extensionFields) {
            this.extensionFields = extensionFields;
            return this;
        }

        /** @return 必填字段完整的不可变 Order */
        public Order build() {
            return new Order(this);
        }
    }
}
