package io.github.cooltheworld.rcs.protocol.vda5050.v3.extension;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.action.ActionParameter;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.action.ActionParameterValue;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.action.ActionScope;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.action.BlockingType;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.ValidationIssue;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.ValidationSeverity;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

final class ActionRegistryTest {
    private static final ValidationIssue INVALID_PARAMETER = new ValidationIssue(
        "INVALID_VENDOR_PARAMETER",
        ValidationSeverity.ERROR,
        "/actionParameters/0",
        "Vendor action parameter is invalid",
        "VDA3-SHARED-012"
    );

    @Test
    @DisplayName("[VDA3-SHARED-012] Definition 强类型适配不可变参数列表")
    void adaptsParametersToTheDeclaredCallerTypeWithImmutableInput() {
        ActionParameter source = parameter("station", "dock-a");
        List<ActionParameter> mutable = new ArrayList<>(List.of(source));
        ActionParameterAdapter<DockParameters> adapter = parameters -> {
            assertThrows(UnsupportedOperationException.class, parameters::clear);
            return new ActionParameterAdapter.Adapted<>(
                new DockParameters(stringValue(parameters.getFirst()))
            );
        };
        ActionDefinition<DockParameters> definition = definition(
            " Vendor.Dock ",
            DockParameters.class,
            adapter
        );

        ActionParameterAdapter.Adaptation<DockParameters> result =
            definition.adapt(mutable);
        mutable.clear();
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
            () -> assertEquals(" Vendor.Dock ", definition.actionType()),
            () -> assertSame(DockParameters.class, definition.parameterType()),
            () -> assertEquals(
                Set.of(ActionScope.INSTANT, ActionScope.NODE),
                definition.allowedScopes()
            ),
            () -> assertEquals(
                Set.of(BlockingType.NONE, BlockingType.SOFT),
                definition.allowedBlockingTypes()
            )
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-012] Adapter 拒绝结果保留结构化 Issue")
    void returnsAClosedRejectionWithoutThrowingForProtocolInputErrors() {
        ActionDefinition<DockParameters> definition = definition(
            "vendor.dock",
            DockParameters.class,
            parameters -> new ActionParameterAdapter.Rejected<>(INVALID_PARAMETER)
        );

        ActionParameterAdapter.Adaptation<DockParameters> result =
            definition.adapt(List.of(parameter("unexpected", "value")));
        ActionParameterAdapter.Rejected<?> rejected = assertInstanceOf(
            ActionParameterAdapter.Rejected.class,
            result
        );

        assertAll(
            () -> assertFalse(result.isAdapted()),
            () -> assertSame(INVALID_PARAMETER, rejected.issue())
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-012] Definition 防御性复制允许的 Scope 与 Blocking")
    void defensivelyCopiesAndFreezesDefinitionCollections() {
        EnumSet<ActionScope> scopes = EnumSet.of(ActionScope.EDGE);
        EnumSet<BlockingType> blockingTypes = EnumSet.of(BlockingType.HARD);
        ActionDefinition<DockParameters> definition = ActionDefinition
            .builder(DockParameters.class)
            .actionType("vendor.edge")
            .allowedScopes(scopes)
            .allowedBlockingTypes(blockingTypes)
            .adapter(parameters -> new ActionParameterAdapter.Adapted<>(
                new DockParameters("edge")
            ))
            .build();
        scopes.clear();
        blockingTypes.clear();

        assertAll(
            () -> assertEquals(Set.of(ActionScope.EDGE), definition.allowedScopes()),
            () -> assertEquals(
                Set.of(BlockingType.HARD),
                definition.allowedBlockingTypes()
            ),
            () -> assertThrows(
                UnsupportedOperationException.class,
                () -> definition.allowedScopes().add(ActionScope.NODE)
            ),
            () -> assertThrows(
                UnsupportedOperationException.class,
                () -> definition.allowedBlockingTypes().add(BlockingType.SOFT)
            )
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-012] Registry 按原文 actionType 和参数 Class 解析")
    void resolvesDefinitionsByExactActionTypeAndParameterClass() {
        AtomicInteger adaptations = new AtomicInteger();
        ActionDefinition<DockParameters> upper = definition(
            "Vendor.Dock",
            DockParameters.class,
            parameters -> {
                adaptations.incrementAndGet();
                return new ActionParameterAdapter.Adapted<>(
                    new DockParameters("upper")
                );
            }
        );
        ActionDefinition<OtherParameters> lower = definition(
            "vendor.dock",
            OtherParameters.class,
            parameters -> new ActionParameterAdapter.Adapted<>(
                new OtherParameters(parameters.size())
            )
        );
        ActionRegistry registry = ActionRegistry.builder()
            .register(upper)
            .register(lower)
            .build();

        assertAll(
            () -> assertSame(
                upper,
                registry.resolve("Vendor.Dock", DockParameters.class).orElseThrow()
            ),
            () -> assertSame(
                lower,
                registry.resolve("vendor.dock", OtherParameters.class).orElseThrow()
            ),
            () -> assertTrue(
                registry.resolve("Vendor.Dock", OtherParameters.class).isEmpty()
            ),
            () -> assertTrue(
                registry.resolve("VENDOR.DOCK", DockParameters.class).isEmpty()
            ),
            () -> assertEquals(0, adaptations.get())
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-012] Registry 重复注册立即拒绝且不覆盖")
    void rejectsExactDuplicateRegistrationWithoutOverwriting() {
        ActionDefinition<DockParameters> first = definition(
            "vendor.dock",
            DockParameters.class,
            parameters -> new ActionParameterAdapter.Adapted<>(
                new DockParameters("first")
            )
        );
        ActionDefinition<OtherParameters> duplicate = definition(
            "vendor.dock",
            OtherParameters.class,
            parameters -> new ActionParameterAdapter.Adapted<>(
                new OtherParameters(2)
            )
        );
        ActionRegistry.Builder builder = ActionRegistry.builder().register(first);

        IllegalArgumentException error = assertThrows(
            IllegalArgumentException.class,
            () -> builder.register(duplicate)
        );
        ActionRegistry registry = builder.build();

        assertAll(
            () -> assertEquals("actionType is already registered", error.getMessage()),
            () -> assertSame(
                first,
                registry.resolve("vendor.dock", DockParameters.class).orElseThrow()
            ),
            () -> assertTrue(
                registry.resolve("vendor.dock", OtherParameters.class).isEmpty()
            )
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-012] 已构造 Registry 是确定性不可变快照")
    void buildsImmutableSnapshotsIndependentFromLaterBuilderChanges() {
        ActionRegistry.Builder builder = ActionRegistry.builder()
            .register(definition(
                "vendor.first",
                DockParameters.class,
                parameters -> new ActionParameterAdapter.Adapted<>(
                    new DockParameters("first")
                )
            ));
        ActionRegistry first = builder.build();
        builder.register(definition(
            "vendor.second",
            OtherParameters.class,
            parameters -> new ActionParameterAdapter.Adapted<>(
                new OtherParameters(2)
            )
        ));
        ActionRegistry second = builder.build();

        assertAll(
            () -> assertEquals(Set.of("vendor.first"), first.actionTypes()),
            () -> assertEquals(
                Set.of("vendor.first", "vendor.second"),
                second.actionTypes()
            ),
            () -> assertThrows(
                UnsupportedOperationException.class,
                () -> first.actionTypes().clear()
            ),
            () -> assertTrue(
                first.resolve("vendor.second", OtherParameters.class).isEmpty()
            )
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-012] 空参数适配与重复调用保持确定")
    void adaptsEmptyParametersDeterministically() {
        ActionDefinition<OtherParameters> definition = definition(
            "vendor.empty",
            OtherParameters.class,
            parameters -> new ActionParameterAdapter.Adapted<>(
                new OtherParameters(parameters.size())
            )
        );

        assertEquals(definition.adapt(List.of()), definition.adapt(List.of()));
    }

    @Test
    @DisplayName("[VDA3-SHARED-012] 契约缺失引用和 Adapter 编程错误封闭失败")
    void rejectsMissingContractValuesAndAdapterProgrammingErrors() {
        ActionParameterAdapter<DockParameters> adapter = parameters ->
            new ActionParameterAdapter.Adapted<>(new DockParameters("dock"));
        ActionDefinition.Builder<DockParameters> base = ActionDefinition
            .builder(DockParameters.class)
            .actionType("vendor.dock")
            .allowedScopes(Set.of(ActionScope.NODE))
            .allowedBlockingTypes(Set.of(BlockingType.NONE))
            .adapter(adapter);

        assertAll(
            () -> assertThrows(
                NullPointerException.class,
                () -> ActionDefinition.builder(null)
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> ActionDefinition.builder(DockParameters.class)
                    .allowedScopes(Set.of(ActionScope.NODE))
                    .allowedBlockingTypes(Set.of(BlockingType.NONE))
                    .adapter(adapter)
                    .build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> ActionDefinition.builder(DockParameters.class)
                    .actionType("vendor.dock")
                    .allowedBlockingTypes(Set.of(BlockingType.NONE))
                    .adapter(adapter)
                    .build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> ActionDefinition.builder(DockParameters.class)
                    .actionType("vendor.dock")
                    .allowedScopes(Set.of(ActionScope.NODE))
                    .adapter(adapter)
                    .build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> ActionDefinition.builder(DockParameters.class)
                    .actionType("vendor.dock")
                    .allowedScopes(Set.of(ActionScope.NODE))
                    .allowedBlockingTypes(Set.of(BlockingType.NONE))
                    .build()
            ),
            () -> assertThrows(NullPointerException.class, () -> base.build().adapt(null)),
            () -> assertThrows(
                NullPointerException.class,
                () -> base.build().adapt(Arrays.asList(parameter("key", "value"), null))
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> new ActionParameterAdapter.Adapted<DockParameters>(null)
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> new ActionParameterAdapter.Rejected<DockParameters>(null)
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> definition(
                    "vendor.null-result",
                    DockParameters.class,
                    parameters -> null
                ).adapt(List.of())
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> ActionRegistry.builder().register(null)
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> ActionRegistry.builder().build().resolve(
                    null,
                    DockParameters.class
                )
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> ActionRegistry.builder().build().resolve(
                    "vendor.dock",
                    null
                )
            )
        );
    }

    private static <P> ActionDefinition<P> definition(
        String actionType,
        Class<P> parameterType,
        ActionParameterAdapter<P> adapter
    ) {
        return ActionDefinition.builder(parameterType)
            .actionType(actionType)
            .allowedScopes(Set.of(ActionScope.INSTANT, ActionScope.NODE))
            .allowedBlockingTypes(Set.of(BlockingType.NONE, BlockingType.SOFT))
            .adapter(adapter)
            .build();
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

    private record DockParameters(String station) {
    }

    private record OtherParameters(int count) {
    }
}
