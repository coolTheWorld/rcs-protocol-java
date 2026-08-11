package io.github.cooltheworld.rcs.protocol.vda5050.v3.codec;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.action.Action;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.action.ActionParameter;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.action.ActionParameterValue;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.action.BlockingType;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.topic.TopicName;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.DecodedMessage;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.DecodingResult;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.RejectedInboundMessage;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unchecked")
final class OrderActionCodecTest {
    private static final ObjectMapper TEST_MAPPER = JsonMapper.builder().build();
    private static final Vda5050JsonCodec CODEC =
        Vda5050JsonCodec.createDefault();

    @Test
    @DisplayName("[VDA3-ORDER-001] Action Codec 确定往返六类递归参数值和扩展")
    void roundTripsEveryActionParameterValueDeterministically()
        throws Exception {
        byte[] payload = json("""
            {
              "actionType":" Vendor.Pick ",
              "actionId":" action-1 ",
              "actionDescriptor":" human-readable ",
              "blockingType":"SOFT",
              "actionParameters":[
                {"key":"bool","value":true,"parameterExtension":null},
                {"key":"number","value":1.5},
                {"key":"integer","value":7},
                {"key":"string","value":"opaque"},
                {"key":"object","value":{"count":2,"enabled":false}},
                {"key":"array","value":[1,2.5,"x",true,{"nested":3},[4]]}
              ],
              "retriable":false,
              "actionExtension":null
            }
            """);

        Action action = decoded(CODEC.decode(
            TopicName.ORDER,
            payload,
            Action.class
        )).message();
        byte[] firstEncoding = CODEC.encode(action);
        byte[] secondEncoding = CODEC.encode(action);
        Action roundTripped = decoded(CODEC.decode(
            TopicName.ORDER,
            firstEncoding,
            Action.class
        )).message();
        List<ActionParameterValue> values = action.actionParameters().stream()
            .map(parameter -> parameter.value())
            .toList();

        assertAll(
            () -> assertEquals(" Vendor.Pick ", action.actionType()),
            () -> assertEquals(" action-1 ", action.actionId()),
            () -> assertInstanceOf(
                ActionParameterValue.BooleanValue.class,
                values.get(0)
            ),
            () -> assertInstanceOf(
                ActionParameterValue.NumberValue.class,
                values.get(1)
            ),
            () -> assertInstanceOf(
                ActionParameterValue.IntegerValue.class,
                values.get(2)
            ),
            () -> assertInstanceOf(
                ActionParameterValue.StringValue.class,
                values.get(3)
            ),
            () -> assertInstanceOf(
                ActionParameterValue.ObjectValue.class,
                values.get(4)
            ),
            () -> assertInstanceOf(
                ActionParameterValue.ArrayValue.class,
                values.get(5)
            ),
            () -> assertFalse(action.extensionFields().isEmpty()),
            () -> assertFalse(
                action.actionParameters().getFirst().extensionFields().isEmpty()
            ),
            () -> assertEquals(TEST_MAPPER.readTree(payload), TEST_MAPPER.readTree(firstEncoding)),
            () -> assertArrayEquals(firstEncoding, secondEncoding),
            () -> assertEquals(action, roundTripped)
        );
    }

    @Test
    @DisplayName("[VDA3-ORDER-001] Action Codec 区分缺失参数与空参数及缺失与显式 false")
    void preservesMissingAndExplicitOptionalValues() throws Exception {
        byte[] minimalPayload = json("""
            {"actionType":"pick","actionId":"a1","blockingType":"NONE"}
            """);
        byte[] explicitPayload = json("""
            {
              "actionType":"pick",
              "actionId":"a1",
              "blockingType":"NONE",
              "actionParameters":[],
              "retriable":false
            }
            """);

        Action minimal = decoded(CODEC.decode(
            TopicName.ORDER,
            minimalPayload,
            Action.class
        )).message();
        Action explicit = decoded(CODEC.decode(
            TopicName.ORDER,
            explicitPayload,
            Action.class
        )).message();
        JsonNode minimalEncoded = TEST_MAPPER.readTree(CODEC.encode(minimal));
        JsonNode explicitEncoded = TEST_MAPPER.readTree(CODEC.encode(explicit));

        assertAll(
            () -> assertNull(minimal.actionDescriptor()),
            () -> assertNull(minimal.actionParameters()),
            () -> assertNull(minimal.retriable()),
            () -> assertFalse(minimalEncoded.has("actionDescriptor")),
            () -> assertFalse(minimalEncoded.has("actionParameters")),
            () -> assertFalse(minimalEncoded.has("retriable")),
            () -> assertEquals(List.of(), explicit.actionParameters()),
            () -> assertEquals(Boolean.FALSE, explicit.retriable()),
            () -> assertTrue(explicitEncoded.has("actionParameters")),
            () -> assertTrue(explicitEncoded.has("retriable"))
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-010] Action Codec 拒绝标准字段和递归数组显式 null")
    void rejectsExplicitNullInStandardActionValues() {
        RejectedInboundMessage<Action> optional = rejected(CODEC.decode(
            TopicName.ORDER,
            json("""
                {
                  "actionType":"pick",
                  "actionId":"a1",
                  "blockingType":"NONE",
                  "actionDescriptor":null
                }
                """),
            Action.class
        ));
        RejectedInboundMessage<Action> parameter = rejected(CODEC.decode(
            TopicName.ORDER,
            json("""
                {
                  "actionType":"pick",
                  "actionId":"a1",
                  "blockingType":"NONE",
                  "actionParameters":[{"key":"x","value":null}]
                }
                """),
            Action.class
        ));
        RejectedInboundMessage<Action> nested = rejected(CODEC.decode(
            TopicName.ORDER,
            json("""
                {
                  "actionType":"pick",
                  "actionId":"a1",
                  "blockingType":"NONE",
                  "actionParameters":[{"key":"x","value":[1,null]}]
                }
                """),
            Action.class
        ));

        assertAll(
            () -> assertExplicitNull(optional, "/actionDescriptor"),
            () -> assertExplicitNull(parameter, "/actionParameters/0/value"),
            () -> assertExplicitNull(nested, "/actionParameters/0/value/1")
        );
    }

    @Test
    @DisplayName("[VDA3-ORDER-001] Action Codec 结构化拒绝缺失字段和非法 shape")
    void rejectsMissingAndInvalidActionShapes() {
        assertAll(
            () -> assertInvalidType("""
                {"actionType":"pick","blockingType":"NONE"}
                """),
            () -> assertInvalidType("[]"),
            () -> assertInvalidType("""
                {"actionType":"pick","actionId":"a1","blockingType":0}
                """),
            () -> assertInvalidType("""
                {
                  "actionType":"pick",
                  "actionId":"a1",
                  "blockingType":"NONE",
                  "actionParameters":{}
                }
                """),
            () -> assertInvalidType("""
                {
                  "actionType":"pick",
                  "actionId":"a1",
                  "blockingType":"NONE",
                  "actionParameters":[{"key":"x","value":1e10000}]
                }
                """),
            () -> assertInvalidType("""
                {
                  "actionType":"pick",
                  "actionId":"a1",
                  "blockingType":"NONE",
                  "actionParameters":[{"key":"x","value":9223372036854775808}]
                }
                """),
            () -> assertInvalidType("""
                {
                  "actionType":"pick",
                  "actionId":"a1",
                  "blockingType":"NONE",
                  "actionParameters":[{"key":"x","value":{"nested":null}}]
                }
                """)
        );
    }

    @Test
    @DisplayName("[VDA3-ORDER-001] Action Adapter 即使调用方允许 NaN 也封闭拒绝")
    void rejectsNonFiniteNumberEnabledByCallerMapper() {
        ObjectMapper permissiveMapper = JsonMapper.builder()
            .enable(JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS)
            .addModule(new Vda5050JacksonModule())
            .build();

        assertThrows(
            MismatchedInputException.class,
            () -> permissiveMapper.readValue(
                json("""
                    {
                      "actionType":"pick",
                      "actionId":"a1",
                      "blockingType":"NONE",
                      "actionParameters":[{"key":"x","value":NaN}]
                    }
                    """),
                Action.class
            )
        );
        assertThrows(
            MismatchedInputException.class,
            () -> permissiveMapper.readValue(
                json("""
                    {
                      "actionType":"pick",
                      "actionId":"a1",
                      "blockingType":"NONE",
                      "actionParameters":[{"key":"x","value":{"nested":null}}]
                    }
                    """),
                Action.class
            )
        );
    }

    @Test
    @DisplayName("[VDA3-ORDER-001] 独立 Jackson Module 注册 Action 对象图")
    void registersActionGraphWithCallerObjectMapper() throws Exception {
        ObjectMapper mapper = JsonMapper.builder()
            .addModule(new Vda5050JacksonModule())
            .build();
        byte[] payload = json("""
            {
              "actionType":"pick",
              "actionId":"a1",
              "blockingType":"HARD",
              "actionParameters":[{"key":"target","value":{"x":1,"y":2.5}}]
            }
            """);

        Action action = mapper.readValue(payload, Action.class);
        byte[] encoded = mapper.writeValueAsBytes(action);

        assertAll(
            () -> assertEquals(TEST_MAPPER.readTree(payload), TEST_MAPPER.readTree(encoded)),
            () -> assertEquals(
                action,
                mapper.readValue(encoded, Action.class)
            )
        );
    }

    @Test
    @DisplayName("[VDA3-ORDER-001] Action Codec 拒绝无法无损往返的程序化参数值")
    void rejectsProgrammaticValuesThatCannotRoundTrip() {
        ActionParameterValue.ObjectValue duplicateObject =
            new ActionParameterValue.ObjectValue(List.of(
                new ActionParameterValue.ObjectMember(
                    "same",
                    new ActionParameterValue.IntegerValue(1L)
                ),
                new ActionParameterValue.ObjectMember(
                    "same",
                    new ActionParameterValue.IntegerValue(2L)
                )
            ));
        Action action = Action.builder()
            .actionType("pick")
            .actionId("a1")
            .blockingType(BlockingType.NONE)
            .actionParameters(List.of(ActionParameter.builder()
                .key("duplicate")
                .value(duplicateObject)
                .build()))
            .build();
        Action nonFinite = actionWithValue(
            new ActionParameterValue.NumberValue(Double.NaN)
        );

        assertAll(
            () -> assertThrows(
                IllegalArgumentException.class,
                () -> CODEC.encode(action)
            ),
            () -> assertThrows(
                IllegalArgumentException.class,
                () -> CODEC.encode(nonFinite)
            )
        );
    }

    private static void assertExplicitNull(
        RejectedInboundMessage<Action> rejected,
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
        RejectedInboundMessage<Action> rejected = rejected(CODEC.decode(
            TopicName.ORDER,
            json(payload),
            Action.class
        ));
        assertEquals("INVALID_JSON_TYPE", rejected.issues().getFirst().code());
    }

    private static Action actionWithValue(ActionParameterValue value) {
        return Action.builder()
            .actionType("pick")
            .actionId("a1")
            .blockingType(BlockingType.NONE)
            .actionParameters(List.of(ActionParameter.builder()
                .key("value")
                .value(value)
                .build()))
            .build();
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
