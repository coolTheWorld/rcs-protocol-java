package io.github.cooltheworld.rcs.protocol.vda5050.v3.extension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

final class ExtensionFieldsTest {
    @Test
    @DisplayName("[VDA3-SHARED-007] 公共 API 只暴露不透明值语义")
    void exposesOnlyOpaqueValueSemantics() {
        ExtensionFields empty = ExtensionFields.empty();

        Set<String> publicMethods = Arrays.stream(ExtensionFields.class.getDeclaredMethods())
            .filter(method -> Modifier.isPublic(method.getModifiers()))
            .map(method -> method.getName())
            .collect(Collectors.toSet());
        long publicConstructors = Arrays.stream(ExtensionFields.class.getDeclaredConstructors())
            .filter(constructor -> Modifier.isPublic(constructor.getModifiers()))
            .count();

        assertTrue(empty.isEmpty());
        assertEquals(Set.of("empty", "isEmpty", "equals", "hashCode"), publicMethods);
        assertEquals(0L, publicConstructors);
    }

    @Test
    void emptyInstancesUseValueEquality() {
        ExtensionFields empty = ExtensionFields.empty();

        assertEquals(empty, empty);
        assertEquals(empty, ExtensionFields.empty());
        assertEquals(empty.hashCode(), ExtensionFields.empty().hashCode());
        assertNotEquals(empty, null);
        assertNotEquals(empty, "extensions");
        assertFalse(empty.equals(new Object()));
    }
}
