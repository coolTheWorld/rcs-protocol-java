package io.github.cooltheworld.rcs.protocol.vda5050.v3.topic;

/** 标准 Topic 是否承载 Mobile Robot 的 MQTT Last Will 语义。 */
public enum LastWillPolicy {
    NOT_REQUIRED,
    MOBILE_ROBOT_CONNECTION_BROKEN
}
