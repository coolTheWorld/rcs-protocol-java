package io.github.cooltheworld.rcs.protocol.vda5050.v3.extension;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.action.ActionParameter;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.action.ActionScope;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.action.BlockingType;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 一个可注册厂商 Action 的强类型定义。
 *
 * <p>Action 类型保持原文；允许的作用域与阻塞类型使用不可变枚举集合。调用 Adapter 前统一冻结参数列表，
 * 使调用方实现不能修改线路模型。</p>
 *
 * @param <P> 调用方参数类型
 */
public final class ActionDefinition<P> {
    private final String actionType;
    private final Class<P> parameterType;
    private final Set<ActionScope> allowedScopes;
    private final Set<BlockingType> allowedBlockingTypes;
    private final ActionParameterAdapter<P> adapter;

    private ActionDefinition(Builder<P> builder) {
        this.actionType = Objects.requireNonNull(builder.actionType, "actionType");
        this.parameterType = Objects.requireNonNull(
            builder.parameterType,
            "parameterType"
        );
        this.allowedScopes = immutableEnumSet(
            builder.allowedScopes,
            ActionScope.class,
            "allowedScopes"
        );
        this.allowedBlockingTypes = immutableEnumSet(
            builder.allowedBlockingTypes,
            BlockingType.class,
            "allowedBlockingTypes"
        );
        this.adapter = Objects.requireNonNull(builder.adapter, "adapter");
    }

    /**
     * 创建绑定参数类型的 Builder。
     *
     * @param parameterType 调用方参数 Class
     * @param <P> 调用方参数类型
     * @return 新 Builder
     */
    public static <P> Builder<P> builder(Class<P> parameterType) {
        return new Builder<>(Objects.requireNonNull(parameterType, "parameterType"));
    }

    /** @return 保持原文且大小写敏感的 Action 类型 */
    public String actionType() {
        return actionType;
    }

    /** @return 调用方参数 Class */
    public Class<P> parameterType() {
        return parameterType;
    }

    /** @return 不可变的允许作用域 */
    public Set<ActionScope> allowedScopes() {
        return allowedScopes;
    }

    /** @return 不可变的允许 Blocking Type */
    public Set<BlockingType> allowedBlockingTypes() {
        return allowedBlockingTypes;
    }

    /**
     * 使用不可变参数快照执行已注册 Adapter。
     *
     * @param parameters 动作参数线路值
     * @return 非空封闭适配结果
     */
    public ActionParameterAdapter.Adaptation<P> adapt(
        List<ActionParameter> parameters
    ) {
        List<ActionParameter> immutableParameters = List.copyOf(
            Objects.requireNonNull(parameters, "parameters")
        );
        return Objects.requireNonNull(
            adapter.adapt(immutableParameters),
            "adapter result"
        );
    }

    private static <E extends Enum<E>> Set<E> immutableEnumSet(
        Set<E> values,
        Class<E> enumType,
        String name
    ) {
        Objects.requireNonNull(values, name);
        EnumSet<E> copy = EnumSet.noneOf(enumType);
        copy.addAll(values);
        return Collections.unmodifiableSet(copy);
    }

    /** 强类型厂商 Action 定义 Builder。 */
    public static final class Builder<P> {
        private final Class<P> parameterType;
        private String actionType;
        private Set<ActionScope> allowedScopes;
        private Set<BlockingType> allowedBlockingTypes;
        private ActionParameterAdapter<P> adapter;

        private Builder(Class<P> parameterType) {
            this.parameterType = parameterType;
        }

        /** @param actionType 保持原文的 Action 类型 @return 当前 Builder */
        public Builder<P> actionType(String actionType) {
            this.actionType = actionType;
            return this;
        }

        /** @param allowedScopes 允许作用域 @return 当前 Builder */
        public Builder<P> allowedScopes(Set<ActionScope> allowedScopes) {
            this.allowedScopes = allowedScopes;
            return this;
        }

        /** @param allowedBlockingTypes 允许 Blocking Type @return 当前 Builder */
        public Builder<P> allowedBlockingTypes(
            Set<BlockingType> allowedBlockingTypes
        ) {
            this.allowedBlockingTypes = allowedBlockingTypes;
            return this;
        }

        /** @param adapter 强类型参数 Adapter @return 当前 Builder */
        public Builder<P> adapter(ActionParameterAdapter<P> adapter) {
            this.adapter = adapter;
            return this;
        }

        /** @return 必填契约完整的 Action 定义 */
        public ActionDefinition<P> build() {
            return new ActionDefinition<>(this);
        }
    }
}
