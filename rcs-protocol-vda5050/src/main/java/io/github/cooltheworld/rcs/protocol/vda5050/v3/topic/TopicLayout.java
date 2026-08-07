package io.github.cooltheworld.rcs.protocol.vda5050.v3.topic;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.RobotIdentity;
import java.util.Objects;

/**
 * 标准 Topic 语义地址与部署 MQTT 路径之间的双向映射。
 *
 * <p>实现可以调整路径层级，但只能使用 {@link TopicName} 表达八个标准 Topic；QoS、保留和
 * Last Will 语义始终由 {@link TopicDescriptor} 决定。入站核心代码必须使用
 * {@link #parseForRobot(TopicLayout, String, RobotIdentity)}，出站核心代码必须使用
 * {@link #format(TopicLayout, TopicAddress)}，以验证布局不会改变标准名称、地址或身份语义。</p>
 *
 * <p>MQTT Broker 或调用方负责在传入本接口前施加部署级 Topic 字节和长度上限；本接口不承担
 * 传输收包。默认布局避免按无限层级分割路径。</p>
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
     * 格式化路径，并验证自定义布局对地址的往返与标准 Topic 名称保持不变。
     *
     * @param topicLayout 部署选用的 Topic 布局
     * @param address 标准 Topic 地址
     * @return 经验证的 MQTT Topic 路径
     * @throws IllegalArgumentException 布局改变标准 Topic 语义时抛出
     */
    static String format(TopicLayout topicLayout, TopicAddress address) {
        Objects.requireNonNull(topicLayout, "topicLayout");
        Objects.requireNonNull(address, "address");
        String topicPath = Objects.requireNonNull(
            topicLayout.format(address),
            "formattedTopicPath"
        );
        TopicAddress parsedAddress = Objects.requireNonNull(
            topicLayout.parse(topicPath),
            "parsedTopicAddress"
        );
        if (!address.equals(parsedAddress)) {
            throw new IllegalArgumentException("Topic layout does not round trip the address");
        }
        requireStandardTopicName(topicPath, address.topicName());
        return topicPath;
    }

    /**
     * 解析路径，并验证自定义布局、标准 Topic 名称与消息头身份的完整性。
     *
     * @param topicLayout 部署选用的 Topic 布局
     * @param topicPath MQTT Topic 路径
     * @param expectedRobotIdentity 消息头中已提取的 Robot Identity
     * @return 路径中的标准 Topic 名称
     * @throws IllegalArgumentException 路径语义或身份不一致时抛出
     */
    static TopicName parseForRobot(
        TopicLayout topicLayout,
        String topicPath,
        RobotIdentity expectedRobotIdentity
    ) {
        Objects.requireNonNull(topicLayout, "topicLayout");
        Objects.requireNonNull(topicPath, "topicPath");
        Objects.requireNonNull(expectedRobotIdentity, "expectedRobotIdentity");
        TopicAddress address = Objects.requireNonNull(
            topicLayout.parse(topicPath),
            "parsedTopicAddress"
        );
        String canonicalTopicPath = Objects.requireNonNull(
            topicLayout.format(address),
            "formattedTopicPath"
        );
        if (!topicPath.equals(canonicalTopicPath)) {
            throw new IllegalArgumentException("Topic layout does not round trip the path");
        }
        requireStandardTopicName(topicPath, address.topicName());
        if (!address.robotIdentity().equals(expectedRobotIdentity)) {
            throw new IllegalArgumentException(
                "Topic identity does not match the expected robot identity"
            );
        }
        return address.topicName();
    }

    private static void requireStandardTopicName(String topicPath, TopicName topicName) {
        String wireName = topicName.wireName();
        int levelStart = 0;
        while (true) {
            int levelEnd = topicPath.indexOf('/', levelStart);
            int effectiveEnd = levelEnd < 0 ? topicPath.length() : levelEnd;
            if (effectiveEnd - levelStart == wireName.length()
                && topicPath.regionMatches(levelStart, wireName, 0, wireName.length())) {
                return;
            }
            if (levelEnd < 0) {
                throw new IllegalArgumentException(
                    "Topic layout omits the standard Topic name"
                );
            }
            levelStart = levelEnd + 1;
        }
    }
}
