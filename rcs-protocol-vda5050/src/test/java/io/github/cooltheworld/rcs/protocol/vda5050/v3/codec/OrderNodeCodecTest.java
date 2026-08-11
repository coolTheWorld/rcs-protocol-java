package io.github.cooltheworld.rcs.protocol.vda5050.v3.codec;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.order.Node;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.topic.TopicName;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.DecodedMessage;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.DecodingResult;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.RejectedInboundMessage;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unchecked")
final class OrderNodeCodecTest {
    private static final ObjectMapper TEST_MAPPER = JsonMapper.builder().build();
    private static final Vda5050JsonCodec CODEC =
        Vda5050JsonCodec.createDefault();

    @Test
    @DisplayName("[VDA3-ORDER-001] Node Codec 确定往返完整位置、Action 和扩展")
    void roundTripsCompleteNodeGraphDeterministically() throws Exception {
        byte[] payload = json("""
            {
              "nodeId":" Node/Main ",
              "sequenceId":4294967295,
              "nodeDescriptor":" human-readable ",
              "released":false,
              "nodePosition":{
                "x":1.25,
                "y":-2.0,
                "theta":3.0,
                "allowedDeviationXY":{
                  "a":0.5,
                  "b":0.25,
                  "theta":0.1,
                  "ellipseExtension":null
                },
                "allowedDeviationTheta":0.2,
                "mapId":" Map/Main ",
                "positionExtension":null
              },
              "actions":[{
                "actionType":"pick",
                "actionId":"a1",
                "blockingType":"SOFT",
                "actionParameters":[{"key":"count","value":2}]
              }],
              "nodeExtension":null
            }
            """);

        Node node = decoded(CODEC.decode(
            TopicName.ORDER,
            payload,
            Node.class
        )).message();
        byte[] firstEncoding = CODEC.encode(node);
        byte[] secondEncoding = CODEC.encode(node);
        Node roundTripped = decoded(CODEC.decode(
            TopicName.ORDER,
            firstEncoding,
            Node.class
        )).message();

        assertAll(
            () -> assertEquals(" Node/Main ", node.nodeId()),
            () -> assertEquals(4_294_967_295L, node.sequenceId()),
            () -> assertEquals(Boolean.FALSE, node.released()),
            () -> assertEquals(" Map/Main ", node.nodePosition().mapId()),
            () -> assertEquals(-2.0D, node.nodePosition().y()),
            () -> assertFalse(node.extensionFields().isEmpty()),
            () -> assertFalse(node.nodePosition().extensionFields().isEmpty()),
            () -> assertFalse(
                node.nodePosition()
                    .allowedDeviationXY()
                    .extensionFields()
                    .isEmpty()
            ),
            () -> assertEquals(1, node.actions().size()),
            () -> assertEquals(TEST_MAPPER.readTree(payload), TEST_MAPPER.readTree(firstEncoding)),
            () -> assertArrayEquals(firstEncoding, secondEncoding),
            () -> assertEquals(node, roundTripped)
        );
    }

    @Test
    @DisplayName("[VDA3-ORDER-001] Node Codec 保留空 Action 与缺失可选位置")
    void preservesEmptyActionsAndMissingOptionalPosition() throws Exception {
        byte[] payload = json("""
            {"nodeId":"n0","sequenceId":0,"released":true,"actions":[]}
            """);

        Node node = decoded(CODEC.decode(
            TopicName.ORDER,
            payload,
            Node.class
        )).message();
        JsonNode encoded = TEST_MAPPER.readTree(CODEC.encode(node));

        assertAll(
            () -> assertEquals(List.of(), node.actions()),
            () -> assertNull(node.nodeDescriptor()),
            () -> assertNull(node.nodePosition()),
            () -> assertFalse(encoded.has("nodeDescriptor")),
            () -> assertFalse(encoded.has("nodePosition")),
            () -> assertTrue(encoded.has("actions"))
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-010] Node Codec 拒绝嵌套标准字段显式 null")
    void rejectsExplicitNullInNestedNodeFields() {
        RejectedInboundMessage<Node> optional = rejected(CODEC.decode(
            TopicName.ORDER,
            json("""
                {
                  "nodeId":"n0",
                  "sequenceId":0,
                  "released":true,
                  "nodePosition":null,
                  "actions":[]
                }
                """),
            Node.class
        ));
        RejectedInboundMessage<Node> ellipse = rejected(CODEC.decode(
            TopicName.ORDER,
            json("""
                {
                  "nodeId":"n0",
                  "sequenceId":0,
                  "released":true,
                  "nodePosition":{
                    "x":1,
                    "y":2,
                    "mapId":"map",
                    "allowedDeviationXY":{"a":null,"b":1,"theta":0}
                  },
                  "actions":[]
                }
                """),
            Node.class
        ));

        assertAll(
            () -> assertExplicitNull(optional, "/nodePosition"),
            () -> assertExplicitNull(
                ellipse,
                "/nodePosition/allowedDeviationXY/a"
            )
        );
    }

    @Test
    @DisplayName("[VDA3-ORDER-001] Node Codec 结构化拒绝缺失字段和非法 shape")
    void rejectsMissingAndInvalidNodeShapes() {
        assertAll(
            () -> assertInvalidType("""
                {"nodeId":"n0","sequenceId":0,"released":true}
                """),
            () -> assertInvalidType("[]"),
            () -> assertInvalidType("""
                {"nodeId":"n0","sequenceId":0.5,"released":true,"actions":[]}
                """),
            () -> assertInvalidType("""
                {"nodeId":"n0","sequenceId":0,"released":true,"actions":{}}
                """),
            () -> assertInvalidType("""
                {
                  "nodeId":"n0",
                  "sequenceId":0,
                  "released":true,
                  "nodePosition":[],
                  "actions":[]
                }
                """)
        );
    }

    @Test
    @DisplayName("[VDA3-ORDER-001] 独立 Jackson Module 注册 Node 对象图")
    void registersNodeGraphWithCallerObjectMapper() throws Exception {
        ObjectMapper mapper = JsonMapper.builder()
            .addModule(new Vda5050JacksonModule())
            .build();
        byte[] payload = json("""
            {
              "nodeId":"n0",
              "sequenceId":0,
              "released":true,
              "nodePosition":{"x":1.0,"y":2.0,"mapId":"map"},
              "actions":[]
            }
            """);

        Node node = mapper.readValue(payload, Node.class);
        byte[] encoded = mapper.writeValueAsBytes(node);

        assertAll(
            () -> assertEquals(TEST_MAPPER.readTree(payload), TEST_MAPPER.readTree(encoded)),
            () -> assertEquals(node, mapper.readValue(encoded, Node.class))
        );
    }

    private static void assertExplicitNull(
        RejectedInboundMessage<Node> rejected,
        String path
    ) {
        assertAll(
            () -> assertEquals("EXPLICIT_NULL", rejected.issues().getFirst().code()),
            () -> assertEquals(path, rejected.issues().getFirst().path()),
            () -> assertEquals(
                "VDA3-SHARED-010",
                rejected.issues().getFirst().requirementId()
            )
        );
    }

    private static void assertInvalidType(String payload) {
        RejectedInboundMessage<Node> rejected = rejected(CODEC.decode(
            TopicName.ORDER,
            json(payload),
            Node.class
        ));
        assertEquals("INVALID_JSON_TYPE", rejected.issues().getFirst().code());
    }

    private static <T> DecodedMessage<T> decoded(DecodingResult<T> result) {
        return (DecodedMessage<T>) assertInstanceOf(DecodedMessage.class, result);
    }

    private static <T> RejectedInboundMessage<T> rejected(
        DecodingResult<T> result
    ) {
        return (RejectedInboundMessage<T>) assertInstanceOf(
            RejectedInboundMessage.class,
            result
        );
    }

    private static byte[] json(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }
}
