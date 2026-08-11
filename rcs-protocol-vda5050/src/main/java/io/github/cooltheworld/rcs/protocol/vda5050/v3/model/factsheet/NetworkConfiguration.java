package io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.ExtensionFields;
import java.util.List;
import java.util.Objects;

/** Factsheet 中只作为数据保存的不可变网络配置元数据。 */
public final class NetworkConfiguration {
    private final List<String> dnsServers;
    private final List<String> ntpServers;
    private final String localIpAddress;
    private final String netmask;
    private final String defaultGateway;
    private final ExtensionFields extensionFields;

    private NetworkConfiguration(Builder builder) {
        this.dnsServers = builder.dnsServers == null
            ? null
            : List.copyOf(builder.dnsServers);
        this.ntpServers = builder.ntpServers == null
            ? null
            : List.copyOf(builder.ntpServers);
        this.localIpAddress = builder.localIpAddress;
        this.netmask = builder.netmask;
        this.defaultGateway = builder.defaultGateway;
        this.extensionFields = builder.extensionFields == null
            ? ExtensionFields.empty()
            : builder.extensionFields;
    }

    /** @return 空的网络配置 Builder */
    public static Builder builder() {
        return new Builder();
    }

    /** @return 可选的原始 DNS 服务器列表；缺失与空列表语义不同 */
    public List<String> dnsServers() {
        return dnsServers;
    }

    /** @return 可选的原始 NTP 服务器列表；缺失与空列表语义不同 */
    public List<String> ntpServers() {
        return ntpServers;
    }

    /** @return 可选的原始本地 IP 地址字符串 */
    public String localIpAddress() {
        return localIpAddress;
    }

    /** @return 可选的原始子网掩码字符串 */
    public String netmask() {
        return netmask;
    }

    /** @return 可选的原始默认网关字符串 */
    public String defaultGateway() {
        return defaultGateway;
    }

    /** @return 不透明保存的未知扩展字段 */
    public ExtensionFields extensionFields() {
        return extensionFields;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
            || other instanceof NetworkConfiguration that
                && Objects.equals(dnsServers, that.dnsServers)
                && Objects.equals(ntpServers, that.ntpServers)
                && Objects.equals(localIpAddress, that.localIpAddress)
                && Objects.equals(netmask, that.netmask)
                && Objects.equals(defaultGateway, that.defaultGateway)
                && extensionFields.equals(that.extensionFields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            dnsServers,
            ntpServers,
            localIpAddress,
            netmask,
            defaultGateway,
            extensionFields
        );
    }

    /** 用于构造全部字段均可选的网络配置元数据。 */
    public static final class Builder {
        private List<String> dnsServers;
        private List<String> ntpServers;
        private String localIpAddress;
        private String netmask;
        private String defaultGateway;
        private ExtensionFields extensionFields;

        private Builder() {}

        public Builder dnsServers(List<String> dnsServers) {
            this.dnsServers = dnsServers;
            return this;
        }

        public Builder ntpServers(List<String> ntpServers) {
            this.ntpServers = ntpServers;
            return this;
        }

        public Builder localIpAddress(String localIpAddress) {
            this.localIpAddress = localIpAddress;
            return this;
        }

        public Builder netmask(String netmask) {
            this.netmask = netmask;
            return this;
        }

        public Builder defaultGateway(String defaultGateway) {
            this.defaultGateway = defaultGateway;
            return this;
        }

        public Builder extensionFields(ExtensionFields extensionFields) {
            this.extensionFields = extensionFields;
            return this;
        }

        /** @return 不可变网络配置元数据 */
        public NetworkConfiguration build() {
            return new NetworkConfiguration(this);
        }
    }
}
