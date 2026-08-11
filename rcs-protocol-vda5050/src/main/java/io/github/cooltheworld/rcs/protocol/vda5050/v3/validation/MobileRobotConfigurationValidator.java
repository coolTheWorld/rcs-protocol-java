package io.github.cooltheworld.rcs.protocol.vda5050.v3.validation;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.internal.Unsigned32;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.BatteryCharging;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.MobileRobotConfiguration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** 对 Factsheet {@code mobileRobotConfiguration} 执行上下文无关语义校验。 */
public final class MobileRobotConfigurationValidator {
    private static final String REQUIREMENT_ID = "VDA3-FACTSHEET-004";
    private static final String BATTERY_PATH = "/batteryCharging";

    private MobileRobotConfigurationValidator() {}

    /** @return 可缓存复用且线程安全的 Mobile Robot 配置 Validator */
    public static MobileRobotConfigurationValidator create() {
        return new MobileRobotConfigurationValidator();
    }

    /**
     * 校验充电百分比、期望区间与最小充电时间。
     *
     * <p>网络信息只作为数据保存；运行期不变规则需要历史状态，不在此入口伪造。</p>
     *
     * @param configuration 已完成强类型绑定的 Mobile Robot 配置片段
     * @return 按字段顺序排列的不可变问题列表
     */
    public List<ValidationIssue> validate(
        MobileRobotConfiguration configuration
    ) {
        Objects.requireNonNull(configuration, "configuration");
        List<ValidationIssue> issues = new ArrayList<>();
        BatteryCharging charging = configuration.batteryCharging();
        if (charging == null) {
            return List.copyOf(issues);
        }
        chargingLevel(
            charging.criticalLowChargingLevel(),
            BATTERY_PATH + "/criticalLowChargingLevel",
            issues
        );
        chargingLevel(
            charging.minimumDesiredChargingLevel(),
            BATTERY_PATH + "/minimumDesiredChargingLevel",
            issues
        );
        chargingLevel(
            charging.maximumDesiredChargingLevel(),
            BATTERY_PATH + "/maximumDesiredChargingLevel",
            issues
        );
        desiredRange(
            charging.minimumDesiredChargingLevel(),
            charging.maximumDesiredChargingLevel(),
            issues
        );
        chargingTime(charging.minimumChargingTime(), issues);
        return List.copyOf(issues);
    }

    private static void chargingLevel(
        Double value,
        String path,
        List<ValidationIssue> issues
    ) {
        if (value == null) {
            return;
        }
        if (!Double.isFinite(value)) {
            issues.add(issue(
                "NON_FINITE_CHARGING_LEVEL",
                path,
                "Charging level must be finite"
            ));
            return;
        }
        if (value < 0.0D || value > 100.0D) {
            issues.add(issue(
                "CHARGING_LEVEL_OUT_OF_RANGE",
                path,
                "Charging level must be between 0 and 100"
            ));
        }
    }

    private static void desiredRange(
        Double minimum,
        Double maximum,
        List<ValidationIssue> issues
    ) {
        if (minimum == null || maximum == null) {
            return;
        }
        if (!Double.isFinite(minimum) || !Double.isFinite(maximum)) {
            return;
        }
        if (minimum > maximum) {
            issues.add(issue(
                "INVALID_CHARGING_RANGE",
                BATTERY_PATH + "/maximumDesiredChargingLevel",
                "Minimum desired charging level must not exceed maximum"
            ));
        }
    }

    private static void chargingTime(
        Long minimumChargingTime,
        List<ValidationIssue> issues
    ) {
        if (minimumChargingTime == null) {
            return;
        }
        if (!Unsigned32.isValid(minimumChargingTime)) {
            issues.add(issue(
                "CHARGING_TIME_OUT_OF_RANGE",
                BATTERY_PATH + "/minimumChargingTime",
                "Minimum charging time must be an unsigned 32-bit integer"
            ));
        }
    }

    private static ValidationIssue issue(
        String code,
        String path,
        String description
    ) {
        return new ValidationIssue(
            code,
            ValidationSeverity.ERROR,
            path,
            description,
            REQUIREMENT_ID
        );
    }
}
