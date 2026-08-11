package io.github.cooltheworld.rcs.protocol.vda5050.v3.codec.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.codec.JsonCodecLimits;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

final class JsonPreflightScannerTest {
    private static final ObjectMapper MAPPER = JsonMapper.builder().build();
    private static final JsonCodecLimits LIMITS = JsonCodecLimits.defaults();

    @Test
    @DisplayName("[VDA3-SHARED-009] 预检拒绝被破坏的容器 Token 不变量")
    void rejectsCorruptedContainerTokenSequences() throws Exception {
        Class<?> stateType = Class.forName(
            "io.github.cooltheworld.rcs.protocol.vda5050.v3.codec.internal."
                + "JsonPreflightScanner$ContainerState"
        );
        Method objectFactory = accessible(stateType.getDeclaredMethod(
            "object",
            boolean.class,
            Map.class,
            JavaType.class
        ));
        Method arrayFactory = accessible(stateType.getDeclaredMethod(
            "array",
            boolean.class,
            JavaType.class
        ));
        Method acceptField = accessible(stateType.getDeclaredMethod(
            "acceptField",
            String.class,
            String.class,
            JsonCodecLimits.class
        ));
        Method consumeValue = accessible(stateType.getDeclaredMethod(
            "consumeValue",
            String.class,
            JsonCodecLimits.class
        ));
        Method close = accessible(stateType.getDeclaredMethod(
            "close",
            JsonToken.class
        ));
        JavaType untyped = MAPPER.constructType(Object.class);

        Object array = arrayFactory.invoke(null, true, untyped);
        Object pendingObject = objectFactory.invoke(
            null,
            true,
            Map.of(),
            untyped
        );
        Object missingValueObject = objectFactory.invoke(
            null,
            true,
            Map.of(),
            untyped
        );
        Object mismatchedCloseObject = objectFactory.invoke(
            null,
            true,
            Map.of(),
            untyped
        );
        Object pendingCloseObject = objectFactory.invoke(
            null,
            true,
            Map.of(),
            untyped
        );

        acceptField.invoke(pendingObject, "first", "/first", LIMITS);
        acceptField.invoke(pendingCloseObject, "first", "/first", LIMITS);

        assertPreflightFailure(
            () -> acceptField.invoke(array, "field", "/field", LIMITS)
        );
        assertPreflightFailure(
            () -> acceptField.invoke(pendingObject, "second", "/second", LIMITS)
        );
        assertPreflightFailure(
            () -> consumeValue.invoke(missingValueObject, "", LIMITS)
        );
        assertPreflightFailure(
            () -> close.invoke(mismatchedCloseObject, JsonToken.END_ARRAY)
        );
        assertPreflightFailure(
            () -> close.invoke(pendingCloseObject, JsonToken.END_OBJECT)
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-009] 预检拒绝没有所属容器的结构 Token")
    void rejectsStructuralTokenWithoutContainer() throws Exception {
        Method requireContainer = accessible(
            JsonPreflightScanner.class.getDeclaredMethod(
                "requireContainer",
                java.util.Deque.class,
                JsonParser.class
            )
        );

        try (JsonParser parser = MAPPER.createParser("{}")) {
            parser.nextToken();
            assertPreflightFailure(
                () -> requireContainer.invoke(null, new ArrayDeque<>(), parser)
            );
        }
    }

    @Test
    @DisplayName("[VDA3-SHARED-010] 错误路径忽略没有字段名和索引的未知引用")
    void ignoresUnaddressableMappingReference() throws Exception {
        JsonMappingException exception = JsonMappingException.from(
            (JsonParser) null,
            "failure"
        );
        exception.prependPath(new Object(), -1);
        Method pathOf = accessible(
            JacksonVda5050JsonCodec.class.getDeclaredMethod(
                "pathOf",
                JsonMappingException.class
            )
        );

        assertEquals("", pathOf.invoke(null, exception));
    }

    private static Method accessible(Method method) {
        method.setAccessible(true);
        return method;
    }

    private static void assertPreflightFailure(Executable executable) {
        InvocationTargetException failure = assertThrows(
            InvocationTargetException.class,
            executable
        );
        assertInstanceOf(JsonPreflightException.class, failure.getCause());
    }
}
