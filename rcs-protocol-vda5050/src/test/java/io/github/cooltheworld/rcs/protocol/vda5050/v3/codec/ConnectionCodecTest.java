package io.github.cooltheworld.rcs.protocol.vda5050.v3.codec;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.Connection;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.ConnectionState;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.topic.TopicName;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.DecodedMessage;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.DecodingResult;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.RejectedInboundMessage;
import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

@SuppressWarnings("unchecked")
final class ConnectionCodecTest {
    private static final ObjectMapper TEST_MAPPER = JsonMapper.builder().build();
    private static final Vda5050JsonCodec CODEC = Vda5050JsonCodec.createDefault();

    @Test
    @DisplayName("[VDA3-CONNECTION-001] 有效 Connection Fixture 平铺消息头并确定性往返")
    void roundTripsTheMinimalConnectionFixtureWithAFlatHeader() throws Exception {
        byte[] payload = fixture("connection/valid/minimal.json");

        Connection connection = decoded(CODEC.decode(
            TopicName.CONNECTION,
            payload,
            Connection.class
        )).message();
        byte[] firstEncoding = CODEC.encode(connection);
        byte[] secondEncoding = CODEC.encode(connection);
        JsonNode encoded = TEST_MAPPER.readTree(firstEncoding);
        Connection roundTripped = decoded(CODEC.decode(
            TopicName.CONNECTION,
            firstEncoding,
            Connection.class
        )).message();

        assertAll(
            () -> assertEquals(0L, connection.header().headerId()),
            () -> assertEquals(
                "2026-08-07T08:00:00.123Z",
                connection.header().timestamp().toString()
            ),
            () -> assertEquals("3.0.0", connection.header().version().toString()),
            () -> assertEquals(
                "Acme",
                connection.header().robotIdentity().manufacturer()
            ),
            () -> assertEquals(
                "R-001",
                connection.header().robotIdentity().serialNumber()
            ),
            () -> assertEquals(ConnectionState.ONLINE, connection.connectionState()),
            () -> assertFalse(encoded.has("header")),
            () -> assertEquals(6, encoded.size()),
            () -> assertArrayEquals(firstEncoding, secondEncoding),
            () -> assertEquals(connection, roundTripped)
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-007] Connection 未知嵌套字段与显式 null 透明往返")
    void preservesUnknownConnectionExtensionsAcrossRoundTrip() throws Exception {
        byte[] payload = fixture("connection/valid/with-extensions.json");
        JsonNode input = TEST_MAPPER.readTree(payload);

        Connection connection = decoded(CODEC.decode(
            TopicName.CONNECTION,
            payload,
            Connection.class
        )).message();
        byte[] encoded = CODEC.encode(connection);
        Connection roundTripped = decoded(CODEC.decode(
            TopicName.CONNECTION,
            encoded,
            Connection.class
        )).message();

        assertAll(
            () -> assertFalse(connection.extensionFields().isEmpty()),
            () -> assertEquals(input, TEST_MAPPER.readTree(encoded)),
            () -> assertEquals(connection, roundTripped)
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-002] Connection Codec 保留超出 uint32 的 Long 值供语义校验")
    void preservesOutOfRangeHeaderIdForSemanticValidation() throws Exception {
        ObjectNode payload = (ObjectNode) TEST_MAPPER.readTree(
            fixture("connection/valid/minimal.json")
        );
        payload.put("headerId", 4_294_967_296L);

        Connection connection = decoded(CODEC.decode(
            TopicName.CONNECTION,
            TEST_MAPPER.writeValueAsBytes(payload),
            Connection.class
        )).message();

        assertEquals(4_294_967_296L, connection.header().headerId());
    }

    @ParameterizedTest(name = "[VDA3-CONNECTION-001] connectionState={0}")
    @EnumSource(ConnectionState.class)
    void decodesEveryNormativeConnectionState(ConnectionState connectionState)
        throws Exception {
        ObjectNode payload = (ObjectNode) TEST_MAPPER.readTree(
            fixture("connection/valid/minimal.json")
        );
        payload.put("connectionState", connectionState.name());

        Connection connection = decoded(CODEC.decode(
            TopicName.CONNECTION,
            TEST_MAPPER.writeValueAsBytes(payload),
            Connection.class
        )).message();

        assertEquals(connectionState, connection.connectionState());
    }

    @ParameterizedTest(name = "[VDA3-SHARED-010] Connection 标准字段 {0} 拒绝显式 null")
    @MethodSource("standardConnectionFields")
    void rejectsExplicitNullInEveryStandardConnectionField(String fieldName)
        throws Exception {
        ObjectNode payload = (ObjectNode) TEST_MAPPER.readTree(
            fixture("connection/valid/minimal.json")
        );
        payload.putNull(fieldName);

        DecodingResult<Connection> result = CODEC.decode(
            TopicName.CONNECTION,
            TEST_MAPPER.writeValueAsBytes(payload),
            Connection.class
        );

        RejectedInboundMessage<Connection> rejected = assertInstanceOf(
            RejectedInboundMessage.class,
            result
        );
        assertAll(
            () -> assertEquals("EXPLICIT_NULL", rejected.issues().getFirst().code()),
            () -> assertEquals("/" + fieldName, rejected.issues().getFirst().path()),
            () -> assertEquals(
                "VDA3-SHARED-010",
                rejected.issues().getFirst().requirementId()
            )
        );
    }

    @ParameterizedTest(name = "[VDA3-CONNECTION-001] Connection 字段 {0} 拒绝基础类型错误")
    @MethodSource("invalidConnectionFieldValues")
    void rejectsConnectionFieldTypeMismatches(
        String fieldName,
        JsonNode invalidValue
    ) throws Exception {
        ObjectNode payload = (ObjectNode) TEST_MAPPER.readTree(
            fixture("connection/valid/minimal.json")
        );
        payload.set(fieldName, invalidValue);

        DecodingResult<Connection> result = CODEC.decode(
            TopicName.CONNECTION,
            TEST_MAPPER.writeValueAsBytes(payload),
            Connection.class
        );

        RejectedInboundMessage<Connection> rejected = assertInstanceOf(
            RejectedInboundMessage.class,
            result
        );
        assertAll(
            () -> assertEquals(
                "INVALID_JSON_TYPE",
                rejected.issues().getFirst().code()
            ),
            () -> assertEquals("/" + fieldName, rejected.issues().getFirst().path())
        );
    }

    @ParameterizedTest(name = "[VDA3-CONNECTION-001] Connection 字段 {0} 拒绝非法协议值")
    @MethodSource("invalidProtocolScalarValues")
    void rejectsInvalidProtocolScalarValues(
        String fieldName,
        String invalidValue
    ) throws Exception {
        ObjectNode payload = (ObjectNode) TEST_MAPPER.readTree(
            fixture("connection/valid/minimal.json")
        );
        payload.put(fieldName, invalidValue);

        DecodingResult<Connection> result = CODEC.decode(
            TopicName.CONNECTION,
            TEST_MAPPER.writeValueAsBytes(payload),
            Connection.class
        );

        RejectedInboundMessage<Connection> rejected = assertInstanceOf(
            RejectedInboundMessage.class,
            result
        );
        assertAll(
            () -> assertEquals(
                "INVALID_JSON_TYPE",
                rejected.issues().getFirst().code()
            ),
            () -> assertEquals("/" + fieldName, rejected.issues().getFirst().path()),
            () -> assertEquals(
                "VDA3-SHARED-009",
                rejected.issues().getFirst().requirementId()
            )
        );
    }

    @Test
    @DisplayName("[VDA3-CONNECTION-001] 独立 Jackson Module 注册 Connection 线路表示")
    void registersConnectionWithAnExplicitCallerObjectMapper() throws Exception {
        ObjectMapper mapper = JsonMapper.builder()
            .addModule(new Vda5050JacksonModule())
            .build();
        byte[] payload = fixture("connection/valid/with-extensions.json");

        Connection connection = mapper.readValue(payload, Connection.class);
        byte[] encoded = mapper.writeValueAsBytes(connection);

        assertAll(
            () -> assertEquals(ConnectionState.HIBERNATING, connection.connectionState()),
            () -> assertTrue(TEST_MAPPER.readTree(encoded).has("vendorStatus")),
            () -> assertEquals(TEST_MAPPER.readTree(payload), TEST_MAPPER.readTree(encoded))
        );
    }

    private static Stream<String> standardConnectionFields() {
        return Stream.of(
            "headerId",
            "timestamp",
            "version",
            "manufacturer",
            "serialNumber",
            "connectionState"
        );
    }

    private static Stream<Arguments> invalidConnectionFieldValues() {
        return Stream.of(
            Arguments.of("headerId", TEST_MAPPER.getNodeFactory().textNode("zero")),
            Arguments.of("connectionState", TEST_MAPPER.getNodeFactory().numberNode(1))
        );
    }

    private static Stream<Arguments> invalidProtocolScalarValues() {
        return Stream.of(
            Arguments.of("version", "not-a-version"),
            Arguments.of("timestamp", "2026-08-07T08:00:00Z")
        );
    }

    @SuppressWarnings("unchecked")
    private static DecodedMessage<Connection> decoded(
        DecodingResult<Connection> result
    ) {
        return (DecodedMessage<Connection>) assertInstanceOf(
            DecodedMessage.class,
            result
        );
    }

    private static byte[] fixture(String path) {
        String resource = "vda5050/v3.0.0/fixtures/" + path;
        try (InputStream input = ConnectionCodecTest.class
            .getClassLoader()
            .getResourceAsStream(resource)) {
            if (input == null) {
                throw new IllegalArgumentException("Missing fixture: " + resource);
            }
            return input.readAllBytes();
        } catch (IOException exception) {
            throw new IllegalStateException("Fixture cannot be read", exception);
        }
    }
}
