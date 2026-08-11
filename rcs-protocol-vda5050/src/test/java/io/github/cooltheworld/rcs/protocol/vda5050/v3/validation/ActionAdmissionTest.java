package io.github.cooltheworld.rcs.protocol.vda5050.v3.validation;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.ActionDefinition;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.ActionParameterAdapter;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.ActionRegistry;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.action.ActionParameter;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.action.ActionParameterValue;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.action.ActionScope;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.action.BlockingType;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

final class ActionAdmissionTest {
    private static final ValidationIssue INVALID_STATION = new ValidationIssue(
        "INVALID_STATION",
        ValidationSeverity.ERROR,
        "/actionParameters/0/value",
        "Station parameter is invalid",
        "VDA3-SHARED-012"
    );

    @Test
    @DisplayName("[VDA3-SHARED-012] 已注册 Action 通过全部约束后强类型准入")
    void admitsRegisteredActionAfterEveryConstraintPasses() {
        AtomicInteger adaptations = new AtomicInteger();
        ActionRegistry registry = registry(parameters -> {
            adaptations.incrementAndGet();
            return new ActionParameterAdapter.Adapted<>(
                new DockParameters(stringValue(parameters.getFirst()))
            );
        });
        ActionAdmission admission = ActionAdmission.create(registry);
        List<ActionParameter> parameters = new ArrayList<>(List.of(
            parameter("station", "dock-a")
        ));

        ActionParameterAdapter.Adaptation<DockParameters> result = admission.admit(
            "vendor.dock",
            DockParameters.class,
            ActionScope.NODE,
            BlockingType.SOFT,
            parameters
        );
        ActionParameterAdapter.Adapted<?> adapted = assertInstanceOf(
            ActionParameterAdapter.Adapted.class,
            result
        );

        assertAll(
            () -> assertTrue(result.isAdapted()),
            () -> assertEquals(
                new DockParameters("dock-a"),
                adapted.parameters()
            ),
            () -> assertEquals(1, adaptations.get()),
            () -> assertEquals(List.of(parameter("station", "dock-a")), parameters)
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-012] 未注册 Action 先于其他约束封闭拒绝")
    void rejectsUnregisteredActionBeforeCheckingOtherConstraints() {
        AtomicInteger adaptations = new AtomicInteger();
        ActionAdmission admission = ActionAdmission.create(registry(parameters -> {
            adaptations.incrementAndGet();
            return new ActionParameterAdapter.Adapted<>(
                new DockParameters("unexpected")
            );
        }));

        ValidationIssue issue = rejectedIssue(admission.admit(
            "Vendor.Dock",
            DockParameters.class,
            ActionScope.EDGE,
            BlockingType.HARD,
            List.of()
        ));

        assertAll(
            () -> assertEquals("UNREGISTERED_ACTION", issue.code()),
            () -> assertEquals("/actionType", issue.path()),
            () -> assertEquals(
                "Action type is not registered",
                issue.description()
            ),
            () -> assertEquals("VDA3-SHARED-012", issue.requirementId()),
            () -> assertEquals(0, adaptations.get())
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-012] 已注册 Action 的参数 Class 错配不执行 Adapter")
    void rejectsRegisteredActionWithTheWrongCallerParameterClass() {
        AtomicInteger adaptations = new AtomicInteger();
        ActionAdmission admission = ActionAdmission.create(registry(parameters -> {
            adaptations.incrementAndGet();
            return new ActionParameterAdapter.Adapted<>(
                new DockParameters("unexpected")
            );
        }));

        ValidationIssue issue = rejectedIssue(admission.admit(
            "vendor.dock",
            OtherParameters.class,
            ActionScope.NODE,
            BlockingType.NONE,
            List.of()
        ));

        assertAll(
            () -> assertEquals("ACTION_PARAMETER_TYPE_MISMATCH", issue.code()),
            () -> assertEquals("/actionParameters", issue.path()),
            () -> assertEquals(0, adaptations.get())
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-012] 不允许的 Action Scope 在适配前拒绝")
    void rejectsDisallowedScopeBeforeAdaptingParameters() {
        AtomicInteger adaptations = new AtomicInteger();
        ActionAdmission admission = ActionAdmission.create(registry(parameters -> {
            adaptations.incrementAndGet();
            return new ActionParameterAdapter.Adapted<>(
                new DockParameters("unexpected")
            );
        }));

        ValidationIssue issue = rejectedIssue(admission.admit(
            "vendor.dock",
            DockParameters.class,
            ActionScope.EDGE,
            BlockingType.NONE,
            List.of()
        ));

        assertAll(
            () -> assertEquals("ACTION_SCOPE_NOT_ALLOWED", issue.code()),
            () -> assertEquals("", issue.path()),
            () -> assertEquals(0, adaptations.get())
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-012] 不允许的 Blocking Type 在适配前拒绝")
    void rejectsDisallowedBlockingTypeBeforeAdaptingParameters() {
        AtomicInteger adaptations = new AtomicInteger();
        ActionAdmission admission = ActionAdmission.create(registry(parameters -> {
            adaptations.incrementAndGet();
            return new ActionParameterAdapter.Adapted<>(
                new DockParameters("unexpected")
            );
        }));

        ValidationIssue issue = rejectedIssue(admission.admit(
            "vendor.dock",
            DockParameters.class,
            ActionScope.INSTANT,
            BlockingType.HARD,
            List.of()
        ));

        assertAll(
            () -> assertEquals(
                "ACTION_BLOCKING_TYPE_NOT_ALLOWED",
                issue.code()
            ),
            () -> assertEquals("/blockingType", issue.path()),
            () -> assertEquals(0, adaptations.get())
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-012] Adapter 普通拒绝保持原始结构化 Issue")
    void preservesRegisteredAdapterRejection() {
        ActionAdmission admission = ActionAdmission.create(registry(
            parameters -> new ActionParameterAdapter.Rejected<>(INVALID_STATION)
        ));

        ActionParameterAdapter.Adaptation<DockParameters> result = admission.admit(
            "vendor.dock",
            DockParameters.class,
            ActionScope.NODE,
            BlockingType.SOFT,
            List.of(parameter("station", "unknown"))
        );

        assertAll(
            () -> assertFalse(result.isAdapted()),
            () -> assertSame(INVALID_STATION, rejectedIssue(result))
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-012] 准入不修改输入且相同输入确定重放")
    void leavesInputUntouchedAndReplaysDeterministically() {
        ActionAdmission admission = ActionAdmission.create(registry(parameters ->
            new ActionParameterAdapter.Adapted<>(
                new DockParameters(stringValue(parameters.getFirst()))
            )
        ));
        List<ActionParameter> parameters = new ArrayList<>(List.of(
            parameter("station", "dock-a")
        ));

        ActionParameterAdapter.Adaptation<DockParameters> first = admission.admit(
            "vendor.dock",
            DockParameters.class,
            ActionScope.INSTANT,
            BlockingType.NONE,
            parameters
        );
        ActionParameterAdapter.Adaptation<DockParameters> replay = admission.admit(
            "vendor.dock",
            DockParameters.class,
            ActionScope.INSTANT,
            BlockingType.NONE,
            parameters
        );

        assertAll(
            () -> assertEquals(first, replay),
            () -> assertEquals(List.of(parameter("station", "dock-a")), parameters)
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-012] 缺失调用契约和 Adapter 异常视为编程错误")
    void rejectsMissingContractValuesAndDoesNotMaskAdapterFailures() {
        ActionAdmission admission = ActionAdmission.create(registry(parameters -> {
            throw new IllegalStateException("adapter bug");
        }));

        assertAll(
            () -> assertThrows(
                NullPointerException.class,
                () -> ActionAdmission.create(null)
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> admission.admit(
                    null,
                    DockParameters.class,
                    ActionScope.NODE,
                    BlockingType.NONE,
                    List.of()
                )
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> admission.admit(
                    "vendor.dock",
                    null,
                    ActionScope.NODE,
                    BlockingType.NONE,
                    List.of()
                )
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> admission.admit(
                    "vendor.dock",
                    DockParameters.class,
                    null,
                    BlockingType.NONE,
                    List.of()
                )
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> admission.admit(
                    "vendor.dock",
                    DockParameters.class,
                    ActionScope.NODE,
                    null,
                    List.of()
                )
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> admission.admit(
                    "vendor.dock",
                    DockParameters.class,
                    ActionScope.NODE,
                    BlockingType.NONE,
                    null
                )
            ),
            () -> assertThrows(
                IllegalStateException.class,
                () -> admission.admit(
                    "vendor.dock",
                    DockParameters.class,
                    ActionScope.NODE,
                    BlockingType.NONE,
                    List.of()
                )
            )
        );
    }

    private static ActionRegistry registry(
        ActionParameterAdapter<DockParameters> adapter
    ) {
        ActionDefinition<DockParameters> definition = ActionDefinition
            .builder(DockParameters.class)
            .actionType("vendor.dock")
            .allowedScopes(Set.of(ActionScope.INSTANT, ActionScope.NODE))
            .allowedBlockingTypes(Set.of(BlockingType.NONE, BlockingType.SOFT))
            .adapter(adapter)
            .build();
        return ActionRegistry.builder().register(definition).build();
    }

    private static ActionParameter parameter(String key, String value) {
        return ActionParameter.builder()
            .key(key)
            .value(new ActionParameterValue.StringValue(value))
            .build();
    }

    private static String stringValue(ActionParameter parameter) {
        ActionParameterValue.StringValue value = assertInstanceOf(
            ActionParameterValue.StringValue.class,
            parameter.value()
        );
        return value.value();
    }

    private static ValidationIssue rejectedIssue(
        ActionParameterAdapter.Adaptation<?> result
    ) {
        ActionParameterAdapter.Rejected<?> rejected = assertInstanceOf(
            ActionParameterAdapter.Rejected.class,
            result
        );
        return rejected.issue();
    }

    private record DockParameters(String station) {
    }

    private record OtherParameters(Long value) {
    }
}
