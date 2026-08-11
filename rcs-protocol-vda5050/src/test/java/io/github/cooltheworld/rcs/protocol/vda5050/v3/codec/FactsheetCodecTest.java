package io.github.cooltheworld.rcs.protocol.vda5050.v3.codec;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.Factsheet;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.topic.TopicName;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.DecodedMessage;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.DecodingResult;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.RejectedInboundMessage;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.Vda5050SchemaValidator;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unchecked")
final class FactsheetCodecTest {
    private static final String FIXTURE =
        "vda5050/v3.0.0/fixtures/factsheet/factsheet-cases.json";
    private static final ObjectMapper TEST_MAPPER = JsonMapper.builder().build();
    private static final Vda5050JsonCodec CODEC =
        Vda5050JsonCodec.createDefault();
    private static final Vda5050SchemaValidator SCHEMA_VALIDATOR =
        Vda5050SchemaValidator.createDefault();

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] 完整 Factsheet 平铺确定性往返全部对象图和扩展")
    void roundTripsCompleteFactsheetDeterministically() throws Exception {
        byte[] payload = fixture("/valid");

        Factsheet factsheet = decoded(CODEC.decode(
            TopicName.FACTSHEET,
            payload,
            Factsheet.class
        )).message();
        byte[] firstEncoding = CODEC.encode(factsheet);
        byte[] secondEncoding = CODEC.encode(factsheet);
        Factsheet roundTripped = decoded(CODEC.decode(
            TopicName.FACTSHEET,
            firstEncoding,
            Factsheet.class
        )).message();
        JsonNode encoded = TEST_MAPPER.readTree(firstEncoding);

        assertAll(
            () -> assertEquals(4_294_967_295L, factsheet.header().headerId()),
            () -> assertEquals(
                "ACME",
                factsheet.header().robotIdentity().manufacturer()
            ),
            () -> assertFalse(factsheet.extensionFields().isEmpty()),
            () -> assertFalse(
                factsheet.content().typeSpecification().extensionFields().isEmpty()
            ),
            () -> assertFalse(
                factsheet.content()
                    .mobileRobotConfiguration()
                    .versions()
                    .getFirst()
                    .extensionFields()
                    .isEmpty()
            ),
            () -> assertFalse(encoded.has("content")),
            () -> assertTrue(encoded.has("typeSpecification")),
            () -> assertEquals(
                TEST_MAPPER.readTree(payload),
                encoded
            ),
            () -> assertArrayEquals(firstEncoding, secondEncoding),
            () -> assertEquals(factsheet, roundTripped),
            () -> assertTrue(SCHEMA_VALIDATOR
                .validate(TopicName.FACTSHEET, firstEncoding)
                .isEmpty())
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] 根边界保留零 headerId 和缺失可选配置")
    void roundTripsBoundaryWithoutOptionalConfiguration() throws Exception {
        byte[] payload = fixture("/boundary");

        Factsheet factsheet = decoded(CODEC.decode(
            TopicName.FACTSHEET,
            payload,
            Factsheet.class
        )).message();
        byte[] encoded = CODEC.encode(factsheet);
        JsonNode encodedTree = TEST_MAPPER.readTree(encoded);

        assertAll(
            () -> assertEquals(0L, factsheet.header().headerId()),
            () -> assertNull(
                factsheet.content().mobileRobotConfiguration()
            ),
            () -> assertFalse(encodedTree.has("mobileRobotConfiguration")),
            () -> assertFalse(encodedTree.has("content")),
            () -> assertEquals(TEST_MAPPER.readTree(payload), encodedTree),
            () -> assertTrue(SCHEMA_VALIDATOR
                .validate(TopicName.FACTSHEET, encoded)
                .isEmpty())
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-010] Factsheet 拒绝可选根字段显式 null")
    void rejectsExplicitNullForOptionalRootField() throws Exception {
        RejectedInboundMessage<Factsheet> rejected = rejected(CODEC.decode(
            TopicName.FACTSHEET,
            fixture("/invalid/explicitNullConfiguration"),
            Factsheet.class
        ));

        assertAll(
            () -> assertEquals(
                "EXPLICIT_NULL",
                rejected.issues().getFirst().code()
            ),
            () -> assertEquals(
                "/mobileRobotConfiguration",
                rejected.issues().getFirst().path()
            ),
            () -> assertEquals(
                "VDA3-SHARED-010",
                rejected.issues().getFirst().requirementId()
            )
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] Factsheet 拒绝缺失必填字段和非法对象形状")
    void rejectsMissingAndInvalidRootShapes() throws Exception {
        byte[] missing = fixture("/invalid/missingLoadSpecification");
        ObjectNode invalidIdentity = (ObjectNode) TEST_MAPPER.readTree(
            fixture("/boundary")
        );
        invalidIdentity.put("manufacturer", "bad/name");

        assertAll(
            () -> assertEquals(
                "INVALID_JSON_TYPE",
                rejected(CODEC.decode(
                    TopicName.FACTSHEET,
                    missing,
                    Factsheet.class
                )).issues().getFirst().code()
            ),
            () -> assertEquals(
                "INVALID_JSON_TYPE",
                rejected(CODEC.decode(
                    TopicName.FACTSHEET,
                    fixture("/invalid/wrongPhysicalShape"),
                    Factsheet.class
                )).issues().getFirst().code()
            ),
            () -> assertEquals(
                "INVALID_JSON_TYPE",
                rejected(CODEC.decode(
                    TopicName.FACTSHEET,
                    fixture("/invalid/notObject"),
                    Factsheet.class
                )).issues().getFirst().code()
            ),
            () -> assertEquals(
                "INVALID_JSON_TYPE",
                rejected(CODEC.decode(
                    TopicName.FACTSHEET,
                    TEST_MAPPER.writeValueAsBytes(invalidIdentity),
                    Factsheet.class
                )).issues().getFirst().code()
            ),
            () -> assertFalse(SCHEMA_VALIDATOR
                .validate(TopicName.FACTSHEET, missing)
                .isEmpty())
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] 独立 Jackson Module 注册平铺 Factsheet 根对象")
    void registersFactsheetWithCallerObjectMapper() throws Exception {
        ObjectMapper mapper = JsonMapper.builder()
            .addModule(new Vda5050JacksonModule())
            .build();
        byte[] payload = fixture("/valid");
        ObjectNode invalidIdentity = (ObjectNode) TEST_MAPPER.readTree(
            fixture("/boundary")
        );
        invalidIdentity.put("manufacturer", "bad/name");

        Factsheet factsheet = mapper.readValue(payload, Factsheet.class);
        byte[] encoded = mapper.writeValueAsBytes(factsheet);
        MismatchedInputException invalidIdentityFailure = assertThrows(
            MismatchedInputException.class,
            () -> mapper.readValue(
                TEST_MAPPER.writeValueAsBytes(invalidIdentity),
                Factsheet.class
            )
        );

        assertAll(
            () -> assertEquals(
                TEST_MAPPER.readTree(payload),
                TEST_MAPPER.readTree(encoded)
            ),
            () -> assertThrows(
                MismatchedInputException.class,
                () -> mapper.readValue(
                    fixture("/invalid/missingLoadSpecification"),
                    Factsheet.class
                )
            ),
            () -> assertTrue(
                invalidIdentityFailure.getOriginalMessage().contains(
                    "Factsheet fields do not satisfy model constraints"
                )
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

    private static byte[] fixture(String pointer) throws IOException {
        try (InputStream input = FactsheetCodecTest.class
            .getClassLoader()
            .getResourceAsStream(FIXTURE)) {
            if (input == null) {
                throw new IllegalArgumentException("Missing fixture: " + FIXTURE);
            }
            JsonNode fixture = TEST_MAPPER.readTree(input).at(pointer);
            if (fixture.isMissingNode()) {
                throw new IllegalArgumentException(
                    "Missing fixture case: " + pointer
                );
            }
            return TEST_MAPPER.writeValueAsBytes(fixture);
        }
    }
}
