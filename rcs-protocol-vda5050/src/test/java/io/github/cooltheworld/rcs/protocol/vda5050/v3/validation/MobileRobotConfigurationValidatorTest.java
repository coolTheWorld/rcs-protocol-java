package io.github.cooltheworld.rcs.protocol.vda5050.v3.validation;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.BatteryCharging;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.MobileRobotConfiguration;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.NetworkConfiguration;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

final class MobileRobotConfigurationValidatorTest {
    private static final MobileRobotConfigurationValidator VALIDATOR =
        MobileRobotConfigurationValidator.create();

    @Test
    @DisplayName("[VDA3-FACTSHEET-004][VDA3-FACTSHEET-005] 合法配置保持原值且网络只作为数据")
    void acceptsValidBoundariesAndTreatsNetworkAsData() {
        NetworkConfiguration network = NetworkConfiguration.builder()
            .dnsServers(List.of("not an address"))
            .localIpAddress("vendor specific value")
            .build();
        MobileRobotConfiguration configuration = configuration(
            BatteryCharging.builder()
                .criticalLowChargingLevel(100.0D)
                .minimumDesiredChargingLevel(0.0D)
                .maximumDesiredChargingLevel(100.0D)
                .minimumChargingTime(4_294_967_295L)
                .build(),
            network
        );
        MobileRobotConfiguration snapshot = configuration(
            configuration.batteryCharging(),
            configuration.network()
        );

        List<ValidationIssue> issues = VALIDATOR.validate(configuration);

        assertAll(
            () -> assertEquals(List.of(), issues),
            () -> assertEquals(snapshot, configuration),
            () -> assertEquals(issues, VALIDATOR.validate(configuration)),
            () -> assertEquals(
                List.of(),
                VALIDATOR.validate(MobileRobotConfiguration.builder().build())
            ),
            () -> assertEquals(
                List.of(),
                VALIDATOR.validate(configuration(BatteryCharging.builder().build(), null))
            ),
            () -> assertEquals(
                List.of(),
                VALIDATOR.validate(configuration(
                    BatteryCharging.builder()
                        .minimumDesiredChargingLevel(20.0D)
                        .build(),
                    null
                ))
            ),
            () -> assertThrows(UnsupportedOperationException.class, issues::clear),
            () -> assertThrows(
                NullPointerException.class,
                () -> VALIDATOR.validate(null)
            )
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-004] 报告全部非有限充电百分比")
    void reportsEveryNonFiniteChargingLevel() {
        MobileRobotConfiguration allNonFinite = configuration(
            BatteryCharging.builder()
                .criticalLowChargingLevel(Double.NaN)
                .minimumDesiredChargingLevel(Double.POSITIVE_INFINITY)
                .maximumDesiredChargingLevel(Double.NEGATIVE_INFINITY)
                .build(),
            null
        );
        MobileRobotConfiguration nonFiniteMaximumAfterFiniteMinimum =
            configuration(
                BatteryCharging.builder()
                    .minimumDesiredChargingLevel(50.0D)
                    .maximumDesiredChargingLevel(Double.NaN)
                    .build(),
                null
            );

        List<ValidationIssue> issues = VALIDATOR.validate(allNonFinite);
        List<ValidationIssue> laterMaximumIssues = VALIDATOR.validate(
            nonFiniteMaximumAfterFiniteMinimum
        );

        assertAll(
            () -> assertEquals(
                List.of(
                    "/batteryCharging/criticalLowChargingLevel",
                    "/batteryCharging/minimumDesiredChargingLevel",
                    "/batteryCharging/maximumDesiredChargingLevel"
                ),
                paths(issues)
            ),
            () -> assertEquals(
                List.of("NON_FINITE_CHARGING_LEVEL"),
                codes(issues)
            ),
            () -> assertEquals(
                List.of("/batteryCharging/maximumDesiredChargingLevel"),
                paths(laterMaximumIssues)
            ),
            () -> assertTrue(issues.stream().allMatch(issue ->
                issue.severity() == ValidationSeverity.ERROR
                    && "VDA3-FACTSHEET-004".equals(issue.requirementId())
                    && "Charging level must be finite".equals(
                        issue.description()
                    )
            ))
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-004] 报告低于零或高于一百的充电百分比")
    void reportsEveryOutOfRangeChargingLevel() {
        MobileRobotConfiguration configuration = configuration(
            BatteryCharging.builder()
                .criticalLowChargingLevel(-1.0D)
                .minimumDesiredChargingLevel(-0.1D)
                .maximumDesiredChargingLevel(101.0D)
                .build(),
            null
        );

        List<ValidationIssue> issues = VALIDATOR.validate(configuration);

        assertAll(
            () -> assertEquals(
                List.of(
                    "/batteryCharging/criticalLowChargingLevel",
                    "/batteryCharging/minimumDesiredChargingLevel",
                    "/batteryCharging/maximumDesiredChargingLevel"
                ),
                paths(issues)
            ),
            () -> assertEquals(
                List.of("CHARGING_LEVEL_OUT_OF_RANGE"),
                codes(issues)
            ),
            () -> assertTrue(issues.stream().allMatch(issue ->
                "Charging level must be between 0 and 100".equals(
                    issue.description()
                )
            ))
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-004] 报告倒置期望区间但不推断临界电量关系")
    void reportsOnlyTheSpecifiedDesiredChargingRange() {
        MobileRobotConfiguration inverted = configuration(
            BatteryCharging.builder()
                .criticalLowChargingLevel(90.0D)
                .minimumDesiredChargingLevel(80.0D)
                .maximumDesiredChargingLevel(20.0D)
                .build(),
            null
        );
        MobileRobotConfiguration unrelatedCriticalLevel = configuration(
            BatteryCharging.builder()
                .criticalLowChargingLevel(90.0D)
                .minimumDesiredChargingLevel(20.0D)
                .maximumDesiredChargingLevel(50.0D)
                .build(),
            null
        );

        List<ValidationIssue> issues = VALIDATOR.validate(inverted);

        assertAll(
            () -> assertEquals(
                List.of("/batteryCharging/maximumDesiredChargingLevel"),
                paths(issues)
            ),
            () -> assertEquals(List.of("INVALID_CHARGING_RANGE"), codes(issues)),
            () -> assertEquals(
                "Minimum desired charging level must not exceed maximum",
                issues.getFirst().description()
            ),
            () -> assertEquals(
                List.of(),
                VALIDATOR.validate(unrelatedCriticalLevel)
            )
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-004] 充电时间严格使用 uint32 闭区间")
    void enforcesUnsigned32ChargingTime() {
        List<ValidationIssue> negative = VALIDATOR.validate(configuration(
            BatteryCharging.builder().minimumChargingTime(-1L).build(),
            null
        ));
        List<ValidationIssue> overflow = VALIDATOR.validate(configuration(
            BatteryCharging.builder()
                .minimumChargingTime(4_294_967_296L)
                .build(),
            null
        ));
        List<ValidationIssue> zero = VALIDATOR.validate(configuration(
            BatteryCharging.builder().minimumChargingTime(0L).build(),
            null
        ));

        assertAll(
            () -> assertEquals(
                List.of("/batteryCharging/minimumChargingTime"),
                paths(negative)
            ),
            () -> assertEquals(
                List.of("CHARGING_TIME_OUT_OF_RANGE"),
                codes(negative)
            ),
            () -> assertEquals(paths(negative), paths(overflow)),
            () -> assertEquals(codes(negative), codes(overflow)),
            () -> assertEquals(
                "Minimum charging time must be an unsigned 32-bit integer",
                negative.getFirst().description()
            ),
            () -> assertEquals(List.of(), zero)
        );
    }

    private static MobileRobotConfiguration configuration(
        BatteryCharging batteryCharging,
        NetworkConfiguration network
    ) {
        return MobileRobotConfiguration.builder()
            .network(network)
            .batteryCharging(batteryCharging)
            .build();
    }

    private static List<String> paths(List<ValidationIssue> issues) {
        return issues.stream().map(ValidationIssue::path).toList();
    }

    private static List<String> codes(List<ValidationIssue> issues) {
        return issues.stream().map(ValidationIssue::code).distinct().toList();
    }
}
