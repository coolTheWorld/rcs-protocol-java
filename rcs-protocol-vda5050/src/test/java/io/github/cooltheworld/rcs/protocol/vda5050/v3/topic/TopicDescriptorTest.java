package io.github.cooltheworld.rcs.protocol.vda5050.v3.topic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

final class TopicDescriptorTest {
    @Test
    @DisplayName("[VDA3-SHARED-011] 八个标准 Topic 具有不可变的传输语义")
    void exposesRequiredTransportMetadataForEveryStandardTopic() {
        Map<TopicName, TopicDescriptor> descriptors = TopicDescriptor.standardTopics()
            .stream()
            .collect(Collectors.toMap(TopicDescriptor::topicName, descriptor -> descriptor));

        assertEquals(Set.of(TopicName.values()), descriptors.keySet());
        assertDescriptor(
            descriptors.get(TopicName.ORDER),
            Set.of(TopicParticipant.FLEET_CONTROL),
            Set.of(TopicParticipant.MOBILE_ROBOT),
            TopicQos.AT_MOST_ONCE,
            RetainedPolicy.UNSPECIFIED,
            LastWillPolicy.NOT_REQUIRED
        );
        assertDescriptor(
            descriptors.get(TopicName.INSTANT_ACTIONS),
            Set.of(TopicParticipant.FLEET_CONTROL),
            Set.of(TopicParticipant.MOBILE_ROBOT),
            TopicQos.AT_MOST_ONCE,
            RetainedPolicy.UNSPECIFIED,
            LastWillPolicy.NOT_REQUIRED
        );
        assertDescriptor(
            descriptors.get(TopicName.STATE),
            Set.of(TopicParticipant.MOBILE_ROBOT),
            Set.of(TopicParticipant.FLEET_CONTROL),
            TopicQos.AT_MOST_ONCE,
            RetainedPolicy.UNSPECIFIED,
            LastWillPolicy.NOT_REQUIRED
        );
        assertDescriptor(
            descriptors.get(TopicName.VISUALIZATION),
            Set.of(TopicParticipant.MOBILE_ROBOT),
            Set.of(TopicParticipant.VISUALIZATION_SYSTEM),
            TopicQos.AT_MOST_ONCE,
            RetainedPolicy.UNSPECIFIED,
            LastWillPolicy.NOT_REQUIRED
        );
        assertDescriptor(
            descriptors.get(TopicName.CONNECTION),
            Set.of(TopicParticipant.MOBILE_ROBOT, TopicParticipant.MQTT_BROKER),
            Set.of(TopicParticipant.FLEET_CONTROL),
            TopicQos.AT_LEAST_ONCE,
            RetainedPolicy.REQUIRED,
            LastWillPolicy.MOBILE_ROBOT_CONNECTION_BROKEN
        );
        assertDescriptor(
            descriptors.get(TopicName.FACTSHEET),
            Set.of(TopicParticipant.MOBILE_ROBOT),
            Set.of(TopicParticipant.FLEET_CONTROL),
            TopicQos.AT_MOST_ONCE,
            RetainedPolicy.REQUIRED,
            LastWillPolicy.NOT_REQUIRED
        );
        assertDescriptor(
            descriptors.get(TopicName.ZONE_SET),
            Set.of(TopicParticipant.FLEET_CONTROL),
            Set.of(TopicParticipant.MOBILE_ROBOT),
            TopicQos.AT_MOST_ONCE,
            RetainedPolicy.UNSPECIFIED,
            LastWillPolicy.NOT_REQUIRED
        );
        assertDescriptor(
            descriptors.get(TopicName.RESPONSES),
            Set.of(TopicParticipant.FLEET_CONTROL),
            Set.of(TopicParticipant.MOBILE_ROBOT),
            TopicQos.AT_MOST_ONCE,
            RetainedPolicy.UNSPECIFIED,
            LastWillPolicy.NOT_REQUIRED
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-011] 描述符按 Topic 名称返回稳定实例且不暴露可变参与方集合")
    void exposesCanonicalImmutableDescriptors() {
        TopicDescriptor descriptor = TopicDescriptor.forTopic(TopicName.CONNECTION);

        assertSame(descriptor, TopicDescriptor.forTopic(TopicName.CONNECTION));
        assertThrows(
            UnsupportedOperationException.class,
            () -> TopicDescriptor.standardTopics().add(descriptor)
        );
        assertThrows(
            UnsupportedOperationException.class,
            () -> descriptor.publishers().add(TopicParticipant.FLEET_CONTROL)
        );
        assertThrows(NullPointerException.class, () -> TopicDescriptor.forTopic(null));
    }

    @Test
    @DisplayName("[VDA3-SHARED-011] 协议自有 QoS 值精确映射 MQTT 数值级别")
    void exposesTheNormativeMqttQosLevels() {
        assertEquals(0L, TopicQos.AT_MOST_ONCE.level());
        assertEquals(1L, TopicQos.AT_LEAST_ONCE.level());
    }

    private static void assertDescriptor(
        TopicDescriptor descriptor,
        Set<TopicParticipant> publishers,
        Set<TopicParticipant> subscribers,
        TopicQos qos,
        RetainedPolicy retainedPolicy,
        LastWillPolicy lastWillPolicy
    ) {
        assertEquals(publishers, descriptor.publishers());
        assertEquals(subscribers, descriptor.subscribers());
        assertEquals(qos, descriptor.qos());
        assertEquals(retainedPolicy, descriptor.retainedPolicy());
        assertEquals(lastWillPolicy, descriptor.lastWillPolicy());
    }
}
