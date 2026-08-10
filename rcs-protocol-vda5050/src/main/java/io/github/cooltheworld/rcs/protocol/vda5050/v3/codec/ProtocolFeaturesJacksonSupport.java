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
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.ExtensionFields;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.internal.ExtensionFieldsJacksonSupport;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.action.ActionParameterDefinition;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.action.ActionScope;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.action.ActionValueDataType;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.action.BlockingType;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.action.MobileRobotAction;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.OptionalParameter;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.OptionalParameterSupport;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.ProtocolFeatures;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/** 注册 Factsheet {@code protocolFeatures} 子树的 Jackson 线路映射。 */
final class ProtocolFeaturesJacksonSupport {
    private static final Set<String> PROTOCOL_FEATURES_FIELDS = Set.of(
        "optionalParameters",
        "mobileRobotActions"
    );
    private static final Set<String> OPTIONAL_PARAMETER_FIELDS = Set.of(
        "parameter",
        "support",
        "description"
    );
    private static final Set<String> MOBILE_ROBOT_ACTION_FIELDS = Set.of(
        "actionType",
        "actionDescription",
        "actionScopes",
        "actionParameters",
        "actionResult",
        "blockingTypes",
        "pauseAllowed",
        "cancelAllowed"
    );
    private static final Set<String> ACTION_PARAMETER_FIELDS = Set.of(
        "key",
        "valueDataType",
        "description",
        "isOptional"
    );

    private ProtocolFeaturesJacksonSupport() {}

    static void register(SimpleModule module) {
        module.addSerializer(
            ProtocolFeatures.class,
            new ProtocolFeaturesSerializer()
        );
        module.addDeserializer(
            ProtocolFeatures.class,
            new ProtocolFeaturesDeserializer()
        );
        module.addSerializer(
            OptionalParameter.class,
            new OptionalParameterSerializer()
        );
        module.addDeserializer(
            OptionalParameter.class,
            new OptionalParameterDeserializer()
        );
        module.addSerializer(
            MobileRobotAction.class,
            new MobileRobotActionSerializer()
        );
        module.addDeserializer(
            MobileRobotAction.class,
            new MobileRobotActionDeserializer()
        );
        module.addSerializer(
            ActionParameterDefinition.class,
            new ActionParameterDefinitionSerializer()
        );
        module.addDeserializer(
            ActionParameterDefinition.class,
            new ActionParameterDefinitionDeserializer()
        );
    }

    private static final class ProtocolFeaturesSerializer
        extends StdSerializer<ProtocolFeatures> {
        private ProtocolFeaturesSerializer() {
            super(ProtocolFeatures.class);
        }

        @Override
        public void serialize(
            ProtocolFeatures value,
            JsonGenerator generator,
            SerializerProvider provider
        ) throws IOException {
            ObjectMapper mapper = requireObjectMapper(generator);
            ObjectNode target = mapper.createObjectNode();
            target.set(
                "optionalParameters",
                mapper.valueToTree(value.optionalParameters())
            );
            target.set(
                "mobileRobotActions",
                mapper.valueToTree(value.mobileRobotActions())
            );
            merge(
                mapper,
                target,
                value.extensionFields(),
                PROTOCOL_FEATURES_FIELDS
            );
            generator.writeTree(target);
        }
    }

    private static final class ProtocolFeaturesDeserializer
        extends StdDeserializer<ProtocolFeatures> {
        private ProtocolFeaturesDeserializer() {
            super(ProtocolFeatures.class);
        }

        @Override
        public ProtocolFeatures deserialize(
            JsonParser parser,
            DeserializationContext context
        ) throws IOException {
            ObjectMapper mapper = requireObjectMapper(parser);
            ObjectNode object = readObject(
                mapper,
                parser,
                context,
                ProtocolFeatures.class,
                "Protocol features"
            );
            return ProtocolFeatures.builder()
                .optionalParameters(readRequiredList(
                    object,
                    "optionalParameters",
                    OptionalParameter.class,
                    ProtocolFeatures.class,
                    context
                ))
                .mobileRobotActions(readRequiredList(
                    object,
                    "mobileRobotActions",
                    MobileRobotAction.class,
                    ProtocolFeatures.class,
                    context
                ))
                .extensionFields(capture(
                    mapper,
                    object,
                    PROTOCOL_FEATURES_FIELDS
                ))
                .build();
        }
    }

    private static final class OptionalParameterSerializer
        extends StdSerializer<OptionalParameter> {
        private OptionalParameterSerializer() {
            super(OptionalParameter.class);
        }

        @Override
        public void serialize(
            OptionalParameter value,
            JsonGenerator generator,
            SerializerProvider provider
        ) throws IOException {
            ObjectMapper mapper = requireObjectMapper(generator);
            ObjectNode target = mapper.createObjectNode();
            target.put("parameter", value.parameter());
            target.put("support", value.support().name());
            putOptional(target, "description", value.description());
            merge(
                mapper,
                target,
                value.extensionFields(),
                OPTIONAL_PARAMETER_FIELDS
            );
            generator.writeTree(target);
        }
    }

    private static final class OptionalParameterDeserializer
        extends StdDeserializer<OptionalParameter> {
        private OptionalParameterDeserializer() {
            super(OptionalParameter.class);
        }

        @Override
        public OptionalParameter deserialize(
            JsonParser parser,
            DeserializationContext context
        ) throws IOException {
            ObjectMapper mapper = requireObjectMapper(parser);
            ObjectNode object = readObject(
                mapper,
                parser,
                context,
                OptionalParameter.class,
                "Optional parameter"
            );
            return OptionalParameter.builder()
                .parameter(readRequired(
                    object,
                    "parameter",
                    String.class,
                    OptionalParameter.class,
                    context
                ))
                .support(readRequired(
                    object,
                    "support",
                    OptionalParameterSupport.class,
                    OptionalParameter.class,
                    context
                ))
                .description(readOptional(
                    object,
                    "description",
                    String.class,
                    OptionalParameter.class,
                    context
                ))
                .extensionFields(capture(
                    mapper,
                    object,
                    OPTIONAL_PARAMETER_FIELDS
                ))
                .build();
        }
    }

    private static final class MobileRobotActionSerializer
        extends StdSerializer<MobileRobotAction> {
        private MobileRobotActionSerializer() {
            super(MobileRobotAction.class);
        }

        @Override
        public void serialize(
            MobileRobotAction value,
            JsonGenerator generator,
            SerializerProvider provider
        ) throws IOException {
            ObjectMapper mapper = requireObjectMapper(generator);
            ObjectNode target = mapper.createObjectNode();
            target.put("actionType", value.actionType());
            putOptional(target, "actionDescription", value.actionDescription());
            target.set("actionScopes", mapper.valueToTree(value.actionScopes()));
            putOptionalTree(
                mapper,
                target,
                "actionParameters",
                value.actionParameters()
            );
            putOptional(target, "actionResult", value.actionResult());
            putOptionalTree(
                mapper,
                target,
                "blockingTypes",
                value.blockingTypes()
            );
            target.put("pauseAllowed", value.pauseAllowed());
            target.put("cancelAllowed", value.cancelAllowed());
            merge(
                mapper,
                target,
                value.extensionFields(),
                MOBILE_ROBOT_ACTION_FIELDS
            );
            generator.writeTree(target);
        }
    }

    private static final class MobileRobotActionDeserializer
        extends StdDeserializer<MobileRobotAction> {
        private MobileRobotActionDeserializer() {
            super(MobileRobotAction.class);
        }

        @Override
        public MobileRobotAction deserialize(
            JsonParser parser,
            DeserializationContext context
        ) throws IOException {
            ObjectMapper mapper = requireObjectMapper(parser);
            ObjectNode object = readObject(
                mapper,
                parser,
                context,
                MobileRobotAction.class,
                "Mobile robot action"
            );
            return MobileRobotAction.builder()
                .actionType(readRequired(
                    object,
                    "actionType",
                    String.class,
                    MobileRobotAction.class,
                    context
                ))
                .actionDescription(readOptional(
                    object,
                    "actionDescription",
                    String.class,
                    MobileRobotAction.class,
                    context
                ))
                .actionScopes(readRequiredList(
                    object,
                    "actionScopes",
                    ActionScope.class,
                    MobileRobotAction.class,
                    context
                ))
                .actionParameters(readOptionalList(
                    object,
                    "actionParameters",
                    ActionParameterDefinition.class,
                    MobileRobotAction.class,
                    context
                ))
                .actionResult(readOptional(
                    object,
                    "actionResult",
                    String.class,
                    MobileRobotAction.class,
                    context
                ))
                .blockingTypes(readOptionalList(
                    object,
                    "blockingTypes",
                    BlockingType.class,
                    MobileRobotAction.class,
                    context
                ))
                .pauseAllowed(readRequired(
                    object,
                    "pauseAllowed",
                    Boolean.class,
                    MobileRobotAction.class,
                    context
                ))
                .cancelAllowed(readRequired(
                    object,
                    "cancelAllowed",
                    Boolean.class,
                    MobileRobotAction.class,
                    context
                ))
                .extensionFields(capture(
                    mapper,
                    object,
                    MOBILE_ROBOT_ACTION_FIELDS
                ))
                .build();
        }
    }

    private static final class ActionParameterDefinitionSerializer
        extends StdSerializer<ActionParameterDefinition> {
        private ActionParameterDefinitionSerializer() {
            super(ActionParameterDefinition.class);
        }

        @Override
        public void serialize(
            ActionParameterDefinition value,
            JsonGenerator generator,
            SerializerProvider provider
        ) throws IOException {
            ObjectMapper mapper = requireObjectMapper(generator);
            ObjectNode target = mapper.createObjectNode();
            target.put("key", value.key());
            target.put("valueDataType", value.valueDataType().name());
            putOptional(target, "description", value.description());
            if (value.isOptional() != null) {
                target.put("isOptional", value.isOptional());
            }
            merge(
                mapper,
                target,
                value.extensionFields(),
                ACTION_PARAMETER_FIELDS
            );
            generator.writeTree(target);
        }
    }

    private static final class ActionParameterDefinitionDeserializer
        extends StdDeserializer<ActionParameterDefinition> {
        private ActionParameterDefinitionDeserializer() {
            super(ActionParameterDefinition.class);
        }

        @Override
        public ActionParameterDefinition deserialize(
            JsonParser parser,
            DeserializationContext context
        ) throws IOException {
            ObjectMapper mapper = requireObjectMapper(parser);
            ObjectNode object = readObject(
                mapper,
                parser,
                context,
                ActionParameterDefinition.class,
                "Action parameter definition"
            );
            return ActionParameterDefinition.builder()
                .key(readRequired(
                    object,
                    "key",
                    String.class,
                    ActionParameterDefinition.class,
                    context
                ))
                .valueDataType(readRequired(
                    object,
                    "valueDataType",
                    ActionValueDataType.class,
                    ActionParameterDefinition.class,
                    context
                ))
                .description(readOptional(
                    object,
                    "description",
                    String.class,
                    ActionParameterDefinition.class,
                    context
                ))
                .isOptional(readOptional(
                    object,
                    "isOptional",
                    Boolean.class,
                    ActionParameterDefinition.class,
                    context
                ))
                .extensionFields(capture(
                    mapper,
                    object,
                    ACTION_PARAMETER_FIELDS
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
        List<?> value
    ) {
        if (value != null) {
            target.set(fieldName, mapper.valueToTree(value));
        }
    }
}
