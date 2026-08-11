package io.github.cooltheworld.rcs.protocol.vda5050.v3.model.action;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.ExtensionFields;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

final class ActionTest {
    @Test
    @DisplayName("[VDA3-ORDER-001][VDA3-INSTANT_ACTIONS-001] 最小 Action 保留必填原文并省略可选值")
    void buildsTheMinimalActionWithoutNormalizingWireValues() {
        Action action = minimalAction()
            .actionType(" Vendor.Custom ")
            .actionId(" Action-01 ")
            .build();

        assertAll(
            () -> assertEquals(" Vendor.Custom ", action.actionType()),
            () -> assertEquals(" Action-01 ", action.actionId()),
            () -> assertEquals(BlockingType.SOFT, action.blockingType()),
            () -> assertNull(action.actionDescriptor()),
            () -> assertNull(action.actionParameters()),
            () -> assertNull(action.retriable()),
            () -> assertTrue(action.extensionFields().isEmpty())
        );
    }

    @Test
    @DisplayName("[VDA3-ORDER-001][VDA3-INSTANT_ACTIONS-001] 完整 Action 防御性复制参数并保留显式 false")
    void buildsTheCompleteImmutableAction() throws ReflectiveOperationException {
        ActionParameter parameter = parameter("deviceId", "fork");
        List<ActionParameter> parameters = new ArrayList<>(List.of(parameter));
        ExtensionFields extensions = extensionFields("{\"vendor\":true}");

        Action action = minimalAction()
            .actionDescriptor(" human-readable ")
            .actionParameters(parameters)
            .retriable(Boolean.FALSE)
            .extensionFields(extensions)
            .build();
        parameters.clear();

        assertAll(
            () -> assertEquals(" human-readable ", action.actionDescriptor()),
            () -> assertEquals(List.of(parameter), action.actionParameters()),
            () -> assertEquals(Boolean.FALSE, action.retriable()),
            () -> assertEquals(extensions, action.extensionFields()),
            () -> assertThrows(
                UnsupportedOperationException.class,
                () -> action.actionParameters().clear()
            )
        );
    }

    @Test
    @DisplayName("[VDA3-ORDER-001] 缺失参数、空参数和显式 false 保持不同线路语义")
    void distinguishesMissingEmptyAndExplicitFalseValues() {
        Action missing = minimalAction().build();
        Action empty = minimalAction().actionParameters(List.of()).build();
        Action explicitFalse = minimalAction().retriable(Boolean.FALSE).build();

        assertAll(
            () -> assertNull(missing.actionParameters()),
            () -> assertEquals(List.of(), empty.actionParameters()),
            () -> assertNotEquals(missing, empty),
            () -> assertNull(missing.retriable()),
            () -> assertFalse(explicitFalse.retriable()),
            () -> assertNotEquals(missing, explicitFalse)
        );
    }

    @Test
    @DisplayName("[VDA3-ORDER-001][VDA3-INSTANT_ACTIONS-001] Action 值相等覆盖全部线路字段")
    void includesEveryWireFieldInValueEquality() throws ReflectiveOperationException {
        ExtensionFields extensions = extensionFields("{\"vendor\":true}");
        Action equal = fullAction(extensions).build();
        Action same = fullAction(extensions).build();

        assertAll(
            () -> assertEquals(equal, equal),
            () -> assertNotEquals(equal, null),
            () -> assertNotEquals(equal, "action"),
            () -> assertNotEquals(equal, fullAction(extensions).actionType("other").build()),
            () -> assertNotEquals(equal, fullAction(extensions).actionId("other").build()),
            () -> assertNotEquals(equal, fullAction(extensions).actionDescriptor("other").build()),
            () -> assertNotEquals(equal, fullAction(extensions).blockingType(BlockingType.HARD).build()),
            () -> assertNotEquals(equal, fullAction(extensions).actionParameters(List.of()).build()),
            () -> assertNotEquals(equal, fullAction(extensions).retriable(Boolean.FALSE).build()),
            () -> assertNotEquals(equal, fullAction(ExtensionFields.empty()).build()),
            () -> assertEquals(equal, same),
            () -> assertEquals(equal.hashCode(), same.hashCode())
        );
    }

    @Test
    @DisplayName("[VDA3-ORDER-001][VDA3-INSTANT_ACTIONS-001] Builder 只拒绝缺失引用和 null 参数元素")
    void rejectsMissingRequiredReferencesAndNullParameterElements() {
        ActionParameter parameter = parameter("key", "value");

        assertAll(
            () -> assertThrows(
                NullPointerException.class,
                () -> Action.builder()
                    .actionId("id")
                    .blockingType(BlockingType.NONE)
                    .build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> Action.builder()
                    .actionType("type")
                    .blockingType(BlockingType.NONE)
                    .build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> Action.builder()
                    .actionType("type")
                    .actionId("id")
                    .build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> minimalAction().actionParameters(
                    Arrays.asList(parameter, null)
                ).build()
            ),
            () -> assertTrue(
                minimalAction().extensionFields(null).build().extensionFields().isEmpty()
            )
        );
    }

    @Test
    @DisplayName("[VDA3-ORDER-001][VDA3-INSTANT_ACTIONS-001] Scope 和 Status 不属于 Action 线路聚合")
    void exposesOnlyTheActionWireFields() {
        Set<String> fieldNames = Arrays.stream(Action.class.getDeclaredFields())
            .map(Field::getName)
            .collect(Collectors.toUnmodifiableSet());

        assertEquals(
            Set.of(
                "actionType",
                "actionId",
                "actionDescriptor",
                "blockingType",
                "actionParameters",
                "retriable",
                "extensionFields"
            ),
            fieldNames
        );
    }

    private static Action.Builder minimalAction() {
        return Action.builder()
            .actionType("pick")
            .actionId("action-1")
            .blockingType(BlockingType.SOFT);
    }

    private static Action.Builder fullAction(ExtensionFields extensionFields) {
        return minimalAction()
            .actionDescriptor("Pick load")
            .actionParameters(List.of(parameter("deviceId", "fork")))
            .retriable(Boolean.TRUE)
            .extensionFields(extensionFields);
    }

    private static ActionParameter parameter(String key, String value) {
        return ActionParameter.builder()
            .key(key)
            .value(new ActionParameterValue.StringValue(value))
            .build();
    }

    private static ExtensionFields extensionFields(String json)
        throws ReflectiveOperationException {
        Method factory = ExtensionFields.class.getDeclaredMethod(
            "fromJsonBytes",
            byte[].class,
            byte[].class
        );
        factory.setAccessible(true);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        return (ExtensionFields) factory.invoke(null, bytes, bytes);
    }
}
