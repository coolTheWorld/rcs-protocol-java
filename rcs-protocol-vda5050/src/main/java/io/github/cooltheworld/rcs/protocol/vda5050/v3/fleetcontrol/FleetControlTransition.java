package io.github.cooltheworld.rcs.protocol.vda5050.v3.fleetcontrol;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.fleetcontrol.effect.FleetControlEffect;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.ValidationIssue;
import java.util.List;
import java.util.Objects;

/**
 * Fleet Control 的确定性转换结果。
 *
 * @param state 新的不可变状态
 * @param effects 与状态原子提交的角色专属效果
 * @param issues 结合当前会话得到的不可变问题列表
 */
public record FleetControlTransition(
    FleetControlState state,
    List<FleetControlEffect> effects,
    List<ValidationIssue> issues
) {
    public FleetControlTransition {
        state = Objects.requireNonNull(state, "state");
        effects = List.copyOf(Objects.requireNonNull(effects, "effects"));
        issues = List.copyOf(Objects.requireNonNull(issues, "issues"));
    }
}
