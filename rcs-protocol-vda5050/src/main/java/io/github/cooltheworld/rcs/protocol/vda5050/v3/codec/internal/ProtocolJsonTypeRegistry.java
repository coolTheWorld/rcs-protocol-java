package io.github.cooltheworld.rcs.protocol.vda5050.v3.codec.internal;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.action.Action;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.action.ActionParameter;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.action.ActionParameterDefinition;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.action.ActionParameterValue;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.action.ActionScope;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.action.ActionValueDataType;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.action.BlockingType;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.connection.Connection;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.connection.ConnectionState;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.BoundingBoxReference;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.BatteryCharging;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.Envelope2d;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.Envelope2dVertex;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.Envelope3d;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.Envelope3dData;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.Factsheet;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.LoadDimensions;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.LoadSet;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.LoadSpecification;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.LocalizationType;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.MaximumArrayLengths;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.MaximumStringLengths;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.action.MobileRobotAction;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.MobileRobotGeometry;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.MobileRobotConfiguration;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.NavigationType;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.NetworkConfiguration;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.OptionalParameter;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.OptionalParameterSupport;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.PhysicalParameters;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.ProtocolFeatures;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.ProtocolLimits;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.ProtocolTiming;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.common.ProtocolTimestamp;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.common.ProtocolVersion;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.order.AllowedDeviationXY;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.order.Node;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.order.NodePosition;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.TypeSpecification;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.VersionInfo;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.WheelDefinition;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.WheelPosition;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.WheelType;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.ZoneType;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** 为自定义反序列化的不可变协议消息提供显式线路字段元数据。 */
final class ProtocolJsonTypeRegistry {
    private ProtocolJsonTypeRegistry() {}

    static Optional<Map<String, JavaType>> findProperties(
        ObjectMapper mapper,
        JavaType type
    ) {
        if (type.hasRawClass(Connection.class)) {
            return Optional.of(connectionProperties(mapper));
        }
        if (type.hasRawClass(Action.class)) {
            return Optional.of(actionProperties(mapper));
        }
        if (type.hasRawClass(ActionParameter.class)) {
            return Optional.of(actionParameterProperties(mapper));
        }
        if (type.hasRawClass(AllowedDeviationXY.class)) {
            return Optional.of(allowedDeviationXYProperties(mapper));
        }
        if (type.hasRawClass(NodePosition.class)) {
            return Optional.of(nodePositionProperties(mapper));
        }
        if (type.hasRawClass(Node.class)) {
            return Optional.of(nodeProperties(mapper));
        }
        if (type.hasRawClass(Factsheet.class)) {
            return Optional.of(factsheetProperties(mapper));
        }
        if (type.hasRawClass(TypeSpecification.class)) {
            return Optional.of(typeSpecificationProperties(mapper));
        }
        if (type.hasRawClass(PhysicalParameters.class)) {
            return Optional.of(physicalParametersProperties(mapper));
        }
        if (type.hasRawClass(MobileRobotConfiguration.class)) {
            return Optional.of(mobileRobotConfigurationProperties(mapper));
        }
        if (type.hasRawClass(VersionInfo.class)) {
            return Optional.of(versionInfoProperties(mapper));
        }
        if (type.hasRawClass(NetworkConfiguration.class)) {
            return Optional.of(networkConfigurationProperties(mapper));
        }
        if (type.hasRawClass(BatteryCharging.class)) {
            return Optional.of(batteryChargingProperties(mapper));
        }
        if (type.hasRawClass(LoadSpecification.class)) {
            return Optional.of(loadSpecificationProperties(mapper));
        }
        if (type.hasRawClass(LoadSet.class)) {
            return Optional.of(loadSetProperties(mapper));
        }
        if (type.hasRawClass(BoundingBoxReference.class)) {
            return Optional.of(boundingBoxReferenceProperties(mapper));
        }
        if (type.hasRawClass(LoadDimensions.class)) {
            return Optional.of(loadDimensionsProperties(mapper));
        }
        if (type.hasRawClass(ProtocolLimits.class)) {
            return Optional.of(protocolLimitsProperties(mapper));
        }
        if (type.hasRawClass(MaximumStringLengths.class)) {
            return Optional.of(maximumStringLengthsProperties(mapper));
        }
        if (type.hasRawClass(MaximumArrayLengths.class)) {
            return Optional.of(maximumArrayLengthsProperties(mapper));
        }
        if (type.hasRawClass(ProtocolTiming.class)) {
            return Optional.of(protocolTimingProperties(mapper));
        }
        if (type.hasRawClass(ProtocolFeatures.class)) {
            return Optional.of(protocolFeaturesProperties(mapper));
        }
        if (type.hasRawClass(OptionalParameter.class)) {
            return Optional.of(optionalParameterProperties(mapper));
        }
        if (type.hasRawClass(MobileRobotAction.class)) {
            return Optional.of(mobileRobotActionProperties(mapper));
        }
        if (type.hasRawClass(ActionParameterDefinition.class)) {
            return Optional.of(actionParameterDefinitionProperties(mapper));
        }
        if (type.hasRawClass(MobileRobotGeometry.class)) {
            return Optional.of(mobileRobotGeometryProperties(mapper));
        }
        if (type.hasRawClass(WheelDefinition.class)) {
            return Optional.of(wheelDefinitionProperties(mapper));
        }
        if (type.hasRawClass(WheelPosition.class)) {
            return Optional.of(wheelPositionProperties(mapper));
        }
        if (type.hasRawClass(Envelope2d.class)) {
            return Optional.of(envelope2dProperties(mapper));
        }
        if (type.hasRawClass(Envelope2dVertex.class)) {
            return Optional.of(envelope2dVertexProperties(mapper));
        }
        if (type.hasRawClass(Envelope3d.class)) {
            return Optional.of(envelope3dProperties(mapper));
        }
        return Optional.empty();
    }

    private static Map<String, JavaType> connectionProperties(ObjectMapper mapper) {
        return Map.of(
            "headerId", mapper.constructType(Long.class),
            "timestamp", mapper.constructType(ProtocolTimestamp.class),
            "version", mapper.constructType(ProtocolVersion.class),
            "manufacturer", mapper.constructType(String.class),
            "serialNumber", mapper.constructType(String.class),
            "connectionState", mapper.constructType(ConnectionState.class)
        );
    }

    private static Map<String, JavaType> actionProperties(ObjectMapper mapper) {
        return Map.of(
            "actionType", mapper.constructType(String.class),
            "actionId", mapper.constructType(String.class),
            "actionDescriptor", mapper.constructType(String.class),
            "blockingType", mapper.constructType(BlockingType.class),
            "actionParameters", listType(mapper, ActionParameter.class),
            "retriable", mapper.constructType(Boolean.class)
        );
    }

    private static Map<String, JavaType> actionParameterProperties(
        ObjectMapper mapper
    ) {
        return Map.of(
            "key", mapper.constructType(String.class),
            "value", mapper.constructType(ActionParameterValue.class)
        );
    }

    private static Map<String, JavaType> allowedDeviationXYProperties(
        ObjectMapper mapper
    ) {
        JavaType numberType = mapper.constructType(Double.class);
        return Map.of(
            "a", numberType,
            "b", numberType,
            "theta", numberType
        );
    }

    private static Map<String, JavaType> nodePositionProperties(
        ObjectMapper mapper
    ) {
        JavaType numberType = mapper.constructType(Double.class);
        return Map.of(
            "x", numberType,
            "y", numberType,
            "theta", numberType,
            "allowedDeviationXY",
            mapper.constructType(AllowedDeviationXY.class),
            "allowedDeviationTheta", numberType,
            "mapId", mapper.constructType(String.class)
        );
    }

    private static Map<String, JavaType> nodeProperties(ObjectMapper mapper) {
        return Map.of(
            "nodeId", mapper.constructType(String.class),
            "sequenceId", mapper.constructType(Long.class),
            "nodeDescriptor", mapper.constructType(String.class),
            "released", mapper.constructType(Boolean.class),
            "nodePosition", mapper.constructType(NodePosition.class),
            "actions", listType(mapper, Action.class)
        );
    }

    private static Map<String, JavaType> factsheetProperties(ObjectMapper mapper) {
        return Map.ofEntries(
            Map.entry("headerId", mapper.constructType(Long.class)),
            Map.entry(
                "timestamp",
                mapper.constructType(ProtocolTimestamp.class)
            ),
            Map.entry("version", mapper.constructType(ProtocolVersion.class)),
            Map.entry("manufacturer", mapper.constructType(String.class)),
            Map.entry("serialNumber", mapper.constructType(String.class)),
            Map.entry(
                "typeSpecification",
                mapper.constructType(TypeSpecification.class)
            ),
            Map.entry(
                "physicalParameters",
                mapper.constructType(PhysicalParameters.class)
            ),
            Map.entry(
                "protocolLimits",
                mapper.constructType(ProtocolLimits.class)
            ),
            Map.entry(
                "protocolFeatures",
                mapper.constructType(ProtocolFeatures.class)
            ),
            Map.entry(
                "mobileRobotGeometry",
                mapper.constructType(MobileRobotGeometry.class)
            ),
            Map.entry(
                "loadSpecification",
                mapper.constructType(LoadSpecification.class)
            ),
            Map.entry(
                "mobileRobotConfiguration",
                mapper.constructType(MobileRobotConfiguration.class)
            )
        );
    }

    private static Map<String, JavaType> typeSpecificationProperties(
        ObjectMapper mapper
    ) {
        return Map.of(
            "seriesName", mapper.constructType(String.class),
            "seriesDescription", mapper.constructType(String.class),
            "mobileRobotKinematics", mapper.constructType(String.class),
            "mobileRobotClass", mapper.constructType(String.class),
            "maximumLoadMass", mapper.constructType(Double.class),
            "localizationTypes", mapper.getTypeFactory().constructCollectionType(
                List.class,
                LocalizationType.class
            ),
            "navigationTypes", mapper.getTypeFactory().constructCollectionType(
                List.class,
                NavigationType.class
            ),
            "supportedZones", mapper.getTypeFactory().constructCollectionType(
                List.class,
                ZoneType.class
            )
        );
    }

    private static Map<String, JavaType> physicalParametersProperties(
        ObjectMapper mapper
    ) {
        JavaType numberType = mapper.constructType(Double.class);
        return Map.of(
            "minimumSpeed", numberType,
            "maximumSpeed", numberType,
            "minimumAngularSpeed", numberType,
            "maximumAngularSpeed", numberType,
            "maximumAcceleration", numberType,
            "maximumDeceleration", numberType,
            "minimumHeight", numberType,
            "maximumHeight", numberType,
            "width", numberType,
            "length", numberType
        );
    }

    private static Map<String, JavaType> mobileRobotConfigurationProperties(
        ObjectMapper mapper
    ) {
        return Map.of(
            "versions", listType(mapper, VersionInfo.class),
            "network", mapper.constructType(NetworkConfiguration.class),
            "batteryCharging", mapper.constructType(BatteryCharging.class)
        );
    }

    private static Map<String, JavaType> versionInfoProperties(
        ObjectMapper mapper
    ) {
        return Map.of(
            "key", mapper.constructType(String.class),
            "value", mapper.constructType(String.class)
        );
    }

    private static Map<String, JavaType> networkConfigurationProperties(
        ObjectMapper mapper
    ) {
        JavaType stringType = mapper.constructType(String.class);
        return Map.of(
            "dnsServers", listType(mapper, String.class),
            "ntpServers", listType(mapper, String.class),
            "localIpAddress", stringType,
            "netmask", stringType,
            "defaultGateway", stringType
        );
    }

    private static Map<String, JavaType> batteryChargingProperties(
        ObjectMapper mapper
    ) {
        JavaType numberType = mapper.constructType(Double.class);
        return Map.of(
            "criticalLowChargingLevel", numberType,
            "minimumDesiredChargingLevel", numberType,
            "maximumDesiredChargingLevel", numberType,
            "minimumChargingTime", mapper.constructType(Long.class)
        );
    }

    private static Map<String, JavaType> loadSpecificationProperties(
        ObjectMapper mapper
    ) {
        return Map.of(
            "loadPositions",
            listType(mapper, String.class),
            "loadSets",
            listType(mapper, LoadSet.class)
        );
    }

    private static Map<String, JavaType> loadSetProperties(ObjectMapper mapper) {
        JavaType numberType = mapper.constructType(Double.class);
        return Map.ofEntries(
            Map.entry("setName", mapper.constructType(String.class)),
            Map.entry("loadType", mapper.constructType(String.class)),
            Map.entry("loadPositions", listType(mapper, String.class)),
            Map.entry(
                "boundingBoxReference",
                mapper.constructType(BoundingBoxReference.class)
            ),
            Map.entry(
                "loadDimensions",
                mapper.constructType(LoadDimensions.class)
            ),
            Map.entry("maximumWeight", numberType),
            Map.entry("minimumLoadhandlingHeight", numberType),
            Map.entry("maximumLoadhandlingHeight", numberType),
            Map.entry("minimumLoadhandlingDepth", numberType),
            Map.entry("maximumLoadhandlingDepth", numberType),
            Map.entry("minimumLoadhandlingTilt", numberType),
            Map.entry("maximumLoadhandlingTilt", numberType),
            Map.entry("maximumSpeed", numberType),
            Map.entry("maximumAcceleration", numberType),
            Map.entry("maximumDeceleration", numberType),
            Map.entry("pickTime", numberType),
            Map.entry("dropTime", numberType),
            Map.entry("description", mapper.constructType(String.class))
        );
    }

    private static Map<String, JavaType> boundingBoxReferenceProperties(
        ObjectMapper mapper
    ) {
        JavaType numberType = mapper.constructType(Double.class);
        return Map.of(
            "x", numberType,
            "y", numberType,
            "z", numberType,
            "theta", numberType
        );
    }

    private static Map<String, JavaType> loadDimensionsProperties(
        ObjectMapper mapper
    ) {
        JavaType numberType = mapper.constructType(Double.class);
        return Map.of(
            "length", numberType,
            "width", numberType,
            "height", numberType
        );
    }

    private static Map<String, JavaType> protocolLimitsProperties(
        ObjectMapper mapper
    ) {
        return Map.of(
            "maximumStringLengths",
            mapper.constructType(MaximumStringLengths.class),
            "maximumArrayLengths",
            mapper.constructType(MaximumArrayLengths.class),
            "timing",
            mapper.constructType(ProtocolTiming.class)
        );
    }

    private static Map<String, JavaType> maximumStringLengthsProperties(
        ObjectMapper mapper
    ) {
        JavaType integerType = mapper.constructType(Long.class);
        return Map.of(
            "maximumMessageLength", integerType,
            "maximumTopicSerialLength", integerType,
            "maximumTopicElementLength", integerType,
            "maximumIdLength", integerType,
            "idNumericalOnly", mapper.constructType(Boolean.class),
            "maximumLoadIdLength", integerType
        );
    }

    private static Map<String, JavaType> maximumArrayLengthsProperties(
        ObjectMapper mapper
    ) {
        JavaType integerType = mapper.constructType(Long.class);
        return Map.ofEntries(
            Map.entry("order.nodes", integerType),
            Map.entry("order.edges", integerType),
            Map.entry("node.actions", integerType),
            Map.entry("edge.actions", integerType),
            Map.entry("actions.actionsParameters", integerType),
            Map.entry("instantActions", integerType),
            Map.entry("trajectory.knotVector", integerType),
            Map.entry("trajectory.controlPoints", integerType),
            Map.entry("zoneSet.zones", integerType),
            Map.entry("state.nodeStates", integerType),
            Map.entry("state.edgeStates", integerType),
            Map.entry("state.loads", integerType),
            Map.entry("state.actionStates", integerType),
            Map.entry("state.instantActionStates", integerType),
            Map.entry("state.zoneActionStates", integerType),
            Map.entry("state.errors", integerType),
            Map.entry("state.information", integerType),
            Map.entry("error.errorReferences", integerType),
            Map.entry("information.infoReferences", integerType)
        );
    }

    private static Map<String, JavaType> protocolTimingProperties(
        ObjectMapper mapper
    ) {
        JavaType numberType = mapper.constructType(Double.class);
        return Map.of(
            "minimumOrderInterval", numberType,
            "minimumStateInterval", numberType,
            "defaultStateInterval", numberType,
            "visualizationInterval", numberType
        );
    }

    private static Map<String, JavaType> protocolFeaturesProperties(
        ObjectMapper mapper
    ) {
        return Map.of(
            "optionalParameters",
            listType(mapper, OptionalParameter.class),
            "mobileRobotActions",
            listType(mapper, MobileRobotAction.class)
        );
    }

    private static Map<String, JavaType> optionalParameterProperties(
        ObjectMapper mapper
    ) {
        return Map.of(
            "parameter", mapper.constructType(String.class),
            "support", mapper.constructType(OptionalParameterSupport.class),
            "description", mapper.constructType(String.class)
        );
    }

    private static Map<String, JavaType> mobileRobotActionProperties(
        ObjectMapper mapper
    ) {
        return Map.of(
            "actionType", mapper.constructType(String.class),
            "actionDescription", mapper.constructType(String.class),
            "actionScopes", listType(mapper, ActionScope.class),
            "actionParameters", listType(mapper, ActionParameterDefinition.class),
            "actionResult", mapper.constructType(String.class),
            "blockingTypes", listType(mapper, BlockingType.class),
            "pauseAllowed", mapper.constructType(Boolean.class),
            "cancelAllowed", mapper.constructType(Boolean.class)
        );
    }

    private static Map<String, JavaType> actionParameterDefinitionProperties(
        ObjectMapper mapper
    ) {
        return Map.of(
            "key", mapper.constructType(String.class),
            "valueDataType", mapper.constructType(ActionValueDataType.class),
            "description", mapper.constructType(String.class),
            "isOptional", mapper.constructType(Boolean.class)
        );
    }

    private static Map<String, JavaType> mobileRobotGeometryProperties(
        ObjectMapper mapper
    ) {
        return Map.of(
            "wheelDefinitions", listType(mapper, WheelDefinition.class),
            "envelopes2d", listType(mapper, Envelope2d.class),
            "envelopes3d", listType(mapper, Envelope3d.class)
        );
    }

    private static Map<String, JavaType> wheelDefinitionProperties(
        ObjectMapper mapper
    ) {
        JavaType numberType = mapper.constructType(Double.class);
        return Map.of(
            "type", mapper.constructType(WheelType.class),
            "isActiveDriven", mapper.constructType(Boolean.class),
            "isActiveSteered", mapper.constructType(Boolean.class),
            "position", mapper.constructType(WheelPosition.class),
            "diameter", numberType,
            "width", numberType,
            "centerDisplacement", numberType,
            "constraints", mapper.constructType(String.class)
        );
    }

    private static Map<String, JavaType> wheelPositionProperties(
        ObjectMapper mapper
    ) {
        JavaType numberType = mapper.constructType(Double.class);
        return Map.of(
            "x", numberType,
            "y", numberType,
            "theta", numberType
        );
    }

    private static Map<String, JavaType> envelope2dProperties(
        ObjectMapper mapper
    ) {
        return Map.of(
            "envelope2dId", mapper.constructType(String.class),
            "vertices", listType(mapper, Envelope2dVertex.class),
            "description", mapper.constructType(String.class)
        );
    }

    private static Map<String, JavaType> envelope2dVertexProperties(
        ObjectMapper mapper
    ) {
        JavaType numberType = mapper.constructType(Double.class);
        return Map.of("x", numberType, "y", numberType);
    }

    private static Map<String, JavaType> envelope3dProperties(
        ObjectMapper mapper
    ) {
        return Map.of(
            "envelope3dId", mapper.constructType(String.class),
            "format", mapper.constructType(String.class),
            "data", mapper.constructType(Envelope3dData.class),
            "url", mapper.constructType(String.class),
            "description", mapper.constructType(String.class)
        );
    }

    private static JavaType listType(ObjectMapper mapper, Class<?> elementType) {
        return mapper.getTypeFactory().constructCollectionType(
            List.class,
            elementType
        );
    }
}
