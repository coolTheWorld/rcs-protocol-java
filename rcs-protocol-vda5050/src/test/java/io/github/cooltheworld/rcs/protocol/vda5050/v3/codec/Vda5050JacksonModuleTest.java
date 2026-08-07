package io.github.cooltheworld.rcs.protocol.vda5050.v3.codec;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.ProtocolTimestamp;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.ProtocolVersion;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

final class Vda5050JacksonModuleTest {
    private static final ProtocolVersion VERSION = ProtocolVersion.parse("3.0.0");
    private static final ProtocolTimestamp TIMESTAMP = ProtocolTimestamp.from(
        Instant.parse("2026-08-07T05:00:00.123Z")
    );

    @Test
    @DisplayName("[VDA3-SHARED-010] Module 注册协议值类型但不修改调用方 null 策略")
    void registersProtocolValuesWithoutChangingCallerPolicy() throws Exception {
        ObjectMapper mapper = JsonMapper.builder()
            .addModule(new Vda5050JacksonModule())
            .build();
        ModuleProbe probe = new ModuleProbe(VERSION, TIMESTAMP, null);

        byte[] json = mapper.writeValueAsBytes(probe);
        JsonNode tree = mapper.readTree(json);
        ModuleProbe decoded = mapper.readValue(json, ModuleProbe.class);

        assertAll(
            () -> assertEquals("3.0.0", tree.path("version").textValue()),
            () -> assertEquals(
                "2026-08-07T05:00:00.123Z",
                tree.path("timestamp").textValue()
            ),
            () -> assertTrue(tree.has("optional")),
            () -> assertTrue(tree.path("optional").isNull()),
            () -> assertEquals(probe, decoded)
        );
    }

    private record ModuleProbe(
        ProtocolVersion version,
        ProtocolTimestamp timestamp,
        String optional
    ) {}
}
