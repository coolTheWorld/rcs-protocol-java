package io.github.cooltheworld.rcs.protocol.vda5050.v3.validation;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.BoundingBoxReference;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.LoadDimensions;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.LoadSet;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.LoadSpecification;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

final class LoadSpecificationValidatorTest {
    private static final LoadSpecificationValidator VALIDATOR =
        LoadSpecificationValidator.create();

    @Test
    @DisplayName("[VDA3-FACTSHEET-003] 合法范围和允许有符号的载荷数值通过")
    void acceptsValidLoadRangesAndPreservesInput() {
        LoadSet loadSet = loadSet("DEFAULT")
            .loadPositions(List.of("front"))
            .boundingBoxReference(BoundingBoxReference.builder()
                .x(-1.0D)
                .y(2.0D)
                .z(-3.0D)
                .theta(-0.5D)
                .build())
            .loadDimensions(LoadDimensions.builder()
                .length(0.0D)
                .width(1.0D)
                .height(2.0D)
                .build())
            .maximumWeight(0.0D)
            .minimumLoadhandlingHeight(0.0D)
            .maximumLoadhandlingHeight(2.0D)
            .minimumLoadhandlingDepth(-1.0D)
            .maximumLoadhandlingDepth(1.0D)
            .minimumLoadhandlingTilt(-0.5D)
            .maximumLoadhandlingTilt(0.5D)
            .maximumSpeed(0.0D)
            .maximumAcceleration(0.0D)
            .maximumDeceleration(-1.0D)
            .pickTime(0.0D)
            .dropTime(0.0D)
            .build();
        LoadSpecification specification = LoadSpecification.builder()
            .loadPositions(List.of("front"))
            .loadSets(List.of(loadSet))
            .build();
        LoadSpecification snapshot = LoadSpecification.builder()
            .loadPositions(specification.loadPositions())
            .loadSets(specification.loadSets())
            .extensionFields(specification.extensionFields())
            .build();

        List<ValidationIssue> issues = VALIDATOR.validate(specification);

        assertAll(
            () -> assertEquals(List.of(), issues),
            () -> assertEquals(snapshot, specification),
            () -> assertEquals(issues, VALIDATOR.validate(specification))
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-003] 缺失可选集合不产生问题且拒绝 null 根")
    void acceptsMissingOptionalCollectionsAndRejectsNullRoot() {
        LoadSpecification missing = LoadSpecification.builder().build();
        LoadSpecification allPositions = LoadSpecification.builder()
            .loadSets(List.of(loadSet("ALL")
                .loadPositions(List.of())
                .minimumLoadhandlingDepth(0.0D)
                .build()))
            .build();

        assertAll(
            () -> assertEquals(List.of(), VALIDATOR.validate(missing)),
            () -> assertEquals(List.of(), VALIDATOR.validate(allPositions)),
            () -> assertThrows(
                NullPointerException.class,
                () -> VALIDATOR.validate(null)
            )
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-003] 报告载荷对象图中的全部非有限数")
    void reportsEveryNonFiniteLoadNumber() {
        LoadSet loadSet = loadSet("NON_FINITE")
            .boundingBoxReference(BoundingBoxReference.builder()
                .x(Double.NaN)
                .y(Double.POSITIVE_INFINITY)
                .z(Double.NEGATIVE_INFINITY)
                .theta(Double.NaN)
                .build())
            .loadDimensions(LoadDimensions.builder()
                .length(Double.NaN)
                .width(Double.POSITIVE_INFINITY)
                .height(Double.NEGATIVE_INFINITY)
                .build())
            .maximumWeight(Double.NaN)
            .minimumLoadhandlingHeight(Double.POSITIVE_INFINITY)
            .maximumLoadhandlingHeight(Double.NEGATIVE_INFINITY)
            .minimumLoadhandlingDepth(Double.NaN)
            .maximumLoadhandlingDepth(Double.POSITIVE_INFINITY)
            .minimumLoadhandlingTilt(Double.NEGATIVE_INFINITY)
            .maximumLoadhandlingTilt(Double.NaN)
            .maximumSpeed(Double.POSITIVE_INFINITY)
            .maximumAcceleration(Double.NEGATIVE_INFINITY)
            .maximumDeceleration(Double.NaN)
            .pickTime(Double.POSITIVE_INFINITY)
            .dropTime(Double.NEGATIVE_INFINITY)
            .build();
        LoadSpecification specification = LoadSpecification.builder()
            .loadSets(List.of(
                loadSet,
                loadSet("NON_FINITE_MAXIMUM")
                    .minimumLoadhandlingDepth(0.0D)
                    .maximumLoadhandlingDepth(Double.NaN)
                    .build()
            ))
            .build();

        List<ValidationIssue> issues = VALIDATOR.validate(specification);

        assertAll(
            () -> assertEquals(
                List.of(
                    "/loadSets/0/boundingBoxReference/x",
                    "/loadSets/0/boundingBoxReference/y",
                    "/loadSets/0/boundingBoxReference/z",
                    "/loadSets/0/boundingBoxReference/theta",
                    "/loadSets/0/loadDimensions/length",
                    "/loadSets/0/loadDimensions/width",
                    "/loadSets/0/loadDimensions/height",
                    "/loadSets/0/maximumWeight",
                    "/loadSets/0/minimumLoadhandlingHeight",
                    "/loadSets/0/maximumLoadhandlingHeight",
                    "/loadSets/0/minimumLoadhandlingDepth",
                    "/loadSets/0/maximumLoadhandlingDepth",
                    "/loadSets/0/minimumLoadhandlingTilt",
                    "/loadSets/0/maximumLoadhandlingTilt",
                    "/loadSets/0/maximumSpeed",
                    "/loadSets/0/maximumAcceleration",
                    "/loadSets/0/maximumDeceleration",
                    "/loadSets/0/pickTime",
                    "/loadSets/0/dropTime",
                    "/loadSets/1/maximumLoadhandlingDepth"
                ),
                issues.stream().map(ValidationIssue::path).toList()
            ),
            () -> assertEquals(
                List.of("NON_FINITE_LOAD_NUMBER"),
                issues.stream().map(ValidationIssue::code).distinct().toList()
            ),
            () -> assertTrue(issues.stream().allMatch(issue ->
                issue.severity() == ValidationSeverity.ERROR
                    && "VDA3-FACTSHEET-003".equals(issue.requirementId())
            )),
            () -> assertThrows(UnsupportedOperationException.class, issues::clear)
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-002][VDA3-FACTSHEET-003] 报告具有非负下限字段中的全部负数")
    void reportsEveryNegativeSchemaBoundedValue() {
        LoadSet loadSet = loadSet("NEGATIVE")
            .loadDimensions(LoadDimensions.builder()
                .length(-1.0D)
                .width(-2.0D)
                .height(-3.0D)
                .build())
            .maximumWeight(-1.0D)
            .minimumLoadhandlingHeight(-2.0D)
            .maximumLoadhandlingHeight(-1.0D)
            .maximumSpeed(-1.0D)
            .maximumAcceleration(-1.0D)
            .pickTime(-1.0D)
            .dropTime(-1.0D)
            .build();
        LoadSpecification specification = LoadSpecification.builder()
            .loadSets(List.of(loadSet))
            .build();

        List<ValidationIssue> issues = VALIDATOR.validate(specification);

        assertAll(
            () -> assertEquals(10, issues.size()),
            () -> assertEquals(
                List.of("NEGATIVE_LOAD_NUMBER"),
                issues.stream().map(ValidationIssue::code).distinct().toList()
            ),
            () -> assertEquals(
                List.of(
                    "/loadSets/0/loadDimensions/length",
                    "/loadSets/0/loadDimensions/width",
                    "/loadSets/0/loadDimensions/height",
                    "/loadSets/0/maximumWeight",
                    "/loadSets/0/minimumLoadhandlingHeight",
                    "/loadSets/0/maximumLoadhandlingHeight",
                    "/loadSets/0/maximumSpeed",
                    "/loadSets/0/maximumAcceleration",
                    "/loadSets/0/pickTime",
                    "/loadSets/0/dropTime"
                ),
                issues.stream().map(ValidationIssue::path).toList()
            )
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-003] 报告高度、深度和倾角的倒置边界")
    void reportsEveryInvertedLoadRange() {
        LoadSet loadSet = loadSet("INVERTED")
            .minimumLoadhandlingHeight(2.0D)
            .maximumLoadhandlingHeight(1.0D)
            .minimumLoadhandlingDepth(2.0D)
            .maximumLoadhandlingDepth(1.0D)
            .minimumLoadhandlingTilt(0.5D)
            .maximumLoadhandlingTilt(-0.5D)
            .build();
        LoadSpecification specification = LoadSpecification.builder()
            .loadSets(List.of(loadSet))
            .build();

        List<ValidationIssue> issues = VALIDATOR.validate(specification);

        assertAll(
            () -> assertEquals(
                List.of(
                    "/loadSets/0/maximumLoadhandlingHeight",
                    "/loadSets/0/maximumLoadhandlingDepth",
                    "/loadSets/0/maximumLoadhandlingTilt"
                ),
                issues.stream().map(ValidationIssue::path).toList()
            ),
            () -> assertEquals(
                List.of("INVALID_LOAD_RANGE"),
                issues.stream().map(ValidationIssue::code).distinct().toList()
            )
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-003] 报告未知位置引用和重复载荷集合名称")
    void reportsUnknownPositionReferencesAndDuplicateSetNames() {
        LoadSpecification specification = LoadSpecification.builder()
            .loadPositions(List.of("front"))
            .loadSets(List.of(
                loadSet("DEFAULT")
                    .loadPositions(List.of("front", "rear"))
                    .build(),
                loadSet("DEFAULT").build()
            ))
            .build();
        LoadSpecification missingPositions = LoadSpecification.builder()
            .loadSets(List.of(loadSet("EXPLICIT")
                .loadPositions(List.of("front"))
                .build()))
            .build();

        List<ValidationIssue> issues = VALIDATOR.validate(specification);
        List<ValidationIssue> missingRootIssues = VALIDATOR.validate(
            missingPositions
        );

        assertAll(
            () -> assertEquals(
                List.of(
                    "UNKNOWN_LOAD_POSITION",
                    "DUPLICATE_LOAD_SET_NAME"
                ),
                issues.stream().map(ValidationIssue::code).toList()
            ),
            () -> assertEquals(
                List.of(
                    "/loadSets/0/loadPositions/1",
                    "/loadSets/1/setName"
                ),
                issues.stream().map(ValidationIssue::path).toList()
            ),
            () -> assertEquals(
                "/loadSets/0/loadPositions/0",
                missingRootIssues.getFirst().path()
            )
        );
    }

    private static LoadSet.Builder loadSet(String setName) {
        return LoadSet.builder().setName(setName).loadType("BOX");
    }
}
