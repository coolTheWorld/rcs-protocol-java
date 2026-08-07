package io.github.cooltheworld.rcs.protocol.vda5050.v3.topic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.RobotIdentity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

final class DefaultTopicLayoutTest {
    private final TopicLayout topicLayout = DefaultTopicLayout.standard();

    @Test
    @DisplayName("[VDA3-SHARED-011] 默认布局精确保留身份并往返标准 Topic")
    void formatsAndParsesTheSuggestedVda5050Layout() {
        TopicAddress address = new TopicAddress(
            new RobotIdentity("Café", "SN-01"),
            TopicName.ORDER
        );

        String topicPath = topicLayout.format(address);

        assertEquals("vda5050/v3/Café/SN-01/order", topicPath);
        assertEquals(address, topicLayout.parse(topicPath));
        assertEquals(TopicName.ORDER, topicLayout.parse(topicPath, address.robotIdentity()));
    }

    @Test
    @DisplayName("[VDA3-SHARED-011] 默认布局拒绝非标准层级、不安全身份与未知 Topic")
    void rejectsUnsafeOrInvalidTopicPaths() {
        assertThrows(
            IllegalArgumentException.class,
            () -> topicLayout.parse("vda5050/v3/ACME/SN-01/order/extra")
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> topicLayout.parse("other/v3/ACME/SN-01/order")
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> topicLayout.parse("vda5050/v2/ACME/SN-01/order")
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> topicLayout.parse("vda5050/v3/AC+ME/SN-01/order")
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> topicLayout.parse("vda5050/v3/ACME/SN 01/order")
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> topicLayout.parse("vda5050/v3/ACME/SN-01/unknown")
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-011] 解析出的身份必须与消息头身份逐字符一致")
    void rejectsAParsedIdentityThatDoesNotMatchTheExpectedMessageHeaderIdentity() {
        assertThrows(
            IllegalArgumentException.class,
            () -> topicLayout.parse(
                "vda5050/v3/ACME/SN-01/state",
                new RobotIdentity("ACME", "SN-02")
            )
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-011] 自定义布局保持标准 Topic 语义并复用身份一致性校验")
    void supportsCustomLayoutsWithoutIntroducingNewTopicSemantics() {
        TopicLayout customLayout = new TopicLayout() {
            @Override
            public String format(TopicAddress address) {
                return "tenant/" + address.robotIdentity().manufacturer()
                    + "/" + address.topicName().wireName()
                    + "/" + address.robotIdentity().serialNumber();
            }

            @Override
            public TopicAddress parse(String topicPath) {
                String[] levels = topicPath.split("/", -1);
                if (levels.length != 4 || !"tenant".equals(levels[0])) {
                    throw new IllegalArgumentException("Invalid custom topic layout");
                }
                return new TopicAddress(
                    new RobotIdentity(levels[1], levels[3]),
                    TopicName.fromWireName(levels[2])
                );
            }
        };
        RobotIdentity identity = new RobotIdentity("ACME", "SN-01");
        TopicAddress address = new TopicAddress(identity, TopicName.STATE);

        String topicPath = customLayout.format(address);

        assertEquals("tenant/ACME/state/SN-01", topicPath);
        assertEquals(address, customLayout.parse(topicPath));
        assertEquals(TopicName.STATE, customLayout.parse(topicPath, identity));
        assertThrows(
            IllegalArgumentException.class,
            () -> customLayout.parse(topicPath, new RobotIdentity("ACME", "SN-02"))
        );
    }
}
