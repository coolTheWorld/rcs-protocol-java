package io.github.cooltheworld.rcs.protocol.vda5050.v3.mobilerobot;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.mobilerobot.internal.DefaultMobileRobotStateMachine;

/** Mobile Robot 角色专属的纯状态机接口。 */
public interface MobileRobotStateMachine {
    /** @return 无状态且线程安全的默认 Mobile Robot 状态机 */
    static MobileRobotStateMachine createDefault() {
        return DefaultMobileRobotStateMachine.instance();
    }

    /**
     * 确定性执行 {@code state + event -> state + effects + issues}。
     *
     * @param state 当前会话状态
     * @param event 不可变输入事件
     * @return 新状态、效果与问题
     */
    MobileRobotTransition transition(
        MobileRobotState state,
        MobileRobotEvent event
    );
}
