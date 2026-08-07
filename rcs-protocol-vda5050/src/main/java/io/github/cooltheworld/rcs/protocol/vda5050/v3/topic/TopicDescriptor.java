package io.github.cooltheworld.rcs.protocol.vda5050.v3.topic;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** VDA 5050 v3.0.0 标准 Topic 的不可变传输语义。 */
public final class TopicDescriptor {
    private static final TopicDescriptor ORDER = descriptor(
        TopicName.ORDER,
        Set.of(TopicParticipant.FLEET_CONTROL),
        Set.of(TopicParticipant.MOBILE_ROBOT),
        TopicQos.AT_MOST_ONCE,
        false,
        LastWillPolicy.NOT_REQUIRED
    );
    private static final TopicDescriptor INSTANT_ACTIONS = descriptor(
        TopicName.INSTANT_ACTIONS,
        Set.of(TopicParticipant.FLEET_CONTROL),
        Set.of(TopicParticipant.MOBILE_ROBOT),
        TopicQos.AT_MOST_ONCE,
        false,
        LastWillPolicy.NOT_REQUIRED
    );
    private static final TopicDescriptor STATE = descriptor(
        TopicName.STATE,
        Set.of(TopicParticipant.MOBILE_ROBOT),
        Set.of(TopicParticipant.FLEET_CONTROL),
        TopicQos.AT_MOST_ONCE,
        false,
        LastWillPolicy.NOT_REQUIRED
    );
    private static final TopicDescriptor VISUALIZATION = descriptor(
        TopicName.VISUALIZATION,
        Set.of(TopicParticipant.MOBILE_ROBOT),
        Set.of(TopicParticipant.VISUALIZATION_SYSTEM),
        TopicQos.AT_MOST_ONCE,
        false,
        LastWillPolicy.NOT_REQUIRED
    );
    private static final TopicDescriptor CONNECTION = descriptor(
        TopicName.CONNECTION,
        Set.of(TopicParticipant.MOBILE_ROBOT, TopicParticipant.MQTT_BROKER),
        Set.of(TopicParticipant.FLEET_CONTROL),
        TopicQos.AT_LEAST_ONCE,
        true,
        LastWillPolicy.MOBILE_ROBOT_CONNECTION_BROKEN
    );
    private static final TopicDescriptor FACTSHEET = descriptor(
        TopicName.FACTSHEET,
        Set.of(TopicParticipant.MOBILE_ROBOT),
        Set.of(TopicParticipant.FLEET_CONTROL),
        TopicQos.AT_MOST_ONCE,
        true,
        LastWillPolicy.NOT_REQUIRED
    );
    private static final TopicDescriptor ZONE_SET = descriptor(
        TopicName.ZONE_SET,
        Set.of(TopicParticipant.FLEET_CONTROL),
        Set.of(TopicParticipant.MOBILE_ROBOT),
        TopicQos.AT_MOST_ONCE,
        false,
        LastWillPolicy.NOT_REQUIRED
    );
    private static final TopicDescriptor RESPONSES = descriptor(
        TopicName.RESPONSES,
        Set.of(TopicParticipant.FLEET_CONTROL),
        Set.of(TopicParticipant.MOBILE_ROBOT),
        TopicQos.AT_MOST_ONCE,
        false,
        LastWillPolicy.NOT_REQUIRED
    );
    private static final List<TopicDescriptor> STANDARD_TOPICS = List.of(
        ORDER,
        INSTANT_ACTIONS,
        STATE,
        VISUALIZATION,
        CONNECTION,
        FACTSHEET,
        ZONE_SET,
        RESPONSES
    );
    private static final Map<TopicName, TopicDescriptor> DESCRIPTORS_BY_NAME = Map.of(
        TopicName.ORDER, ORDER,
        TopicName.INSTANT_ACTIONS, INSTANT_ACTIONS,
        TopicName.STATE, STATE,
        TopicName.VISUALIZATION, VISUALIZATION,
        TopicName.CONNECTION, CONNECTION,
        TopicName.FACTSHEET, FACTSHEET,
        TopicName.ZONE_SET, ZONE_SET,
        TopicName.RESPONSES, RESPONSES
    );

    private final TopicName topicName;
    private final Set<TopicParticipant> publishers;
    private final Set<TopicParticipant> subscribers;
    private final TopicQos qos;
    private final boolean retained;
    private final LastWillPolicy lastWillPolicy;

    private TopicDescriptor(
        TopicName topicName,
        Set<TopicParticipant> publishers,
        Set<TopicParticipant> subscribers,
        TopicQos qos,
        boolean retained,
        LastWillPolicy lastWillPolicy
    ) {
        this.topicName = Objects.requireNonNull(topicName, "topicName");
        this.publishers = Set.copyOf(Objects.requireNonNull(publishers, "publishers"));
        this.subscribers = Set.copyOf(Objects.requireNonNull(subscribers, "subscribers"));
        this.qos = Objects.requireNonNull(qos, "qos");
        this.retained = retained;
        this.lastWillPolicy = Objects.requireNonNull(lastWillPolicy, "lastWillPolicy");
    }

    private static TopicDescriptor descriptor(
        TopicName topicName,
        Set<TopicParticipant> publishers,
        Set<TopicParticipant> subscribers,
        TopicQos qos,
        boolean retained,
        LastWillPolicy lastWillPolicy
    ) {
        return new TopicDescriptor(
            topicName,
            publishers,
            subscribers,
            qos,
            retained,
            lastWillPolicy
        );
    }

    /**
     * 返回指定标准 Topic 的规范描述符。
     *
     * @param topicName 标准 Topic 名称
     * @return 稳定的不可变描述符
     */
    public static TopicDescriptor forTopic(TopicName topicName) {
        return DESCRIPTORS_BY_NAME.get(Objects.requireNonNull(topicName, "topicName"));
    }

    /**
     * 返回全部八个标准 Topic 的不可变描述符列表。
     *
     * @return 规范声明顺序的标准 Topic 描述符
     */
    public static List<TopicDescriptor> standardTopics() {
        return STANDARD_TOPICS;
    }

    public TopicName topicName() {
        return topicName;
    }

    public Set<TopicParticipant> publishers() {
        return publishers;
    }

    public Set<TopicParticipant> subscribers() {
        return subscribers;
    }

    public TopicQos qos() {
        return qos;
    }

    public boolean retained() {
        return retained;
    }

    public LastWillPolicy lastWillPolicy() {
        return lastWillPolicy;
    }
}
