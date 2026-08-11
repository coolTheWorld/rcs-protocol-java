package io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.ExtensionFields;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.common.ProtocolHeader;
import java.util.Objects;

/** VDA 5050 v3.0.0 不可变 Factsheet 根消息。 */
public final class Factsheet {
    private final ProtocolHeader header;
    private final FactsheetContent content;
    private final ExtensionFields extensionFields;

    private Factsheet(Builder builder) {
        header = Objects.requireNonNull(builder.header, "header");
        content = Objects.requireNonNull(builder.content, "content");
        extensionFields = builder.extensionFields == null
            ? ExtensionFields.empty()
            : builder.extensionFields;
    }

    /** @return 空的 Factsheet Builder */
    public static Builder builder() {
        return new Builder();
    }

    /** @return 必填的公共协议 Header */
    public ProtocolHeader header() {
        return header;
    }

    /** @return 必填的头部无关能力内容 */
    public FactsheetContent content() {
        return content;
    }

    /** @return 不透明保存的根级未知扩展字段 */
    public ExtensionFields extensionFields() {
        return extensionFields;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
            || other instanceof Factsheet that
                && header.equals(that.header)
                && content.equals(that.content)
                && extensionFields.equals(that.extensionFields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(header, content, extensionFields);
    }

    /** 用于构造 Factsheet 根消息。 */
    public static final class Builder {
        private ProtocolHeader header;
        private FactsheetContent content;
        private ExtensionFields extensionFields;

        private Builder() {}

        public Builder header(ProtocolHeader value) {
            header = value;
            return this;
        }

        public Builder content(FactsheetContent value) {
            content = value;
            return this;
        }

        public Builder extensionFields(ExtensionFields value) {
            extensionFields = value;
            return this;
        }

        /** @return 不可变 Factsheet 根消息 */
        public Factsheet build() {
            return new Factsheet(this);
        }
    }
}
