package io.github.cooltheworld.rcs.protocol.vda5050.v3.validation;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.BoundingBoxReference;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.LoadDimensions;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.LoadSet;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.LoadSpecification;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** 对 Factsheet {@code loadSpecification} 执行上下文无关语义校验。 */
public final class LoadSpecificationValidator {
    private static final String REQUIREMENT_ID = "VDA3-FACTSHEET-003";

    private LoadSpecificationValidator() {}

    /** @return 可缓存复用且线程安全的载荷说明 Validator */
    public static LoadSpecificationValidator create() {
        return new LoadSpecificationValidator();
    }

    /**
     * 校验有限数、范围、载荷位置引用与载荷集合名称唯一性。
     *
     * @param specification 已完成强类型绑定的载荷说明片段
     * @return 按线路遍历顺序排列的不可变问题列表
     */
    public List<ValidationIssue> validate(LoadSpecification specification) {
        Objects.requireNonNull(specification, "specification");
        List<ValidationIssue> issues = new ArrayList<>();
        Set<String> knownPositions = new HashSet<>();
        if (specification.loadPositions() != null) {
            knownPositions.addAll(specification.loadPositions());
        }
        validateLoadSets(specification.loadSets(), knownPositions, issues);
        return List.copyOf(issues);
    }

    private static void validateLoadSets(
        List<LoadSet> loadSets,
        Set<String> knownPositions,
        List<ValidationIssue> issues
    ) {
        if (loadSets == null) {
            return;
        }
        Set<String> setNames = new HashSet<>();
        for (int index = 0; index < loadSets.size(); index++) {
            LoadSet loadSet = loadSets.get(index);
            String prefix = "/loadSets/" + index;
            if (!setNames.add(loadSet.setName())) {
                issues.add(issue(
                    "DUPLICATE_LOAD_SET_NAME",
                    prefix + "/setName",
                    "Load set name must be unique"
                ));
            }
            validatePositions(
                loadSet.loadPositions(),
                knownPositions,
                prefix,
                issues
            );
            validateBoundingBox(loadSet.boundingBoxReference(), prefix, issues);
            validateDimensions(loadSet.loadDimensions(), prefix, issues);
            validateCapabilities(loadSet, prefix, issues);
            validateRanges(loadSet, prefix, issues);
        }
    }

    private static void validatePositions(
        List<String> positions,
        Set<String> knownPositions,
        String prefix,
        List<ValidationIssue> issues
    ) {
        if (positions == null) {
            return;
        }
        for (int index = 0; index < positions.size(); index++) {
            if (!knownPositions.contains(positions.get(index))) {
                issues.add(issue(
                    "UNKNOWN_LOAD_POSITION",
                    prefix + "/loadPositions/" + index,
                    "Load set position must reference a declared load position"
                ));
            }
        }
    }

    private static void validateBoundingBox(
        BoundingBoxReference reference,
        String prefix,
        List<ValidationIssue> issues
    ) {
        if (reference == null) {
            return;
        }
        String path = prefix + "/boundingBoxReference";
        number(reference.x(), path + "/x", false, issues);
        number(reference.y(), path + "/y", false, issues);
        number(reference.z(), path + "/z", false, issues);
        number(reference.theta(), path + "/theta", false, issues);
    }

    private static void validateDimensions(
        LoadDimensions dimensions,
        String prefix,
        List<ValidationIssue> issues
    ) {
        if (dimensions == null) {
            return;
        }
        String path = prefix + "/loadDimensions";
        number(dimensions.length(), path + "/length", true, issues);
        number(dimensions.width(), path + "/width", true, issues);
        number(dimensions.height(), path + "/height", true, issues);
    }

    private static void validateCapabilities(
        LoadSet loadSet,
        String prefix,
        List<ValidationIssue> issues
    ) {
        number(loadSet.maximumWeight(), prefix + "/maximumWeight", true, issues);
        number(
            loadSet.minimumLoadhandlingHeight(),
            prefix + "/minimumLoadhandlingHeight",
            true,
            issues
        );
        number(
            loadSet.maximumLoadhandlingHeight(),
            prefix + "/maximumLoadhandlingHeight",
            true,
            issues
        );
        number(
            loadSet.minimumLoadhandlingDepth(),
            prefix + "/minimumLoadhandlingDepth",
            false,
            issues
        );
        number(
            loadSet.maximumLoadhandlingDepth(),
            prefix + "/maximumLoadhandlingDepth",
            false,
            issues
        );
        number(
            loadSet.minimumLoadhandlingTilt(),
            prefix + "/minimumLoadhandlingTilt",
            false,
            issues
        );
        number(
            loadSet.maximumLoadhandlingTilt(),
            prefix + "/maximumLoadhandlingTilt",
            false,
            issues
        );
        number(loadSet.maximumSpeed(), prefix + "/maximumSpeed", true, issues);
        number(
            loadSet.maximumAcceleration(),
            prefix + "/maximumAcceleration",
            true,
            issues
        );
        number(
            loadSet.maximumDeceleration(),
            prefix + "/maximumDeceleration",
            false,
            issues
        );
        number(loadSet.pickTime(), prefix + "/pickTime", true, issues);
        number(loadSet.dropTime(), prefix + "/dropTime", true, issues);
    }

    private static void validateRanges(
        LoadSet loadSet,
        String prefix,
        List<ValidationIssue> issues
    ) {
        range(
            loadSet.minimumLoadhandlingHeight(),
            loadSet.maximumLoadhandlingHeight(),
            prefix + "/maximumLoadhandlingHeight",
            issues
        );
        range(
            loadSet.minimumLoadhandlingDepth(),
            loadSet.maximumLoadhandlingDepth(),
            prefix + "/maximumLoadhandlingDepth",
            issues
        );
        range(
            loadSet.minimumLoadhandlingTilt(),
            loadSet.maximumLoadhandlingTilt(),
            prefix + "/maximumLoadhandlingTilt",
            issues
        );
    }

    private static void number(
        Double value,
        String path,
        boolean nonNegative,
        List<ValidationIssue> issues
    ) {
        if (value == null) {
            return;
        }
        if (!Double.isFinite(value)) {
            issues.add(issue(
                "NON_FINITE_LOAD_NUMBER",
                path,
                "Load number must be finite"
            ));
            return;
        }
        if (nonNegative && value < 0.0D) {
            issues.add(issue(
                "NEGATIVE_LOAD_NUMBER",
                path,
                "Absolute load value must not be negative"
            ));
        }
    }

    private static void range(
        Double minimum,
        Double maximum,
        String maximumPath,
        List<ValidationIssue> issues
    ) {
        if (minimum != null
            && maximum != null
            && Double.isFinite(minimum)
            && Double.isFinite(maximum)
            && minimum > maximum) {
            issues.add(issue(
                "INVALID_LOAD_RANGE",
                maximumPath,
                "Load range minimum must not exceed maximum"
            ));
        }
    }

    private static ValidationIssue issue(
        String code,
        String path,
        String description
    ) {
        return new ValidationIssue(
            code,
            ValidationSeverity.ERROR,
            path,
            description,
            REQUIREMENT_ID
        );
    }
}
