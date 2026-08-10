package io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.ExtensionFields;
import java.util.List;
import java.util.Objects;

/** Factsheet 中载荷处理位置的不可变说明。 */
public final class LoadSpecification {
    private final List<String> loadPositions;
    private final ExtensionFields extensionFields;

    private LoadSpecification(Builder builder) {
        this.loadPositions = builder.loadPositions == null
            ? null
            : List.copyOf(builder.loadPositions);
        this.extensionFields = builder.extensionFields == null
            ? ExtensionFields.empty()
            : builder.extensionFields;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * 返回载荷位置或载荷处理设备的名称。
     *
     * @return 可选的不可变位置列表；缺失与空列表保持不同线路语义
     */
    public List<String> loadPositions() {
        return loadPositions;
    }

    public ExtensionFields extensionFields() {
        return extensionFields;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
            || other instanceof LoadSpecification that
                && Objects.equals(loadPositions, that.loadPositions)
                && extensionFields.equals(that.extensionFields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(loadPositions, extensionFields);
    }

    public static final class Builder {
        private List<String> loadPositions;
        private ExtensionFields extensionFields;

        private Builder() {}

        public Builder loadPositions(List<String> loadPositions) {
            this.loadPositions = loadPositions;
            return this;
        }

        public Builder extensionFields(ExtensionFields extensionFields) {
            this.extensionFields = extensionFields;
            return this;
        }

        public LoadSpecification build() {
            return new LoadSpecification(this);
        }
    }
}
