package io.github.cooltheworld.rcs.protocol.vda5050.v3.validation;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.codec.JsonCodecLimits;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.topic.TopicName;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

final class Vda5050SchemaValidatorTest {
    private final Vda5050SchemaValidator validator =
        Vda5050SchemaValidator.createDefault();

    @Test
    @DisplayName("[VDA3-CONNECTION-001] 有效 Connection Fixture 通过 Schema")
    void acceptsValidConnectionFixtures() {
        assertAll(
            () -> assertTrue(validator.validate(
                TopicName.CONNECTION,
                fixture("connection/valid/minimal.json")
            ).isEmpty()),
            () -> assertTrue(validator.validate(
                TopicName.CONNECTION,
                fixture("connection/valid/with-extensions.json")
            ).isEmpty())
        );
    }

    @Test
    @DisplayName("[VDA3-CONNECTION-001] 缺失必填字段返回稳定代码和路径")
    void mapsMissingRequiredPropertyToStableIssue() {
        List<ValidationIssue> issues = validator.validate(
            TopicName.CONNECTION,
            fixture("connection/invalid/missing-connection-state.json")
        );

        assertEquals(1, issues.size());
        ValidationIssue issue = issues.getFirst();
        assertAll(
            () -> assertEquals("SCHEMA_REQUIRED", issue.code()),
            () -> assertEquals(ValidationSeverity.ERROR, issue.severity()),
            () -> assertEquals("/connectionState", issue.path()),
            () -> assertEquals("VDA3-CONNECTION-001", issue.requirementId())
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-001] Draft 2020-12 启用 date-time 断言")
    void rejectsInvalidStandardDateTimeFormat() {
        List<ValidationIssue> issues = validator.validate(
            TopicName.CONNECTION,
            fixture("connection/invalid/non-date-time-timestamp.json")
        );

        assertEquals(1, issues.size());
        ValidationIssue issue = issues.getFirst();
        assertAll(
            () -> assertEquals("SCHEMA_FORMAT", issue.code()),
            () -> assertEquals("/timestamp", issue.path()),
            () -> assertEquals("VDA3-SHARED-001", issue.requirementId())
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-002] uint32 范围保留给 Long 语义校验")
    void doesNotTreatCustomUint32FormatAsRangeValidation() {
        List<ValidationIssue> issues = validator.validate(
            TopicName.RESPONSES,
            fixture("responses/boundary/uint32-requires-semantic-validation.json")
        );

        assertTrue(issues.isEmpty());
    }

    @Test
    @DisplayName("[VDA3-SHARED-009] Schema 解析前执行 payload 硬上限")
    void rejectsResourceLimitBeforeSchemaValidation() {
        Vda5050SchemaValidator constrained = Vda5050SchemaValidator.create(
            JsonCodecLimits.builder().maxPayloadBytes(1).build()
        );

        List<ValidationIssue> issues = constrained.validate(
            TopicName.CONNECTION,
            "{}".getBytes(java.nio.charset.StandardCharsets.UTF_8)
        );

        assertAll(
            () -> assertEquals(1, issues.size()),
            () -> assertEquals("PAYLOAD_TOO_LARGE", issues.getFirst().code()),
            () -> assertEquals(
                "VDA3-SHARED-009",
                issues.getFirst().requirementId()
            )
        );
    }

    @Test
    void passesThroughMalformedJsonAsStructuredIssue() {
        List<ValidationIssue> issues = validator.validate(
            TopicName.ORDER,
            "{".getBytes(java.nio.charset.StandardCharsets.UTF_8)
        );

        assertAll(
            () -> assertEquals(1, issues.size()),
            () -> assertEquals("INVALID_JSON", issues.getFirst().code())
        );
    }

    @Test
    void doesNotCopyUntrustedValuesIntoSchemaIssueDescription() {
        String secretMarker = "DO_NOT_COPY_THIS_VALUE";
        String payload = """
            {
              "headerId": 0,
              "timestamp": "2026-08-07T08:00:00.123Z",
              "version": "3.0.0",
              "manufacturer": "Acme",
              "serialNumber": "R-001",
              "connectionState": "%s"
            }
            """.formatted(secretMarker);

        List<ValidationIssue> issues = validator.validate(
            TopicName.CONNECTION,
            payload.getBytes(java.nio.charset.StandardCharsets.UTF_8)
        );

        assertAll(
            () -> assertEquals("SCHEMA_ENUM", issues.getFirst().code()),
            () -> assertEquals("/connectionState", issues.getFirst().path()),
            () -> assertTrue(
                issues.getFirst().description().contains("schema constraint")
            ),
            () -> assertTrue(
                !issues.getFirst().description().contains(secretMarker)
            ),
            () -> assertThrows(
                UnsupportedOperationException.class,
                () -> issues.add(issues.getFirst())
            )
        );
    }

    @Test
    void rejectsMissingProgrammingArguments() {
        byte[] payload = fixture("connection/valid/minimal.json");

        assertAll(
            () -> assertThrows(
                NullPointerException.class,
                () -> validator.validate(null, payload)
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> validator.validate(TopicName.CONNECTION, null)
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> Vda5050SchemaValidator.create(null)
            )
        );
    }

    private static byte[] fixture(String path) {
        String resource = "vda5050/v3.0.0/fixtures/" + path;
        try (InputStream input = Vda5050SchemaValidatorTest.class
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
