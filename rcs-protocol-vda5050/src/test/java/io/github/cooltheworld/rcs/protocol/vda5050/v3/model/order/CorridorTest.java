package io.github.cooltheworld.rcs.protocol.vda5050.v3.model.order;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.ExtensionFields;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

final class CorridorTest {
    @Test
    @DisplayName("[VDA3-ORDER-001] 最小 Corridor 保持可选默认字段缺失")
    void buildsTheMinimalCorridorWithoutMaterializingDefaults() {
        Corridor corridor = Corridor.builder()
            .leftWidth(1.25D)
            .rightWidth(2.5D)
            .build();

        assertAll(
            () -> assertEquals(1.25D, corridor.leftWidth()),
            () -> assertEquals(2.5D, corridor.rightWidth()),
            () -> assertNull(corridor.corridorReferencePoint()),
            () -> assertNull(corridor.releaseRequired()),
            () -> assertNull(corridor.releaseLossBehavior()),
            () -> assertTrue(corridor.extensionFields().isEmpty())
        );
    }

    @Test
    @DisplayName("[VDA3-ORDER-001] 完整 Corridor 保存显式值和未知扩展")
    void buildsTheCompleteCorridor() throws ReflectiveOperationException {
        ExtensionFields extensions = extensionFields("{\"vendor\":true}");
        Corridor corridor = fullCorridor(extensions).build();

        assertAll(
            () -> assertEquals(1.0D, corridor.leftWidth()),
            () -> assertEquals(2.0D, corridor.rightWidth()),
            () -> assertEquals(
                CorridorReferencePoint.CONTOUR,
                corridor.corridorReferencePoint()
            ),
            () -> assertEquals(Boolean.FALSE, corridor.releaseRequired()),
            () -> assertEquals(
                CorridorReleaseLossBehavior.RETURN,
                corridor.releaseLossBehavior()
            ),
            () -> assertEquals(extensions, corridor.extensionFields())
        );
    }

    @Test
    @DisplayName("[VDA3-ORDER-003] 模型无损保留待 Validator 检查的数值")
    void preservesProgrammaticNumericBoundariesForValidation() {
        Corridor corridor = Corridor.builder()
            .leftWidth(-1.0D)
            .rightWidth(Double.POSITIVE_INFINITY)
            .build();

        assertAll(
            () -> assertEquals(-1.0D, corridor.leftWidth()),
            () -> assertEquals(
                Double.POSITIVE_INFINITY,
                corridor.rightWidth()
            )
        );
    }

    @Test
    @DisplayName("[VDA3-ORDER-001] Corridor 只拒绝缺失必填引用")
    void rejectsOnlyMissingRequiredReferences() {
        assertAll(
            () -> assertThrows(
                NullPointerException.class,
                () -> Corridor.builder().rightWidth(1.0D).build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> Corridor.builder().leftWidth(1.0D).build()
            ),
            () -> assertTrue(
                Corridor.builder()
                    .leftWidth(0.0D)
                    .rightWidth(0.0D)
                    .extensionFields(null)
                    .build()
                    .extensionFields()
                    .isEmpty()
            )
        );
    }

    @Test
    @DisplayName("[VDA3-ORDER-001] Corridor 值相等覆盖全部线路字段")
    void includesEveryFieldInValueEquality() throws ReflectiveOperationException {
        ExtensionFields extensions = extensionFields("{\"vendor\":true}");
        Corridor equal = fullCorridor(extensions).build();
        Corridor same = fullCorridor(extensions).build();

        assertAll(
            () -> assertEquals(equal, equal),
            () -> assertNotEquals(equal, null),
            () -> assertNotEquals(equal, "corridor"),
            () -> assertNotEquals(
                equal,
                fullCorridor(extensions).leftWidth(9.0D).build()
            ),
            () -> assertNotEquals(
                equal,
                fullCorridor(extensions).rightWidth(9.0D).build()
            ),
            () -> assertNotEquals(
                equal,
                fullCorridor(extensions)
                    .corridorReferencePoint(
                        CorridorReferencePoint.KINEMATIC_CENTER
                    )
                    .build()
            ),
            () -> assertNotEquals(
                equal,
                fullCorridor(extensions).releaseRequired(Boolean.TRUE).build()
            ),
            () -> assertNotEquals(
                equal,
                fullCorridor(extensions)
                    .releaseLossBehavior(CorridorReleaseLossBehavior.STOP)
                    .build()
            ),
            () -> assertNotEquals(
                equal,
                fullCorridor(ExtensionFields.empty()).build()
            ),
            () -> assertEquals(equal, same),
            () -> assertEquals(equal.hashCode(), same.hashCode())
        );
    }

    @Test
    @DisplayName("[VDA3-ORDER-001] Corridor 词汇和字段集合精确匹配正文")
    void exposesOnlyTheSpecifiedVocabularyAndWireFields() {
        assertAll(
            () -> assertEquals(
                Set.of("KINEMATIC_CENTER", "CONTOUR"),
                enumNames(CorridorReferencePoint.class)
            ),
            () -> assertEquals(
                Set.of("STOP", "RETURN"),
                enumNames(CorridorReleaseLossBehavior.class)
            ),
            () -> assertEquals(
                Set.of(
                    "leftWidth",
                    "rightWidth",
                    "corridorReferencePoint",
                    "releaseRequired",
                    "releaseLossBehavior",
                    "extensionFields"
                ),
                fieldNames(Corridor.class)
            )
        );
    }

    private static Corridor.Builder fullCorridor(
        ExtensionFields extensionFields
    ) {
        return Corridor.builder()
            .leftWidth(1.0D)
            .rightWidth(2.0D)
            .corridorReferencePoint(CorridorReferencePoint.CONTOUR)
            .releaseRequired(Boolean.FALSE)
            .releaseLossBehavior(CorridorReleaseLossBehavior.RETURN)
            .extensionFields(extensionFields);
    }

    private static Set<String> fieldNames(Class<?> type) {
        return Arrays.stream(type.getDeclaredFields())
            .map(Field::getName)
            .collect(Collectors.toUnmodifiableSet());
    }

    private static Set<String> enumNames(Class<? extends Enum<?>> type) {
        return Arrays.stream(type.getEnumConstants())
            .map(Enum::name)
            .collect(Collectors.toUnmodifiableSet());
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
