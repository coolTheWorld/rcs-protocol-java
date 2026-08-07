package io.github.cooltheworld.rcs.protocol.vda5050.v3.codec;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.topic.TopicName;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.DecodedMessage;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.DecodingResult;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.RejectedInboundMessage;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unchecked")
final class Vda5050JsonCodecTest {
    private final Vda5050JsonCodec codec = Vda5050JsonCodec.createDefault();

    @Test
    @DisplayName("[VDA3-SHARED-010] UTF-8 编码省略标准可选 null")
    void encodesUtf8AndOmitsOptionalNull() {
        CodecProbe probe = new CodecProbe("机器人", null, 7L);

        String json = new String(codec.encode(probe), StandardCharsets.UTF_8);

        assertAll(
            () -> assertTrue(json.contains("\"required\":\"机器人\"")),
            () -> assertTrue(json.contains("\"count\":7")),
            () -> assertFalse(json.contains("optional"))
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-009] 合法 JSON 产生未验证的强类型消息")
    void decodesTypedMessageWithoutMintingValidationCredential() {
        byte[] payload = """
            {"required":"ok","count":7}
            """.getBytes(StandardCharsets.UTF_8);

        DecodingResult<CodecProbe> result = codec.decode(
            TopicName.CONNECTION,
            payload,
            CodecProbe.class
        );

        DecodedMessage<CodecProbe> decoded = decoded(result);
        assertAll(
            () -> assertTrue(decoded.isDecoded()),
            () -> assertEquals("ok", decoded.message().required()),
            () -> assertNull(decoded.message().optional()),
            () -> assertEquals(7L, decoded.message().count()),
            () -> assertTrue(decoded.issues().isEmpty())
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-009] 畸形 JSON 作为结构化拒绝数据返回")
    void reportsMalformedJsonAsData() {
        DecodingResult<CodecProbe> result = codec.decode(
            TopicName.ORDER,
            "{".getBytes(StandardCharsets.UTF_8),
            CodecProbe.class
        );

        RejectedInboundMessage<CodecProbe> rejected = assertInstanceOf(
            RejectedInboundMessage.class,
            result
        );
        assertAll(
            () -> assertFalse(rejected.isDecoded()),
            () -> assertEquals(TopicName.ORDER, rejected.topic()),
            () -> assertEquals("INVALID_JSON", rejected.issues().getFirst().code()),
            () -> assertEquals("", rejected.issues().getFirst().path()),
            () -> assertEquals(
                "VDA3-SHARED-009",
                rejected.issues().getFirst().requirementId()
            )
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-010] 标准字段显式 null 产生稳定 Issue")
    void reportsExplicitStandardNull() {
        DecodingResult<CodecProbe> result = codec.decode(
            TopicName.STATE,
            """
                {"required":"ok","optional":null,"count":7}
                """.getBytes(StandardCharsets.UTF_8),
            CodecProbe.class
        );

        RejectedInboundMessage<CodecProbe> rejected = assertInstanceOf(
            RejectedInboundMessage.class,
            result
        );
        assertAll(
            () -> assertEquals("EXPLICIT_NULL", rejected.issues().getFirst().code()),
            () -> assertEquals("/optional", rejected.issues().getFirst().path()),
            () -> assertEquals(
                "VDA3-SHARED-010",
                rejected.issues().getFirst().requirementId()
            )
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-010] 嵌套标准字段显式 null 保留准确路径")
    void reportsNestedExplicitStandardNull() {
        DecodingResult<CodecProbe> result = codec.decode(
            TopicName.STATE,
            """
                {"required":"ok","count":7,"nested":{"name":null}}
                """.getBytes(StandardCharsets.UTF_8),
            CodecProbe.class
        );

        RejectedInboundMessage<CodecProbe> rejected = assertInstanceOf(
            RejectedInboundMessage.class,
            result
        );
        assertAll(
            () -> assertEquals("EXPLICIT_NULL", rejected.issues().getFirst().code()),
            () -> assertEquals("/nested/name", rejected.issues().getFirst().path())
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-010] 未知扩展字段的 null 不按标准字段拒绝")
    void allowsUnknownExtensionNullForLaterExtensionBinding() {
        DecodingResult<CodecProbe> result = codec.decode(
            TopicName.STATE,
            """
                {"required":"ok","count":7,"vendor":{"nested":null}}
                """.getBytes(StandardCharsets.UTF_8),
            CodecProbe.class
        );

        assertInstanceOf(DecodedMessage.class, result);
    }

    @Test
    void reportsBasicTypeMismatch() {
        DecodingResult<CodecProbe> result = codec.decode(
            TopicName.STATE,
            """
                {"required":"ok","count":"seven"}
                """.getBytes(StandardCharsets.UTF_8),
            CodecProbe.class
        );

        RejectedInboundMessage<CodecProbe> rejected = assertInstanceOf(
            RejectedInboundMessage.class,
            result
        );
        assertAll(
            () -> assertEquals(
                "INVALID_JSON_TYPE",
                rejected.issues().getFirst().code()
            ),
            () -> assertEquals("/count", rejected.issues().getFirst().path())
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-009] 默认 Codec 不启用任意类名多态")
    void keepsDefaultTypingDisabled() {
        DecodingResult<DynamicProbe> result = codec.decode(
            TopicName.FACTSHEET,
            """
                {"value":["java.lang.Runtime",{}]}
                """.getBytes(StandardCharsets.UTF_8),
            DynamicProbe.class
        );

        DecodedMessage<DynamicProbe> decoded = decoded(result);
        List<?> value = assertInstanceOf(List.class, decoded.message().value());
        assertEquals("java.lang.Runtime", value.getFirst());
    }

    @Test
    void rejectsMissingProgrammingArguments() {
        byte[] payload = "{}".getBytes(StandardCharsets.UTF_8);

        assertAll(
            () -> assertThrows(
                NullPointerException.class,
                () -> codec.decode(null, payload, CodecProbe.class)
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> codec.decode(TopicName.STATE, null, CodecProbe.class)
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> codec.decode(TopicName.STATE, payload, null)
            ),
            () -> assertThrows(NullPointerException.class, () -> codec.encode(null)),
            () -> assertThrows(
                NullPointerException.class,
                () -> Vda5050JsonCodec.create(null)
            )
        );
    }

    private record CodecProbe(
        String required,
        String optional,
        Long count,
        NestedProbe nested
    ) {
        private CodecProbe(String required, String optional, Long count) {
            this(required, optional, count, null);
        }
    }

    private record NestedProbe(String name) {}

    private record DynamicProbe(Object value) {}

    @SuppressWarnings("unchecked")
    private static <T> DecodedMessage<T> decoded(DecodingResult<T> result) {
        return (DecodedMessage<T>) assertInstanceOf(DecodedMessage.class, result);
    }
}
