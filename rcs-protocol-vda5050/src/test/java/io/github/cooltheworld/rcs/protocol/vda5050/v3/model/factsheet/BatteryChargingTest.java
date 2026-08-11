package io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.ExtensionFields;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.internal.ExtensionFieldsJacksonSupport;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

final class BatteryChargingTest {
    @Test
    @DisplayName("[VDA3-FACTSHEET-001] 电池充电参数使用正确包装类型并保持可选")
    void buildsOptionalBatteryChargingParameters() {
        BatteryCharging charging = completeBuilder().build();
        BatteryCharging equalCharging = completeBuilder().build();
        BatteryCharging empty = BatteryCharging.builder().build();

        assertAll(
            () -> assertEquals(15.0D, charging.criticalLowChargingLevel()),
            () -> assertEquals(20.0D, charging.minimumDesiredChargingLevel()),
            () -> assertEquals(80.0D, charging.maximumDesiredChargingLevel()),
            () -> assertEquals(120L, charging.minimumChargingTime()),
            () -> assertNull(empty.criticalLowChargingLevel()),
            () -> assertNull(empty.minimumDesiredChargingLevel()),
            () -> assertNull(empty.maximumDesiredChargingLevel()),
            () -> assertNull(empty.minimumChargingTime()),
            () -> assertTrue(charging.extensionFields().isEmpty()),
            () -> assertEquals(charging, charging),
            () -> assertEquals(charging, equalCharging),
            () -> assertNotEquals(charging, null),
            () -> assertNotEquals(charging, "battery"),
            () -> assertEquals(charging.hashCode(), equalCharging.hashCode())
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] Builder 保留待 Validator 报告的原始充电边界")
    void preservesRawChargingBoundariesForValidation() {
        BatteryCharging charging = BatteryCharging.builder()
            .criticalLowChargingLevel(Double.NaN)
            .minimumDesiredChargingLevel(-1.0D)
            .maximumDesiredChargingLevel(101.0D)
            .minimumChargingTime(-1L)
            .build();

        assertAll(
            () -> assertEquals(
                Double.NaN,
                charging.criticalLowChargingLevel()
            ),
            () -> assertEquals(-1.0D, charging.minimumDesiredChargingLevel()),
            () -> assertEquals(101.0D, charging.maximumDesiredChargingLevel()),
            () -> assertEquals(-1L, charging.minimumChargingTime())
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] 充电参数全部字段和扩展参与值语义")
    void includesEveryChargingFieldInValueSemantics() throws Exception {
        BatteryCharging charging = completeBuilder().build();
        ObjectMapper mapper = new ObjectMapper();
        ExtensionFields extensions = ExtensionFieldsJacksonSupport.capture(
            mapper,
            mapper.createObjectNode().put("chargingProfile", "slow"),
            Set.of()
        );

        assertAll(
            () -> assertNotEquals(
                charging,
                completeBuilder().criticalLowChargingLevel(10.0D).build()
            ),
            () -> assertNotEquals(
                charging,
                completeBuilder().minimumDesiredChargingLevel(25.0D).build()
            ),
            () -> assertNotEquals(
                charging,
                completeBuilder().maximumDesiredChargingLevel(90.0D).build()
            ),
            () -> assertNotEquals(
                charging,
                completeBuilder().minimumChargingTime(180L).build()
            ),
            () -> assertNotEquals(
                charging,
                completeBuilder().extensionFields(extensions).build()
            )
        );
    }

    private static BatteryCharging.Builder completeBuilder() {
        return BatteryCharging.builder()
            .criticalLowChargingLevel(15.0D)
            .minimumDesiredChargingLevel(20.0D)
            .maximumDesiredChargingLevel(80.0D)
            .minimumChargingTime(120L)
            .extensionFields(ExtensionFields.empty());
    }
}
