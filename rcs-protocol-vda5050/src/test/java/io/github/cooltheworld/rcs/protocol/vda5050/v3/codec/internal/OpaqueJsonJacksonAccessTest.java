package io.github.cooltheworld.rcs.protocol.vda5050.v3.codec.internal;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

final class OpaqueJsonJacksonAccessTest {
    @Test
    @DisplayName("[VDA3-SHARED-007] 不透明 JSON 桥拒绝被破坏的非对象内部值")
    void rejectsCorruptedNonObjectOpaqueValue() throws Exception {
        Method objectReader = OpaqueJsonJacksonAccess.class.getDeclaredMethod(
            "object",
            ObjectMapper.class,
            byte[].class
        );
        objectReader.setAccessible(true);

        InvocationTargetException failure = assertThrows(
            InvocationTargetException.class,
            () -> objectReader.invoke(
                null,
                JsonMapper.builder().build(),
                "[]".getBytes(StandardCharsets.UTF_8)
            )
        );

        assertInstanceOf(IllegalStateException.class, failure.getCause());
    }
}
