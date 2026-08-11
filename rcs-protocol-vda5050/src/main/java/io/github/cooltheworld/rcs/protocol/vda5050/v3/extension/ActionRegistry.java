package io.github.cooltheworld.rcs.protocol.vda5050.v3.extension;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * 厂商 Action 定义的不可变注册快照。
 *
 * <p>注册键保持原文并区分大小写。解析只有在参数 Class 精确相同时才返回定义，不根据线路数据中的类名
 * 实例化类型。</p>
 */
public final class ActionRegistry {
    private final Map<String, ActionDefinition<?>> definitions;
    private final Set<String> actionTypes;

    private ActionRegistry(Builder builder) {
        this.definitions = Collections.unmodifiableMap(
            new LinkedHashMap<>(builder.definitions)
        );
        this.actionTypes = Collections.unmodifiableSet(
            new LinkedHashSet<>(definitions.keySet())
        );
    }

    /** @return 空注册表 Builder */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 按原文 Action 类型和参数 Class 解析强类型定义。
     *
     * @param actionType 原文 Action 类型
     * @param parameterType 期望参数 Class
     * @param <P> 期望参数类型
     * @return 类型与 Class 都精确匹配时的定义，否则为空
     */
    public <P> Optional<ActionDefinition<P>> resolve(
        String actionType,
        Class<P> parameterType
    ) {
        Objects.requireNonNull(actionType, "actionType");
        Objects.requireNonNull(parameterType, "parameterType");
        ActionDefinition<?> definition = definitions.get(actionType);
        if (definition == null || definition.parameterType() != parameterType) {
            return Optional.empty();
        }
        return Optional.of(narrow(definition));
    }

    /** @return 按注册顺序排列的不可变 Action 类型集合 */
    public Set<String> actionTypes() {
        return actionTypes;
    }

    @SuppressWarnings("unchecked")
    private static <P> ActionDefinition<P> narrow(
        ActionDefinition<?> definition
    ) {
        return (ActionDefinition<P>) definition;
    }

    /** 不可变注册快照 Builder。 */
    public static final class Builder {
        private final Map<String, ActionDefinition<?>> definitions =
            new LinkedHashMap<>();

        private Builder() {
        }

        /**
         * 注册一个定义；精确重复的 Action 类型立即失败且不覆盖旧值。
         *
         * @param definition 强类型定义
         * @return 当前 Builder
         */
        public Builder register(ActionDefinition<?> definition) {
            Objects.requireNonNull(definition, "definition");
            if (definitions.containsKey(definition.actionType())) {
                throw new IllegalArgumentException(
                    "actionType is already registered"
                );
            }
            definitions.put(definition.actionType(), definition);
            return this;
        }

        /** @return 与后续 Builder 变化隔离的不可变注册快照 */
        public ActionRegistry build() {
            return new ActionRegistry(this);
        }
    }
}
