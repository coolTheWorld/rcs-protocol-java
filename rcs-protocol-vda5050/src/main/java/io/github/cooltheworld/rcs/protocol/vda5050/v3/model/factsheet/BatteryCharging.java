package io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.ExtensionFields;
import java.util.Objects;

/** Factsheet 中不可变的电池充电参数。 */
public final class BatteryCharging {
    private final Double criticalLowChargingLevel;
    private final Double minimumDesiredChargingLevel;
    private final Double maximumDesiredChargingLevel;
    private final Long minimumChargingTime;
    private final ExtensionFields extensionFields;

    private BatteryCharging(Builder builder) {
        this.criticalLowChargingLevel = builder.criticalLowChargingLevel;
        this.minimumDesiredChargingLevel = builder.minimumDesiredChargingLevel;
        this.maximumDesiredChargingLevel = builder.maximumDesiredChargingLevel;
        this.minimumChargingTime = builder.minimumChargingTime;
        this.extensionFields = builder.extensionFields == null
            ? ExtensionFields.empty()
            : builder.extensionFields;
    }

    /** @return 空的电池充电参数 Builder */
    public static Builder builder() {
        return new Builder();
    }

    /** @return 可选的临界低电量百分比 */
    public Double criticalLowChargingLevel() {
        return criticalLowChargingLevel;
    }

    /** @return 可选的最小期望充电百分比 */
    public Double minimumDesiredChargingLevel() {
        return minimumDesiredChargingLevel;
    }

    /** @return 可选的最大期望充电百分比 */
    public Double maximumDesiredChargingLevel() {
        return maximumDesiredChargingLevel;
    }

    /** @return 可选的最小充电时间，单位秒，线路类型为 uint32 */
    public Long minimumChargingTime() {
        return minimumChargingTime;
    }

    /** @return 不透明保存的未知扩展字段 */
    public ExtensionFields extensionFields() {
        return extensionFields;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
            || other instanceof BatteryCharging that
                && Objects.equals(
                    criticalLowChargingLevel,
                    that.criticalLowChargingLevel
                )
                && Objects.equals(
                    minimumDesiredChargingLevel,
                    that.minimumDesiredChargingLevel
                )
                && Objects.equals(
                    maximumDesiredChargingLevel,
                    that.maximumDesiredChargingLevel
                )
                && Objects.equals(
                    minimumChargingTime,
                    that.minimumChargingTime
                )
                && extensionFields.equals(that.extensionFields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            criticalLowChargingLevel,
            minimumDesiredChargingLevel,
            maximumDesiredChargingLevel,
            minimumChargingTime,
            extensionFields
        );
    }

    /** 用于构造全部字段均可选的电池充电参数。 */
    public static final class Builder {
        private Double criticalLowChargingLevel;
        private Double minimumDesiredChargingLevel;
        private Double maximumDesiredChargingLevel;
        private Long minimumChargingTime;
        private ExtensionFields extensionFields;

        private Builder() {}

        public Builder criticalLowChargingLevel(Double value) {
            this.criticalLowChargingLevel = value;
            return this;
        }

        public Builder minimumDesiredChargingLevel(Double value) {
            this.minimumDesiredChargingLevel = value;
            return this;
        }

        public Builder maximumDesiredChargingLevel(Double value) {
            this.maximumDesiredChargingLevel = value;
            return this;
        }

        public Builder minimumChargingTime(Long value) {
            this.minimumChargingTime = value;
            return this;
        }

        public Builder extensionFields(ExtensionFields extensionFields) {
            this.extensionFields = extensionFields;
            return this;
        }

        /** @return 不可变电池充电参数 */
        public BatteryCharging build() {
            return new BatteryCharging(this);
        }
    }
}
