package io.github.cooltheworld.rcs.protocol.vda5050.v3.topic;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.RobotIdentity;
import java.util.Objects;

/** VDA 5050 v3.0.0 为本地 MQTT Broker 建议的默认 Topic 布局。 */
public final class DefaultTopicLayout implements TopicLayout {
    private static final String INTERFACE_NAME = "vda5050";
    private static final String MAJOR_VERSION = "v3";
    private static final DefaultTopicLayout STANDARD = new DefaultTopicLayout();

    private DefaultTopicLayout() {
    }

    /**
     * 返回无状态的默认 Topic 布局。
     *
     * @return 标准 VDA 5050 v3 Topic 布局
     */
    public static DefaultTopicLayout standard() {
        return STANDARD;
    }

    @Override
    public String format(TopicAddress address) {
        Objects.requireNonNull(address, "address");
        RobotIdentity robotIdentity = address.robotIdentity();
        return INTERFACE_NAME
            + "/" + MAJOR_VERSION
            + "/" + robotIdentity.manufacturer()
            + "/" + robotIdentity.serialNumber()
            + "/" + address.topicName().wireName();
    }

    @Override
    public TopicAddress parse(String topicPath) {
        Objects.requireNonNull(topicPath, "topicPath");
        String[] levels = topicPath.split("/", 6);
        if (levels.length != 5) {
            throw new IllegalArgumentException("Topic path must contain exactly five levels");
        }
        if (!INTERFACE_NAME.equals(levels[0]) || !MAJOR_VERSION.equals(levels[1])) {
            throw new IllegalArgumentException("Topic path uses an unsupported VDA 5050 layout");
        }
        return new TopicAddress(
            new RobotIdentity(levels[2], levels[3]),
            TopicName.fromWireName(levels[4])
        );
    }
}
