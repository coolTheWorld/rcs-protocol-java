package io.github.cooltheworld.rcs.protocol.vda5050.v3.model.action;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.ExtensionFields;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

final class ActionParameterTest {
    @Test
    @DisplayName("[VDA3-SHARED-012] Action 参数封闭表达六类 JSON 值")
    void representsEverySupportedJsonValueType() {
        ActionParameterValue.BooleanValue bool =
            new ActionParameterValue.BooleanValue(Boolean.TRUE);
        ActionParameterValue.NumberValue number =
            new ActionParameterValue.NumberValue(1.5D);
        ActionParameterValue.IntegerValue integer =
            new ActionParameterValue.IntegerValue(7L);
        ActionParameterValue.StringValue string =
            new ActionParameterValue.StringValue("opaque");
        ActionParameterValue.ObjectMember member =
            new ActionParameterValue.ObjectMember("enabled", bool);
        ActionParameterValue.ObjectValue object =
            new ActionParameterValue.ObjectValue(List.of(member));
        ActionParameterValue.ArrayValue array =
            new ActionParameterValue.ArrayValue(List.of(integer, object));

        assertAll(
            () -> assertEquals(ActionValueDataType.BOOL, bool.valueDataType()),
            () -> assertEquals(Boolean.TRUE, bool.value()),
            () -> assertEquals(ActionValueDataType.NUMBER, number.valueDataType()),
            () -> assertEquals(1.5D, number.value()),
            () -> assertEquals(ActionValueDataType.INTEGER, integer.valueDataType()),
            () -> assertEquals(7L, integer.value()),
            () -> assertEquals(ActionValueDataType.STRING, string.valueDataType()),
            () -> assertEquals("opaque", string.value()),
            () -> assertEquals(ActionValueDataType.OBJECT, object.valueDataType()),
            () -> assertEquals("enabled", object.members().getFirst().name()),
            () -> assertEquals(bool, object.members().getFirst().value()),
            () -> assertEquals(ActionValueDataType.ARRAY, array.valueDataType()),
            () -> assertEquals(List.of(integer, object), array.values())
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-012] 递归对象和数组执行防御性复制")
    void defensivelyCopiesRecursiveCollections() {
        List<ActionParameterValue.ObjectMember> members = new ArrayList<>();
        members.add(new ActionParameterValue.ObjectMember(
            "count",
            new ActionParameterValue.IntegerValue(1L)
        ));
        ActionParameterValue.ObjectValue object =
            new ActionParameterValue.ObjectValue(members);
        List<ActionParameterValue> values = new ArrayList<>(List.of(object));
        ActionParameterValue.ArrayValue array =
            new ActionParameterValue.ArrayValue(values);
        members.clear();
        values.clear();

        assertAll(
            () -> assertEquals(1, object.members().size()),
            () -> assertEquals(List.of(object), array.values()),
            () -> assertThrows(
                UnsupportedOperationException.class,
                () -> object.members().clear()
            ),
            () -> assertThrows(
                UnsupportedOperationException.class,
                () -> array.values().clear()
            )
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-012] Action 参数保留原文键和值与空扩展")
    void buildsAnImmutableActionParameterWithoutNormalizingTheKey() {
        ActionParameterValue value = new ActionParameterValue.StringValue(
            "station-a"
        );
        ActionParameter parameter = ActionParameter.builder()
            .key(" Vendor.DeviceId ")
            .value(value)
            .build();

        assertAll(
            () -> assertEquals(" Vendor.DeviceId ", parameter.key()),
            () -> assertEquals(value, parameter.value()),
            () -> assertTrue(parameter.extensionFields().isEmpty())
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-012] 模型只保证形状并保留待 Validator 检查的数值")
    void preservesProgrammaticNumberBoundariesForLaterValidation() {
        ActionParameterValue.NumberValue notANumber =
            new ActionParameterValue.NumberValue(Double.NaN);
        ActionParameterValue.NumberValue positiveInfinity =
            new ActionParameterValue.NumberValue(Double.POSITIVE_INFINITY);
        ActionParameterValue.IntegerValue minimumLong =
            new ActionParameterValue.IntegerValue(Long.MIN_VALUE);

        assertAll(
            () -> assertTrue(Double.isNaN(notANumber.value())),
            () -> assertEquals(Double.POSITIVE_INFINITY, positiveInfinity.value()),
            () -> assertEquals(Long.MIN_VALUE, minimumLong.value())
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-012] 值相等覆盖参数的全部标准字段")
    void includesEveryActionParameterFieldInValueEquality()
        throws ReflectiveOperationException {
        ActionParameter equal = parameter("key", "value", ExtensionFields.empty());
        ActionParameter same = parameter("key", "value", ExtensionFields.empty());
        ActionParameter differentExtension = parameter(
            "key",
            "value",
            extensionFields("{\"vendor\":true}")
        );

        assertAll(
            () -> assertEquals(equal, equal),
            () -> assertNotEquals(equal, null),
            () -> assertNotEquals(equal, "parameter"),
            () -> assertNotEquals(
                equal,
                parameter("other", "value", ExtensionFields.empty())
            ),
            () -> assertNotEquals(
                equal,
                parameter("key", "other", ExtensionFields.empty())
            ),
            () -> assertNotEquals(equal, differentExtension),
            () -> assertEquals(equal, same),
            () -> assertEquals(equal.hashCode(), same.hashCode()),
            () -> assertNotEquals(
                new ActionParameterValue.ObjectMember(
                    "key",
                    new ActionParameterValue.StringValue("value")
                ),
                new ActionParameterValue.ObjectMember(
                    "other",
                    new ActionParameterValue.StringValue("value")
                )
            )
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-012] 缺失引用和 null 递归元素封闭失败")
    void rejectsMissingRequiredValuesAndNullRecursiveElements() {
        ActionParameterValue.StringValue value =
            new ActionParameterValue.StringValue("value");

        assertAll(
            () -> assertThrows(
                NullPointerException.class,
                () -> new ActionParameterValue.BooleanValue(null)
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> new ActionParameterValue.NumberValue(null)
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> new ActionParameterValue.IntegerValue(null)
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> new ActionParameterValue.StringValue(null)
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> new ActionParameterValue.ObjectMember(null, value)
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> new ActionParameterValue.ObjectMember("key", null)
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> new ActionParameterValue.ObjectValue(null)
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> new ActionParameterValue.ObjectValue(
                    Arrays.asList(
                        new ActionParameterValue.ObjectMember("key", value),
                        null
                    )
                )
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> new ActionParameterValue.ArrayValue(null)
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> new ActionParameterValue.ArrayValue(
                    Arrays.asList(value, null)
                )
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> ActionParameter.builder().value(value).build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> ActionParameter.builder().key("key").build()
            ),
            () -> assertFalse(
                ActionParameter.builder()
                    .key("")
                    .value(value)
                    .extensionFields(null)
                    .build()
                    .key()
                    .equals("non-empty")
            )
        );
    }

    private static ActionParameter parameter(
        String key,
        String value,
        ExtensionFields extensionFields
    ) {
        return ActionParameter.builder()
            .key(key)
            .value(new ActionParameterValue.StringValue(value))
            .extensionFields(extensionFields)
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
