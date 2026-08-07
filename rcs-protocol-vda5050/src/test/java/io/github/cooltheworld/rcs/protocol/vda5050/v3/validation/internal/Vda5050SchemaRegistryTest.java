package io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.internal;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.topic.TopicName;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

final class Vda5050SchemaRegistryTest {
    @ParameterizedTest(name = "[{1}] {0} Schema 可以离线编译")
    @MethodSource("topics")
    void compilesEveryBundledSchema(
        TopicName topic,
        String requirementId
    ) {
        Vda5050SchemaRegistry registry = Vda5050SchemaRegistry.create();

        assertNotNull(registry.schemaFor(topic));
    }

    private static Stream<Arguments> topics() {
        return Stream.of(
            Arguments.of(TopicName.CONNECTION, "VDA3-CONNECTION-001"),
            Arguments.of(TopicName.FACTSHEET, "VDA3-FACTSHEET-001"),
            Arguments.of(TopicName.INSTANT_ACTIONS, "VDA3-INSTANT_ACTIONS-001"),
            Arguments.of(TopicName.ORDER, "VDA3-ORDER-001"),
            Arguments.of(TopicName.RESPONSES, "VDA3-RESPONSES-001"),
            Arguments.of(TopicName.STATE, "VDA3-STATE-001"),
            Arguments.of(TopicName.VISUALIZATION, "VDA3-VISUALIZATION-001"),
            Arguments.of(TopicName.ZONE_SET, "VDA3-ZONE_SET-001")
        );
    }
}
