package io.github.cooltheworld.rcs.protocol.vda5050.v3.codec;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.LoadSet;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.LoadSpecification;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.topic.TopicName;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.DecodedMessage;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.DecodingResult;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.RejectedInboundMessage;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unchecked")
final class LoadSpecificationCodecTest {
    private static final String FIXTURE =
        "vda5050/v3.0.0/fixtures/factsheet/load-specification-cases.json";
    private static final ObjectMapper TEST_MAPPER = JsonMapper.builder().build();
    private static final Vda5050JsonCodec CODEC = Vda5050JsonCodec.createDefault();

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] 完整载荷片段确定性往返全部字段和扩展")
    void roundTripsCompleteLoadSpecificationFixtureDeterministically()
        throws Exception {
        byte[] payload = fixture("/valid");

        LoadSpecification specification = decoded(CODEC.decode(
            TopicName.FACTSHEET,
            payload,
            LoadSpecification.class
        )).message();
        byte[] firstEncoding = CODEC.encode(specification);
        byte[] secondEncoding = CODEC.encode(specification);
        LoadSpecification roundTripped = decoded(CODEC.decode(
            TopicName.FACTSHEET,
            firstEncoding,
            LoadSpecification.class
        )).message();
        LoadSet loadSet = specification.loadSets().getFirst();

        assertAll(
            () -> assertEquals("EURO_PALLET", loadSet.setName()),
            () -> assertEquals(1.5D, loadSet.maximumSpeed()),
            () -> assertFalse(specification.extensionFields().isEmpty()),
            () -> assertFalse(loadSet.extensionFields().isEmpty()),
            () -> assertFalse(loadSet
                .boundingBoxReference()
                .extensionFields()
                .isEmpty()),
            () -> assertFalse(loadSet
                .loadDimensions()
                .extensionFields()
                .isEmpty()),
            () -> assertEquals(
                TEST_MAPPER.readTree(payload),
                TEST_MAPPER.readTree(firstEncoding)
            ),
            () -> assertArrayEquals(firstEncoding, secondEncoding),
            () -> assertEquals(specification, roundTripped)
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] 边界载荷片段区分缺失、空列表和扩展 null")
    void roundTripsBoundaryLoadSpecificationFixture() throws Exception {
        byte[] payload = fixture("/boundary");

        LoadSpecification specification = decoded(CODEC.decode(
            TopicName.FACTSHEET,
            payload,
            LoadSpecification.class
        )).message();
        byte[] encoded = CODEC.encode(specification);
        LoadSet minimal = specification.loadSets().getFirst();
        LoadSet minimalGeometry = specification.loadSets().get(1);
        LoadSpecification absentCollections = LoadSpecification.builder().build();
        byte[] absentCollectionsJson = CODEC.encode(absentCollections);

        assertAll(
            () -> assertEquals(0, specification.loadPositions().size()),
            () -> assertNull(minimal.loadPositions()),
            () -> assertNull(minimal.boundingBoxReference()),
            () -> assertNull(minimal.loadDimensions()),
            () -> assertNull(minimal.maximumWeight()),
            () -> assertNull(minimal.description()),
            () -> assertNull(minimalGeometry.boundingBoxReference().theta()),
            () -> assertNull(minimalGeometry.loadDimensions().height()),
            () -> assertFalse(specification.extensionFields().isEmpty()),
            () -> assertEquals(
                TEST_MAPPER.readTree(payload),
                TEST_MAPPER.readTree(encoded)
            ),
            () -> assertEquals(
                TEST_MAPPER.createObjectNode(),
                TEST_MAPPER.readTree(absentCollectionsJson)
            )
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-010] 载荷片段拒绝嵌套标准可选字段显式 null")
    void rejectsExplicitNullInNestedOptionalLoadSetField() throws Exception {
        RejectedInboundMessage<LoadSpecification> rejected = rejected(CODEC.decode(
            TopicName.FACTSHEET,
            fixture("/invalid/explicitNull"),
            LoadSpecification.class
        ));

        assertAll(
            () -> assertEquals("EXPLICIT_NULL", rejected.issues().getFirst().code()),
            () -> assertEquals(
                "/loadSets/0/maximumWeight",
                rejected.issues().getFirst().path()
            ),
            () -> assertEquals(
                "VDA3-SHARED-010",
                rejected.issues().getFirst().requirementId()
            )
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] 载荷片段拒绝缺失必填字段和非法对象形状")
    void rejectsInvalidLoadSpecificationFixtureShapes() throws Exception {
        assertAll(
            () -> assertEquals(
                "INVALID_JSON_TYPE",
                rejected(CODEC.decode(
                    TopicName.FACTSHEET,
                    fixture("/invalid/missingSetName"),
                    LoadSpecification.class
                )).issues().getFirst().code()
            ),
            () -> assertEquals(
                "INVALID_JSON_TYPE",
                rejected(CODEC.decode(
                    TopicName.FACTSHEET,
                    fixture("/invalid/wrongLoadSetsShape"),
                    LoadSpecification.class
                )).issues().getFirst().code()
            ),
            () -> assertEquals(
                "INVALID_JSON_TYPE",
                rejected(CODEC.decode(
                    TopicName.FACTSHEET,
                    fixture("/invalid/notObject"),
                    LoadSpecification.class
                )).issues().getFirst().code()
            )
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] 独立 Jackson Module 注册载荷对象图")
    void registersLoadSpecificationWithCallerObjectMapper() throws Exception {
        ObjectMapper mapper = JsonMapper.builder()
            .addModule(new Vda5050JacksonModule())
            .build();
        byte[] payload = fixture("/valid");

        LoadSpecification specification = mapper.readValue(
            payload,
            LoadSpecification.class
        );
        byte[] encoded = mapper.writeValueAsBytes(specification);

        assertAll(
            () -> assertEquals(
                TEST_MAPPER.readTree(payload),
                TEST_MAPPER.readTree(encoded)
            ),
            () -> assertThrows(
                MismatchedInputException.class,
                () -> mapper.readValue(
                    fixture("/invalid/missingSetName"),
                    LoadSpecification.class
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
        try (InputStream input = LoadSpecificationCodecTest.class
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
