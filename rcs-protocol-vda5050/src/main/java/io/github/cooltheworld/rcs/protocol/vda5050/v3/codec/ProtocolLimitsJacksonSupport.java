package io.github.cooltheworld.rcs.protocol.vda5050.v3.codec;

import static io.github.cooltheworld.rcs.protocol.vda5050.v3.codec.FactsheetFragmentJacksonSupport.putOptional;
import static io.github.cooltheworld.rcs.protocol.vda5050.v3.codec.FactsheetFragmentJacksonSupport.readObject;
import static io.github.cooltheworld.rcs.protocol.vda5050.v3.codec.FactsheetFragmentJacksonSupport.readOptional;
import static io.github.cooltheworld.rcs.protocol.vda5050.v3.codec.FactsheetFragmentJacksonSupport.readRequired;
import static io.github.cooltheworld.rcs.protocol.vda5050.v3.codec.FactsheetFragmentJacksonSupport.requireObjectMapper;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.internal.ExtensionFieldsJacksonSupport;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.MaximumArrayLengths;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.MaximumStringLengths;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.ProtocolLimits;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.ProtocolTiming;
import java.io.IOException;
import java.util.Set;

/** Factsheet Protocol Limits 子对象的包内 Jackson 映射支持。 */
final class ProtocolLimitsJacksonSupport {
    private static final Set<String> PROTOCOL_LIMITS_FIELDS = Set.of(
        "maximumStringLengths",
        "maximumArrayLengths",
        "timing"
    );
    private static final Set<String> STRING_FIELDS = Set.of(
        "maximumMessageLength",
        "maximumTopicSerialLength",
        "maximumTopicElementLength",
        "maximumIdLength",
        "idNumericalOnly",
        "maximumLoadIdLength"
    );
    private static final Set<String> ARRAY_FIELDS = Set.of(
        "order.nodes",
        "order.edges",
        "node.actions",
        "edge.actions",
        "actions.actionsParameters",
        "instantActions",
        "trajectory.knotVector",
        "trajectory.controlPoints",
        "zoneSet.zones",
        "state.nodeStates",
        "state.edgeStates",
        "state.loads",
        "state.actionStates",
        "state.instantActionStates",
        "state.zoneActionStates",
        "state.errors",
        "state.information",
        "error.errorReferences",
        "information.infoReferences"
    );
    private static final Set<String> TIMING_FIELDS = Set.of(
        "minimumOrderInterval",
        "minimumStateInterval",
        "defaultStateInterval",
        "visualizationInterval"
    );

    private ProtocolLimitsJacksonSupport() {}

    static void register(SimpleModule module) {
        module.addSerializer(ProtocolLimits.class, new ProtocolLimitsSerializer());
        module.addDeserializer(
            ProtocolLimits.class,
            new ProtocolLimitsDeserializer()
        );
        module.addSerializer(
            MaximumStringLengths.class,
            new MaximumStringLengthsSerializer()
        );
        module.addDeserializer(
            MaximumStringLengths.class,
            new MaximumStringLengthsDeserializer()
        );
        module.addSerializer(
            MaximumArrayLengths.class,
            new MaximumArrayLengthsSerializer()
        );
        module.addDeserializer(
            MaximumArrayLengths.class,
            new MaximumArrayLengthsDeserializer()
        );
        module.addSerializer(ProtocolTiming.class, new ProtocolTimingSerializer());
        module.addDeserializer(
            ProtocolTiming.class,
            new ProtocolTimingDeserializer()
        );
    }

    private static final class ProtocolLimitsSerializer
        extends StdSerializer<ProtocolLimits> {
        private ProtocolLimitsSerializer() {
            super(ProtocolLimits.class);
        }

        @Override
        public void serialize(
            ProtocolLimits value,
            JsonGenerator generator,
            SerializerProvider provider
        ) throws IOException {
            ObjectMapper mapper = requireObjectMapper(generator);
            ObjectNode target = mapper.createObjectNode();
            target.set(
                "maximumStringLengths",
                mapper.valueToTree(value.maximumStringLengths())
            );
            target.set(
                "maximumArrayLengths",
                mapper.valueToTree(value.maximumArrayLengths())
            );
            target.set("timing", mapper.valueToTree(value.timing()));
            ExtensionFieldsJacksonSupport.merge(
                mapper,
                target,
                value.extensionFields(),
                PROTOCOL_LIMITS_FIELDS
            );
            generator.writeTree(target);
        }
    }

    private static final class ProtocolLimitsDeserializer
        extends StdDeserializer<ProtocolLimits> {
        private ProtocolLimitsDeserializer() {
            super(ProtocolLimits.class);
        }

        @Override
        public ProtocolLimits deserialize(
            JsonParser parser,
            DeserializationContext context
        ) throws IOException {
            ObjectMapper mapper = requireObjectMapper(parser);
            ObjectNode object = readObject(
                mapper,
                parser,
                context,
                ProtocolLimits.class,
                "Protocol limits"
            );
            try {
                return ProtocolLimits.builder()
                    .maximumStringLengths(readRequired(
                        object,
                        "maximumStringLengths",
                        MaximumStringLengths.class,
                        ProtocolLimits.class,
                        context
                    ))
                    .maximumArrayLengths(readRequired(
                        object,
                        "maximumArrayLengths",
                        MaximumArrayLengths.class,
                        ProtocolLimits.class,
                        context
                    ))
                    .timing(readRequired(
                        object,
                        "timing",
                        ProtocolTiming.class,
                        ProtocolLimits.class,
                        context
                    ))
                    .extensionFields(ExtensionFieldsJacksonSupport.capture(
                        mapper,
                        object,
                        PROTOCOL_LIMITS_FIELDS
                    ))
                    .build();
            } catch (IllegalArgumentException | NullPointerException exception) {
                return context.reportInputMismatch(
                    ProtocolLimits.class,
                    "Protocol limit fields do not satisfy model constraints"
                );
            }
        }
    }

    private static final class MaximumStringLengthsSerializer
        extends StdSerializer<MaximumStringLengths> {
        private MaximumStringLengthsSerializer() {
            super(MaximumStringLengths.class);
        }

        @Override
        public void serialize(
            MaximumStringLengths value,
            JsonGenerator generator,
            SerializerProvider provider
        ) throws IOException {
            ObjectMapper mapper = requireObjectMapper(generator);
            ObjectNode target = mapper.createObjectNode();
            putOptional(
                target,
                "maximumMessageLength",
                value.maximumMessageLength()
            );
            putOptional(
                target,
                "maximumTopicSerialLength",
                value.maximumTopicSerialLength()
            );
            putOptional(
                target,
                "maximumTopicElementLength",
                value.maximumTopicElementLength()
            );
            putOptional(target, "maximumIdLength", value.maximumIdLength());
            putOptional(target, "idNumericalOnly", value.idNumericalOnly());
            putOptional(
                target,
                "maximumLoadIdLength",
                value.maximumLoadIdLength()
            );
            ExtensionFieldsJacksonSupport.merge(
                mapper,
                target,
                value.extensionFields(),
                STRING_FIELDS
            );
            generator.writeTree(target);
        }
    }

    private static final class MaximumStringLengthsDeserializer
        extends StdDeserializer<MaximumStringLengths> {
        private MaximumStringLengthsDeserializer() {
            super(MaximumStringLengths.class);
        }

        @Override
        public MaximumStringLengths deserialize(
            JsonParser parser,
            DeserializationContext context
        ) throws IOException {
            ObjectMapper mapper = requireObjectMapper(parser);
            ObjectNode object = readObject(
                mapper,
                parser,
                context,
                MaximumStringLengths.class,
                "Maximum string lengths"
            );
            return MaximumStringLengths.builder()
                .maximumMessageLength(readOptional(
                    object,
                    "maximumMessageLength",
                    Long.class,
                    MaximumStringLengths.class,
                    context
                ))
                .maximumTopicSerialLength(readOptional(
                    object,
                    "maximumTopicSerialLength",
                    Long.class,
                    MaximumStringLengths.class,
                    context
                ))
                .maximumTopicElementLength(readOptional(
                    object,
                    "maximumTopicElementLength",
                    Long.class,
                    MaximumStringLengths.class,
                    context
                ))
                .maximumIdLength(readOptional(
                    object,
                    "maximumIdLength",
                    Long.class,
                    MaximumStringLengths.class,
                    context
                ))
                .idNumericalOnly(readOptional(
                    object,
                    "idNumericalOnly",
                    Boolean.class,
                    MaximumStringLengths.class,
                    context
                ))
                .maximumLoadIdLength(readOptional(
                    object,
                    "maximumLoadIdLength",
                    Long.class,
                    MaximumStringLengths.class,
                    context
                ))
                .extensionFields(ExtensionFieldsJacksonSupport.capture(
                    mapper,
                    object,
                    STRING_FIELDS
                ))
                .build();
        }
    }

    private static final class MaximumArrayLengthsSerializer
        extends StdSerializer<MaximumArrayLengths> {
        private MaximumArrayLengthsSerializer() {
            super(MaximumArrayLengths.class);
        }

        @Override
        public void serialize(
            MaximumArrayLengths value,
            JsonGenerator generator,
            SerializerProvider provider
        ) throws IOException {
            ObjectMapper mapper = requireObjectMapper(generator);
            ObjectNode target = mapper.createObjectNode();
            putOptional(target, "order.nodes", value.orderNodes());
            putOptional(target, "order.edges", value.orderEdges());
            putOptional(target, "node.actions", value.nodeActions());
            putOptional(target, "edge.actions", value.edgeActions());
            putOptional(
                target,
                "actions.actionsParameters",
                value.actionParameters()
            );
            putOptional(target, "instantActions", value.instantActions());
            putOptional(
                target,
                "trajectory.knotVector",
                value.trajectoryKnotVector()
            );
            putOptional(
                target,
                "trajectory.controlPoints",
                value.trajectoryControlPoints()
            );
            putOptional(target, "zoneSet.zones", value.zoneSetZones());
            putOptional(target, "state.nodeStates", value.stateNodeStates());
            putOptional(target, "state.edgeStates", value.stateEdgeStates());
            putOptional(target, "state.loads", value.stateLoads());
            putOptional(target, "state.actionStates", value.stateActionStates());
            putOptional(
                target,
                "state.instantActionStates",
                value.stateInstantActionStates()
            );
            putOptional(
                target,
                "state.zoneActionStates",
                value.stateZoneActionStates()
            );
            putOptional(target, "state.errors", value.stateErrors());
            putOptional(target, "state.information", value.stateInformation());
            putOptional(
                target,
                "error.errorReferences",
                value.errorErrorReferences()
            );
            putOptional(
                target,
                "information.infoReferences",
                value.informationInfoReferences()
            );
            ExtensionFieldsJacksonSupport.merge(
                mapper,
                target,
                value.extensionFields(),
                ARRAY_FIELDS
            );
            generator.writeTree(target);
        }
    }

    private static final class MaximumArrayLengthsDeserializer
        extends StdDeserializer<MaximumArrayLengths> {
        private MaximumArrayLengthsDeserializer() {
            super(MaximumArrayLengths.class);
        }

        @Override
        public MaximumArrayLengths deserialize(
            JsonParser parser,
            DeserializationContext context
        ) throws IOException {
            ObjectMapper mapper = requireObjectMapper(parser);
            ObjectNode object = readObject(
                mapper,
                parser,
                context,
                MaximumArrayLengths.class,
                "Maximum array lengths"
            );
            return MaximumArrayLengths.builder()
                .orderNodes(readLong(
                    object,
                    "order.nodes",
                    MaximumArrayLengths.class,
                    context
                ))
                .orderEdges(readLong(
                    object,
                    "order.edges",
                    MaximumArrayLengths.class,
                    context
                ))
                .nodeActions(readLong(
                    object,
                    "node.actions",
                    MaximumArrayLengths.class,
                    context
                ))
                .edgeActions(readLong(
                    object,
                    "edge.actions",
                    MaximumArrayLengths.class,
                    context
                ))
                .actionParameters(readLong(
                    object,
                    "actions.actionsParameters",
                    MaximumArrayLengths.class,
                    context
                ))
                .instantActions(readLong(
                    object,
                    "instantActions",
                    MaximumArrayLengths.class,
                    context
                ))
                .trajectoryKnotVector(readLong(
                    object,
                    "trajectory.knotVector",
                    MaximumArrayLengths.class,
                    context
                ))
                .trajectoryControlPoints(readLong(
                    object,
                    "trajectory.controlPoints",
                    MaximumArrayLengths.class,
                    context
                ))
                .zoneSetZones(readLong(
                    object,
                    "zoneSet.zones",
                    MaximumArrayLengths.class,
                    context
                ))
                .stateNodeStates(readLong(
                    object,
                    "state.nodeStates",
                    MaximumArrayLengths.class,
                    context
                ))
                .stateEdgeStates(readLong(
                    object,
                    "state.edgeStates",
                    MaximumArrayLengths.class,
                    context
                ))
                .stateLoads(readLong(
                    object,
                    "state.loads",
                    MaximumArrayLengths.class,
                    context
                ))
                .stateActionStates(readLong(
                    object,
                    "state.actionStates",
                    MaximumArrayLengths.class,
                    context
                ))
                .stateInstantActionStates(readLong(
                    object,
                    "state.instantActionStates",
                    MaximumArrayLengths.class,
                    context
                ))
                .stateZoneActionStates(readLong(
                    object,
                    "state.zoneActionStates",
                    MaximumArrayLengths.class,
                    context
                ))
                .stateErrors(readLong(
                    object,
                    "state.errors",
                    MaximumArrayLengths.class,
                    context
                ))
                .stateInformation(readLong(
                    object,
                    "state.information",
                    MaximumArrayLengths.class,
                    context
                ))
                .errorErrorReferences(readLong(
                    object,
                    "error.errorReferences",
                    MaximumArrayLengths.class,
                    context
                ))
                .informationInfoReferences(readLong(
                    object,
                    "information.infoReferences",
                    MaximumArrayLengths.class,
                    context
                ))
                .extensionFields(ExtensionFieldsJacksonSupport.capture(
                    mapper,
                    object,
                    ARRAY_FIELDS
                ))
                .build();
        }
    }

    private static final class ProtocolTimingSerializer
        extends StdSerializer<ProtocolTiming> {
        private ProtocolTimingSerializer() {
            super(ProtocolTiming.class);
        }

        @Override
        public void serialize(
            ProtocolTiming value,
            JsonGenerator generator,
            SerializerProvider provider
        ) throws IOException {
            ObjectMapper mapper = requireObjectMapper(generator);
            ObjectNode target = mapper.createObjectNode();
            target.put("minimumOrderInterval", value.minimumOrderInterval());
            target.put("minimumStateInterval", value.minimumStateInterval());
            putOptional(
                target,
                "defaultStateInterval",
                value.defaultStateInterval()
            );
            putOptional(
                target,
                "visualizationInterval",
                value.visualizationInterval()
            );
            ExtensionFieldsJacksonSupport.merge(
                mapper,
                target,
                value.extensionFields(),
                TIMING_FIELDS
            );
            generator.writeTree(target);
        }
    }

    private static final class ProtocolTimingDeserializer
        extends StdDeserializer<ProtocolTiming> {
        private ProtocolTimingDeserializer() {
            super(ProtocolTiming.class);
        }

        @Override
        public ProtocolTiming deserialize(
            JsonParser parser,
            DeserializationContext context
        ) throws IOException {
            ObjectMapper mapper = requireObjectMapper(parser);
            ObjectNode object = readObject(
                mapper,
                parser,
                context,
                ProtocolTiming.class,
                "Protocol timing"
            );
            try {
                return ProtocolTiming.builder()
                    .minimumOrderInterval(readRequired(
                        object,
                        "minimumOrderInterval",
                        Double.class,
                        ProtocolTiming.class,
                        context
                    ))
                    .minimumStateInterval(readRequired(
                        object,
                        "minimumStateInterval",
                        Double.class,
                        ProtocolTiming.class,
                        context
                    ))
                    .defaultStateInterval(readOptional(
                        object,
                        "defaultStateInterval",
                        Double.class,
                        ProtocolTiming.class,
                        context
                    ))
                    .visualizationInterval(readOptional(
                        object,
                        "visualizationInterval",
                        Double.class,
                        ProtocolTiming.class,
                        context
                    ))
                    .extensionFields(ExtensionFieldsJacksonSupport.capture(
                        mapper,
                        object,
                        TIMING_FIELDS
                    ))
                    .build();
            } catch (IllegalArgumentException | NullPointerException exception) {
                return context.reportInputMismatch(
                    ProtocolTiming.class,
                    "Protocol timing fields do not satisfy model constraints"
                );
            }
        }
    }

    private static Long readLong(
        ObjectNode object,
        String fieldName,
        Class<?> ownerType,
        DeserializationContext context
    ) throws IOException {
        return readOptional(object, fieldName, Long.class, ownerType, context);
    }

    private static void putOptional(
        ObjectNode target,
        String fieldName,
        Long value
    ) {
        if (value != null) {
            target.put(fieldName, value);
        }
    }

    private static void putOptional(
        ObjectNode target,
        String fieldName,
        Double value
    ) {
        if (value != null) {
            target.put(fieldName, value);
        }
    }

    private static void putOptional(
        ObjectNode target,
        String fieldName,
        Boolean value
    ) {
        if (value != null) {
            target.put(fieldName, value);
        }
    }

}
