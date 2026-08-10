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
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.LocalizationType;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.MobileRobotClass;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.MobileRobotKinematics;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.NavigationType;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.PhysicalParameters;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.TypeSpecification;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.ZoneType;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.topic.TopicName;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.DecodedMessage;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.DecodingResult;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.RejectedInboundMessage;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.ValidationIssue;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unchecked")
final class FactsheetFragmentCodecTest {
    private static final ObjectMapper TEST_MAPPER = JsonMapper.builder().build();
    private static final Vda5050JsonCodec CODEC = Vda5050JsonCodec.createDefault();

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] TypeSpecification 片段透明往返扩展值和未知字段")
    void roundTripsTypeSpecificationFragment() throws Exception {
        byte[] payload = bytes("""
            {
              "seriesName":"Forkbot X",
              "mobileRobotKinematics":"CUSTOM_STEERING",
              "mobileRobotClass":"FORKLIFT",
              "maximumLoadMass":1250.5,
              "localizationTypes":["NATURAL","ULTRA_WIDEBAND"],
              "navigationTypes":["FREELY_NAVIGATING"],
              "supportedZones":[],
              "vendorCapabilities":{"revision":1,"optional":null}
            }
            """);

        TypeSpecification specification = decoded(CODEC.decode(
            TopicName.FACTSHEET,
            payload,
            TypeSpecification.class
        )).message();
        byte[] firstEncoding = CODEC.encode(specification);
        byte[] secondEncoding = CODEC.encode(specification);
        TypeSpecification roundTripped = decoded(CODEC.decode(
            TopicName.FACTSHEET,
            firstEncoding,
            TypeSpecification.class
        )).message();

        assertAll(
            () -> assertEquals("CUSTOM_STEERING", specification
                .mobileRobotKinematics()
                .value()),
            () -> assertEquals(
                List.of(
                    LocalizationType.NATURAL,
                    LocalizationType.of("ULTRA_WIDEBAND")
                ),
                specification.localizationTypes()
            ),
            () -> assertNull(specification.seriesDescription()),
            () -> assertEquals(List.of(), specification.supportedZones()),
            () -> assertFalse(specification.extensionFields().isEmpty()),
            () -> assertEquals(
                TEST_MAPPER.readTree(payload),
                TEST_MAPPER.readTree(firstEncoding)
            ),
            () -> assertArrayEquals(firstEncoding, secondEncoding),
            () -> assertEquals(specification, roundTripped)
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] PhysicalParameters 片段省略可选字段并透明往返扩展")
    void roundTripsPhysicalParametersFragment() throws Exception {
        byte[] payload = bytes("""
            {
              "minimumSpeed":0.05,
              "maximumSpeed":2.0,
              "maximumAcceleration":0.8,
              "maximumDeceleration":1.2,
              "minimumHeight":0.2,
              "maximumHeight":2.2,
              "width":1.1,
              "length":2.0,
              "vendorCalibration":null
            }
            """);

        PhysicalParameters parameters = decoded(CODEC.decode(
            TopicName.FACTSHEET,
            payload,
            PhysicalParameters.class
        )).message();
        byte[] encoded = CODEC.encode(parameters);
        PhysicalParameters roundTripped = decoded(CODEC.decode(
            TopicName.FACTSHEET,
            encoded,
            PhysicalParameters.class
        )).message();
        JsonNode encodedTree = TEST_MAPPER.readTree(encoded);

        assertAll(
            () -> assertNull(parameters.minimumAngularSpeed()),
            () -> assertNull(parameters.maximumAngularSpeed()),
            () -> assertFalse(encodedTree.has("minimumAngularSpeed")),
            () -> assertFalse(encodedTree.has("maximumAngularSpeed")),
            () -> assertEquals(TEST_MAPPER.readTree(payload), encodedTree),
            () -> assertEquals(parameters, roundTripped)
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-010] Factsheet 片段拒绝标准可选字段显式 null")
    void rejectsExplicitNullForOptionalStandardFragmentField() {
        byte[] payload = bytes("""
            {
              "seriesName":"Forkbot X",
              "seriesDescription":null,
              "mobileRobotKinematics":"DIFFERENTIAL",
              "mobileRobotClass":"FORKLIFT",
              "maximumLoadMass":1250.5,
              "localizationTypes":["NATURAL"],
              "navigationTypes":["FREELY_NAVIGATING"]
            }
            """);

        RejectedInboundMessage<TypeSpecification> rejected = rejected(CODEC.decode(
            TopicName.FACTSHEET,
            payload,
            TypeSpecification.class
        ));

        assertAll(
            () -> assertEquals("EXPLICIT_NULL", rejected.issues().getFirst().code()),
            () -> assertEquals(
                "/seriesDescription",
                rejected.issues().getFirst().path()
            )
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] Codec 往返存在的可选字段和独立扩展值")
    void roundTripsPresentOptionalFieldsAndStandaloneExtensibleValues()
        throws Exception {
        TypeSpecification specification = TypeSpecification.builder()
            .seriesName("Forkbot X")
            .seriesDescription("Warehouse forklift series")
            .mobileRobotKinematics(MobileRobotKinematics.DIFFERENTIAL)
            .mobileRobotClass(MobileRobotClass.FORKLIFT)
            .maximumLoadMass(1_250.5D)
            .localizationTypes(List.of(LocalizationType.NATURAL))
            .navigationTypes(List.of(NavigationType.FREELY_NAVIGATING))
            .build();
        PhysicalParameters parameters = PhysicalParameters.builder()
            .minimumSpeed(0.05D)
            .maximumSpeed(2.0D)
            .minimumAngularSpeed(0.1D)
            .maximumAngularSpeed(1.5D)
            .maximumAcceleration(0.8D)
            .maximumDeceleration(1.2D)
            .minimumHeight(0.2D)
            .maximumHeight(2.2D)
            .width(1.1D)
            .length(2.0D)
            .build();

        byte[] specificationJson = CODEC.encode(specification);
        byte[] parametersJson = CODEC.encode(parameters);
        byte[] extensibleValueJson = CODEC.encode(
            LocalizationType.of("ULTRA_WIDEBAND")
        );
        TypeSpecification decodedSpecification = decoded(CODEC.decode(
            TopicName.FACTSHEET,
            specificationJson,
            TypeSpecification.class
        )).message();
        PhysicalParameters decodedParameters = decoded(CODEC.decode(
            TopicName.FACTSHEET,
            parametersJson,
            PhysicalParameters.class
        )).message();
        LocalizationType decodedValue = decoded(CODEC.decode(
            TopicName.FACTSHEET,
            extensibleValueJson,
            LocalizationType.class
        )).message();
        JsonNode specificationTree = TEST_MAPPER.readTree(specificationJson);
        JsonNode parametersTree = TEST_MAPPER.readTree(parametersJson);

        assertAll(
            () -> assertEquals(
                "Warehouse forklift series",
                specificationTree.path("seriesDescription").textValue()
            ),
            () -> assertFalse(specificationTree.has("supportedZones")),
            () -> assertEquals(
                0.1D,
                parametersTree.path("minimumAngularSpeed").doubleValue()
            ),
            () -> assertEquals(
                1.5D,
                parametersTree.path("maximumAngularSpeed").doubleValue()
            ),
            () -> assertEquals(
                "\"ULTRA_WIDEBAND\"",
                new String(extensibleValueJson, StandardCharsets.UTF_8)
            ),
            () -> assertEquals(specification, decodedSpecification),
            () -> assertEquals(parameters, decodedParameters),
            () -> assertEquals(
                LocalizationType.of("ULTRA_WIDEBAND"),
                decodedValue
            )
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] Codec 拒绝非法子对象形状和封闭 Zone 值")
    void rejectsInvalidFragmentShapesAndClosedZoneValues() {
        String validPrefix = """
            {
              "seriesName":"Forkbot X",
              "mobileRobotKinematics":"DIFFERENTIAL",
              "mobileRobotClass":"FORKLIFT",
              "maximumLoadMass":1250.5,
              "localizationTypes":["NATURAL"],
              "navigationTypes":["FREELY_NAVIGATING"]
            """;

        assertAll(
            () -> assertEquals(
                "INVALID_JSON_TYPE",
                firstIssue(CODEC.decode(
                    TopicName.FACTSHEET,
                    bytes("[]"),
                    TypeSpecification.class
                )).code()
            ),
            () -> assertEquals(
                "INVALID_JSON_TYPE",
                firstIssue(CODEC.decode(
                    TopicName.FACTSHEET,
                    bytes(validPrefix.replace(
                        "\"seriesName\":\"Forkbot X\",",
                        ""
                    ) + "}"),
                    TypeSpecification.class
                )).code()
            ),
            () -> assertEquals(
                "INVALID_JSON_TYPE",
                firstIssue(CODEC.decode(
                    TopicName.FACTSHEET,
                    bytes(validPrefix.replace(
                        "[\"NATURAL\"]",
                        "\"NATURAL\""
                    ) + "}"),
                    TypeSpecification.class
                )).code()
            ),
            () -> assertEquals(
                "INVALID_JSON_TYPE",
                firstIssue(CODEC.decode(
                    TopicName.FACTSHEET,
                    bytes(validPrefix.replace(
                        "\"DIFFERENTIAL\"",
                        "1"
                    ) + "}"),
                    TypeSpecification.class
                )).code()
            ),
            () -> assertEquals(
                "INVALID_JSON_TYPE",
                firstIssue(CODEC.decode(
                    TopicName.FACTSHEET,
                    bytes(validPrefix + ",\"supportedZones\":[\"VENDOR_ZONE\"]}"),
                    TypeSpecification.class
                )).code()
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

    private static <T> ValidationIssue firstIssue(
        DecodingResult<T> result
    ) {
        return rejected(result).issues().getFirst();
    }

    private static byte[] bytes(String json) {
        return json.getBytes(StandardCharsets.UTF_8);
    }
}
