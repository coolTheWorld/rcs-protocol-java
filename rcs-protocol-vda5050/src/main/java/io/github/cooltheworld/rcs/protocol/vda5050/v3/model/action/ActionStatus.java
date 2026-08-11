package io.github.cooltheworld.rcs.protocol.vda5050.v3.model.action;

/** VDA 5050 Action State（动作状态）的封闭状态值。 */
public enum ActionStatus {
    WAITING,
    INITIALIZING,
    RUNNING,
    PAUSED,
    RETRIABLE,
    FINISHED,
    FAILED
}
