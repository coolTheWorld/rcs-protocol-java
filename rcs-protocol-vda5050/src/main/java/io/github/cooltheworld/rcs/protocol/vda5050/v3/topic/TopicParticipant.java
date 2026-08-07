package io.github.cooltheworld.rcs.protocol.vda5050.v3.topic;

/**
 * VDA 5050 Topic 的传输参与方。
 *
 * <p>该枚举只表达消息在 MQTT 传输中的发布或订阅方，不表示可启用状态机的运行时协议角色。</p>
 */
public enum TopicParticipant {
    FLEET_CONTROL,
    MOBILE_ROBOT,
    MQTT_BROKER,
    VISUALIZATION_SYSTEM
}
