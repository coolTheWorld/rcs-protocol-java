package io.github.cooltheworld.rcs.protocol.vda5050.v3.codec;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.MobileRobotGeometry;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.WheelType;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.topic.TopicName;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.DecodedMessage;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.DecodingResult;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.RejectedInboundMessage;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unchecked")
final class MobileRobotGeometryCodecTest {
    private static final ObjectMapper TEST_MAPPER = JsonMapper.builder().build();
    private static final Vda5050JsonCodec CODEC = Vda5050JsonCodec.createDefault();

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] 车轮几何精确往返可选字段与嵌套扩展")
    void roundTripsWheelGeometryAndExtensions() throws Exception {
        byte[] payload = bytes("""
            {
              "wheelDefinitions":[{
                "type":"SPHERICAL",
                "isActiveDriven":true,
                "isActiveSteered":false,
                "position":{"x":0.8,"y":-0.4,"theta":1.57,"vendorFrame":"rear"},
                "diameter":0.32,
                "width":0.1,
                "centerDisplacement":0.0,
                "constraints":"rear axle",
                "vendorWheel":{"revision":1}
              }],
              "vendorGeometry":null
            }
            """);

        MobileRobotGeometry geometry = decoded(CODEC.decode(
            TopicName.FACTSHEET,
            payload,
            MobileRobotGeometry.class
        )).message();
        byte[] firstEncoding = CODEC.encode(geometry);
        byte[] secondEncoding = CODEC.encode(geometry);
        MobileRobotGeometry roundTripped = decoded(CODEC.decode(
            TopicName.FACTSHEET,
            firstEncoding,
            MobileRobotGeometry.class
        )).message();
        MobileRobotGeometry withoutGeometryExtension = decoded(CODEC.decode(
            TopicName.FACTSHEET,
            withoutField(payload, "", "vendorGeometry"),
            MobileRobotGeometry.class
        )).message();
        MobileRobotGeometry withoutWheelExtension = decoded(CODEC.decode(
            TopicName.FACTSHEET,
            withoutField(payload, "/wheelDefinitions/0", "vendorWheel"),
            MobileRobotGeometry.class
        )).message();
        MobileRobotGeometry withoutPositionExtension = decoded(CODEC.decode(
            TopicName.FACTSHEET,
            withoutField(
                payload,
                "/wheelDefinitions/0/position",
                "vendorFrame"
            ),
            MobileRobotGeometry.class
        )).message();

        assertAll(
            () -> assertEquals(
                WheelType.of("SPHERICAL"),
                geometry.wheelDefinitions().getFirst().type()
            ),
            () -> assertEquals(
                1.57D,
                geometry.wheelDefinitions().getFirst().position().theta()
            ),
            () -> assertNull(geometry.envelopes2d()),
            () -> assertNull(geometry.envelopes3d()),
            () -> assertFalse(geometry.extensionFields().isEmpty()),
            () -> assertFalse(
                geometry.wheelDefinitions().getFirst().extensionFields().isEmpty()
            ),
            () -> assertFalse(
                geometry.wheelDefinitions()
                    .getFirst()
                    .position()
                    .extensionFields()
                    .isEmpty()
            ),
            () -> assertEquals(
                TEST_MAPPER.readTree(payload),
                TEST_MAPPER.readTree(firstEncoding)
            ),
            () -> assertArrayEquals(firstEncoding, secondEncoding),
            () -> assertEquals(geometry, roundTripped),
            () -> assertNotEquals(geometry, withoutGeometryExtension),
            () -> assertNotEquals(
                geometry.wheelDefinitions().getFirst(),
                withoutWheelExtension.wheelDefinitions().getFirst()
            ),
            () -> assertNotEquals(
                geometry.wheelDefinitions().getFirst().position(),
                withoutPositionExtension.wheelDefinitions().getFirst().position()
            )
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] 车轮几何保留缺失与空数组")
    void preservesMissingAndEmptyLists() {
        MobileRobotGeometry missing = decoded(CODEC.decode(
            TopicName.FACTSHEET,
            bytes("{}"),
            MobileRobotGeometry.class
        )).message();
        MobileRobotGeometry empty = decoded(CODEC.decode(
            TopicName.FACTSHEET,
            bytes("{\"wheelDefinitions\":[]}"),
            MobileRobotGeometry.class
        )).message();

        assertAll(
            () -> assertNull(missing.wheelDefinitions()),
            () -> assertEquals(List.of(), empty.wheelDefinitions()),
            () -> assertEquals("{}", text(CODEC.encode(missing))),
            () -> assertEquals(
                "{\"wheelDefinitions\":[]}",
                text(CODEC.encode(empty))
            )
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] 二维与三维包络精确往返不透明 Data")
    void roundTripsTwoAndThreeDimensionalEnvelopes() throws Exception {
        byte[] payload = bytes("""
            {
              "envelopes2d":[{
                "envelope2dId":"footprint",
                "vertices":[
                  {"x":-1.0,"y":-0.5,"vendorVertex":true},
                  {"x":1.0,"y":-0.5},
                  {"x":0.0,"y":0.8}
                ],
                "description":"Simple footprint",
                "vendorEnvelope":1
              }],
              "envelopes3d":[{
                "envelope3dId":"body",
                "format":"gltf",
                "data":{"mesh":[1,2,3],"material":null},
                "url":"https://example.invalid/body.gltf",
                "description":"Robot body",
                "vendorEnvelope":false
              }]
            }
            """);

        MobileRobotGeometry geometry = decoded(CODEC.decode(
            TopicName.FACTSHEET,
            payload,
            MobileRobotGeometry.class
        )).message();
        byte[] encoded = CODEC.encode(geometry);
        MobileRobotGeometry roundTripped = decoded(CODEC.decode(
            TopicName.FACTSHEET,
            encoded,
            MobileRobotGeometry.class
        )).message();
        MobileRobotGeometry withoutVertexExtension = decoded(CODEC.decode(
            TopicName.FACTSHEET,
            withoutField(
                payload,
                "/envelopes2d/0/vertices/0",
                "vendorVertex"
            ),
            MobileRobotGeometry.class
        )).message();
        MobileRobotGeometry withoutEnvelope2dExtension = decoded(CODEC.decode(
            TopicName.FACTSHEET,
            withoutField(payload, "/envelopes2d/0", "vendorEnvelope"),
            MobileRobotGeometry.class
        )).message();
        MobileRobotGeometry withoutEnvelope3dExtension = decoded(CODEC.decode(
            TopicName.FACTSHEET,
            withoutField(payload, "/envelopes3d/0", "vendorEnvelope"),
            MobileRobotGeometry.class
        )).message();
        MobileRobotGeometry withEmptyEnvelope3dData = decoded(CODEC.decode(
            TopicName.FACTSHEET,
            withEmptyObjectField(payload, "/envelopes3d/0", "data"),
            MobileRobotGeometry.class
        )).message();

        assertAll(
            () -> assertEquals(
                3,
                geometry.envelopes2d().getFirst().vertices().size()
            ),
            () -> assertFalse(
                geometry.envelopes2d()
                    .getFirst()
                    .vertices()
                    .getFirst()
                    .extensionFields()
                    .isEmpty()
            ),
            () -> assertFalse(geometry.envelopes3d().getFirst().data().isEmpty()),
            () -> assertFalse(
                geometry.envelopes3d().getFirst().extensionFields().isEmpty()
            ),
            () -> assertEquals(
                TEST_MAPPER.readTree(payload),
                TEST_MAPPER.readTree(encoded)
            ),
            () -> assertEquals(geometry, roundTripped),
            () -> assertNotEquals(
                geometry.envelopes2d().getFirst().vertices().getFirst(),
                withoutVertexExtension.envelopes2d()
                    .getFirst()
                    .vertices()
                    .getFirst()
            ),
            () -> assertNotEquals(
                geometry.envelopes2d().getFirst(),
                withoutEnvelope2dExtension.envelopes2d().getFirst()
            ),
            () -> assertNotEquals(
                geometry.envelopes3d().getFirst(),
                withoutEnvelope3dExtension.envelopes3d().getFirst()
            ),
            () -> assertEquals(
                geometry.envelopes3d().getFirst().data(),
                geometry.envelopes3d().getFirst().data()
            ),
            () -> assertNotEquals(
                geometry.envelopes3d().getFirst().data(),
                withEmptyEnvelope3dData.envelopes3d().getFirst().data()
            ),
            () -> assertNotEquals(
                geometry.envelopes3d().getFirst().data(),
                "data"
            ),
            () -> assertTrue(
                withEmptyEnvelope3dData.envelopes3d().getFirst().data().isEmpty()
            )
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] 车轮几何拒绝错误形状和缺失必填字段")
    void rejectsInvalidWheelGeometryShapes() {
        assertAll(
            () -> assertRejected(bytes("[]")),
            () -> assertRejected(bytes("{\"wheelDefinitions\":{}}")),
            () -> assertRejected(bytes("""
                {"wheelDefinitions":[{
                  "isActiveDriven":true,
                  "isActiveSteered":false,
                  "position":{"x":0.0,"y":0.0},
                  "diameter":0.3,
                  "width":0.1
                }]}
                """)),
            () -> assertRejected(bytes("""
                {"wheelDefinitions":[{
                  "type":"DRIVE",
                  "isActiveDriven":"true",
                  "isActiveSteered":false,
                  "position":{"x":0.0,"y":0.0},
                  "diameter":0.3,
                  "width":0.1
                }]}
                """)),
            () -> assertRejected(bytes("""
                {"wheelDefinitions":[{
                  "type":"DRIVE",
                  "isActiveDriven":true,
                  "isActiveSteered":false,
                  "position":{"x":0.0},
                  "diameter":0.3,
                  "width":0.1
                }]}
                """))
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] 包络拒绝非法 Data 形状和缺失必填字段")
    void rejectsInvalidEnvelopeShapes() {
        assertAll(
            () -> assertRejected(bytes("""
                {"envelopes2d":[{"envelope2dId":"footprint"}]}
                """)),
            () -> assertRejected(bytes("""
                {"envelopes2d":[{
                  "envelope2dId":"footprint",
                  "vertices":[{"x":0.0}]
                }]}
                """)),
            () -> assertRejected(bytes("""
                {"envelopes3d":[{"envelope3dId":"body"}]}
                """)),
            () -> assertRejected(bytes("""
                {"envelopes3d":[{
                  "envelope3dId":"body",
                  "format":"gltf",
                  "data":[]
                }]}
                """))
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-010] 几何拒绝嵌套标准可选字段显式 null")
    void rejectsExplicitNullForNestedOptionalField() {
        RejectedInboundMessage<MobileRobotGeometry> rejected = rejected(
            CODEC.decode(
                TopicName.FACTSHEET,
                bytes("""
                    {"envelopes3d":[{
                      "envelope3dId":"body",
                      "format":"gltf",
                      "url":null
                    }]}
                    """),
                MobileRobotGeometry.class
            )
        );

        assertAll(
            () -> assertEquals("EXPLICIT_NULL", rejected.issues().getFirst().code()),
            () -> assertEquals(
                "/envelopes3d/0/url",
                rejected.issues().getFirst().path()
            )
        );
    }

    private static void assertRejected(byte[] payload) {
        assertEquals(
            "INVALID_JSON_TYPE",
            rejected(CODEC.decode(
                TopicName.FACTSHEET,
                payload,
                MobileRobotGeometry.class
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

    private static byte[] withoutField(
        byte[] payload,
        String parentPointer,
        String field
    ) throws Exception {
        ObjectNode root = (ObjectNode) TEST_MAPPER.readTree(payload);
        ((ObjectNode) root.at(parentPointer)).remove(field);
        return TEST_MAPPER.writeValueAsBytes(root);
    }

    private static byte[] withEmptyObjectField(
        byte[] payload,
        String parentPointer,
        String field
    ) throws Exception {
        ObjectNode root = (ObjectNode) TEST_MAPPER.readTree(payload);
        ((ObjectNode) root.at(parentPointer)).set(
            field,
            TEST_MAPPER.createObjectNode()
        );
        return TEST_MAPPER.writeValueAsBytes(root);
    }

    private static String text(byte[] json) {
        return new String(json, StandardCharsets.UTF_8);
    }
}
