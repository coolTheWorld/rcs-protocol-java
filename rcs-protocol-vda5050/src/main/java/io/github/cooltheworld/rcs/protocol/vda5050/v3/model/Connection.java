package io.github.cooltheworld.rcs.protocol.vda5050.v3.model;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.ExtensionFields;
import java.util.Objects;

/** VDA 5050 v3.0.0 {@code connection} Topic 的不可变消息。 */
public final class Connection {
    private final ProtocolHeader header;
    private final ConnectionState connectionState;
    private final ExtensionFields extensionFields;

    private Connection(Builder builder) {
        this.header = Objects.requireNonNull(builder.header, "header");
        this.connectionState = Objects.requireNonNull(
            builder.connectionState,
            "connectionState"
        );
        this.extensionFields = builder.extensionFields == null
            ? ExtensionFields.empty()
            : builder.extensionFields;
    }

    /**
     * 创建空的 Connection Builder。
     *
     * @return Connection Builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /** @return 公共协议消息头；在线路 JSON 中保持平铺 */
    public ProtocolHeader header() {
        return header;
    }

    /** @return 移动机器人与 MQTT Broker 的连接状态 */
    public ConnectionState connectionState() {
        return connectionState;
    }

    /** @return 不透明保存的未知扩展字段 */
    public ExtensionFields extensionFields() {
        return extensionFields;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
            || other instanceof Connection that
                && header.equals(that.header)
                && connectionState == that.connectionState
                && extensionFields.equals(that.extensionFields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(header, connectionState, extensionFields);
    }

    /** 用于构造必填字段完整的 {@link Connection}。 */
    public static final class Builder {
        private ProtocolHeader header;
        private ConnectionState connectionState;
        private ExtensionFields extensionFields;

        private Builder() {}

        /**
         * @param header 公共协议消息头
         * @return 当前 Builder
         */
        public Builder header(ProtocolHeader header) {
            this.header = header;
            return this;
        }

        /**
         * @param connectionState 连接状态
         * @return 当前 Builder
         */
        public Builder connectionState(ConnectionState connectionState) {
            this.connectionState = connectionState;
            return this;
        }

        /**
         * @param extensionFields 不透明未知扩展；未设置时使用空值
         * @return 当前 Builder
         */
        public Builder extensionFields(ExtensionFields extensionFields) {
            this.extensionFields = extensionFields;
            return this;
        }

        /**
         * 构造不可变 Connection 消息。
         *
         * @return 必填字段完整的 Connection
         * @throws NullPointerException 消息头或连接状态未设置时
         */
        public Connection build() {
            return new Connection(this);
        }
    }
}
