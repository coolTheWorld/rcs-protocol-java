package io.github.cooltheworld.rcs.protocol.vda5050.v3.validation;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.action.ActionParameterDefinition;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.action.ActionScope;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.action.BlockingType;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.action.MobileRobotAction;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.OptionalParameter;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.ProtocolFeatures;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** 对 Factsheet {@code protocolFeatures} 执行上下文无关语义校验。 */
public final class ProtocolFeaturesValidator {
    private static final String REQUIREMENT_ID = "VDA3-FACTSHEET-001";

    private ProtocolFeaturesValidator() {}

    /** @return 可缓存复用且线程安全的协议能力 Validator */
    public static ProtocolFeaturesValidator create() {
        return new ProtocolFeaturesValidator();
    }

    /**
     * 报告能力声明中的重复与冲突，不修改或去重输入。
     *
     * @param features 已完成强类型绑定的协议能力片段
     * @return 按输入遍历顺序排列的不可变问题列表
     */
    public List<ValidationIssue> validate(ProtocolFeatures features) {
        Objects.requireNonNull(features, "features");
        List<ValidationIssue> issues = new ArrayList<>();
        validateOptionalParameters(features.optionalParameters(), issues);
        validateActions(features.mobileRobotActions(), issues);
        return List.copyOf(issues);
    }

    private static void validateOptionalParameters(
        List<OptionalParameter> parameters,
        List<ValidationIssue> issues
    ) {
        Map<String, OptionalParameter> declarations = new HashMap<>();
        for (int index = 0; index < parameters.size(); index++) {
            OptionalParameter current = parameters.get(index);
            OptionalParameter previous = declarations.putIfAbsent(
                current.parameter(),
                current
            );
            if (previous != null) {
                boolean duplicate = previous.equals(current);
                issues.add(issue(
                    duplicate
                        ? "DUPLICATE_OPTIONAL_PARAMETER"
                        : "CONFLICTING_OPTIONAL_PARAMETER",
                    "/optionalParameters/" + index + "/parameter",
                    duplicate
                        ? "Optional parameter is declared more than once"
                        : "Optional parameter has conflicting declarations"
                ));
            }
        }
    }

    private static void validateActions(
        List<MobileRobotAction> actions,
        List<ValidationIssue> issues
    ) {
        Map<String, MobileRobotAction> declarations = new HashMap<>();
        for (int index = 0; index < actions.size(); index++) {
            MobileRobotAction current = actions.get(index);
            MobileRobotAction previous = declarations.putIfAbsent(
                current.actionType(),
                current
            );
            if (previous != null) {
                boolean duplicate = previous.equals(current);
                issues.add(issue(
                    duplicate ? "DUPLICATE_ACTION" : "CONFLICTING_ACTION",
                    "/mobileRobotActions/" + index + "/actionType",
                    duplicate
                        ? "Action is declared more than once"
                        : "Action has conflicting declarations"
                ));
            }
            validateAction(current, index, issues);
        }
    }

    private static void validateAction(
        MobileRobotAction action,
        int actionIndex,
        List<ValidationIssue> issues
    ) {
        String prefix = "/mobileRobotActions/" + actionIndex;
        validateScopes(action.actionScopes(), prefix, issues);
        if (action.actionParameters() != null) {
            validateActionParameters(action.actionParameters(), prefix, issues);
        }
        if (action.blockingTypes() != null) {
            validateBlockingTypes(action.blockingTypes(), prefix, issues);
        }
    }

    private static void validateScopes(
        List<ActionScope> scopes,
        String prefix,
        List<ValidationIssue> issues
    ) {
        EnumSet<ActionScope> seen = EnumSet.noneOf(ActionScope.class);
        for (int index = 0; index < scopes.size(); index++) {
            if (!seen.add(scopes.get(index))) {
                issues.add(issue(
                    "DUPLICATE_ACTION_SCOPE",
                    prefix + "/actionScopes/" + index,
                    "Action scope is declared more than once"
                ));
            }
        }
    }

    private static void validateActionParameters(
        List<ActionParameterDefinition> parameters,
        String prefix,
        List<ValidationIssue> issues
    ) {
        Map<String, ActionParameterDefinition> declarations = new HashMap<>();
        for (int index = 0; index < parameters.size(); index++) {
            ActionParameterDefinition current = parameters.get(index);
            ActionParameterDefinition previous = declarations.putIfAbsent(
                current.key(),
                current
            );
            if (previous != null) {
                boolean duplicate = previous.equals(current);
                issues.add(issue(
                    duplicate
                        ? "DUPLICATE_ACTION_PARAMETER"
                        : "CONFLICTING_ACTION_PARAMETER",
                    prefix + "/actionParameters/" + index + "/key",
                    duplicate
                        ? "Action parameter is declared more than once"
                        : "Action parameter has conflicting declarations"
                ));
            }
        }
    }

    private static void validateBlockingTypes(
        List<BlockingType> blockingTypes,
        String prefix,
        List<ValidationIssue> issues
    ) {
        EnumSet<BlockingType> seen = EnumSet.noneOf(BlockingType.class);
        for (int index = 0; index < blockingTypes.size(); index++) {
            if (!seen.add(blockingTypes.get(index))) {
                issues.add(issue(
                    "DUPLICATE_BLOCKING_TYPE",
                    prefix + "/blockingTypes/" + index,
                    "Blocking type is declared more than once"
                ));
            }
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
