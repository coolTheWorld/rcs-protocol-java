package io.github.cooltheworld.rcs.protocol.vda5050.v3.model.action;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

final class ActionVocabularyTest {
    @Test
    @DisplayName("[VDA3-FACTSHEET-001] Action Scope 精确保持四个规范值")
    void exposesTheExactActionScopes() {
        assertEquals(
            List.of(
                ActionScope.INSTANT,
                ActionScope.NODE,
                ActionScope.EDGE,
                ActionScope.ZONE
            ),
            List.of(ActionScope.values())
        );
    }

    @Test
    @DisplayName("[VDA3-ORDER-001] Blocking Type 精确保持四个规范值")
    void exposesTheExactBlockingTypes() {
        assertEquals(
            List.of(
                BlockingType.NONE,
                BlockingType.SOFT,
                BlockingType.SINGLE,
                BlockingType.HARD
            ),
            List.of(BlockingType.values())
        );
    }

    @Test
    @DisplayName("[VDA3-STATE-001] Action Status 精确保持七个规范值")
    void exposesTheExactActionStatuses() {
        assertEquals(
            List.of(
                ActionStatus.WAITING,
                ActionStatus.INITIALIZING,
                ActionStatus.RUNNING,
                ActionStatus.PAUSED,
                ActionStatus.RETRIABLE,
                ActionStatus.FINISHED,
                ActionStatus.FAILED
            ),
            List.of(ActionStatus.values())
        );
    }

    @Test
    @DisplayName("[VDA3-ORDER-001][VDA3-STATE-001] Scope 和 Status 不进入 Action 命令对象")
    void keepsScopeAndStatusOutsideTheActionAggregate() {
        Set<String> fields = Arrays.stream(Action.class.getDeclaredFields())
            .map(Field::getName)
            .collect(Collectors.toUnmodifiableSet());
        Set<String> accessors = Arrays.stream(Action.class.getDeclaredMethods())
            .filter(method -> Modifier.isPublic(method.getModifiers()))
            .map(Method::getName)
            .collect(Collectors.toUnmodifiableSet());

        assertAll(
            () -> assertFalse(fields.contains("actionScope")),
            () -> assertFalse(fields.contains("actionStatus")),
            () -> assertFalse(accessors.contains("actionScope")),
            () -> assertFalse(accessors.contains("actionStatus"))
        );
    }
}
