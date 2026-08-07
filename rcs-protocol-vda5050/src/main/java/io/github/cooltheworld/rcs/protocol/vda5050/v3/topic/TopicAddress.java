package io.github.cooltheworld.rcs.protocol.vda5050.v3.topic;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.RobotIdentity;
import java.util.Objects;

/** 由 Robot Identity 与标准 Topic 名称组成的路径语义地址。 */
public record TopicAddress(RobotIdentity robotIdentity, TopicName topicName) {
    /** 验证 Topic 地址的必填组成部分。 */
    public TopicAddress {
        robotIdentity = Objects.requireNonNull(robotIdentity, "robotIdentity");
        topicName = Objects.requireNonNull(topicName, "topicName");
    }
}
