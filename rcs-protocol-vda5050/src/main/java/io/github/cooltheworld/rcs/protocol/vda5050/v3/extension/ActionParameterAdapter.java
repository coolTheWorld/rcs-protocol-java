package io.github.cooltheworld.rcs.protocol.vda5050.v3.extension;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.action.ActionParameter;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.ValidationIssue;
import java.util.List;
import java.util.Objects;

/**
 * 把动作参数线路值适配为调用方强类型参数的纯函数。
 *
 * <p>普通参数缺失、重复、类型或范围问题应返回 {@link Rejected}，不得用异常表达。</p>
 *
 * @param <P> 调用方参数类型
 */
@FunctionalInterface
public interface ActionParameterAdapter<P> {
    /**
     * 适配不可变动作参数列表。
     *
     * @param parameters 不可变参数列表
     * @return 非空封闭适配结果
     */
    Adaptation<P> adapt(List<ActionParameter> parameters);

    /**
     * 动作参数适配的封闭结果。
     *
     * @param <P> 调用方参数类型
     */
    sealed interface Adaptation<P> permits Adapted, Rejected {
        /** @return 成功适配时返回 {@code true} */
        boolean isAdapted();
    }

    /**
     * 成功适配的调用方参数。
     *
     * @param parameters 强类型参数
     * @param <P> 调用方参数类型
     */
    record Adapted<P>(P parameters) implements Adaptation<P> {
        public Adapted {
            Objects.requireNonNull(parameters, "parameters");
        }

        @Override
        public boolean isAdapted() {
            return true;
        }
    }

    /**
     * 普通协议输入问题的结构化拒绝。
     *
     * @param issue 安全且不可变的校验问题
     * @param <P> 期望的调用方参数类型
     */
    record Rejected<P>(ValidationIssue issue) implements Adaptation<P> {
        public Rejected {
            Objects.requireNonNull(issue, "issue");
        }

        @Override
        public boolean isAdapted() {
            return false;
        }
    }
}
