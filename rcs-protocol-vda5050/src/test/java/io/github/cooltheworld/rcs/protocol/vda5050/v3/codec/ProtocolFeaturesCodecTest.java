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
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.ActionScope;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.ActionValueDataType;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.BlockingType;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.OptionalParameterSupport;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.ProtocolFeatures;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.topic.TopicName;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.DecodedMessage;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.DecodingResult;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.RejectedInboundMessage;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unchecked")
final class ProtocolFeaturesCodecTest {
    private static final ObjectMapper TEST_MAPPER = JsonMapper.builder().build();
    private static final Vda5050JsonCodec CODEC = Vda5050JsonCodec.createDefault();

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] 协议能力精确往返全部字段与嵌套扩展")
    void roundTripsEveryProtocolFeatureAndExtension() throws Exception {
        byte[] payload = bytes("""
            {
              "optionalParameters":[{
                "parameter":"order.nodes.nodePosition.allowedDeviationTheta",
                "support":"REQUIRED",
                "description":"Required for navigation",
                "vendorRule":null
              }],
              "mobileRobotActions":[{
                "actionType":"pick",
                "actionDescription":"Pick a load",
                "actionScopes":["INSTANT","NODE","EDGE","ZONE"],
                "actionParameters":[{
                  "key":"height",
                  "valueDataType":"NUMBER",
                  "description":"Lift height in metres",
                  "isOptional":false,
                  "vendorUnit":"m"
                }],
                "actionResult":"Picked load identifier",
                "blockingTypes":["NONE","SOFT","SINGLE","HARD"],
                "pauseAllowed":false,
                "cancelAllowed":true,
                "vendorAction":{"revision":1}
              }],
              "vendorFeatures":{"mode":"safe"}
            }
            """);

        ProtocolFeatures features = decoded(CODEC.decode(
            TopicName.FACTSHEET,
            payload,
            ProtocolFeatures.class
        )).message();
        byte[] firstEncoding = CODEC.encode(features);
        byte[] secondEncoding = CODEC.encode(features);
        ProtocolFeatures roundTripped = decoded(CODEC.decode(
            TopicName.FACTSHEET,
            firstEncoding,
            ProtocolFeatures.class
        )).message();

        assertAll(
            () -> assertEquals(
                OptionalParameterSupport.REQUIRED,
                features.optionalParameters().getFirst().support()
            ),
            () -> assertEquals(
                List.of(
                    ActionScope.INSTANT,
                    ActionScope.NODE,
                    ActionScope.EDGE,
                    ActionScope.ZONE
                ),
                features.mobileRobotActions().getFirst().actionScopes()
            ),
            () -> assertEquals(
                ActionValueDataType.NUMBER,
                features.mobileRobotActions()
                    .getFirst()
                    .actionParameters()
                    .getFirst()
                    .valueDataType()
            ),
            () -> assertEquals(
                List.of(
                    BlockingType.NONE,
                    BlockingType.SOFT,
                    BlockingType.SINGLE,
                    BlockingType.HARD
                ),
                features.mobileRobotActions().getFirst().blockingTypes()
            ),
            () -> assertEquals(
                false,
                features.mobileRobotActions().getFirst().pauseAllowed()
            ),
            () -> assertFalse(features.extensionFields().isEmpty()),
            () -> assertFalse(
                features.optionalParameters().getFirst().extensionFields().isEmpty()
            ),
            () -> assertFalse(
                features.mobileRobotActions().getFirst().extensionFields().isEmpty()
            ),
            () -> assertFalse(
                features.mobileRobotActions()
                    .getFirst()
                    .actionParameters()
                    .getFirst()
                    .extensionFields()
                    .isEmpty()
            ),
            () -> assertEquals(
                TEST_MAPPER.readTree(payload),
                TEST_MAPPER.readTree(firstEncoding)
            ),
            () -> assertArrayEquals(firstEncoding, secondEncoding),
            () -> assertEquals(features, roundTripped)
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] Codec 保留缺失与空数组并精确保留 false")
    void preservesMissingEmptyAndFalseValues() throws Exception {
        byte[] payload = bytes("""
            {
              "optionalParameters":[],
              "mobileRobotActions":[{
                "actionType":"noop",
                "actionScopes":[],
                "pauseAllowed":false,
                "cancelAllowed":false
              }]
            }
            """);

        ProtocolFeatures features = decoded(CODEC.decode(
            TopicName.FACTSHEET,
            payload,
            ProtocolFeatures.class
        )).message();
        JsonNode encoded = TEST_MAPPER.readTree(CODEC.encode(features));

        assertAll(
            () -> assertEquals(List.of(), features.optionalParameters()),
            () -> assertEquals(
                List.of(),
                features.mobileRobotActions().getFirst().actionScopes()
            ),
            () -> assertNull(
                features.mobileRobotActions().getFirst().actionParameters()
            ),
            () -> assertNull(
                features.mobileRobotActions().getFirst().blockingTypes()
            ),
            () -> assertEquals(
                false,
                features.mobileRobotActions().getFirst().pauseAllowed()
            ),
            () -> assertEquals(TEST_MAPPER.readTree(payload), encoded)
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-010] 协议能力拒绝嵌套标准可选字段显式 null")
    void rejectsExplicitNullForNestedOptionalField() {
        RejectedInboundMessage<ProtocolFeatures> rejected = rejected(CODEC.decode(
            TopicName.FACTSHEET,
            bytes("""
                {
                  "optionalParameters":[],
                  "mobileRobotActions":[{
                    "actionType":"noop",
                    "actionDescription":null,
                    "actionScopes":[],
                    "pauseAllowed":false,
                    "cancelAllowed":false
                  }]
                }
                """),
            ProtocolFeatures.class
        ));

        assertAll(
            () -> assertEquals("EXPLICIT_NULL", rejected.issues().getFirst().code()),
            () -> assertEquals(
                "/mobileRobotActions/0/actionDescription",
                rejected.issues().getFirst().path()
            )
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] 协议能力拒绝非对象和缺失必填字段")
    void rejectsInvalidShapesAndMissingRequiredFields() {
        assertAll(
            () -> assertRejected(bytes("[]")),
            () -> assertRejected(bytes("{\"optionalParameters\":[]}")),
            () -> assertRejected(bytes("""
                {"optionalParameters":{},"mobileRobotActions":[]}
                """)),
            () -> assertRejected(bytes("""
                {
                  "optionalParameters":[{"parameter":"order.edges.trajectory"}],
                  "mobileRobotActions":[]
                }
                """)),
            () -> assertRejected(bytes("""
                {
                  "optionalParameters":[],
                  "mobileRobotActions":[{
                    "actionType":"pick",
                    "actionScopes":["NODE"],
                    "cancelAllowed":true
                  }]
                }
                """)),
            () -> assertRejected(bytes("""
                {
                  "optionalParameters":[],
                  "mobileRobotActions":[{
                    "actionType":"pick",
                    "actionScopes":["NODE"],
                    "actionParameters":[{"key":"height"}],
                    "pauseAllowed":true,
                    "cancelAllowed":true
                  }]
                }
                """))
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] 协议能力拒绝错误标量与封闭枚举值")
    void rejectsInvalidScalarAndClosedEnumValues() {
        assertAll(
            () -> assertRejected(bytes("""
                {
                  "optionalParameters":[{
                    "parameter":"order.edges.trajectory",
                    "support":"OPTIONAL"
                  }],
                  "mobileRobotActions":[]
                }
                """)),
            () -> assertRejected(bytes("""
                {
                  "optionalParameters":[],
                  "mobileRobotActions":[{
                    "actionType":"pick",
                    "actionScopes":["ROBOT"],
                    "pauseAllowed":true,
                    "cancelAllowed":true
                  }]
                }
                """)),
            () -> assertRejected(bytes("""
                {
                  "optionalParameters":[],
                  "mobileRobotActions":[{
                    "actionType":"pick",
                    "actionScopes":["NODE"],
                    "blockingTypes":["BLOCKING"],
                    "pauseAllowed":true,
                    "cancelAllowed":true
                  }]
                }
                """)),
            () -> assertRejected(bytes("""
                {
                  "optionalParameters":[],
                  "mobileRobotActions":[{
                    "actionType":"pick",
                    "actionScopes":["NODE"],
                    "actionParameters":[{
                      "key":"height",
                      "valueDataType":"FLOAT"
                    }],
                    "pauseAllowed":true,
                    "cancelAllowed":true
                  }]
                }
                """)),
            () -> assertRejected(bytes("""
                {
                  "optionalParameters":[],
                  "mobileRobotActions":[{
                    "actionType":"pick",
                    "actionScopes":["NODE"],
                    "pauseAllowed":"true",
                    "cancelAllowed":true
                  }]
                }
                """))
        );
    }

    private static void assertRejected(byte[] payload) {
        assertEquals(
            "INVALID_JSON_TYPE",
            rejected(CODEC.decode(
                TopicName.FACTSHEET,
                payload,
                ProtocolFeatures.class
            )).issues().getFirst().code()
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
