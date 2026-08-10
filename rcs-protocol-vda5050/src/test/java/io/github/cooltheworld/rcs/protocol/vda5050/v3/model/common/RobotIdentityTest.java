package io.github.cooltheworld.rcs.protocol.vda5050.v3.model.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.ValueSource;

final class RobotIdentityTest {
    @Test
    @DisplayName("[VDA3-SHARED-004] 身份保留合法原文并精确区分大小写与 Unicode")
    void preservesExactLegalIdentityValues() {
        RobotIdentity identity = new RobotIdentity(" Café ", "Az09_.:-");

        assertEquals(" Café ", identity.manufacturer());
        assertEquals("Az09_.:-", identity.serialNumber());
        assertNotEquals(identity, new RobotIdentity(" CAFÉ ", "Az09_.:-"));
        assertNotEquals(
            new RobotIdentity("Café", "SN-1"),
            new RobotIdentity("Cafe\u0301", "SN-1")
        );
    }

    @ParameterizedTest(name = "[VDA3-SHARED-004] 拒绝不安全 manufacturer：{0}")
    @EmptySource
    @ValueSource(strings = {
        "AC/ME",
        "AC+ME",
        "AC#ME",
        "AC$ME",
        "AC\nME"
    })
    void rejectsEmptyOrTopicUnsafeManufacturer(String manufacturer) {
        assertThrows(
            IllegalArgumentException.class,
            () -> new RobotIdentity(manufacturer, "SN-1")
        );
    }

    @ParameterizedTest(name = "[VDA3-SHARED-004] 拒绝非法 serialNumber：{0}")
    @EmptySource
    @ValueSource(strings = {
        "SN/1",
        "SN+1",
        "SN#1",
        "SN$1",
        "SN 1",
        "序列号",
        "SN\n1"
    })
    void rejectsSerialNumberOutsideNormativeCharacterSet(String serialNumber) {
        assertThrows(
            IllegalArgumentException.class,
            () -> new RobotIdentity("ACME", serialNumber)
        );
    }

    @Test
    void rejectsNullAndControlCharacterInputs() {
        assertThrows(NullPointerException.class, () -> new RobotIdentity(null, "SN-1"));
        assertThrows(NullPointerException.class, () -> new RobotIdentity("ACME", null));
        assertThrows(
            IllegalArgumentException.class,
            () -> new RobotIdentity("AC" + (char) 0, "SN-1")
        );
    }
}
