package io.github.cooltheworld.rcs.protocol.vda5050.v3.codec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.topic.TopicName;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.DecodedMessage;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.DecodingResult;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.RejectedInboundMessage;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("[VDA3-SHARED-009] JSON Codec 绑定前资源上限")
final class Vda5050JsonCodecLimitsTest {
    @Test
    void enforcesPayloadByteLimit() {
        Vda5050JsonCodec accepting = codec(
            JsonCodecLimits.builder().maxPayloadBytes(2).build()
        );
        Vda5050JsonCodec rejecting = codec(
            JsonCodecLimits.builder().maxPayloadBytes(1).build()
        );

        assertDecoded(accepting, "{}");
        RejectedInboundMessage<LimitProbe> rejected = rejected(
            rejecting,
            "{}",
            LimitProbe.class
        );

        assertEquals("PAYLOAD_TOO_LARGE", rejected.issues().getFirst().code());
    }

    @Test
    void enforcesNestingDepthLimit() {
        Vda5050JsonCodec codec = codec(
            JsonCodecLimits.builder().maxNestingDepth(1).build()
        );

        assertDecoded(codec, "{}");
        assertLimitExceeded(codec, "{\"nested\":{}}");
    }

    @Test
    void measuresStringLimitInCharactersRatherThanUtf8Bytes() {
        Vda5050JsonCodec accepting = codec(
            JsonCodecLimits.builder().maxStringCharacters(3).build()
        );
        Vda5050JsonCodec rejecting = codec(
            JsonCodecLimits.builder().maxStringCharacters(2).build()
        );
        String payload = "{\"text\":\"机器人\"}";

        assertDecoded(accepting, payload);
        assertLimitExceeded(rejecting, payload);
    }

    @Test
    void enforcesFieldNameLimit() {
        Vda5050JsonCodec codec = codec(
            JsonCodecLimits.builder().maxNameCharacters(3).build()
        );

        assertDecoded(codec, "{\"abc\":1}");
        assertLimitExceeded(codec, "{\"abcd\":1}");
    }

    @Test
    void enforcesNumberTextLimit() {
        Vda5050JsonCodec codec = codec(
            JsonCodecLimits.builder().maxNumberCharacters(2).build()
        );

        assertDecoded(codec, "{\"number\":12}");
        assertLimitExceeded(codec, "{\"number\":123}");
    }

    @Test
    void enforcesArrayElementLimit() {
        Vda5050JsonCodec codec = codec(
            JsonCodecLimits.builder().maxArrayElements(2).build()
        );

        assertDecoded(codec, "{\"items\":[\"a\",\"b\"]}");
        assertLimitExceeded(codec, "{\"items\":[\"a\",\"b\",\"c\"]}");
    }

    @Test
    void enforcesObjectPropertyLimit() {
        Vda5050JsonCodec codec = codec(
            JsonCodecLimits.builder().maxObjectProperties(2).build()
        );

        assertDecoded(codec, "{\"a\":1,\"b\":2}");
        assertLimitExceeded(codec, "{\"a\":1,\"b\":2,\"c\":3}");
    }

    @Test
    void enforcesTokenLimit() {
        Vda5050JsonCodec codec = codec(
            JsonCodecLimits.builder().maxTokens(3).build()
        );

        assertDecoded(codec, "{}");
        assertLimitExceeded(codec, "{\"a\":1}");
    }

    @Test
    void rejectsLimitViolationBeforeConstructingTargetObject() {
        CountingProbe.CONSTRUCTIONS.set(0);
        Vda5050JsonCodec codec = codec(
            JsonCodecLimits.builder().maxStringCharacters(2).build()
        );

        rejected(codec, "{\"value\":\"too long\"}", CountingProbe.class);

        assertEquals(0, CountingProbe.CONSTRUCTIONS.get());
    }

    private static Vda5050JsonCodec codec(JsonCodecLimits limits) {
        return Vda5050JsonCodec.create(limits);
    }

    private static void assertDecoded(Vda5050JsonCodec codec, String payload) {
        DecodingResult<LimitProbe> result = codec.decode(
            TopicName.STATE,
            bytes(payload),
            LimitProbe.class
        );
        assertInstanceOf(DecodedMessage.class, result);
    }

    private static void assertLimitExceeded(
        Vda5050JsonCodec codec,
        String payload
    ) {
        RejectedInboundMessage<LimitProbe> rejected = rejected(
            codec,
            payload,
            LimitProbe.class
        );
        assertEquals("JSON_LIMIT_EXCEEDED", rejected.issues().getFirst().code());
        assertEquals(
            "VDA3-SHARED-009",
            rejected.issues().getFirst().requirementId()
        );
    }

    @SuppressWarnings("unchecked")
    private static <T> RejectedInboundMessage<T> rejected(
        Vda5050JsonCodec codec,
        String payload,
        Class<T> type
    ) {
        DecodingResult<T> result = codec.decode(
            TopicName.STATE,
            bytes(payload),
            type
        );
        RejectedInboundMessage<T> rejected = assertInstanceOf(
            RejectedInboundMessage.class,
            result
        );
        assertTrue(rejected.issues().getFirst().description().contains("limit"));
        return rejected;
    }

    private static byte[] bytes(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }

    private record LimitProbe(
        String text,
        Long number,
        List<String> items,
        LimitProbe nested
    ) {}

    private record CountingProbe(String value) {
        private static final AtomicInteger CONSTRUCTIONS = new AtomicInteger();

        private CountingProbe {
            CONSTRUCTIONS.incrementAndGet();
        }
    }
}
