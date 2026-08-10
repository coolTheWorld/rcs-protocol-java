package io.github.cooltheworld.rcs.protocol.vda5050.v3.mobilerobot;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.mobilerobot.effect.MobileRobotEffect;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.ValidationIssue;
import java.util.List;
import java.util.Objects;

/** Mobile Robot 的确定性转换结果。 */
public record MobileRobotTransition(
    MobileRobotState state,
    List<MobileRobotEffect> effects,
    List<ValidationIssue> issues
) {
    public MobileRobotTransition {
        state = Objects.requireNonNull(state, "state");
        effects = List.copyOf(Objects.requireNonNull(effects, "effects"));
        issues = List.copyOf(Objects.requireNonNull(issues, "issues"));
    }
}
