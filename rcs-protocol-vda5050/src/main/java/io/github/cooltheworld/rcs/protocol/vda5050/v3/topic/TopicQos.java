package io.github.cooltheworld.rcs.protocol.vda5050.v3.topic;

/** 不依赖具体 MQTT 客户端的 VDA 5050 Topic QoS。 */
public enum TopicQos {
    AT_MOST_ONCE(0L),
    AT_LEAST_ONCE(1L);

    private final Long level;

    TopicQos(Long level) {
        this.level = level;
    }

    /**
     * 返回 MQTT QoS 的数值级别。
     *
     * @return 协议使用的 QoS 数值
     */
    public Long level() {
        return level;
    }
}
