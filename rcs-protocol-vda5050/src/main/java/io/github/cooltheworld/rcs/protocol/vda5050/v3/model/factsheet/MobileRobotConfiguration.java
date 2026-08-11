package io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.ExtensionFields;
import java.util.List;
import java.util.Objects;

/** Factsheet 中不可变的 Mobile Robot 配置聚合。 */
public final class MobileRobotConfiguration {
    private final List<VersionInfo> versions;
    private final NetworkConfiguration network;
    private final BatteryCharging batteryCharging;
    private final ExtensionFields extensionFields;

    private MobileRobotConfiguration(Builder builder) {
        this.versions = builder.versions == null
            ? null
            : List.copyOf(builder.versions);
        this.network = builder.network;
        this.batteryCharging = builder.batteryCharging;
        this.extensionFields = builder.extensionFields == null
            ? ExtensionFields.empty()
            : builder.extensionFields;
    }

    /** @return 空的 Mobile Robot 配置 Builder */
    public static Builder builder() {
        return new Builder();
    }

    /** @return 可选的不可变软件与硬件版本列表 */
    public List<VersionInfo> versions() {
        return versions;
    }

    /** @return 可选的网络配置元数据 */
    public NetworkConfiguration network() {
        return network;
    }

    /** @return 可选的电池充电参数 */
    public BatteryCharging batteryCharging() {
        return batteryCharging;
    }

    /** @return 不透明保存的未知扩展字段 */
    public ExtensionFields extensionFields() {
        return extensionFields;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
            || other instanceof MobileRobotConfiguration that
                && Objects.equals(versions, that.versions)
                && Objects.equals(network, that.network)
                && Objects.equals(batteryCharging, that.batteryCharging)
                && extensionFields.equals(that.extensionFields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            versions,
            network,
            batteryCharging,
            extensionFields
        );
    }

    /** 用于构造全部字段均可选的 Mobile Robot 配置。 */
    public static final class Builder {
        private List<VersionInfo> versions;
        private NetworkConfiguration network;
        private BatteryCharging batteryCharging;
        private ExtensionFields extensionFields;

        private Builder() {}

        public Builder versions(List<VersionInfo> versions) {
            this.versions = versions;
            return this;
        }

        public Builder network(NetworkConfiguration network) {
            this.network = network;
            return this;
        }

        public Builder batteryCharging(BatteryCharging batteryCharging) {
            this.batteryCharging = batteryCharging;
            return this;
        }

        public Builder extensionFields(ExtensionFields extensionFields) {
            this.extensionFields = extensionFields;
            return this;
        }

        /** @return 不可变 Mobile Robot 配置 */
        public MobileRobotConfiguration build() {
            return new MobileRobotConfiguration(this);
        }
    }
}
