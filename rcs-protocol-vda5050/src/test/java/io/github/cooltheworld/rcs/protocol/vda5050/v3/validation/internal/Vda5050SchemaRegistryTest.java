package io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.networknt.schema.Error;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.topic.TopicName;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

final class Vda5050SchemaRegistryTest {
    @ParameterizedTest(name = "[{1}] {0} Schema 可以离线编译")
    @MethodSource("topics")
    void compilesEveryBundledSchema(
        TopicName topic,
        String requirementId
    ) {
        Vda5050SchemaRegistry registry = Vda5050SchemaRegistry.create();

        assertNotNull(registry.schemaFor(topic));
    }

    @Test
    void rejectsIncompleteRegistryAndMissingSchemaLookup() throws Exception {
        var constructor = Vda5050SchemaRegistry.class.getDeclaredConstructor(
            Map.class
        );
        constructor.setAccessible(true);
        Vda5050SchemaRegistry emptyRegistry = constructor.newInstance(Map.of());

        assertThrows(
            IllegalStateException.class,
            () -> Vda5050SchemaRegistry.requireComplete(Map.of())
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> emptyRegistry.schemaFor(TopicName.CONNECTION)
        );
    }

    @Test
    void rejectsInvalidOrMissingBundledSchemaResources() {
        Error schemaError = Error.builder()
            .keyword("type")
            .message("invalid schema")
            .build();

        Vda5050SchemaRegistry.requireValidSchema(List.of(), "valid.schema");
        assertThrows(
            IllegalStateException.class,
            () -> Vda5050SchemaRegistry.requireValidSchema(
                List.of(schemaError),
                "invalid.schema"
            )
        );
        assertThrows(
            IllegalStateException.class,
            () -> Vda5050SchemaRegistry.readResource("missing.schema")
        );
    }

    @Test
    void mapsMissingSchemaKeywordsToStableFallbackCode() {
        Error missingKeyword = Error.builder().message("missing").build();
        Error blankKeyword = Error.builder()
            .keyword("  ")
            .message("blank")
            .build();

        assertEquals(
            "SCHEMA_VIOLATION",
            NetworkntVda5050SchemaValidator.codeOf(missingKeyword)
        );
        assertEquals(
            "SCHEMA_VIOLATION",
            NetworkntVda5050SchemaValidator.codeOf(blankKeyword)
        );
    }

    private static Stream<Arguments> topics() {
        return Stream.of(
            Arguments.of(TopicName.CONNECTION, "VDA3-CONNECTION-001"),
            Arguments.of(TopicName.FACTSHEET, "VDA3-FACTSHEET-001"),
            Arguments.of(TopicName.INSTANT_ACTIONS, "VDA3-INSTANT_ACTIONS-001"),
            Arguments.of(TopicName.ORDER, "VDA3-ORDER-001"),
            Arguments.of(TopicName.RESPONSES, "VDA3-RESPONSES-001"),
            Arguments.of(TopicName.STATE, "VDA3-STATE-001"),
            Arguments.of(TopicName.VISUALIZATION, "VDA3-VISUALIZATION-001"),
            Arguments.of(TopicName.ZONE_SET, "VDA3-ZONE_SET-001")
        );
    }
}
