package io.github.cooltheworld.rcs.protocol.vda5050.v3.codec;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.ExtensionFields;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.internal.ExtensionFieldsJacksonSupport;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.LocalizationType;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.MobileRobotClass;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.MobileRobotKinematics;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.NavigationType;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.PhysicalParameters;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.TypeSpecification;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.ZoneType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/** Factsheet 子对象的包内 Jackson 映射支持。 */
final class FactsheetFragmentJacksonSupport {
    private static final Set<String> TYPE_SPECIFICATION_FIELDS = Set.of(
        "seriesName",
        "seriesDescription",
        "mobileRobotKinematics",
        "mobileRobotClass",
        "maximumLoadMass",
        "localizationTypes",
        "navigationTypes",
        "supportedZones"
    );
    private static final Set<String> PHYSICAL_PARAMETERS_FIELDS = Set.of(
        "minimumSpeed",
        "maximumSpeed",
        "minimumAngularSpeed",
        "maximumAngularSpeed",
        "maximumAcceleration",
        "maximumDeceleration",
        "minimumHeight",
        "maximumHeight",
        "width",
        "length"
    );

    private FactsheetFragmentJacksonSupport() {}

    static void register(SimpleModule module) {
        addStringValue(
            module,
            MobileRobotKinematics.class,
            MobileRobotKinematics::value,
            MobileRobotKinematics::of
        );
        addStringValue(
            module,
            MobileRobotClass.class,
            MobileRobotClass::value,
            MobileRobotClass::of
        );
        addStringValue(
            module,
            LocalizationType.class,
            LocalizationType::value,
            LocalizationType::of
        );
        addStringValue(
            module,
            NavigationType.class,
            NavigationType::value,
            NavigationType::of
        );
        module.addSerializer(
            TypeSpecification.class,
            new TypeSpecificationSerializer()
        );
        module.addDeserializer(
            TypeSpecification.class,
            new TypeSpecificationDeserializer()
        );
        module.addSerializer(
            PhysicalParameters.class,
            new PhysicalParametersSerializer()
        );
        module.addDeserializer(
            PhysicalParameters.class,
            new PhysicalParametersDeserializer()
        );
    }

    private static <T> void addStringValue(
        SimpleModule module,
        Class<T> valueType,
        Function<T, String> valueReader,
        Function<String, T> valueFactory
    ) {
        module.addSerializer(
            valueType,
            new StringValueSerializer<>(valueType, valueReader)
        );
        module.addDeserializer(
            valueType,
            new StringValueDeserializer<>(valueType, valueFactory)
        );
    }

    private static final class StringValueSerializer<T>
        extends StdScalarSerializer<T> {
        private final Function<T, String> valueReader;

        private StringValueSerializer(
            Class<T> valueType,
            Function<T, String> valueReader
        ) {
            super(valueType);
            this.valueReader = valueReader;
        }

        @Override
        public void serialize(
            T value,
            JsonGenerator generator,
            SerializerProvider provider
        ) throws IOException {
            generator.writeString(valueReader.apply(value));
        }
    }

    private static final class StringValueDeserializer<T>
        extends StdDeserializer<T> {
        private final Class<T> valueType;
        private final Function<String, T> valueFactory;

        private StringValueDeserializer(
            Class<T> valueType,
            Function<String, T> valueFactory
        ) {
            super(valueType);
            this.valueType = valueType;
            this.valueFactory = valueFactory;
        }

        @Override
        public T deserialize(JsonParser parser, DeserializationContext context)
            throws IOException {
            if (!parser.hasToken(JsonToken.VALUE_STRING)) {
                return valueType.cast(context.handleUnexpectedToken(valueType, parser));
            }
            return valueFactory.apply(parser.getText());
        }
    }

    private static final class TypeSpecificationSerializer
        extends StdSerializer<TypeSpecification> {
        private TypeSpecificationSerializer() {
            super(TypeSpecification.class);
        }

        @Override
        public void serialize(
            TypeSpecification value,
            JsonGenerator generator,
            SerializerProvider provider
        ) throws IOException {
            ObjectMapper mapper = requireObjectMapper(generator);
            ObjectNode target = mapper.createObjectNode();
            target.put("seriesName", value.seriesName());
            putOptional(target, "seriesDescription", value.seriesDescription());
            target.put(
                "mobileRobotKinematics",
                value.mobileRobotKinematics().value()
            );
            target.put("mobileRobotClass", value.mobileRobotClass().value());
            target.put("maximumLoadMass", value.maximumLoadMass());
            putStringValues(
                target.putArray("localizationTypes"),
                value.localizationTypes(),
                LocalizationType::value
            );
            putStringValues(
                target.putArray("navigationTypes"),
                value.navigationTypes(),
                NavigationType::value
            );
            if (value.supportedZones() != null) {
                ArrayNode zones = target.putArray("supportedZones");
                value.supportedZones().forEach(zone -> zones.add(zone.name()));
            }
            ExtensionFieldsJacksonSupport.merge(
                mapper,
                target,
                value.extensionFields(),
                TYPE_SPECIFICATION_FIELDS
            );
            generator.writeTree(target);
        }
    }

    private static final class TypeSpecificationDeserializer
        extends StdDeserializer<TypeSpecification> {
        private TypeSpecificationDeserializer() {
            super(TypeSpecification.class);
        }

        @Override
        public TypeSpecification deserialize(
            JsonParser parser,
            DeserializationContext context
        ) throws IOException {
            ObjectMapper mapper = requireObjectMapper(parser);
            ObjectNode object = readObject(
                mapper,
                parser,
                context,
                TypeSpecification.class,
                "Type specification"
            );
            String seriesName = readRequired(
                object,
                "seriesName",
                String.class,
                TypeSpecification.class,
                context
            );
            String seriesDescription = readOptional(
                object,
                "seriesDescription",
                String.class,
                TypeSpecification.class,
                context
            );
            MobileRobotKinematics mobileRobotKinematics = readRequired(
                object,
                "mobileRobotKinematics",
                MobileRobotKinematics.class,
                TypeSpecification.class,
                context
            );
            MobileRobotClass mobileRobotClass = readRequired(
                object,
                "mobileRobotClass",
                MobileRobotClass.class,
                TypeSpecification.class,
                context
            );
            Double maximumLoadMass = readRequired(
                object,
                "maximumLoadMass",
                Double.class,
                TypeSpecification.class,
                context
            );
            List<LocalizationType> localizationTypes = readRequiredList(
                object,
                "localizationTypes",
                LocalizationType.class,
                TypeSpecification.class,
                context
            );
            List<NavigationType> navigationTypes = readRequiredList(
                object,
                "navigationTypes",
                NavigationType.class,
                TypeSpecification.class,
                context
            );
            List<ZoneType> supportedZones = readOptionalList(
                object,
                "supportedZones",
                ZoneType.class,
                TypeSpecification.class,
                context
            );
            ExtensionFields extensionFields = ExtensionFieldsJacksonSupport.capture(
                mapper,
                object,
                TYPE_SPECIFICATION_FIELDS
            );
            try {
                return TypeSpecification.builder()
                    .seriesName(seriesName)
                    .seriesDescription(seriesDescription)
                    .mobileRobotKinematics(mobileRobotKinematics)
                    .mobileRobotClass(mobileRobotClass)
                    .maximumLoadMass(maximumLoadMass)
                    .localizationTypes(localizationTypes)
                    .navigationTypes(navigationTypes)
                    .supportedZones(supportedZones)
                    .extensionFields(extensionFields)
                    .build();
            } catch (IllegalArgumentException | NullPointerException exception) {
                return context.reportInputMismatch(
                    TypeSpecification.class,
                    "Type specification fields do not satisfy model constraints"
                );
            }
        }
    }

    private static final class PhysicalParametersSerializer
        extends StdSerializer<PhysicalParameters> {
        private PhysicalParametersSerializer() {
            super(PhysicalParameters.class);
        }

        @Override
        public void serialize(
            PhysicalParameters value,
            JsonGenerator generator,
            SerializerProvider provider
        ) throws IOException {
            ObjectMapper mapper = requireObjectMapper(generator);
            ObjectNode target = mapper.createObjectNode();
            target.put("minimumSpeed", value.minimumSpeed());
            target.put("maximumSpeed", value.maximumSpeed());
            putOptional(
                target,
                "minimumAngularSpeed",
                value.minimumAngularSpeed()
            );
            putOptional(
                target,
                "maximumAngularSpeed",
                value.maximumAngularSpeed()
            );
            target.put("maximumAcceleration", value.maximumAcceleration());
            target.put("maximumDeceleration", value.maximumDeceleration());
            target.put("minimumHeight", value.minimumHeight());
            target.put("maximumHeight", value.maximumHeight());
            target.put("width", value.width());
            target.put("length", value.length());
            ExtensionFieldsJacksonSupport.merge(
                mapper,
                target,
                value.extensionFields(),
                PHYSICAL_PARAMETERS_FIELDS
            );
            generator.writeTree(target);
        }
    }

    private static final class PhysicalParametersDeserializer
        extends StdDeserializer<PhysicalParameters> {
        private PhysicalParametersDeserializer() {
            super(PhysicalParameters.class);
        }

        @Override
        public PhysicalParameters deserialize(
            JsonParser parser,
            DeserializationContext context
        ) throws IOException {
            ObjectMapper mapper = requireObjectMapper(parser);
            ObjectNode object = readObject(
                mapper,
                parser,
                context,
                PhysicalParameters.class,
                "Physical parameters"
            );
            PhysicalParameters.Builder builder = PhysicalParameters.builder()
                .minimumSpeed(readRequired(
                    object,
                    "minimumSpeed",
                    Double.class,
                    PhysicalParameters.class,
                    context
                ))
                .maximumSpeed(readRequired(
                    object,
                    "maximumSpeed",
                    Double.class,
                    PhysicalParameters.class,
                    context
                ))
                .minimumAngularSpeed(readOptional(
                    object,
                    "minimumAngularSpeed",
                    Double.class,
                    PhysicalParameters.class,
                    context
                ))
                .maximumAngularSpeed(readOptional(
                    object,
                    "maximumAngularSpeed",
                    Double.class,
                    PhysicalParameters.class,
                    context
                ))
                .maximumAcceleration(readRequired(
                    object,
                    "maximumAcceleration",
                    Double.class,
                    PhysicalParameters.class,
                    context
                ))
                .maximumDeceleration(readRequired(
                    object,
                    "maximumDeceleration",
                    Double.class,
                    PhysicalParameters.class,
                    context
                ))
                .minimumHeight(readRequired(
                    object,
                    "minimumHeight",
                    Double.class,
                    PhysicalParameters.class,
                    context
                ))
                .maximumHeight(readRequired(
                    object,
                    "maximumHeight",
                    Double.class,
                    PhysicalParameters.class,
                    context
                ))
                .width(readRequired(
                    object,
                    "width",
                    Double.class,
                    PhysicalParameters.class,
                    context
                ))
                .length(readRequired(
                    object,
                    "length",
                    Double.class,
                    PhysicalParameters.class,
                    context
                ))
                .extensionFields(ExtensionFieldsJacksonSupport.capture(
                    mapper,
                    object,
                    PHYSICAL_PARAMETERS_FIELDS
                ));
            try {
                return builder.build();
            } catch (IllegalArgumentException | NullPointerException exception) {
                return context.reportInputMismatch(
                    PhysicalParameters.class,
                    "Physical parameter fields do not satisfy model constraints"
                );
            }
        }
    }

    private static <T> void putStringValues(
        ArrayNode target,
        List<T> values,
        Function<T, String> valueReader
    ) {
        values.forEach(value -> target.add(valueReader.apply(value)));
    }

    static void putOptional(
        ObjectNode target,
        String fieldName,
        String value
    ) {
        if (value != null) {
            target.put(fieldName, value);
        }
    }

    static void putOptional(
        ObjectNode target,
        String fieldName,
        Double value
    ) {
        if (value != null) {
            target.put(fieldName, value);
        }
    }

    static ObjectNode readObject(
        ObjectMapper mapper,
        JsonParser parser,
        DeserializationContext context,
        Class<?> targetType,
        String displayName
    ) throws IOException {
        JsonNode tree = mapper.readTree(parser);
        if (tree instanceof ObjectNode object) {
            return object;
        }
        return context.reportInputMismatch(
            targetType,
            displayName + " must be a JSON object"
        );
    }

    static <T> T readRequired(
        ObjectNode object,
        String fieldName,
        Class<T> fieldType,
        Class<?> ownerType,
        DeserializationContext context
    ) throws IOException {
        JsonNode value = object.get(fieldName);
        if (value == null) {
            return context.reportInputMismatch(
                ownerType,
                "Protocol object is missing a required field"
            );
        }
        return readValue(value, fieldName, fieldType, ownerType, context);
    }

    static <T> T readOptional(
        ObjectNode object,
        String fieldName,
        Class<T> fieldType,
        Class<?> ownerType,
        DeserializationContext context
    ) throws IOException {
        JsonNode value = object.get(fieldName);
        return value == null
            ? null
            : readValue(value, fieldName, fieldType, ownerType, context);
    }

    private static <T> T readValue(
        JsonNode value,
        String fieldName,
        Class<T> fieldType,
        Class<?> ownerType,
        DeserializationContext context
    ) throws IOException {
        try {
            return context.readTreeAsValue(value, fieldType);
        } catch (JsonMappingException exception) {
            exception.prependPath(ownerType, fieldName);
            throw exception;
        }
    }

    static <T> List<T> readRequiredList(
        ObjectNode object,
        String fieldName,
        Class<T> elementType,
        Class<?> ownerType,
        DeserializationContext context
    ) throws IOException {
        JsonNode value = object.get(fieldName);
        if (value == null) {
            return context.reportInputMismatch(
                ownerType,
                "Protocol object is missing a required field"
            );
        }
        return readList(value, fieldName, elementType, ownerType, context);
    }

    static <T> List<T> readOptionalList(
        ObjectNode object,
        String fieldName,
        Class<T> elementType,
        Class<?> ownerType,
        DeserializationContext context
    ) throws IOException {
        JsonNode value = object.get(fieldName);
        return value == null
            ? null
            : readList(value, fieldName, elementType, ownerType, context);
    }

    private static <T> List<T> readList(
        JsonNode value,
        String fieldName,
        Class<T> elementType,
        Class<?> ownerType,
        DeserializationContext context
    ) throws IOException {
        if (!value.isArray()) {
            return context.reportInputMismatch(
                ownerType,
                "Protocol list field must be a JSON array"
            );
        }
        List<T> result = new ArrayList<>();
        int index = 0;
        for (JsonNode element : value) {
            try {
                result.add(context.readTreeAsValue(element, elementType));
            } catch (JsonMappingException exception) {
                exception.prependPath(ownerType, fieldName);
                exception.prependPath(result, index);
                throw exception;
            }
            index++;
        }
        return List.copyOf(result);
    }

    static ObjectMapper requireObjectMapper(JsonGenerator generator)
        throws IOException {
        if (generator.getCodec() instanceof ObjectMapper mapper) {
            return mapper;
        }
        throw new IOException("VDA 5050 Module requires an ObjectMapper codec");
    }

    static ObjectMapper requireObjectMapper(JsonParser parser)
        throws IOException {
        if (parser.getCodec() instanceof ObjectMapper mapper) {
            return mapper;
        }
        throw new IOException("VDA 5050 Module requires an ObjectMapper codec");
    }
}
