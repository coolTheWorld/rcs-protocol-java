package io.github.cooltheworld.rcs.protocol.vda5050.v3.topic;

import java.util.Objects;

/** VDA 5050 v3.0.0 定义的八个标准 Topic 名称。 */
public enum TopicName {
    ORDER("order"),
    INSTANT_ACTIONS("instantActions"),
    STATE("state"),
    VISUALIZATION("visualization"),
    CONNECTION("connection"),
    FACTSHEET("factsheet"),
    ZONE_SET("zoneSet"),
    RESPONSES("responses");

    private final String wireName;

    TopicName(String wireName) {
        this.wireName = wireName;
    }

    /**
     * 返回规范线路 Topic 的末级名称。
     *
     * @return 区分大小写的标准 Topic 名称
     */
    public String wireName() {
        return wireName;
    }

    /**
     * 解析一个区分大小写的标准 Topic 末级名称。
     *
     * @param wireName 标准 Topic 名称
     * @return 对应的标准 Topic
     * @throws IllegalArgumentException 名称不属于八个标准 Topic 时抛出
     */
    public static TopicName fromWireName(String wireName) {
        Objects.requireNonNull(wireName, "wireName");
        for (TopicName topicName : values()) {
            if (topicName.wireName.equals(wireName)) {
                return topicName;
            }
        }
        throw new IllegalArgumentException("Unknown standard Topic name");
    }
}
