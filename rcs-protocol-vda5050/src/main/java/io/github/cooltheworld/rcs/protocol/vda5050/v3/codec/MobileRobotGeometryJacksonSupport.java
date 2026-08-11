package io.github.cooltheworld.rcs.protocol.vda5050.v3.codec;

import static io.github.cooltheworld.rcs.protocol.vda5050.v3.codec.FactsheetFragmentJacksonSupport.putOptional;
import static io.github.cooltheworld.rcs.protocol.vda5050.v3.codec.FactsheetFragmentJacksonSupport.readObject;
import static io.github.cooltheworld.rcs.protocol.vda5050.v3.codec.FactsheetFragmentJacksonSupport.readOptional;
import static io.github.cooltheworld.rcs.protocol.vda5050.v3.codec.FactsheetFragmentJacksonSupport.readOptionalList;
import static io.github.cooltheworld.rcs.protocol.vda5050.v3.codec.FactsheetFragmentJacksonSupport.readRequired;
import static io.github.cooltheworld.rcs.protocol.vda5050.v3.codec.FactsheetFragmentJacksonSupport.readRequiredList;
import static io.github.cooltheworld.rcs.protocol.vda5050.v3.codec.FactsheetFragmentJacksonSupport.requireObjectMapper;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.codec.internal.OpaqueJsonJacksonAccess;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.ExtensionFields;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.internal.ExtensionFieldsJacksonSupport;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.Envelope2d;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.Envelope2dVertex;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.Envelope3d;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.Envelope3dData;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.MobileRobotGeometry;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.WheelDefinition;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.WheelPosition;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.WheelType;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/** Mobile Robot 几何子树的包内 Jackson 映射支持。 */
final class MobileRobotGeometryJacksonSupport {
    private static final Set<String> GEOMETRY_FIELDS = Set.of(
        "wheelDefinitions",
        "envelopes2d",
        "envelopes3d"
    );
    private static final Set<String> WHEEL_FIELDS = Set.of(
        "type",
        "isActiveDriven",
        "isActiveSteered",
        "position",
        "diameter",
        "width",
        "centerDisplacement",
        "constraints"
    );
    private static final Set<String> WHEEL_POSITION_FIELDS = Set.of(
        "x",
        "y",
        "theta"
    );
    private static final Set<String> ENVELOPE_2D_FIELDS = Set.of(
        "envelope2dId",
        "vertices",
        "description"
    );
    private static final Set<String> VERTEX_FIELDS = Set.of("x", "y");
    private static final Set<String> ENVELOPE_3D_FIELDS = Set.of(
        "envelope3dId",
        "format",
        "data",
        "url",
        "description"
    );

    private MobileRobotGeometryJacksonSupport() {}

    static void register(SimpleModule module) {
        module.addSerializer(WheelType.class, new WheelTypeSerializer());
        module.addDeserializer(WheelType.class, new WheelTypeDeserializer());
        module.addSerializer(
            MobileRobotGeometry.class,
            new GeometrySerializer()
        );
        module.addDeserializer(
            MobileRobotGeometry.class,
            new GeometryDeserializer()
        );
        module.addSerializer(WheelDefinition.class, new WheelSerializer());
        module.addDeserializer(WheelDefinition.class, new WheelDeserializer());
        module.addSerializer(WheelPosition.class, new WheelPositionSerializer());
        module.addDeserializer(
            WheelPosition.class,
            new WheelPositionDeserializer()
        );
        module.addSerializer(Envelope2d.class, new Envelope2dSerializer());
        module.addDeserializer(Envelope2d.class, new Envelope2dDeserializer());
        module.addSerializer(
            Envelope2dVertex.class,
            new Envelope2dVertexSerializer()
        );
        module.addDeserializer(
            Envelope2dVertex.class,
            new Envelope2dVertexDeserializer()
        );
        module.addSerializer(Envelope3d.class, new Envelope3dSerializer());
        module.addDeserializer(Envelope3d.class, new Envelope3dDeserializer());
        module.addSerializer(
            Envelope3dData.class,
            new Envelope3dDataSerializer()
        );
        module.addDeserializer(
            Envelope3dData.class,
            new Envelope3dDataDeserializer()
        );
    }

    private static final class WheelTypeSerializer
        extends StdScalarSerializer<WheelType> {
        private WheelTypeSerializer() {
            super(WheelType.class);
        }

        @Override
        public void serialize(
            WheelType value,
            JsonGenerator generator,
            SerializerProvider provider
        ) throws IOException {
            generator.writeString(value.value());
        }
    }

    private static final class WheelTypeDeserializer
        extends StdDeserializer<WheelType> {
        private WheelTypeDeserializer() {
            super(WheelType.class);
        }

        @Override
        public WheelType deserialize(
            JsonParser parser,
            DeserializationContext context
        ) throws IOException {
            if (!parser.hasToken(JsonToken.VALUE_STRING)) {
                return (WheelType) context.handleUnexpectedToken(
                    WheelType.class,
                    parser
                );
            }
            return WheelType.of(parser.getText());
        }
    }

    private static final class GeometrySerializer
        extends StdSerializer<MobileRobotGeometry> {
        private GeometrySerializer() {
            super(MobileRobotGeometry.class);
        }

        @Override
        public void serialize(
            MobileRobotGeometry value,
            JsonGenerator generator,
            SerializerProvider provider
        ) throws IOException {
            ObjectMapper mapper = requireObjectMapper(generator);
            ObjectNode target = mapper.createObjectNode();
            putOptionalTree(
                mapper,
                target,
                "wheelDefinitions",
                value.wheelDefinitions()
            );
            putOptionalTree(mapper, target, "envelopes2d", value.envelopes2d());
            putOptionalTree(mapper, target, "envelopes3d", value.envelopes3d());
            merge(mapper, target, value.extensionFields(), GEOMETRY_FIELDS);
            generator.writeTree(target);
        }
    }

    private static final class GeometryDeserializer
        extends StdDeserializer<MobileRobotGeometry> {
        private GeometryDeserializer() {
            super(MobileRobotGeometry.class);
        }

        @Override
        public MobileRobotGeometry deserialize(
            JsonParser parser,
            DeserializationContext context
        ) throws IOException {
            ObjectMapper mapper = requireObjectMapper(parser);
            ObjectNode object = readObject(
                mapper,
                parser,
                context,
                MobileRobotGeometry.class,
                "Mobile robot geometry"
            );
            return MobileRobotGeometry.builder()
                .wheelDefinitions(readOptionalList(
                    object,
                    "wheelDefinitions",
                    WheelDefinition.class,
                    MobileRobotGeometry.class,
                    context
                ))
                .envelopes2d(readOptionalList(
                    object,
                    "envelopes2d",
                    Envelope2d.class,
                    MobileRobotGeometry.class,
                    context
                ))
                .envelopes3d(readOptionalList(
                    object,
                    "envelopes3d",
                    Envelope3d.class,
                    MobileRobotGeometry.class,
                    context
                ))
                .extensionFields(capture(mapper, object, GEOMETRY_FIELDS))
                .build();
        }
    }

    private static final class WheelSerializer
        extends StdSerializer<WheelDefinition> {
        private WheelSerializer() {
            super(WheelDefinition.class);
        }

        @Override
        public void serialize(
            WheelDefinition value,
            JsonGenerator generator,
            SerializerProvider provider
        ) throws IOException {
            ObjectMapper mapper = requireObjectMapper(generator);
            ObjectNode target = mapper.createObjectNode();
            target.put("type", value.type().value());
            target.put("isActiveDriven", value.isActiveDriven());
            target.put("isActiveSteered", value.isActiveSteered());
            target.set("position", mapper.valueToTree(value.position()));
            target.put("diameter", value.diameter());
            target.put("width", value.width());
            putOptional(
                target,
                "centerDisplacement",
                value.centerDisplacement()
            );
            putOptional(target, "constraints", value.constraints());
            merge(mapper, target, value.extensionFields(), WHEEL_FIELDS);
            generator.writeTree(target);
        }
    }

    private static final class WheelDeserializer
        extends StdDeserializer<WheelDefinition> {
        private WheelDeserializer() {
            super(WheelDefinition.class);
        }

        @Override
        public WheelDefinition deserialize(
            JsonParser parser,
            DeserializationContext context
        ) throws IOException {
            ObjectMapper mapper = requireObjectMapper(parser);
            ObjectNode object = readObject(
                mapper,
                parser,
                context,
                WheelDefinition.class,
                "Wheel definition"
            );
            return WheelDefinition.builder()
                .type(readRequired(
                    object,
                    "type",
                    WheelType.class,
                    WheelDefinition.class,
                    context
                ))
                .isActiveDriven(readRequired(
                    object,
                    "isActiveDriven",
                    Boolean.class,
                    WheelDefinition.class,
                    context
                ))
                .isActiveSteered(readRequired(
                    object,
                    "isActiveSteered",
                    Boolean.class,
                    WheelDefinition.class,
                    context
                ))
                .position(readRequired(
                    object,
                    "position",
                    WheelPosition.class,
                    WheelDefinition.class,
                    context
                ))
                .diameter(readRequired(
                    object,
                    "diameter",
                    Double.class,
                    WheelDefinition.class,
                    context
                ))
                .width(readRequired(
                    object,
                    "width",
                    Double.class,
                    WheelDefinition.class,
                    context
                ))
                .centerDisplacement(readOptional(
                    object,
                    "centerDisplacement",
                    Double.class,
                    WheelDefinition.class,
                    context
                ))
                .constraints(readOptional(
                    object,
                    "constraints",
                    String.class,
                    WheelDefinition.class,
                    context
                ))
                .extensionFields(capture(mapper, object, WHEEL_FIELDS))
                .build();
        }
    }

    private static final class WheelPositionSerializer
        extends StdSerializer<WheelPosition> {
        private WheelPositionSerializer() {
            super(WheelPosition.class);
        }

        @Override
        public void serialize(
            WheelPosition value,
            JsonGenerator generator,
            SerializerProvider provider
        ) throws IOException {
            ObjectMapper mapper = requireObjectMapper(generator);
            ObjectNode target = mapper.createObjectNode();
            target.put("x", value.x());
            target.put("y", value.y());
            putOptional(target, "theta", value.theta());
            merge(mapper, target, value.extensionFields(), WHEEL_POSITION_FIELDS);
            generator.writeTree(target);
        }
    }

    private static final class WheelPositionDeserializer
        extends StdDeserializer<WheelPosition> {
        private WheelPositionDeserializer() {
            super(WheelPosition.class);
        }

        @Override
        public WheelPosition deserialize(
            JsonParser parser,
            DeserializationContext context
        ) throws IOException {
            ObjectMapper mapper = requireObjectMapper(parser);
            ObjectNode object = readObject(
                mapper,
                parser,
                context,
                WheelPosition.class,
                "Wheel position"
            );
            return WheelPosition.builder()
                .x(readRequired(
                    object,
                    "x",
                    Double.class,
                    WheelPosition.class,
                    context
                ))
                .y(readRequired(
                    object,
                    "y",
                    Double.class,
                    WheelPosition.class,
                    context
                ))
                .theta(readOptional(
                    object,
                    "theta",
                    Double.class,
                    WheelPosition.class,
                    context
                ))
                .extensionFields(capture(mapper, object, WHEEL_POSITION_FIELDS))
                .build();
        }
    }

    private static final class Envelope2dSerializer
        extends StdSerializer<Envelope2d> {
        private Envelope2dSerializer() {
            super(Envelope2d.class);
        }

        @Override
        public void serialize(
            Envelope2d value,
            JsonGenerator generator,
            SerializerProvider provider
        ) throws IOException {
            ObjectMapper mapper = requireObjectMapper(generator);
            ObjectNode target = mapper.createObjectNode();
            target.put("envelope2dId", value.envelope2dId());
            target.set("vertices", mapper.valueToTree(value.vertices()));
            putOptional(target, "description", value.description());
            merge(mapper, target, value.extensionFields(), ENVELOPE_2D_FIELDS);
            generator.writeTree(target);
        }
    }

    private static final class Envelope2dDeserializer
        extends StdDeserializer<Envelope2d> {
        private Envelope2dDeserializer() {
            super(Envelope2d.class);
        }

        @Override
        public Envelope2d deserialize(
            JsonParser parser,
            DeserializationContext context
        ) throws IOException {
            ObjectMapper mapper = requireObjectMapper(parser);
            ObjectNode object = readObject(
                mapper,
                parser,
                context,
                Envelope2d.class,
                "2D envelope"
            );
            return Envelope2d.builder()
                .envelope2dId(readRequired(
                    object,
                    "envelope2dId",
                    String.class,
                    Envelope2d.class,
                    context
                ))
                .vertices(readRequiredList(
                    object,
                    "vertices",
                    Envelope2dVertex.class,
                    Envelope2d.class,
                    context
                ))
                .description(readOptional(
                    object,
                    "description",
                    String.class,
                    Envelope2d.class,
                    context
                ))
                .extensionFields(capture(mapper, object, ENVELOPE_2D_FIELDS))
                .build();
        }
    }

    private static final class Envelope2dVertexSerializer
        extends StdSerializer<Envelope2dVertex> {
        private Envelope2dVertexSerializer() {
            super(Envelope2dVertex.class);
        }

        @Override
        public void serialize(
            Envelope2dVertex value,
            JsonGenerator generator,
            SerializerProvider provider
        ) throws IOException {
            ObjectMapper mapper = requireObjectMapper(generator);
            ObjectNode target = mapper.createObjectNode();
            target.put("x", value.x());
            target.put("y", value.y());
            merge(mapper, target, value.extensionFields(), VERTEX_FIELDS);
            generator.writeTree(target);
        }
    }

    private static final class Envelope2dVertexDeserializer
        extends StdDeserializer<Envelope2dVertex> {
        private Envelope2dVertexDeserializer() {
            super(Envelope2dVertex.class);
        }

        @Override
        public Envelope2dVertex deserialize(
            JsonParser parser,
            DeserializationContext context
        ) throws IOException {
            ObjectMapper mapper = requireObjectMapper(parser);
            ObjectNode object = readObject(
                mapper,
                parser,
                context,
                Envelope2dVertex.class,
                "2D envelope vertex"
            );
            return Envelope2dVertex.builder()
                .x(readRequired(
                    object,
                    "x",
                    Double.class,
                    Envelope2dVertex.class,
                    context
                ))
                .y(readRequired(
                    object,
                    "y",
                    Double.class,
                    Envelope2dVertex.class,
                    context
                ))
                .extensionFields(capture(mapper, object, VERTEX_FIELDS))
                .build();
        }
    }

    private static final class Envelope3dSerializer
        extends StdSerializer<Envelope3d> {
        private Envelope3dSerializer() {
            super(Envelope3d.class);
        }

        @Override
        public void serialize(
            Envelope3d value,
            JsonGenerator generator,
            SerializerProvider provider
        ) throws IOException {
            ObjectMapper mapper = requireObjectMapper(generator);
            ObjectNode target = mapper.createObjectNode();
            target.put("envelope3dId", value.envelope3dId());
            target.put("format", value.format());
            if (value.data() != null) {
                target.set("data", mapper.valueToTree(value.data()));
            }
            putOptional(target, "url", value.url());
            putOptional(target, "description", value.description());
            merge(mapper, target, value.extensionFields(), ENVELOPE_3D_FIELDS);
            generator.writeTree(target);
        }
    }

    private static final class Envelope3dDataSerializer
        extends StdSerializer<Envelope3dData> {
        private Envelope3dDataSerializer() {
            super(Envelope3dData.class);
        }

        @Override
        public void serialize(
            Envelope3dData value,
            JsonGenerator generator,
            SerializerProvider provider
        ) throws IOException {
            generator.writeTree(OpaqueJsonJacksonAccess.object(
                requireObjectMapper(generator),
                value
            ));
        }
    }

    private static final class Envelope3dDataDeserializer
        extends StdDeserializer<Envelope3dData> {
        private Envelope3dDataDeserializer() {
            super(Envelope3dData.class);
        }

        @Override
        public Envelope3dData deserialize(
            JsonParser parser,
            DeserializationContext context
        ) throws IOException {
            ObjectMapper mapper = requireObjectMapper(parser);
            JsonNode value = mapper.readTree(parser);
            if (!value.isObject()) {
                return context.reportInputMismatch(
                    Envelope3dData.class,
                    "3D envelope data must be a JSON object"
                );
            }
            ObjectNode object = (ObjectNode) value;
            return OpaqueJsonJacksonAccess.envelope3dData(mapper, object);
        }
    }

    private static final class Envelope3dDeserializer
        extends StdDeserializer<Envelope3d> {
        private Envelope3dDeserializer() {
            super(Envelope3d.class);
        }

        @Override
        public Envelope3d deserialize(
            JsonParser parser,
            DeserializationContext context
        ) throws IOException {
            ObjectMapper mapper = requireObjectMapper(parser);
            ObjectNode object = readObject(
                mapper,
                parser,
                context,
                Envelope3d.class,
                "3D envelope"
            );
            return Envelope3d.builder()
                .envelope3dId(readRequired(
                    object,
                    "envelope3dId",
                    String.class,
                    Envelope3d.class,
                    context
                ))
                .format(readRequired(
                    object,
                    "format",
                    String.class,
                    Envelope3d.class,
                    context
                ))
                .data(readOptional(
                    object,
                    "data",
                    Envelope3dData.class,
                    Envelope3d.class,
                    context
                ))
                .url(readOptional(
                    object,
                    "url",
                    String.class,
                    Envelope3d.class,
                    context
                ))
                .description(readOptional(
                    object,
                    "description",
                    String.class,
                    Envelope3d.class,
                    context
                ))
                .extensionFields(capture(mapper, object, ENVELOPE_3D_FIELDS))
                .build();
        }
    }

    private static ExtensionFields capture(
        ObjectMapper mapper,
        ObjectNode source,
        Set<String> standardFields
    ) throws IOException {
        return ExtensionFieldsJacksonSupport.capture(
            mapper,
            source,
            standardFields
        );
    }

    private static void merge(
        ObjectMapper mapper,
        ObjectNode target,
        ExtensionFields extensionFields,
        Set<String> standardFields
    ) {
        ExtensionFieldsJacksonSupport.merge(
            mapper,
            target,
            extensionFields,
            standardFields
        );
    }

    private static void putOptionalTree(
        ObjectMapper mapper,
        ObjectNode target,
        String fieldName,
        List<?> value
    ) {
        if (value != null) {
            target.set(fieldName, mapper.valueToTree(value));
        }
    }
}
