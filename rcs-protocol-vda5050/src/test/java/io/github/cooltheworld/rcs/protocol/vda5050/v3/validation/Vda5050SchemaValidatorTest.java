package io.github.cooltheworld.rcs.protocol.vda5050.v3.validation;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    void acceptsValidConnectionFixture() {
        List<ValidationIssue> issues = validator.validate(
            TopicName.CONNECTION,
            fixture("connection/valid/minimal.json")
        );

        assertTrue(issues.isEmpty());
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
