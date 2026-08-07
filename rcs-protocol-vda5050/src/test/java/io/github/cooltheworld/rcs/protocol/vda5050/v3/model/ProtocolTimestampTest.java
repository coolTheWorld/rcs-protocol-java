package io.github.cooltheworld.rcs.protocol.vda5050.v3.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.ValueSource;

final class ProtocolTimestampTest {
    @Test
    @DisplayName("[VDA3-SHARED-001] Instant 输入截断到毫秒并输出规范 UTC 格式")
    void truncatesInstantToCanonicalMillisecondPrecision() {
        ProtocolTimestamp timestamp = ProtocolTimestamp.from(
            Instant.parse("2017-04-15T11:40:03.123987654Z")
        );

        assertEquals(Instant.parse("2017-04-15T11:40:03.123Z"), timestamp.instant());
        assertEquals("2017-04-15T11:40:03.123Z", timestamp.toString());
    }

    @Test
    @DisplayName("[VDA3-SHARED-001] 规范时间戳严格往返")
    void parsesAndRoundTripsCanonicalTimestamp() {
        String value = "2017-04-15T11:40:03.123Z";

        ProtocolTimestamp timestamp = ProtocolTimestamp.parse(value);

        assertEquals(value, timestamp.toString());
        assertEquals(timestamp, ProtocolTimestamp.from(timestamp.instant()));
        assertEquals(timestamp.hashCode(), ProtocolTimestamp.from(timestamp.instant()).hashCode());
    }

    @ParameterizedTest(name = "[VDA3-SHARED-001] 拒绝非规范时间戳：{0}")
    @EmptySource
    @ValueSource(strings = {
        "2017-04-15T11:40:03Z",
        "2017-04-15T11:40:03.1Z",
        "2017-04-15T11:40:03.12Z",
        "2017-04-15T11:40:03.1234Z",
        "2017-04-15T11:40:03.123+00:00",
        "2017-04-15T11:40:03.123z",
        " 2017-04-15T11:40:03.123Z",
        "2017-04-15T11:40:03.123Z ",
        "2017-02-30T11:40:03.123Z",
        "2016-12-31T23:59:60.123Z",
        "2017-04-15T24:00:00.000Z"
    })
    void rejectsNonCanonicalTimestamp(String value) {
        assertThrows(IllegalArgumentException.class, () -> ProtocolTimestamp.parse(value));
    }

    @Test
    void rejectsNullInputs() {
        assertThrows(NullPointerException.class, () -> ProtocolTimestamp.parse(null));
        assertThrows(NullPointerException.class, () -> ProtocolTimestamp.from(null));
    }

    @Test
    void rejectsInstantOutsideFourDigitYearRange() {
        assertThrows(
            IllegalArgumentException.class,
            () -> ProtocolTimestamp.from(Instant.parse("+10000-01-01T00:00:00Z"))
        );
    }

    @Test
    void usesValueEquality() {
        ProtocolTimestamp timestamp = ProtocolTimestamp.parse("2017-04-15T11:40:03.123Z");

        assertEquals(timestamp, timestamp);
        assertNotEquals(
            timestamp,
            ProtocolTimestamp.parse("2017-04-15T11:40:03.124Z")
        );
        assertNotEquals(timestamp, null);
        assertNotEquals(timestamp, timestamp.toString());
    }
}
