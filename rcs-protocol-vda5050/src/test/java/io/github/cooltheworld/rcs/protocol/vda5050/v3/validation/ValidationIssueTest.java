package io.github.cooltheworld.rcs.protocol.vda5050.v3.validation;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

final class ValidationIssueTest {
    @Test
    @DisplayName("[VDA3-SHARED-008] Issue 提供稳定代码、级别、路径、说明和需求引用")
    void exposesStructuredValidationContext() {
        ValidationIssue issue = new ValidationIssue(
            "UNSUPPORTED_PROTOCOL_VERSION",
            ValidationSeverity.ERROR,
            "/version",
            "Protocol version has no explicit support profile",
            "VDA3-SHARED-003"
        );

        assertAll(
            () -> assertEquals("UNSUPPORTED_PROTOCOL_VERSION", issue.code()),
            () -> assertEquals(ValidationSeverity.ERROR, issue.severity()),
            () -> assertEquals("/version", issue.path()),
            () -> assertEquals(
                "Protocol version has no explicit support profile",
                issue.description()
            ),
            () -> assertEquals("VDA3-SHARED-003", issue.requirementId())
        );
    }

    @Test
    void preservesEmptyRootPath() {
        ValidationIssue issue = new ValidationIssue(
            "INVALID_JSON",
            ValidationSeverity.ERROR,
            "",
            "Payload is not valid JSON",
            "VDA3-SHARED-008"
        );

        assertEquals("", issue.path());
    }

    @Test
    void rejectsInvalidIssueMetadata() {
        assertAll(
            () -> assertThrows(
                IllegalArgumentException.class,
                () -> issue("lower_case", ValidationSeverity.ERROR, "/version")
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> issue("INVALID_JSON", null, "/version")
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> issue("INVALID_JSON", ValidationSeverity.ERROR, null)
            ),
            () -> assertThrows(
                IllegalArgumentException.class,
                () -> new ValidationIssue(
                    "INVALID_JSON",
                    ValidationSeverity.ERROR,
                    "",
                    " ",
                    "VDA3-SHARED-008"
                )
            ),
            () -> assertThrows(
                IllegalArgumentException.class,
                () -> new ValidationIssue(
                    "INVALID_JSON",
                    ValidationSeverity.ERROR,
                    "",
                    "Payload is not valid JSON",
                    "SHARED-008"
                )
            )
        );
    }

    private static ValidationIssue issue(
        String code,
        ValidationSeverity severity,
        String path
    ) {
        return new ValidationIssue(
            code,
            severity,
            path,
            "Description",
            "VDA3-SHARED-008"
        );
    }
}
