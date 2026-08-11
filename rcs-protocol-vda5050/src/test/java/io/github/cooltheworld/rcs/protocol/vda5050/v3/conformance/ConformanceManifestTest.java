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
        "SCHEMA_INCORRECT",
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
        RequirementRow validationBoundaryRule = rowsById.get("VDA3-SHARED-008");
        RequirementRow topicMetadataRule = rowsById.get("VDA3-SHARED-011");
        RequirementRow extensionAdmissionRule = rowsById.get(
            "VDA3-SHARED-012"
        );
        RequirementRow connectionRule = rowsById.get("VDA3-CONNECTION-001");
        RequirementRow factsheetRule = rowsById.get("VDA3-FACTSHEET-001");
        RequirementRow factsheetSpeedUnitRule = rowsById.get(
            "VDA3-FACTSHEET-002"
        );
        RequirementRow factsheetLoadSemanticsRule = rowsById.get(
            "VDA3-FACTSHEET-003"
        );
        RequirementRow factsheetChargingRule = rowsById.get(
            "VDA3-FACTSHEET-004"
        );
        RequirementRow factsheetNetworkRule = rowsById.get(
            "VDA3-FACTSHEET-005"
        );
        RequirementRow factsheetTypeRule = rowsById.get(
            "VDA3-FACTSHEET-006"
        );
        RequirementRow factsheetPhysicalRule = rowsById.get(
            "VDA3-FACTSHEET-007"
        );
        RequirementRow factsheetPolygonRule = rowsById.get(
            "VDA3-FACTSHEET-008"
        );
        assertNotNull(timestampRule, "Missing strict timestamp rule");
        assertNotNull(unsigned32Rule, "Missing uint32 range rule");
        assertNotNull(versionProfileRule, "Missing explicit version profile rule");
        assertNotNull(robotIdentityRule, "Missing Robot Identity rule");
        assertNotNull(headerCounterRule, "Missing headerId counter rule");
        assertNotNull(protocolHeaderRule, "Missing common protocol header rule");
        assertNotNull(extensionFieldsRule, "Missing extension fields rule");
        assertNotNull(validationBoundaryRule, "Missing validation boundary rule");
        assertNotNull(topicMetadataRule, "Missing Topic metadata and layout rule");
        assertNotNull(
            extensionAdmissionRule,
            "Missing typed extension admission rule"
        );
        assertNotNull(connectionRule, "Missing Connection rule");
        assertNotNull(factsheetRule, "Missing Factsheet rule");
        assertNotNull(
            factsheetSpeedUnitRule,
            "Missing Factsheet load-set speed unit rule"
        );
        assertNotNull(
            factsheetLoadSemanticsRule,
            "Missing Factsheet load semantics rule"
        );
        assertNotNull(
            factsheetChargingRule,
            "Missing Factsheet charging semantics rule"
        );
        assertNotNull(
            factsheetNetworkRule,
            "Missing Factsheet runtime network rule"
        );
        assertNotNull(
            factsheetTypeRule,
            "Missing Factsheet type specification rule"
        );
        assertNotNull(
            factsheetPhysicalRule,
            "Missing Factsheet physical parameters rule"
        );
        assertNotNull(
            factsheetPolygonRule,
            "Missing Factsheet simple polygon rule"
        );
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
        assertEquals("NONE", validationBoundaryRule.schemaGap());
        assertEquals("NONE", topicMetadataRule.schemaGap());
        assertEquals("VERIFIED", topicMetadataRule.status());
        assertEquals("NONE", extensionAdmissionRule.schemaGap());
        assertEquals("PARTIAL", extensionAdmissionRule.status());
        assertTrue(
            extensionAdmissionRule.test().contains("ActionParameterTest"),
            "FS08a must project its typed parameter evidence"
        );
        assertEquals("VERIFIED", connectionRule.status());
        assertEquals("VERIFIED", factsheetRule.status());
        assertTrue(
            factsheetRule.fixture().contains(
                "fixtures/factsheet/dialogue/capability.json"
            ),
            "Factsheet rule must retain FS07l cross-role Fixture evidence"
        );
        assertTrue(
            factsheetRule.test().contains("FactsheetDialogueTest"),
            "Factsheet rule must retain FS07l cross-role dialogue evidence"
        );
        assertTrue(
            factsheetRule.validator().contains(
                "MobileRobotConfigurationJacksonSupport"
            ),
            "Factsheet rule must retain FS07b configuration Codec evidence"
        );
        assertTrue(
            factsheetRule.validator().contains("FactsheetJacksonSupport"),
            "Factsheet rule must retain FS07c root Codec evidence"
        );
        assertTrue(
            factsheetRule.validator().contains("FactsheetValidator"),
            "Factsheet rule must retain FS07g root Validator evidence"
        );
        assertTrue(
            factsheetRule.fixture().contains(
                "mobile-robot-configuration-cases.json"
            ),
            "Factsheet rule must retain FS07b configuration Fixture evidence"
        );
        assertTrue(
            factsheetRule.fixture().contains("factsheet-cases.json"),
            "Factsheet rule must retain FS07c root Fixture evidence"
        );
        assertTrue(
            factsheetRule.test().contains(
                "MobileRobotConfigurationCodecTest"
            ),
            "Factsheet rule must retain FS07b configuration test evidence"
        );
        assertTrue(
            factsheetRule.test().contains("FactsheetCodecTest"),
            "Factsheet rule must retain FS07c root test evidence"
        );
        assertTrue(
            factsheetRule.test().contains("FactsheetValidatorTest"),
            "Factsheet rule must retain FS07g root Validator test evidence"
        );
        assertTrue(
            factsheetRule.transition().contains("MobileRobotState"),
            "Factsheet rule must retain FS07h Mobile Robot contract evidence"
        );
        assertTrue(
            factsheetRule.transition().contains(
                "DefaultMobileRobotStateMachine#transition"
            ),
            "Factsheet rule must retain FS07i Mobile Robot transition evidence"
        );
        assertTrue(
            factsheetRule.test().contains("MobileRobotStateTest"),
            "Factsheet rule must retain FS07h Mobile Robot contract tests"
        );
        assertTrue(
            factsheetRule.transition().contains("FleetControlState"),
            "Factsheet rule must retain FS07j Fleet Control contract evidence"
        );
        assertTrue(
            factsheetRule.test().contains("FleetControlStateTest"),
            "Factsheet rule must retain FS07j Fleet Control contract tests"
        );
        assertTrue(
            factsheetRule.transition().contains(
                "DefaultFleetControlStateMachine#transition"
            ),
            "Factsheet rule must retain FS07k Fleet Control transition evidence"
        );
        assertTrue(
            factsheetRule.test().contains(
                "FleetControlFactsheetStateMachineTest"
            ),
            "Factsheet rule must retain FS07k Fleet Control transition tests"
        );
        assertEquals("SCHEMA_INCORRECT", factsheetSpeedUnitRule.schemaGap());
        assertEquals("VERIFIED", factsheetSpeedUnitRule.status());
        assertEquals("SCHEMA_MISSING", factsheetLoadSemanticsRule.schemaGap());
        assertEquals("VERIFIED", factsheetLoadSemanticsRule.status());
        assertTrue(
            factsheetLoadSemanticsRule.validator().contains(
                "LoadSpecificationValidator"
            ),
            "Load semantics rule must retain FS05d Validator evidence"
        );
        assertEquals("SCHEMA_WEAKER", factsheetChargingRule.schemaGap());
        assertEquals("VERIFIED", factsheetChargingRule.status());
        assertTrue(
            factsheetChargingRule.validator().contains(
                "MobileRobotConfigurationValidator"
            ),
            "Charging rule must retain FS06c Validator evidence"
        );
        assertEquals("NONE", factsheetNetworkRule.schemaGap());
        assertEquals("VERIFIED", factsheetNetworkRule.status());
        assertTrue(
            factsheetNetworkRule.fixture().contains(
                "fixtures/factsheet/dialogue/capability.json"
            ),
            "Network rule must retain FS07l cross-role Fixture evidence"
        );
        assertTrue(
            factsheetNetworkRule.test().contains("FactsheetDialogueTest"),
            "Network rule must retain FS07l cross-role dialogue evidence"
        );
        assertTrue(
            factsheetNetworkRule.test().contains(
                "MobileRobotConfigurationTest"
            ),
            "Network rule must retain FS06 immutable model evidence"
        );
        assertTrue(
            factsheetNetworkRule.transition().contains(
                "DefaultFleetControlStateMachine#transition"
            ),
            "Network rule must retain FS07k transition evidence"
        );
        assertTrue(
            factsheetNetworkRule.test().contains(
                "FleetControlFactsheetStateMachineTest"
            ),
            "Network rule must retain FS07k scenario evidence"
        );
        assertEquals("NONE", factsheetTypeRule.schemaGap());
        assertEquals("VERIFIED", factsheetTypeRule.status());
        assertTrue(
            factsheetTypeRule.validator().contains(
                "TypeSpecificationValidator"
            ),
            "Type rule must retain FS07d Validator evidence"
        );
        assertEquals("SCHEMA_MISSING", factsheetPhysicalRule.schemaGap());
        assertEquals("VERIFIED", factsheetPhysicalRule.status());
        assertTrue(
            factsheetPhysicalRule.validator().contains(
                "PhysicalParametersValidator"
            ),
            "Physical rule must retain FS07e Validator evidence"
        );
        assertEquals("SCHEMA_MISSING", factsheetPolygonRule.schemaGap());
        assertEquals("VERIFIED", factsheetPolygonRule.status());
        assertTrue(
            factsheetPolygonRule.validator().contains(
                "MobileRobotGeometryValidator"
            ),
            "Polygon rule must retain FS07f Validator evidence"
        );
        assertTrue(
            connectionRule.test().contains("ConnectionCodecTest"),
            "Connection rule must retain C08 Codec evidence"
        );
        assertAll(
            () -> assertTrue(
                timestampRule.transition().contains(
                    "DefaultMobileRobotStateMachine"
                ),
                "Timestamp rule must include C11 publication evidence"
            ),
            () -> assertTrue(
                versionProfileRule.transition().contains(
                    "DefaultMobileRobotStateMachine"
                ),
                "Version profile rule must include C11 publication evidence"
            ),
            () -> assertTrue(
                robotIdentityRule.transition().contains(
                    "DefaultFleetControlStateMachine"
                ),
                "Robot identity rule must include C10 session evidence"
            ),
            () -> assertTrue(
                robotIdentityRule.transition().contains(
                    "DefaultMobileRobotStateMachine"
                ),
                "Robot identity rule must include C11 publication evidence"
            ),
            () -> assertTrue(
                extensionFieldsRule.transition().contains(
                    "DefaultFleetControlStateMachine"
                ),
                "Extension rule must include C10 diagnostic evidence"
            ),
            () -> assertTrue(
                validationBoundaryRule.transition().contains(
                    "DefaultFleetControlStateMachine"
                ),
                "Validation boundary must include C10 rejection evidence"
            ),
            () -> assertTrue(
                headerCounterRule.transition().contains(
                    "DefaultMobileRobotStateMachine"
                ),
                "Header counter rule must include C11 publication evidence"
            ),
            () -> assertTrue(
                headerCounterRule.test().contains(
                    "wrapsTheIndependentFactsheetHeaderId"
                ),
                "Header counter rule must include FS07i Factsheet evidence"
            ),
            () -> assertTrue(
                protocolHeaderRule.transition().contains(
                    "DefaultMobileRobotStateMachine"
                ),
                "Protocol header rule must include C11 publication evidence"
            ),
            () -> assertTrue(
                topicMetadataRule.transition().contains(
                    "DefaultMobileRobotStateMachine"
                ),
                "Topic metadata rule must include C11 Last Will evidence"
            ),
            () -> assertTrue(
                connectionRule.transition().contains(
                    "DefaultFleetControlStateMachine"
                ),
                "Connection rule must include C10 transition evidence"
            ),
            () -> assertTrue(
                connectionRule.transition().contains(
                    "DefaultMobileRobotStateMachine"
                ),
                "Connection rule must include C11 transition evidence"
            ),
            () -> assertTrue(
                connectionRule.fixture().contains(
                    "fixtures/connection/dialogue/"
                ),
                "Connection rule must include C12 dialogue fixtures"
            ),
            () -> assertTrue(
                connectionRule.test().contains("ConnectionDialogueTest"),
                "Connection rule must include C12 cross-role evidence"
            ),
            () -> assertTrue(
                factsheetRule.validator().contains(
                    "FactsheetFragmentJacksonSupport"
                ),
                "Factsheet rule must include FS01 fragment Codec evidence"
            ),
            () -> assertTrue(
                factsheetRule.validator().contains(
                    "ProtocolFeaturesJacksonSupport"
                ) && factsheetRule.validator().contains(
                    "ProtocolFeaturesValidator"
                ),
                "Factsheet rule must include FS03 Codec and semantic evidence"
            ),
            () -> assertTrue(
                factsheetRule.test().contains("TypeSpecificationTest")
                    && factsheetRule.test().contains("PhysicalParametersTest")
                    && factsheetRule.test().contains(
                        "FactsheetFragmentCodecTest"
                ),
                "Factsheet rule must include both FS01 models and Codec tests"
            ),
            () -> assertTrue(
                factsheetRule.test().contains("ProtocolFeaturesTest")
                    && factsheetRule.test().contains(
                        "ProtocolFeaturesCodecTest"
                    )
                    && factsheetRule.test().contains(
                        "ProtocolFeaturesValidatorTest"
                    ),
                "Factsheet rule must include FS03 model, Codec and semantic tests"
            ),
            () -> assertTrue(
                factsheetRule.test().contains("LoadSetTest"),
                "Factsheet rule must include FS05b load-set model evidence"
            ),
            () -> assertTrue(
                factsheetSpeedUnitRule.test().contains("LoadSetTest"),
                "Load-set speed unit rule must include FS05b model evidence"
            )
        );
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
