package io.github.cooltheworld.rcs.protocol.vda5050.v3.codec;

import static io.github.cooltheworld.rcs.protocol.vda5050.v3.codec.FactsheetFragmentJacksonSupport.readObject;
import static io.github.cooltheworld.rcs.protocol.vda5050.v3.codec.FactsheetFragmentJacksonSupport.readOptional;
import static io.github.cooltheworld.rcs.protocol.vda5050.v3.codec.FactsheetFragmentJacksonSupport.readRequired;
import static io.github.cooltheworld.rcs.protocol.vda5050.v3.codec.FactsheetFragmentJacksonSupport.readRequiredList;
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
import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.ExtensionFields;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.internal.ExtensionFieldsJacksonSupport;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.action.Action;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.order.AllowedDeviationXY;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.order.Node;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.order.NodePosition;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/** 注册 Order Node 对象图的 Jackson 线路映射。 */
final class OrderNodeJacksonSupport {
    private static final Set<String> ALLOWED_DEVIATION_XY_FIELDS = Set.of(
        "a",
        "b",
        "theta"
    );
    private static final Set<String> NODE_POSITION_FIELDS = Set.of(
        "x",
        "y",
        "theta",
        "allowedDeviationXY",
        "allowedDeviationTheta",
        "mapId"
    );
    private static final Set<String> NODE_FIELDS = Set.of(
        "nodeId",
        "sequenceId",
        "nodeDescriptor",
        "released",
        "nodePosition",
        "actions"
    );

    private OrderNodeJacksonSupport() {}

    static void register(SimpleModule module) {
        module.addSerializer(
            AllowedDeviationXY.class,
            new AllowedDeviationXYSerializer()
        );
        module.addDeserializer(
            AllowedDeviationXY.class,
            new AllowedDeviationXYDeserializer()
        );
        module.addSerializer(NodePosition.class, new NodePositionSerializer());
        module.addDeserializer(
            NodePosition.class,
            new NodePositionDeserializer()
        );
        module.addSerializer(Node.class, new NodeSerializer());
        module.addDeserializer(Node.class, new NodeDeserializer());
    }

    private static final class AllowedDeviationXYSerializer
        extends StdSerializer<AllowedDeviationXY> {
        private AllowedDeviationXYSerializer() {
            super(AllowedDeviationXY.class);
        }

        @Override
        public void serialize(
            AllowedDeviationXY value,
            JsonGenerator generator,
            SerializerProvider provider
        ) throws IOException {
            ObjectMapper mapper = requireObjectMapper(generator);
            ObjectNode target = mapper.createObjectNode();
            target.put("a", value.a());
            target.put("b", value.b());
            target.put("theta", value.theta());
            ExtensionFieldsJacksonSupport.merge(
                mapper,
                target,
                value.extensionFields(),
                ALLOWED_DEVIATION_XY_FIELDS
            );
            generator.writeTree(target);
        }
    }

    private static final class AllowedDeviationXYDeserializer
        extends StdDeserializer<AllowedDeviationXY> {
        private AllowedDeviationXYDeserializer() {
            super(AllowedDeviationXY.class);
        }

        @Override
        public AllowedDeviationXY deserialize(
            JsonParser parser,
            DeserializationContext context
        ) throws IOException {
            ObjectMapper mapper = requireObjectMapper(parser);
            ObjectNode object = readObject(
                mapper,
                parser,
                context,
                AllowedDeviationXY.class,
                "Allowed deviation XY"
            );
            Double a = readRequired(
                object,
                "a",
                Double.class,
                AllowedDeviationXY.class,
                context
            );
            Double b = readRequired(
                object,
                "b",
                Double.class,
                AllowedDeviationXY.class,
                context
            );
            Double theta = readRequired(
                object,
                "theta",
                Double.class,
                AllowedDeviationXY.class,
                context
            );
            ExtensionFields extensionFields = ExtensionFieldsJacksonSupport.capture(
                mapper,
                object,
                ALLOWED_DEVIATION_XY_FIELDS
            );
            return AllowedDeviationXY.builder()
                .a(a)
                .b(b)
                .theta(theta)
                .extensionFields(extensionFields)
                .build();
        }
    }

    private static final class NodePositionSerializer
        extends StdSerializer<NodePosition> {
        private NodePositionSerializer() {
            super(NodePosition.class);
        }

        @Override
        public void serialize(
            NodePosition value,
            JsonGenerator generator,
            SerializerProvider provider
        ) throws IOException {
            ObjectMapper mapper = requireObjectMapper(generator);
            ObjectNode target = mapper.createObjectNode();
            target.put("x", value.x());
            target.put("y", value.y());
            putOptional(target, "theta", value.theta());
            if (value.allowedDeviationXY() != null) {
                target.set(
                    "allowedDeviationXY",
                    mapper.valueToTree(value.allowedDeviationXY())
                );
            }
            putOptional(
                target,
                "allowedDeviationTheta",
                value.allowedDeviationTheta()
            );
            target.put("mapId", value.mapId());
            ExtensionFieldsJacksonSupport.merge(
                mapper,
                target,
                value.extensionFields(),
                NODE_POSITION_FIELDS
            );
            generator.writeTree(target);
        }
    }

    private static final class NodePositionDeserializer
        extends StdDeserializer<NodePosition> {
        private NodePositionDeserializer() {
            super(NodePosition.class);
        }

        @Override
        public NodePosition deserialize(
            JsonParser parser,
            DeserializationContext context
        ) throws IOException {
            ObjectMapper mapper = requireObjectMapper(parser);
            ObjectNode object = readObject(
                mapper,
                parser,
                context,
                NodePosition.class,
                "Node position"
            );
            Double x = readRequired(
                object,
                "x",
                Double.class,
                NodePosition.class,
                context
            );
            Double y = readRequired(
                object,
                "y",
                Double.class,
                NodePosition.class,
                context
            );
            Double theta = readOptional(
                object,
                "theta",
                Double.class,
                NodePosition.class,
                context
            );
            AllowedDeviationXY allowedDeviationXY = readOptional(
                object,
                "allowedDeviationXY",
                AllowedDeviationXY.class,
                NodePosition.class,
                context
            );
            Double allowedDeviationTheta = readOptional(
                object,
                "allowedDeviationTheta",
                Double.class,
                NodePosition.class,
                context
            );
            String mapId = readRequired(
                object,
                "mapId",
                String.class,
                NodePosition.class,
                context
            );
            ExtensionFields extensionFields = ExtensionFieldsJacksonSupport.capture(
                mapper,
                object,
                NODE_POSITION_FIELDS
            );
            return NodePosition.builder()
                .x(x)
                .y(y)
                .theta(theta)
                .allowedDeviationXY(allowedDeviationXY)
                .allowedDeviationTheta(allowedDeviationTheta)
                .mapId(mapId)
                .extensionFields(extensionFields)
                .build();
        }
    }

    private static final class NodeSerializer extends StdSerializer<Node> {
        private NodeSerializer() {
            super(Node.class);
        }

        @Override
        public void serialize(
            Node value,
            JsonGenerator generator,
            SerializerProvider provider
        ) throws IOException {
            ObjectMapper mapper = requireObjectMapper(generator);
            ObjectNode target = mapper.createObjectNode();
            target.put("nodeId", value.nodeId());
            target.put("sequenceId", value.sequenceId());
            putOptional(target, "nodeDescriptor", value.nodeDescriptor());
            target.put("released", value.released());
            if (value.nodePosition() != null) {
                target.set(
                    "nodePosition",
                    mapper.valueToTree(value.nodePosition())
                );
            }
            target.set("actions", mapper.valueToTree(value.actions()));
            ExtensionFieldsJacksonSupport.merge(
                mapper,
                target,
                value.extensionFields(),
                NODE_FIELDS
            );
            generator.writeTree(target);
        }
    }

    private static final class NodeDeserializer extends StdDeserializer<Node> {
        private NodeDeserializer() {
            super(Node.class);
        }

        @Override
        public Node deserialize(
            JsonParser parser,
            DeserializationContext context
        ) throws IOException {
            ObjectMapper mapper = requireObjectMapper(parser);
            ObjectNode object = readObject(
                mapper,
                parser,
                context,
                Node.class,
                "Node"
            );
            String nodeId = readRequired(
                object,
                "nodeId",
                String.class,
                Node.class,
                context
            );
            Long sequenceId = readRequired(
                object,
                "sequenceId",
                Long.class,
                Node.class,
                context
            );
            String nodeDescriptor = readOptional(
                object,
                "nodeDescriptor",
                String.class,
                Node.class,
                context
            );
            Boolean released = readRequired(
                object,
                "released",
                Boolean.class,
                Node.class,
                context
            );
            NodePosition nodePosition = readOptional(
                object,
                "nodePosition",
                NodePosition.class,
                Node.class,
                context
            );
            List<Action> actions = readRequiredList(
                object,
                "actions",
                Action.class,
                Node.class,
                context
            );
            ExtensionFields extensionFields = ExtensionFieldsJacksonSupport.capture(
                mapper,
                object,
                NODE_FIELDS
            );
            return Node.builder()
                .nodeId(nodeId)
                .sequenceId(sequenceId)
                .nodeDescriptor(nodeDescriptor)
                .released(released)
                .nodePosition(nodePosition)
                .actions(actions)
                .extensionFields(extensionFields)
                .build();
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
        String value
    ) {
        if (value != null) {
            target.put(fieldName, value);
        }
    }
}
