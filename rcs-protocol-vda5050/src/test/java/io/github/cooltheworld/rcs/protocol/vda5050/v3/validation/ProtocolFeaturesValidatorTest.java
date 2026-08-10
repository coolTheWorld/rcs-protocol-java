package io.github.cooltheworld.rcs.protocol.vda5050.v3.validation;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.action.ActionParameterDefinition;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.action.ActionScope;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.action.ActionValueDataType;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.action.BlockingType;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.action.MobileRobotAction;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.OptionalParameter;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.OptionalParameterSupport;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.ProtocolFeatures;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

final class ProtocolFeaturesValidatorTest {
    private static final ProtocolFeaturesValidator VALIDATOR =
        ProtocolFeaturesValidator.create();

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] 不同且大小写敏感的能力声明通过语义校验")
    void acceptsDistinctCaseSensitiveDeclarations() {
        ProtocolFeatures features = ProtocolFeatures.builder()
            .optionalParameters(List.of(
                optionalParameter("order.edges.trajectory", OptionalParameterSupport.SUPPORTED),
                optionalParameter("Order.edges.trajectory", OptionalParameterSupport.REQUIRED)
            ))
            .mobileRobotActions(List.of(
                action("pick", true, List.of(parameter("height", ActionValueDataType.NUMBER))),
                action("Pick", false, List.of(parameter("height", ActionValueDataType.INTEGER)))
            ))
            .build();

        assertEquals(List.of(), VALIDATOR.validate(features));
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] 可选参数同键的重复与冲突分别报告")
    void reportsDuplicateAndConflictingOptionalParameters() {
        OptionalParameter supported = optionalParameter(
            "order.edges.trajectory",
            OptionalParameterSupport.SUPPORTED
        );
        ProtocolFeatures features = ProtocolFeatures.builder()
            .optionalParameters(List.of(
                supported,
                supported,
                optionalParameter(
                    "order.edges.trajectory",
                    OptionalParameterSupport.REQUIRED
                )
            ))
            .mobileRobotActions(List.of())
            .build();

        List<ValidationIssue> issues = VALIDATOR.validate(features);

        assertAll(
            () -> assertEquals(
                List.of(
                    "DUPLICATE_OPTIONAL_PARAMETER",
                    "CONFLICTING_OPTIONAL_PARAMETER"
                ),
                issues.stream().map(ValidationIssue::code).toList()
            ),
            () -> assertEquals(
                List.of(
                    "/optionalParameters/1/parameter",
                    "/optionalParameters/2/parameter"
                ),
                issues.stream().map(ValidationIssue::path).toList()
            ),
            () -> assertTrue(
                issues.stream().allMatch(issue ->
                    issue.severity() == ValidationSeverity.ERROR
                        && "VDA3-FACTSHEET-001".equals(issue.requirementId())
                )
            ),
            () -> assertThrows(
                UnsupportedOperationException.class,
                () -> issues.add(issues.getFirst())
            )
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] Action 同键的重复与冲突分别报告")
    void reportsDuplicateAndConflictingActions() {
        MobileRobotAction pick = action("pick", true, List.of());
        ProtocolFeatures features = ProtocolFeatures.builder()
            .optionalParameters(List.of())
            .mobileRobotActions(List.of(
                pick,
                pick,
                action("pick", false, List.of())
            ))
            .build();

        List<ValidationIssue> issues = VALIDATOR.validate(features);

        assertAll(
            () -> assertEquals(
                List.of("DUPLICATE_ACTION", "CONFLICTING_ACTION"),
                issues.stream().map(ValidationIssue::code).toList()
            ),
            () -> assertEquals(
                List.of(
                    "/mobileRobotActions/1/actionType",
                    "/mobileRobotActions/2/actionType"
                ),
                issues.stream().map(ValidationIssue::path).toList()
            )
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] Action 内部重复 Scope、参数与 Blocking 全部报告")
    void reportsEveryNestedDuplicateAndConflict() {
        ActionParameterDefinition height = parameter(
            "height",
            ActionValueDataType.NUMBER
        );
        MobileRobotAction action = MobileRobotAction.builder()
            .actionType("pick")
            .actionScopes(List.of(
                ActionScope.NODE,
                ActionScope.NODE,
                ActionScope.EDGE,
                ActionScope.EDGE
            ))
            .actionParameters(List.of(
                height,
                height,
                parameter("height", ActionValueDataType.INTEGER)
            ))
            .blockingTypes(List.of(
                BlockingType.NONE,
                BlockingType.SOFT,
                BlockingType.NONE,
                BlockingType.SOFT
            ))
            .pauseAllowed(true)
            .cancelAllowed(true)
            .build();
        ProtocolFeatures features = ProtocolFeatures.builder()
            .optionalParameters(List.of())
            .mobileRobotActions(List.of(action))
            .build();

        List<ValidationIssue> issues = VALIDATOR.validate(features);

        assertAll(
            () -> assertEquals(
                List.of(
                    "DUPLICATE_ACTION_SCOPE",
                    "DUPLICATE_ACTION_SCOPE",
                    "DUPLICATE_ACTION_PARAMETER",
                    "CONFLICTING_ACTION_PARAMETER",
                    "DUPLICATE_BLOCKING_TYPE",
                    "DUPLICATE_BLOCKING_TYPE"
                ),
                issues.stream().map(ValidationIssue::code).toList()
            ),
            () -> assertEquals(
                List.of(
                    "/mobileRobotActions/0/actionScopes/1",
                    "/mobileRobotActions/0/actionScopes/3",
                    "/mobileRobotActions/0/actionParameters/1/key",
                    "/mobileRobotActions/0/actionParameters/2/key",
                    "/mobileRobotActions/0/blockingTypes/2",
                    "/mobileRobotActions/0/blockingTypes/3"
                ),
                issues.stream().map(ValidationIssue::path).toList()
            )
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] 缺失可选 Action 列表不产生虚假问题")
    void ignoresMissingOptionalActionListsAndRejectsNullRoot() {
        ProtocolFeatures features = ProtocolFeatures.builder()
            .optionalParameters(List.of())
            .mobileRobotActions(List.of(
                MobileRobotAction.builder()
                    .actionType("noop")
                    .actionScopes(List.of())
                    .pauseAllowed(false)
                    .cancelAllowed(false)
                    .build()
            ))
            .build();

        assertAll(
            () -> assertEquals(List.of(), VALIDATOR.validate(features)),
            () -> assertThrows(
                NullPointerException.class,
                () -> VALIDATOR.validate(null)
            )
        );
    }

    private static OptionalParameter optionalParameter(
        String parameter,
        OptionalParameterSupport support
    ) {
        return OptionalParameter.builder()
            .parameter(parameter)
            .support(support)
            .build();
    }

    private static MobileRobotAction action(
        String actionType,
        boolean cancelAllowed,
        List<ActionParameterDefinition> parameters
    ) {
        return MobileRobotAction.builder()
            .actionType(actionType)
            .actionScopes(List.of(ActionScope.NODE))
            .actionParameters(parameters)
            .blockingTypes(List.of(BlockingType.NONE))
            .pauseAllowed(true)
            .cancelAllowed(cancelAllowed)
            .build();
    }

    private static ActionParameterDefinition parameter(
        String key,
        ActionValueDataType valueDataType
    ) {
        return ActionParameterDefinition.builder()
            .key(key)
            .valueDataType(valueDataType)
            .build();
    }
}
