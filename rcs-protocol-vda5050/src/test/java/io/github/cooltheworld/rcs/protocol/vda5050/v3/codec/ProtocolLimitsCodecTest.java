package io.github.cooltheworld.rcs.protocol.vda5050.v3.codec;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.ProtocolLimits;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.topic.TopicName;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.DecodedMessage;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.DecodingResult;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.RejectedInboundMessage;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unchecked")
final class ProtocolLimitsCodecTest {
    private static final ObjectMapper TEST_MAPPER = JsonMapper.builder().build();
    private static final Vda5050JsonCodec CODEC = Vda5050JsonCodec.createDefault();

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] Protocol Limits 精确往返全部字段、零值与扩展")
    void roundTripsEveryProtocolLimitAndExtension() throws Exception {
        byte[] payload = bytes("""
            {
              "maximumStringLengths":{
                "maximumMessageLength":0,
                "maximumTopicSerialLength":2,
                "maximumTopicElementLength":3,
                "maximumIdLength":4,
                "idNumericalOnly":true,
                "maximumLoadIdLength":5,
                "vendorStringRule":{"unit":"code-point"}
              },
              "maximumArrayLengths":{
                "order.nodes":6,
                "order.edges":7,
                "node.actions":8,
                "edge.actions":9,
                "actions.actionsParameters":10,
                "instantActions":11,
                "trajectory.knotVector":12,
                "trajectory.controlPoints":13,
                "zoneSet.zones":14,
                "state.nodeStates":15,
                "state.edgeStates":16,
                "state.loads":17,
                "state.actionStates":18,
                "state.instantActionStates":19,
                "state.zoneActionStates":20,
                "state.errors":21,
                "state.information":22,
                "error.errorReferences":23,
                "information.infoReferences":24,
                "vendor.array":25
              },
              "timing":{
                "minimumOrderInterval":0.0,
                "minimumStateInterval":0.5,
                "defaultStateInterval":1.0,
                "visualizationInterval":0.1,
                "vendorTiming":null
              },
              "vendorPolicy":{"revision":1}
            }
            """);

        ProtocolLimits limits = decoded(CODEC.decode(
            TopicName.FACTSHEET,
            payload,
            ProtocolLimits.class
        )).message();
        byte[] firstEncoding = CODEC.encode(limits);
        byte[] secondEncoding = CODEC.encode(limits);
        ProtocolLimits roundTripped = decoded(CODEC.decode(
            TopicName.FACTSHEET,
            firstEncoding,
            ProtocolLimits.class
        )).message();

        assertAll(
            () -> assertEquals(0L, limits
                .maximumStringLengths()
                .maximumMessageLength()),
            () -> assertEquals(10L, limits
                .maximumArrayLengths()
                .actionParameters()),
            () -> assertEquals(24L, limits
                .maximumArrayLengths()
                .informationInfoReferences()),
            () -> assertEquals(0.0D, limits.timing().minimumOrderInterval()),
            () -> assertFalse(limits.extensionFields().isEmpty()),
            () -> assertFalse(limits.maximumStringLengths()
                .extensionFields()
                .isEmpty()),
            () -> assertFalse(limits.maximumArrayLengths()
                .extensionFields()
                .isEmpty()),
            () -> assertFalse(limits.timing().extensionFields().isEmpty()),
            () -> assertEquals(
                TEST_MAPPER.readTree(payload),
                TEST_MAPPER.readTree(firstEncoding)
            ),
            () -> assertArrayEquals(firstEncoding, secondEncoding),
            () -> assertEquals(limits, roundTripped)
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] Codec 保留缺失的可选限制")
    void preservesMissingOptionalLimits() throws Exception {
        byte[] payload = bytes("""
            {
              "maximumStringLengths":{},
              "maximumArrayLengths":{},
              "timing":{
                "minimumOrderInterval":0.25,
                "minimumStateInterval":0.5
              }
            }
            """);

        ProtocolLimits limits = decoded(CODEC.decode(
            TopicName.FACTSHEET,
            payload,
            ProtocolLimits.class
        )).message();
        JsonNode encoded = TEST_MAPPER.readTree(CODEC.encode(limits));

        assertAll(
            () -> assertNull(limits
                .maximumStringLengths()
                .maximumMessageLength()),
            () -> assertNull(limits.maximumArrayLengths().orderNodes()),
            () -> assertNull(limits.timing().defaultStateInterval()),
            () -> assertEquals(TEST_MAPPER.readTree(payload), encoded)
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-010] Protocol Limits 拒绝标准可选字段显式 null")
    void rejectsExplicitNullForOptionalStandardLimit() {
        byte[] payload = bytes("""
            {
              "maximumStringLengths":{"maximumIdLength":null},
              "maximumArrayLengths":{},
              "timing":{
                "minimumOrderInterval":0.25,
                "minimumStateInterval":0.5
              }
            }
            """);

        RejectedInboundMessage<ProtocolLimits> rejected = rejected(CODEC.decode(
            TopicName.FACTSHEET,
            payload,
            ProtocolLimits.class
        ));

        assertAll(
            () -> assertEquals("EXPLICIT_NULL", rejected.issues().getFirst().code()),
            () -> assertEquals(
                "/maximumStringLengths/maximumIdLength",
                rejected.issues().getFirst().path()
            )
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] Protocol Limits 拒绝非对象和缺失必填字段")
    void rejectsInvalidShapesAndMissingRequiredFields() {
        assertAll(
            () -> assertEquals(
                "INVALID_JSON_TYPE",
                rejected(CODEC.decode(
                    TopicName.FACTSHEET,
                    bytes("[]"),
                    ProtocolLimits.class
                )).issues().getFirst().code()
            ),
            () -> assertEquals(
                "INVALID_JSON_TYPE",
                rejected(CODEC.decode(
                    TopicName.FACTSHEET,
                    bytes("""
                        {
                          "maximumStringLengths":{},
                          "maximumArrayLengths":{},
                          "timing":{"minimumOrderInterval":0.25}
                        }
                        """),
                    ProtocolLimits.class
                )).issues().getFirst().code()
            ),
            () -> assertEquals(
                "INVALID_JSON_TYPE",
                rejected(CODEC.decode(
                    TopicName.FACTSHEET,
                    bytes("""
                        {
                          "maximumStringLengths":[],
                          "maximumArrayLengths":{},
                          "timing":{
                            "minimumOrderInterval":0.25,
                            "minimumStateInterval":0.5
                          }
                        }
                        """),
                    ProtocolLimits.class
                )).issues().getFirst().code()
            )
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] Protocol Limits 拒绝错误的标准标量类型")
    void rejectsInvalidStandardScalarTypes() {
        assertAll(
            () -> assertEquals(
                "INVALID_JSON_TYPE",
                rejected(CODEC.decode(
                    TopicName.FACTSHEET,
                    bytes("""
                        {
                          "maximumStringLengths":{"maximumMessageLength":1.5},
                          "maximumArrayLengths":{},
                          "timing":{
                            "minimumOrderInterval":0.25,
                            "minimumStateInterval":0.5
                          }
                        }
                        """),
                    ProtocolLimits.class
                )).issues().getFirst().code()
            ),
            () -> assertEquals(
                "INVALID_JSON_TYPE",
                rejected(CODEC.decode(
                    TopicName.FACTSHEET,
                    bytes("""
                        {
                          "maximumStringLengths":{"idNumericalOnly":"true"},
                          "maximumArrayLengths":{},
                          "timing":{
                            "minimumOrderInterval":0.25,
                            "minimumStateInterval":0.5
                          }
                        }
                        """),
                    ProtocolLimits.class
                )).issues().getFirst().code()
            ),
            () -> assertEquals(
                "INVALID_JSON_TYPE",
                rejected(CODEC.decode(
                    TopicName.FACTSHEET,
                    bytes("""
                        {
                          "maximumStringLengths":{},
                          "maximumArrayLengths":{"order.nodes":"6"},
                          "timing":{
                            "minimumOrderInterval":0.25,
                            "minimumStateInterval":0.5
                          }
                        }
                        """),
                    ProtocolLimits.class
                )).issues().getFirst().code()
            ),
            () -> assertEquals(
                "INVALID_JSON_TYPE",
                rejected(CODEC.decode(
                    TopicName.FACTSHEET,
                    bytes("""
                        {
                          "maximumStringLengths":{},
                          "maximumArrayLengths":{},
                          "timing":{
                            "minimumOrderInterval":"0.25",
                            "minimumStateInterval":0.5
                          }
                        }
                        """),
                    ProtocolLimits.class
                )).issues().getFirst().code()
            )
        );
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

    private static byte[] bytes(String json) {
        return json.getBytes(StandardCharsets.UTF_8);
    }
}
