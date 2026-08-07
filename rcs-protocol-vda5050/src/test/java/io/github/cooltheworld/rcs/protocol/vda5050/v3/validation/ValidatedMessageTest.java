package io.github.cooltheworld.rcs.protocol.vda5050.v3.validation;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.ProtocolVersionProfile;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

final class ValidatedMessageTest {
    private static final ValidationIssue WARNING = new ValidationIssue(
        "IGNORED_EXTENSION",
        ValidationSeverity.WARNING,
        "/vendorField",
        "Unknown extension has no executable semantics",
        "VDA3-SHARED-007"
    );

    @Test
    @DisplayName("[VDA3-SHARED-008] 成功凭证保留消息、版本配置和只读警告")
    void retainsValidatedMessageAndProfile() {
        String message = "typed-message";
        ValidatedMessage<String> validated = new ValidatedMessage<>(
            message,
            ProtocolVersionProfile.V3_0_0,
            List.of(WARNING)
        );

        assertAll(
            () -> assertTrue(validated.isAccepted()),
            () -> assertEquals(message, validated.message()),
            () -> assertEquals(
                ProtocolVersionProfile.V3_0_0,
                validated.versionProfile()
            ),
            () -> assertEquals(List.of(WARNING), validated.issues()),
            () -> assertThrows(
                UnsupportedOperationException.class,
                () -> validated.issues().add(WARNING)
            )
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-008] 公共 API 不提供成功凭证构造入口")
    void exposesNoPublicMintingEntryPoint() {
        boolean hasPublicConstructor = Arrays.stream(
            ValidatedMessage.class.getDeclaredConstructors()
        ).anyMatch(constructor -> Modifier.isPublic(constructor.getModifiers()));
        boolean hasPublicFactory = Arrays.stream(
            ValidatedMessage.class.getDeclaredMethods()
        ).anyMatch(method ->
            Modifier.isPublic(method.getModifiers())
                && Modifier.isStatic(method.getModifiers())
                && ValidatedMessage.class.isAssignableFrom(method.getReturnType())
        );

        assertAll(
            () -> assertFalse(hasPublicConstructor),
            () -> assertFalse(hasPublicFactory)
        );
    }

    @Test
    void refusesErrorIssuesInSuccessfulResult() {
        ValidationIssue error = new ValidationIssue(
            "INVALID_MESSAGE",
            ValidationSeverity.ERROR,
            "",
            "Message is invalid",
            "VDA3-SHARED-008"
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> new ValidatedMessage<>(
                "typed-message",
                ProtocolVersionProfile.V3_0_0,
                List.of(error)
            )
        );
    }

    @Test
    void rejectsMissingSuccessfulResultState() {
        assertAll(
            () -> assertThrows(
                NullPointerException.class,
                () -> new ValidatedMessage<>(
                    null,
                    ProtocolVersionProfile.V3_0_0,
                    List.of()
                )
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> new ValidatedMessage<>("message", null, List.of())
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> new ValidatedMessage<>(
                    "message",
                    ProtocolVersionProfile.V3_0_0,
                    null
                )
            )
        );
    }
}
