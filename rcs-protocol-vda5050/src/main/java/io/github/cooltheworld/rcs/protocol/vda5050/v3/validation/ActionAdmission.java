package io.github.cooltheworld.rcs.protocol.vda5050.v3.validation;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.ActionDefinition;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.ActionParameterAdapter;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.ActionRegistry;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.action.ActionParameter;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.action.ActionScope;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.action.BlockingType;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 已注册厂商 Action 的纯准入服务。
 *
 * <p>准入依次检查原文 Action 类型与参数 Class、作用域、Blocking Type 和强类型 Adapter。服务不执行
 * 设备动作，不访问时钟或外部 I/O。</p>
 */
public final class ActionAdmission {
    private static final String REQUIREMENT_ID = "VDA3-SHARED-012";

    private final ActionRegistry registry;

    private ActionAdmission(ActionRegistry registry) {
        this.registry = Objects.requireNonNull(registry, "registry");
    }

    /**
     * 创建绑定不可变注册快照的准入服务。
     *
     * @param registry 厂商 Action 注册快照
     * @return 无状态准入服务
     */
    public static ActionAdmission create(ActionRegistry registry) {
        return new ActionAdmission(registry);
    }

    /**
     * 对一个厂商 Action 的参数执行强类型准入。
     *
     * @param actionType 保持原文的 Action 类型
     * @param parameterType 调用方期望参数 Class
     * @param scope 当前 Action 作用域
     * @param blockingType 当前 Blocking Type
     * @param parameters 动作参数线路值
     * @param <P> 调用方参数类型
     * @return 强类型成功结果或安全结构化拒绝
     */
    public <P> ActionParameterAdapter.Adaptation<P> admit(
        String actionType,
        Class<P> parameterType,
        ActionScope scope,
        BlockingType blockingType,
        List<ActionParameter> parameters
    ) {
        Objects.requireNonNull(actionType, "actionType");
        Objects.requireNonNull(parameterType, "parameterType");
        Objects.requireNonNull(scope, "scope");
        Objects.requireNonNull(blockingType, "blockingType");
        Objects.requireNonNull(parameters, "parameters");

        Optional<ActionDefinition<P>> resolved = registry.resolve(
            actionType,
            parameterType
        );
        if (resolved.isEmpty()) {
            if (registry.actionTypes().contains(actionType)) {
                return rejected(
                    "ACTION_PARAMETER_TYPE_MISMATCH",
                    "/actionParameters",
                    "Registered Action parameter type does not match"
                );
            }
            return rejected(
                "UNREGISTERED_ACTION",
                "/actionType",
                "Action type is not registered"
            );
        }

        ActionDefinition<P> definition = resolved.orElseThrow();
        if (!definition.allowedScopes().contains(scope)) {
            return rejected(
                "ACTION_SCOPE_NOT_ALLOWED",
                "",
                "Action scope is not allowed"
            );
        }
        if (!definition.allowedBlockingTypes().contains(blockingType)) {
            return rejected(
                "ACTION_BLOCKING_TYPE_NOT_ALLOWED",
                "/blockingType",
                "Action blocking type is not allowed"
            );
        }
        return definition.adapt(parameters);
    }

    private static <P> ActionParameterAdapter.Adaptation<P> rejected(
        String code,
        String path,
        String description
    ) {
        return new ActionParameterAdapter.Rejected<>(new ValidationIssue(
            code,
            ValidationSeverity.ERROR,
            path,
            description,
            REQUIREMENT_ID
        ));
    }
}
