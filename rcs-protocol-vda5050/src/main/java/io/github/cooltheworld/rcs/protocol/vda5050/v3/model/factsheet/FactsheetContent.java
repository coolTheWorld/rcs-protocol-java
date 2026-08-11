package io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet;

import java.util.Objects;

/**
 * Factsheet 中不含协议 Header 的不可变能力内容。
 *
 * <p>该聚合用于角色 Event 和根消息构造；Codec 会把字段写入 Factsheet 根对象，
 * 不会产生额外的 {@code content} 线路层级。</p>
 */
public final class FactsheetContent {
    private final TypeSpecification typeSpecification;
    private final PhysicalParameters physicalParameters;
    private final ProtocolLimits protocolLimits;
    private final ProtocolFeatures protocolFeatures;
    private final MobileRobotGeometry mobileRobotGeometry;
    private final LoadSpecification loadSpecification;
    private final MobileRobotConfiguration mobileRobotConfiguration;

    private FactsheetContent(Builder builder) {
        typeSpecification = Objects.requireNonNull(
            builder.typeSpecification,
            "typeSpecification"
        );
        physicalParameters = Objects.requireNonNull(
            builder.physicalParameters,
            "physicalParameters"
        );
        protocolLimits = Objects.requireNonNull(
            builder.protocolLimits,
            "protocolLimits"
        );
        protocolFeatures = Objects.requireNonNull(
            builder.protocolFeatures,
            "protocolFeatures"
        );
        mobileRobotGeometry = Objects.requireNonNull(
            builder.mobileRobotGeometry,
            "mobileRobotGeometry"
        );
        loadSpecification = Objects.requireNonNull(
            builder.loadSpecification,
            "loadSpecification"
        );
        mobileRobotConfiguration = builder.mobileRobotConfiguration;
    }

    /** @return 空的 Factsheet 内容 Builder */
    public static Builder builder() {
        return new Builder();
    }

    /** @return 必填的机器人类型说明 */
    public TypeSpecification typeSpecification() {
        return typeSpecification;
    }

    /** @return 必填的物理参数 */
    public PhysicalParameters physicalParameters() {
        return physicalParameters;
    }

    /** @return 必填的协议限制 */
    public ProtocolLimits protocolLimits() {
        return protocolLimits;
    }

    /** @return 必填的协议能力 */
    public ProtocolFeatures protocolFeatures() {
        return protocolFeatures;
    }

    /** @return 必填的 Mobile Robot 几何 */
    public MobileRobotGeometry mobileRobotGeometry() {
        return mobileRobotGeometry;
    }

    /** @return 必填的载荷说明 */
    public LoadSpecification loadSpecification() {
        return loadSpecification;
    }

    /** @return 可选的 Mobile Robot 配置 */
    public MobileRobotConfiguration mobileRobotConfiguration() {
        return mobileRobotConfiguration;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
            || other instanceof FactsheetContent that
                && typeSpecification.equals(that.typeSpecification)
                && physicalParameters.equals(that.physicalParameters)
                && protocolLimits.equals(that.protocolLimits)
                && protocolFeatures.equals(that.protocolFeatures)
                && mobileRobotGeometry.equals(that.mobileRobotGeometry)
                && loadSpecification.equals(that.loadSpecification)
                && Objects.equals(
                    mobileRobotConfiguration,
                    that.mobileRobotConfiguration
                );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            typeSpecification,
            physicalParameters,
            protocolLimits,
            protocolFeatures,
            mobileRobotGeometry,
            loadSpecification,
            mobileRobotConfiguration
        );
    }

    /** 用于构造必填能力片段与可选配置。 */
    public static final class Builder {
        private TypeSpecification typeSpecification;
        private PhysicalParameters physicalParameters;
        private ProtocolLimits protocolLimits;
        private ProtocolFeatures protocolFeatures;
        private MobileRobotGeometry mobileRobotGeometry;
        private LoadSpecification loadSpecification;
        private MobileRobotConfiguration mobileRobotConfiguration;

        private Builder() {}

        public Builder typeSpecification(TypeSpecification value) {
            typeSpecification = value;
            return this;
        }

        public Builder physicalParameters(PhysicalParameters value) {
            physicalParameters = value;
            return this;
        }

        public Builder protocolLimits(ProtocolLimits value) {
            protocolLimits = value;
            return this;
        }

        public Builder protocolFeatures(ProtocolFeatures value) {
            protocolFeatures = value;
            return this;
        }

        public Builder mobileRobotGeometry(MobileRobotGeometry value) {
            mobileRobotGeometry = value;
            return this;
        }

        public Builder loadSpecification(LoadSpecification value) {
            loadSpecification = value;
            return this;
        }

        public Builder mobileRobotConfiguration(
            MobileRobotConfiguration value
        ) {
            mobileRobotConfiguration = value;
            return this;
        }

        /** @return 不可变 Factsheet 内容 */
        public FactsheetContent build() {
            return new FactsheetContent(this);
        }
    }
}
