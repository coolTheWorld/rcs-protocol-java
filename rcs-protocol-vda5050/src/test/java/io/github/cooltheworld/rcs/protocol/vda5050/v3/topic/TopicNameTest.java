package io.github.cooltheworld.rcs.protocol.vda5050.v3.topic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

final class TopicNameTest {
    @Test
    @DisplayName("[VDA3-SHARED-008] 拒绝上下文只使用八个标准 Topic 名称")
    void exposesAllStandardWireNames() {
        List<String> wireNames = Arrays.stream(TopicName.values())
            .map(TopicName::wireName)
            .toList();

        assertEquals(
            List.of(
                "order",
                "instantActions",
                "state",
                "visualization",
                "connection",
                "factsheet",
                "zoneSet",
                "responses"
            ),
            wireNames
        );
    }
}
