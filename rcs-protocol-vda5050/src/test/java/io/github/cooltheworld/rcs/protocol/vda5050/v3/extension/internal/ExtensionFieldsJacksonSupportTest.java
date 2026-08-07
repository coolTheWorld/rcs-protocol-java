package io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.ExtensionFields;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

final class ExtensionFieldsJacksonSupportTest {
    private static final ObjectMapper MAPPER = JsonMapper.builder().build();
    private static final Set<String> STANDARD_FIELDS = Set.of("headerId");

    @Test
    @DisplayName("[VDA3-SHARED-007] 未知字段与显式 null 透明往返")
    void roundTripsNestedUnknownFieldsAndExplicitNull() throws Exception {
        ObjectNode input = objectNode("""
            {
              "headerId": 7,
              "vendorObject": {
                "enabled": true,
                "values": [1, null, {"label": "x"}]
              },
              "vendorNull": null,
              "vendorNumber": 1.25
            }
            """);

        ExtensionFields extensions = ExtensionFieldsJacksonSupport.capture(
            MAPPER,
            input,
            STANDARD_FIELDS
        );
        ObjectNode output = MAPPER.createObjectNode().put("headerId", 7L);
        ExtensionFieldsJacksonSupport.merge(
            MAPPER,
            output,
            extensions,
            STANDARD_FIELDS
        );

        assertFalse(extensions.isEmpty());
        assertEquals(
            MAPPER.writeValueAsString(input),
            MAPPER.writeValueAsString(output)
        );
        assertTrue(output.required("vendorNull").isNull());
    }

    @Test
    void defensivelyCopiesCapturedAndWrittenTrees() throws Exception {
        ObjectNode input = objectNode("""
            {"headerId": 7, "vendorObject": {"nested": [1, 2]}}
            """);
        ExtensionFields extensions = ExtensionFieldsJacksonSupport.capture(
            MAPPER,
            input,
            STANDARD_FIELDS
        );

        ((ObjectNode) input.required("vendorObject")).put("afterCapture", true);
        ObjectNode firstOutput = MAPPER.createObjectNode().put("headerId", 7L);
        ExtensionFieldsJacksonSupport.merge(
            MAPPER,
            firstOutput,
            extensions,
            STANDARD_FIELDS
        );
        ((ObjectNode) firstOutput.required("vendorObject")).put("afterWrite", true);
        ObjectNode secondOutput = MAPPER.createObjectNode().put("headerId", 7L);
        ExtensionFieldsJacksonSupport.merge(
            MAPPER,
            secondOutput,
            extensions,
            STANDARD_FIELDS
        );

        ObjectNode preserved = (ObjectNode) secondOutput.required("vendorObject");
        assertFalse(preserved.has("afterCapture"));
        assertFalse(preserved.has("afterWrite"));
    }

    @Test
    void keepsTargetUnchangedWhenAnyExtensionConflicts() throws Exception {
        ObjectNode rawExtensions = objectNode("""
            {"vendorField": "safe", "headerId": 999}
            """);
        ExtensionFields extensions = ExtensionFieldsJacksonSupport.capture(
            MAPPER,
            rawExtensions,
            Set.of()
        );
        ObjectNode target = MAPPER.createObjectNode().put("headerId", 7L);
        ObjectNode beforeMerge = target.deepCopy();

        assertThrows(
            IllegalArgumentException.class,
            () -> ExtensionFieldsJacksonSupport.merge(
                MAPPER,
                target,
                extensions,
                STANDARD_FIELDS
            )
        );
        assertEquals(beforeMerge, target);
    }

    @Test
    void rejectsCollisionWithExistingNonStandardTargetField() throws Exception {
        ObjectNode rawExtensions = objectNode("""
            {"vendorField": "extension"}
            """);
        ExtensionFields extensions = ExtensionFieldsJacksonSupport.capture(
            MAPPER,
            rawExtensions,
            Set.of()
        );
        ObjectNode target = MAPPER.createObjectNode().put("vendorField", "model");

        assertThrows(
            IllegalArgumentException.class,
            () -> ExtensionFieldsJacksonSupport.merge(
                MAPPER,
                target,
                extensions,
                Set.of()
            )
        );
        assertEquals("model", target.required("vendorField").textValue());
    }

    @Test
    void capturesEmptyFieldsAndRejectsNullArguments() throws Exception {
        ObjectNode standardOnly = MAPPER.createObjectNode().put("headerId", 7L);
        ExtensionFields empty = ExtensionFieldsJacksonSupport.capture(
            MAPPER,
            standardOnly,
            STANDARD_FIELDS
        );
        ObjectNode target = standardOnly.deepCopy();

        ExtensionFieldsJacksonSupport.merge(MAPPER, target, empty, STANDARD_FIELDS);

        assertTrue(empty.isEmpty());
        assertEquals(standardOnly, target);
        assertThrows(
            NullPointerException.class,
            () -> ExtensionFieldsJacksonSupport.capture(null, standardOnly, STANDARD_FIELDS)
        );
        assertThrows(
            NullPointerException.class,
            () -> ExtensionFieldsJacksonSupport.capture(MAPPER, null, STANDARD_FIELDS)
        );
        assertThrows(
            NullPointerException.class,
            () -> ExtensionFieldsJacksonSupport.capture(MAPPER, standardOnly, null)
        );
        assertThrows(
            NullPointerException.class,
            () -> ExtensionFieldsJacksonSupport.merge(null, target, empty, STANDARD_FIELDS)
        );
        assertThrows(
            NullPointerException.class,
            () -> ExtensionFieldsJacksonSupport.merge(MAPPER, null, empty, STANDARD_FIELDS)
        );
        assertThrows(
            NullPointerException.class,
            () -> ExtensionFieldsJacksonSupport.merge(MAPPER, target, null, STANDARD_FIELDS)
        );
        assertThrows(
            NullPointerException.class,
            () -> ExtensionFieldsJacksonSupport.merge(MAPPER, target, empty, null)
        );
    }

    private static ObjectNode objectNode(String json) throws Exception {
        return (ObjectNode) MAPPER.readTree(json);
    }
}
