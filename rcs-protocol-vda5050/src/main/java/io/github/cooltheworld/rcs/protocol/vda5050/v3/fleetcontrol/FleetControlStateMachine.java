package io.github.cooltheworld.rcs.protocol.vda5050.v3.fleetcontrol;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.fleetcontrol.event.FleetControlEvent;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.fleetcontrol.internal.DefaultFleetControlStateMachine;

/** Fleet Control 角色专属的纯状态机接口。 */
public interface FleetControlStateMachine {
    /** @return 无状态且线程安全的默认 Fleet Control 状态机 */
    static FleetControlStateMachine createDefault() {
        return DefaultFleetControlStateMachine.instance();
    }

    /**
     * 确定性执行 {@code state + event -> state + effects + issues}。
     *
     * @param state 当前会话状态
     * @param event 不可变输入事件
     * @return 新状态、效果与问题
     */
    FleetControlTransition transition(
        FleetControlState state,
        FleetControlEvent event
    );
}
