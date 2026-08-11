package io.github.cooltheworld.rcs.protocol.vda5050.v3.codec;

import static io.github.cooltheworld.rcs.protocol.vda5050.v3.codec.FactsheetFragmentJacksonSupport.putOptional;
import static io.github.cooltheworld.rcs.protocol.vda5050.v3.codec.FactsheetFragmentJacksonSupport.readObject;
import static io.github.cooltheworld.rcs.protocol.vda5050.v3.codec.FactsheetFragmentJacksonSupport.readOptional;
import static io.github.cooltheworld.rcs.protocol.vda5050.v3.codec.FactsheetFragmentJacksonSupport.readOptionalList;
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
import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.ExtensionFields;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.internal.ExtensionFieldsJacksonSupport;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.BoundingBoxReference;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.LoadDimensions;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.LoadSet;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.LoadSpecification;
import java.io.IOException;
import java.util.Set;

/** 注册 Factsheet {@code loadSpecification} 子树的 Jackson 线路映射。 */
final class LoadSpecificationJacksonSupport {
    private static final Set<String> LOAD_SPECIFICATION_FIELDS = Set.of(
        "loadPositions",
        "loadSets"
    );
    private static final Set<String> LOAD_SET_FIELDS = Set.of(
        "setName",
        "loadType",
        "loadPositions",
        "boundingBoxReference",
        "loadDimensions",
        "maximumWeight",
        "minimumLoadhandlingHeight",
        "maximumLoadhandlingHeight",
        "minimumLoadhandlingDepth",
        "maximumLoadhandlingDepth",
        "minimumLoadhandlingTilt",
        "maximumLoadhandlingTilt",
        "maximumSpeed",
        "maximumAcceleration",
        "maximumDeceleration",
        "pickTime",
        "dropTime",
        "description"
    );
    private static final Set<String> BOUNDING_BOX_REFERENCE_FIELDS = Set.of(
        "x",
        "y",
        "z",
        "theta"
    );
    private static final Set<String> LOAD_DIMENSIONS_FIELDS = Set.of(
        "length",
        "width",
        "height"
    );

    private LoadSpecificationJacksonSupport() {}

    static void register(SimpleModule module) {
        module.addSerializer(
            LoadSpecification.class,
            new LoadSpecificationSerializer()
        );
        module.addDeserializer(
            LoadSpecification.class,
            new LoadSpecificationDeserializer()
        );
        module.addSerializer(LoadSet.class, new LoadSetSerializer());
        module.addDeserializer(LoadSet.class, new LoadSetDeserializer());
        module.addSerializer(
            BoundingBoxReference.class,
            new BoundingBoxReferenceSerializer()
        );
        module.addDeserializer(
            BoundingBoxReference.class,
            new BoundingBoxReferenceDeserializer()
        );
        module.addSerializer(
            LoadDimensions.class,
            new LoadDimensionsSerializer()
        );
        module.addDeserializer(
            LoadDimensions.class,
            new LoadDimensionsDeserializer()
        );
    }

    private static final class LoadSpecificationSerializer
        extends StdSerializer<LoadSpecification> {
        private LoadSpecificationSerializer() {
            super(LoadSpecification.class);
        }

        @Override
        public void serialize(
            LoadSpecification value,
            JsonGenerator generator,
            SerializerProvider provider
        ) throws IOException {
            ObjectMapper mapper = requireObjectMapper(generator);
            ObjectNode target = mapper.createObjectNode();
            putOptionalTree(
                mapper,
                target,
                "loadPositions",
                value.loadPositions()
            );
            putOptionalTree(mapper, target, "loadSets", value.loadSets());
            merge(
                mapper,
                target,
                value.extensionFields(),
                LOAD_SPECIFICATION_FIELDS
            );
            generator.writeTree(target);
        }
    }

    private static final class LoadSpecificationDeserializer
        extends StdDeserializer<LoadSpecification> {
        private LoadSpecificationDeserializer() {
            super(LoadSpecification.class);
        }

        @Override
        public LoadSpecification deserialize(
            JsonParser parser,
            DeserializationContext context
        ) throws IOException {
            ObjectMapper mapper = requireObjectMapper(parser);
            ObjectNode object = readObject(
                mapper,
                parser,
                context,
                LoadSpecification.class,
                "Load specification"
            );
            return LoadSpecification.builder()
                .loadPositions(readOptionalList(
                    object,
                    "loadPositions",
                    String.class,
                    LoadSpecification.class,
                    context
                ))
                .loadSets(readOptionalList(
                    object,
                    "loadSets",
                    LoadSet.class,
                    LoadSpecification.class,
                    context
                ))
                .extensionFields(capture(
                    mapper,
                    object,
                    LOAD_SPECIFICATION_FIELDS
                ))
                .build();
        }
    }

    private static final class LoadSetSerializer extends StdSerializer<LoadSet> {
        private LoadSetSerializer() {
            super(LoadSet.class);
        }

        @Override
        public void serialize(
            LoadSet value,
            JsonGenerator generator,
            SerializerProvider provider
        ) throws IOException {
            ObjectMapper mapper = requireObjectMapper(generator);
            ObjectNode target = mapper.createObjectNode();
            target.put("setName", value.setName());
            target.put("loadType", value.loadType());
            putOptionalTree(
                mapper,
                target,
                "loadPositions",
                value.loadPositions()
            );
            putOptionalTree(
                mapper,
                target,
                "boundingBoxReference",
                value.boundingBoxReference()
            );
            putOptionalTree(
                mapper,
                target,
                "loadDimensions",
                value.loadDimensions()
            );
            putOptional(target, "maximumWeight", value.maximumWeight());
            putOptional(
                target,
                "minimumLoadhandlingHeight",
                value.minimumLoadhandlingHeight()
            );
            putOptional(
                target,
                "maximumLoadhandlingHeight",
                value.maximumLoadhandlingHeight()
            );
            putOptional(
                target,
                "minimumLoadhandlingDepth",
                value.minimumLoadhandlingDepth()
            );
            putOptional(
                target,
                "maximumLoadhandlingDepth",
                value.maximumLoadhandlingDepth()
            );
            putOptional(
                target,
                "minimumLoadhandlingTilt",
                value.minimumLoadhandlingTilt()
            );
            putOptional(
                target,
                "maximumLoadhandlingTilt",
                value.maximumLoadhandlingTilt()
            );
            putOptional(target, "maximumSpeed", value.maximumSpeed());
            putOptional(
                target,
                "maximumAcceleration",
                value.maximumAcceleration()
            );
            putOptional(
                target,
                "maximumDeceleration",
                value.maximumDeceleration()
            );
            putOptional(target, "pickTime", value.pickTime());
            putOptional(target, "dropTime", value.dropTime());
            putOptional(target, "description", value.description());
            merge(mapper, target, value.extensionFields(), LOAD_SET_FIELDS);
            generator.writeTree(target);
        }
    }

    private static final class LoadSetDeserializer
        extends StdDeserializer<LoadSet> {
        private LoadSetDeserializer() {
            super(LoadSet.class);
        }

        @Override
        public LoadSet deserialize(
            JsonParser parser,
            DeserializationContext context
        ) throws IOException {
            ObjectMapper mapper = requireObjectMapper(parser);
            ObjectNode object = readObject(
                mapper,
                parser,
                context,
                LoadSet.class,
                "Load set"
            );
            return LoadSet.builder()
                .setName(readRequired(
                    object,
                    "setName",
                    String.class,
                    LoadSet.class,
                    context
                ))
                .loadType(readRequired(
                    object,
                    "loadType",
                    String.class,
                    LoadSet.class,
                    context
                ))
                .loadPositions(readOptionalList(
                    object,
                    "loadPositions",
                    String.class,
                    LoadSet.class,
                    context
                ))
                .boundingBoxReference(readOptional(
                    object,
                    "boundingBoxReference",
                    BoundingBoxReference.class,
                    LoadSet.class,
                    context
                ))
                .loadDimensions(readOptional(
                    object,
                    "loadDimensions",
                    LoadDimensions.class,
                    LoadSet.class,
                    context
                ))
                .maximumWeight(readOptional(
                    object,
                    "maximumWeight",
                    Double.class,
                    LoadSet.class,
                    context
                ))
                .minimumLoadhandlingHeight(readOptional(
                    object,
                    "minimumLoadhandlingHeight",
                    Double.class,
                    LoadSet.class,
                    context
                ))
                .maximumLoadhandlingHeight(readOptional(
                    object,
                    "maximumLoadhandlingHeight",
                    Double.class,
                    LoadSet.class,
                    context
                ))
                .minimumLoadhandlingDepth(readOptional(
                    object,
                    "minimumLoadhandlingDepth",
                    Double.class,
                    LoadSet.class,
                    context
                ))
                .maximumLoadhandlingDepth(readOptional(
                    object,
                    "maximumLoadhandlingDepth",
                    Double.class,
                    LoadSet.class,
                    context
                ))
                .minimumLoadhandlingTilt(readOptional(
                    object,
                    "minimumLoadhandlingTilt",
                    Double.class,
                    LoadSet.class,
                    context
                ))
                .maximumLoadhandlingTilt(readOptional(
                    object,
                    "maximumLoadhandlingTilt",
                    Double.class,
                    LoadSet.class,
                    context
                ))
                .maximumSpeed(readOptional(
                    object,
                    "maximumSpeed",
                    Double.class,
                    LoadSet.class,
                    context
                ))
                .maximumAcceleration(readOptional(
                    object,
                    "maximumAcceleration",
                    Double.class,
                    LoadSet.class,
                    context
                ))
                .maximumDeceleration(readOptional(
                    object,
                    "maximumDeceleration",
                    Double.class,
                    LoadSet.class,
                    context
                ))
                .pickTime(readOptional(
                    object,
                    "pickTime",
                    Double.class,
                    LoadSet.class,
                    context
                ))
                .dropTime(readOptional(
                    object,
                    "dropTime",
                    Double.class,
                    LoadSet.class,
                    context
                ))
                .description(readOptional(
                    object,
                    "description",
                    String.class,
                    LoadSet.class,
                    context
                ))
                .extensionFields(capture(mapper, object, LOAD_SET_FIELDS))
                .build();
        }
    }

    private static final class BoundingBoxReferenceSerializer
        extends StdSerializer<BoundingBoxReference> {
        private BoundingBoxReferenceSerializer() {
            super(BoundingBoxReference.class);
        }

        @Override
        public void serialize(
            BoundingBoxReference value,
            JsonGenerator generator,
            SerializerProvider provider
        ) throws IOException {
            ObjectMapper mapper = requireObjectMapper(generator);
            ObjectNode target = mapper.createObjectNode();
            target.put("x", value.x());
            target.put("y", value.y());
            target.put("z", value.z());
            putOptional(target, "theta", value.theta());
            merge(
                mapper,
                target,
                value.extensionFields(),
                BOUNDING_BOX_REFERENCE_FIELDS
            );
            generator.writeTree(target);
        }
    }

    private static final class BoundingBoxReferenceDeserializer
        extends StdDeserializer<BoundingBoxReference> {
        private BoundingBoxReferenceDeserializer() {
            super(BoundingBoxReference.class);
        }

        @Override
        public BoundingBoxReference deserialize(
            JsonParser parser,
            DeserializationContext context
        ) throws IOException {
            ObjectMapper mapper = requireObjectMapper(parser);
            ObjectNode object = readObject(
                mapper,
                parser,
                context,
                BoundingBoxReference.class,
                "Bounding box reference"
            );
            return BoundingBoxReference.builder()
                .x(readRequired(
                    object,
                    "x",
                    Double.class,
                    BoundingBoxReference.class,
                    context
                ))
                .y(readRequired(
                    object,
                    "y",
                    Double.class,
                    BoundingBoxReference.class,
                    context
                ))
                .z(readRequired(
                    object,
                    "z",
                    Double.class,
                    BoundingBoxReference.class,
                    context
                ))
                .theta(readOptional(
                    object,
                    "theta",
                    Double.class,
                    BoundingBoxReference.class,
                    context
                ))
                .extensionFields(capture(
                    mapper,
                    object,
                    BOUNDING_BOX_REFERENCE_FIELDS
                ))
                .build();
        }
    }

    private static final class LoadDimensionsSerializer
        extends StdSerializer<LoadDimensions> {
        private LoadDimensionsSerializer() {
            super(LoadDimensions.class);
        }

        @Override
        public void serialize(
            LoadDimensions value,
            JsonGenerator generator,
            SerializerProvider provider
        ) throws IOException {
            ObjectMapper mapper = requireObjectMapper(generator);
            ObjectNode target = mapper.createObjectNode();
            target.put("length", value.length());
            target.put("width", value.width());
            putOptional(target, "height", value.height());
            merge(
                mapper,
                target,
                value.extensionFields(),
                LOAD_DIMENSIONS_FIELDS
            );
            generator.writeTree(target);
        }
    }

    private static final class LoadDimensionsDeserializer
        extends StdDeserializer<LoadDimensions> {
        private LoadDimensionsDeserializer() {
            super(LoadDimensions.class);
        }

        @Override
        public LoadDimensions deserialize(
            JsonParser parser,
            DeserializationContext context
        ) throws IOException {
            ObjectMapper mapper = requireObjectMapper(parser);
            ObjectNode object = readObject(
                mapper,
                parser,
                context,
                LoadDimensions.class,
                "Load dimensions"
            );
            return LoadDimensions.builder()
                .length(readRequired(
                    object,
                    "length",
                    Double.class,
                    LoadDimensions.class,
                    context
                ))
                .width(readRequired(
                    object,
                    "width",
                    Double.class,
                    LoadDimensions.class,
                    context
                ))
                .height(readOptional(
                    object,
                    "height",
                    Double.class,
                    LoadDimensions.class,
                    context
                ))
                .extensionFields(capture(
                    mapper,
                    object,
                    LOAD_DIMENSIONS_FIELDS
                ))
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
        Object value
    ) {
        if (value != null) {
            target.set(fieldName, mapper.valueToTree(value));
        }
    }
}
