package io.github.cooltheworld.rcs.protocol.vda5050.v3.topic;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.RobotIdentity;
import java.util.Objects;

/**
 * 标准 Topic 语义地址与部署 MQTT 路径之间的双向映射。
 *
 * <p>实现可以调整路径层级，但只能使用 {@link TopicName} 表达八个标准 Topic；QoS、保留和
 * Last Will 语义始终由 {@link TopicDescriptor} 决定。</p>
 */
public interface TopicLayout {
    /**
     * 将标准 Topic 地址格式化为部署使用的 MQTT 路径。
     *
     * @param address 标准 Topic 地址
     * @return MQTT Topic 路径
     */
    String format(TopicAddress address);

    /**
     * 将 MQTT Topic 路径解析为标准 Topic 地址。
     *
     * @param topicPath MQTT Topic 路径
     * @return 含有精确 Robot Identity 的标准 Topic 地址
     */
    TopicAddress parse(String topicPath);

    /**
     * 解析路径并验证其 Robot Identity 与消息头身份逐字符一致。
     *
     * @param topicPath MQTT Topic 路径
     * @param expectedRobotIdentity 消息头中已提取的 Robot Identity
     * @return 路径中的标准 Topic 名称
     * @throws IllegalArgumentException 路径身份与消息头身份不一致时抛出
     */
    default TopicName parse(String topicPath, RobotIdentity expectedRobotIdentity) {
        Objects.requireNonNull(expectedRobotIdentity, "expectedRobotIdentity");
        TopicAddress address = Objects.requireNonNull(parse(topicPath), "parsedTopicAddress");
        if (!address.robotIdentity().equals(expectedRobotIdentity)) {
            throw new IllegalArgumentException(
                "Topic identity does not match the expected robot identity"
            );
        }
        return address.topicName();
    }
}
