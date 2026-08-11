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
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.MobileRobotConfiguration;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.topic.TopicName;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.DecodedMessage;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.DecodingResult;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.RejectedInboundMessage;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unchecked")
final class MobileRobotConfigurationCodecTest {
    private static final String FIXTURE =
        "vda5050/v3.0.0/fixtures/factsheet/mobile-robot-configuration-cases.json";
    private static final ObjectMapper TEST_MAPPER = JsonMapper.builder().build();
    private static final Vda5050JsonCodec CODEC =
        Vda5050JsonCodec.createDefault();

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] 完整配置对象图确定性往返字段与扩展")
    void roundTripsCompleteConfigurationDeterministically() throws Exception {
        byte[] payload = fixture("/valid");

        MobileRobotConfiguration configuration = decoded(CODEC.decode(
            TopicName.FACTSHEET,
            payload,
            MobileRobotConfiguration.class
        )).message();
        byte[] firstEncoding = CODEC.encode(configuration);
        byte[] secondEncoding = CODEC.encode(configuration);
        MobileRobotConfiguration roundTripped = decoded(CODEC.decode(
            TopicName.FACTSHEET,
            firstEncoding,
            MobileRobotConfiguration.class
        )).message();

        assertAll(
            () -> assertEquals(
                "softwareVersion",
                configuration.versions().getFirst().key()
            ),
            () -> assertEquals(
                4_294_967_295L,
                configuration.batteryCharging().minimumChargingTime()
            ),
            () -> assertFalse(configuration.extensionFields().isEmpty()),
            () -> assertFalse(
                configuration.versions().getFirst().extensionFields().isEmpty()
            ),
            () -> assertFalse(configuration.network().extensionFields().isEmpty()),
            () -> assertFalse(
                configuration.batteryCharging().extensionFields().isEmpty()
            ),
            () -> assertEquals(
                TEST_MAPPER.readTree(payload),
                TEST_MAPPER.readTree(firstEncoding)
            ),
            () -> assertArrayEquals(firstEncoding, secondEncoding),
            () -> assertEquals(configuration, roundTripped)
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] 配置边界区分缺失空列表空对象和扩展 null")
    void roundTripsBoundaryAndAbsentConfigurationFields() throws Exception {
        byte[] payload = fixture("/boundary");

        MobileRobotConfiguration configuration = decoded(CODEC.decode(
            TopicName.FACTSHEET,
            payload,
            MobileRobotConfiguration.class
        )).message();
        byte[] encoded = CODEC.encode(configuration);
        MobileRobotConfiguration absent = MobileRobotConfiguration.builder()
            .build();

        assertAll(
            () -> assertEquals(0, configuration.versions().size()),
            () -> assertEquals(0, configuration.network().dnsServers().size()),
            () -> assertNull(configuration.network().ntpServers()),
            () -> assertNull(configuration.network().localIpAddress()),
            () -> assertNull(
                configuration.batteryCharging().minimumChargingTime()
            ),
            () -> assertFalse(configuration.extensionFields().isEmpty()),
            () -> assertEquals(
                TEST_MAPPER.readTree(payload),
                TEST_MAPPER.readTree(encoded)
            ),
            () -> assertEquals(
                TEST_MAPPER.createObjectNode(),
                TEST_MAPPER.readTree(CODEC.encode(absent))
            )
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-010] 配置对象图拒绝嵌套标准可选字段显式 null")
    void rejectsExplicitNullInNestedConfigurationField() throws Exception {
        RejectedInboundMessage<MobileRobotConfiguration> rejected = rejected(
            CODEC.decode(
                TopicName.FACTSHEET,
                fixture("/invalid/explicitNull"),
                MobileRobotConfiguration.class
            )
        );

        assertAll(
            () -> assertEquals(
                "EXPLICIT_NULL",
                rejected.issues().getFirst().code()
            ),
            () -> assertEquals(
                "/batteryCharging/minimumChargingTime",
                rejected.issues().getFirst().path()
            ),
            () -> assertEquals(
                "VDA3-SHARED-010",
                rejected.issues().getFirst().requirementId()
            )
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] 配置对象图拒绝缺失必填字段和非法形状")
    void rejectsInvalidConfigurationShapes() throws Exception {
        assertAll(
            () -> assertEquals(
                "INVALID_JSON_TYPE",
                rejected(CODEC.decode(
                    TopicName.FACTSHEET,
                    fixture("/invalid/missingVersionKey"),
                    MobileRobotConfiguration.class
                )).issues().getFirst().code()
            ),
            () -> assertEquals(
                "INVALID_JSON_TYPE",
                rejected(CODEC.decode(
                    TopicName.FACTSHEET,
                    fixture("/invalid/wrongVersionsShape"),
                    MobileRobotConfiguration.class
                )).issues().getFirst().code()
            ),
            () -> assertEquals(
                "INVALID_JSON_TYPE",
                rejected(CODEC.decode(
                    TopicName.FACTSHEET,
                    fixture("/invalid/notObject"),
                    MobileRobotConfiguration.class
                )).issues().getFirst().code()
            )
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] 独立 Jackson Module 注册配置对象图")
    void registersConfigurationWithCallerObjectMapper() throws Exception {
        ObjectMapper mapper = JsonMapper.builder()
            .addModule(new Vda5050JacksonModule())
            .build();
        byte[] payload = fixture("/valid");

        MobileRobotConfiguration configuration = mapper.readValue(
            payload,
            MobileRobotConfiguration.class
        );
        byte[] encoded = mapper.writeValueAsBytes(configuration);

        assertAll(
            () -> assertEquals(
                TEST_MAPPER.readTree(payload),
                TEST_MAPPER.readTree(encoded)
            ),
            () -> assertThrows(
                MismatchedInputException.class,
                () -> mapper.readValue(
                    fixture("/invalid/missingVersionKey"),
                    MobileRobotConfiguration.class
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
        try (InputStream input = MobileRobotConfigurationCodecTest.class
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
