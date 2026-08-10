package io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.ExtensionFields;
import java.util.List;
import java.util.Objects;

/** Factsheet 中描述一个移动机器人类型系列的不可变类型说明。 */
public final class TypeSpecification {
    private final String seriesName;
    private final String seriesDescription;
    private final MobileRobotKinematics mobileRobotKinematics;
    private final MobileRobotClass mobileRobotClass;
    private final Double maximumLoadMass;
    private final List<LocalizationType> localizationTypes;
    private final List<NavigationType> navigationTypes;
    private final List<ZoneType> supportedZones;
    private final ExtensionFields extensionFields;

    private TypeSpecification(Builder builder) {
        this.seriesName = Objects.requireNonNull(builder.seriesName, "seriesName");
        this.seriesDescription = builder.seriesDescription;
        this.mobileRobotKinematics = Objects.requireNonNull(
            builder.mobileRobotKinematics,
            "mobileRobotKinematics"
        );
        this.mobileRobotClass = Objects.requireNonNull(
            builder.mobileRobotClass,
            "mobileRobotClass"
        );
        this.maximumLoadMass = Objects.requireNonNull(
            builder.maximumLoadMass,
            "maximumLoadMass"
        );
        this.localizationTypes = List.copyOf(Objects.requireNonNull(
            builder.localizationTypes,
            "localizationTypes"
        ));
        this.navigationTypes = List.copyOf(Objects.requireNonNull(
            builder.navigationTypes,
            "navigationTypes"
        ));
        this.supportedZones = builder.supportedZones == null
            ? null
            : List.copyOf(builder.supportedZones);
        this.extensionFields = builder.extensionFields == null
            ? ExtensionFields.empty()
            : builder.extensionFields;
    }

    /** @return 空的类型说明 Builder */
    public static Builder builder() {
        return new Builder();
    }

    /** @return 移动机器人类型系列名称 */
    public String seriesName() {
        return seriesName;
    }

    /** @return 可选的人类可读系列说明，缺失时为 {@code null} */
    public String seriesDescription() {
        return seriesDescription;
    }

    /** @return 移动机器人运动学类型 */
    public MobileRobotKinematics mobileRobotKinematics() {
        return mobileRobotKinematics;
    }

    /** @return 移动机器人类别 */
    public MobileRobotClass mobileRobotClass() {
        return mobileRobotClass;
    }

    /** @return 最大载荷质量，单位 kg */
    public Double maximumLoadMass() {
        return maximumLoadMass;
    }

    /** @return 不可变定位类型列表 */
    public List<LocalizationType> localizationTypes() {
        return localizationTypes;
    }

    /** @return 按优先级排序的不可变导航类型列表 */
    public List<NavigationType> navigationTypes() {
        return navigationTypes;
    }

    /** @return 可选的不可变受支持区域类型列表，缺失时为 {@code null} */
    public List<ZoneType> supportedZones() {
        return supportedZones;
    }

    /** @return 不透明保存的未知扩展字段 */
    public ExtensionFields extensionFields() {
        return extensionFields;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
            || other instanceof TypeSpecification that
                && seriesName.equals(that.seriesName)
                && Objects.equals(seriesDescription, that.seriesDescription)
                && mobileRobotKinematics.equals(that.mobileRobotKinematics)
                && mobileRobotClass.equals(that.mobileRobotClass)
                && maximumLoadMass.equals(that.maximumLoadMass)
                && localizationTypes.equals(that.localizationTypes)
                && navigationTypes.equals(that.navigationTypes)
                && Objects.equals(supportedZones, that.supportedZones)
                && extensionFields.equals(that.extensionFields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            seriesName,
            seriesDescription,
            mobileRobotKinematics,
            mobileRobotClass,
            maximumLoadMass,
            localizationTypes,
            navigationTypes,
            supportedZones,
            extensionFields
        );
    }

    /** 用于构造必填字段完整的 {@link TypeSpecification}。 */
    public static final class Builder {
        private String seriesName;
        private String seriesDescription;
        private MobileRobotKinematics mobileRobotKinematics;
        private MobileRobotClass mobileRobotClass;
        private Double maximumLoadMass;
        private List<LocalizationType> localizationTypes;
        private List<NavigationType> navigationTypes;
        private List<ZoneType> supportedZones;
        private ExtensionFields extensionFields;

        private Builder() {}

        public Builder seriesName(String seriesName) {
            this.seriesName = seriesName;
            return this;
        }

        public Builder seriesDescription(String seriesDescription) {
            this.seriesDescription = seriesDescription;
            return this;
        }

        public Builder mobileRobotKinematics(
            MobileRobotKinematics mobileRobotKinematics
        ) {
            this.mobileRobotKinematics = mobileRobotKinematics;
            return this;
        }

        public Builder mobileRobotClass(MobileRobotClass mobileRobotClass) {
            this.mobileRobotClass = mobileRobotClass;
            return this;
        }

        public Builder maximumLoadMass(Double maximumLoadMass) {
            this.maximumLoadMass = maximumLoadMass;
            return this;
        }

        public Builder localizationTypes(List<LocalizationType> localizationTypes) {
            this.localizationTypes = localizationTypes;
            return this;
        }

        public Builder navigationTypes(List<NavigationType> navigationTypes) {
            this.navigationTypes = navigationTypes;
            return this;
        }

        public Builder supportedZones(List<ZoneType> supportedZones) {
            this.supportedZones = supportedZones;
            return this;
        }

        public Builder extensionFields(ExtensionFields extensionFields) {
            this.extensionFields = extensionFields;
            return this;
        }

        /** @return 必填字段完整的类型说明 */
        public TypeSpecification build() {
            return new TypeSpecification(this);
        }
    }
}
