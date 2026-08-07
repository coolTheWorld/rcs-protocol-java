package io.github.cooltheworld.rcs.protocol.vda5050.v3.topic;

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
}
