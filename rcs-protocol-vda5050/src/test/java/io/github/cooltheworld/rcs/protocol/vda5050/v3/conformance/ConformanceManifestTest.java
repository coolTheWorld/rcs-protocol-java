package io.github.cooltheworld.rcs.protocol.vda5050.v3.conformance;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

final class ConformanceManifestTest {
    private static final String MANIFEST_RESOURCE =
        "/vda5050/v3.0.0/conformance/requirements.tsv";
    private static final String EXPECTED_HEADER =
        "id\ttopic\tsource\trequirement\tschemaGap\tvalidator\ttransition\tfixture\ttest\tstatus";
    private static final Set<String> EXPECTED_TOPICS = Set.of(
        "CONNECTION",
        "FACTSHEET",
        "INSTANT_ACTIONS",
        "ORDER",
        "RESPONSES",
        "STATE",
        "VISUALIZATION",
        "ZONE_SET"
    );
    private static final Set<String> ALLOWED_SCHEMA_GAPS = Set.of(
        "NONE",
        "SCHEMA_MISSING",
        "SCHEMA_WEAKER"
    );
    private static final Set<String> ALLOWED_STATUSES = Set.of(
        "PLANNED",
        "PARTIAL",
        "VERIFIED"
    );
    private static final Pattern REQUIREMENT_ID = Pattern.compile(
        "VDA3-(SHARED|CONNECTION|FACTSHEET|INSTANT_ACTIONS|ORDER|RESPONSES|STATE|VISUALIZATION|ZONE_SET)-\\d{3}"
    );

    @Test
    void definesStableTraceableRequirementsForEveryTopic() throws IOException {
        List<RequirementRow> rows = readRows();
        assertFalse(rows.isEmpty(), "Conformance manifest must contain requirements");

        Set<String> ids = new HashSet<>();
        assertAll(rows.stream().map(row -> () -> assertValid(row, ids)));

        Set<String> coveredTopics = new HashSet<>();
        rows.stream()
            .map(RequirementRow::topic)
            .filter(topic -> !"SHARED".equals(topic))
            .forEach(coveredTopics::add);
        assertEquals(EXPECTED_TOPICS, coveredTopics, "Every VDA 5050 topic needs a seed row");
        Map<String, RequirementRow> rowsById = rows.stream().collect(Collectors.toMap(
            RequirementRow::id,
            Function.identity()
        ));
        RequirementRow timestampRule = rowsById.get("VDA3-SHARED-001");
        RequirementRow unsigned32Rule = rowsById.get("VDA3-SHARED-002");
        RequirementRow versionProfileRule = rowsById.get("VDA3-SHARED-003");
        RequirementRow robotIdentityRule = rowsById.get("VDA3-SHARED-004");
        RequirementRow headerCounterRule = rowsById.get("VDA3-SHARED-005");
        RequirementRow protocolHeaderRule = rowsById.get("VDA3-SHARED-006");
        RequirementRow extensionFieldsRule = rowsById.get("VDA3-SHARED-007");
        assertNotNull(timestampRule, "Missing strict timestamp rule");
        assertNotNull(unsigned32Rule, "Missing uint32 range rule");
        assertNotNull(versionProfileRule, "Missing explicit version profile rule");
        assertNotNull(robotIdentityRule, "Missing Robot Identity rule");
        assertNotNull(headerCounterRule, "Missing headerId counter rule");
        assertNotNull(protocolHeaderRule, "Missing common protocol header rule");
        assertNotNull(extensionFieldsRule, "Missing extension fields rule");
        assertEquals(
            "SCHEMA_WEAKER",
            timestampRule.schemaGap(),
            "Strict timestamp gap must remain explicit"
        );
        assertEquals(
            "SCHEMA_MISSING",
            unsigned32Rule.schemaGap(),
            "uint32 range gap must remain explicit"
        );
        assertEquals(
            "NONE",
            versionProfileRule.schemaGap(),
            "Version profile support is not a Schema gap"
        );
        assertEquals("SCHEMA_MISSING", robotIdentityRule.schemaGap());
        assertEquals("SCHEMA_MISSING", headerCounterRule.schemaGap());
        assertEquals("NONE", protocolHeaderRule.schemaGap());
        assertEquals("NONE", extensionFieldsRule.schemaGap());
    }

    private static List<RequirementRow> readRows() throws IOException {
        try (InputStream resource = ConformanceManifestTest.class.getResourceAsStream(
            MANIFEST_RESOURCE
        )) {
            assertNotNull(resource, "Missing conformance manifest: " + MANIFEST_RESOURCE);
            try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource, StandardCharsets.UTF_8)
            )) {
                List<String> lines = reader.lines().toList();
                assertFalse(lines.isEmpty(), "Conformance manifest must not be empty");
                assertEquals(EXPECTED_HEADER, lines.getFirst(), "Unexpected manifest columns");
                return lines.stream().skip(1).map(ConformanceManifestTest::parseRow).toList();
            }
        }
    }

    private static RequirementRow parseRow(String line) {
        assertFalse(line.isBlank(), "Manifest must not contain blank rows");
        String[] columns = line.split("\\t", -1);
        assertEquals(10, columns.length, () -> "Expected 10 columns: " + line);
        return new RequirementRow(
            columns[0],
            columns[1],
            columns[2],
            columns[3],
            columns[4],
            columns[5],
            columns[6],
            columns[7],
            columns[8],
            columns[9]
        );
    }

    private static void assertValid(RequirementRow row, Set<String> ids) {
        assertTrue(REQUIREMENT_ID.matcher(row.id()).matches(), () -> "Invalid ID: " + row.id());
        assertTrue(ids.add(row.id()), () -> "Duplicate ID: " + row.id());
        assertEquals(topicFrom(row.id()), row.topic(), () -> "ID/topic mismatch: " + row.id());
        assertFalse(row.source().isBlank(), () -> "Missing source: " + row.id());
        assertFalse(row.requirement().isBlank(), () -> "Missing requirement: " + row.id());
        assertTrue(
            ALLOWED_SCHEMA_GAPS.contains(row.schemaGap()),
            () -> "Invalid Schema gap: " + row.id()
        );
        assertTrue(ALLOWED_STATUSES.contains(row.status()), () -> "Invalid status: " + row.id());

        List<String> references = List.of(
            row.validator(),
            row.transition(),
            row.fixture(),
            row.test()
        );
        references.stream()
            .filter(reference -> !"-".equals(reference))
            .forEach(reference -> assertTrue(
                reference.contains(row.id()) || reference.startsWith("N/A:"),
                () -> "Artifact reference must contain ID or an N/A reason: " + row.id()
            ));
        if ("PLANNED".equals(row.status())) {
            assertTrue(
                references.stream().allMatch("-"::equals),
                () -> "PLANNED rows cannot claim artifacts: " + row.id()
            );
        } else {
            assertTrue(
                references.stream().anyMatch(reference -> !"-".equals(reference)),
                () -> "Implemented rows need at least one artifact: " + row.id()
            );
        }
        if ("VERIFIED".equals(row.status())) {
            assertTrue(
                references.stream().noneMatch("-"::equals),
                () -> "VERIFIED rows must resolve every artifact column: " + row.id()
            );
        }
    }

    private static String topicFrom(String requirementId) {
        return requirementId.substring("VDA3-".length(), requirementId.lastIndexOf('-'));
    }

    private record RequirementRow(
        String id,
        String topic,
        String source,
        String requirement,
        String schemaGap,
        String validator,
        String transition,
        String fixture,
        String test,
        String status
    ) {}
}
