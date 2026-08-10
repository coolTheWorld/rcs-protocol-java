package io.github.cooltheworld.rcs.protocol.vda5050.v3.codec.internal;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.Connection;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.ConnectionState;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.LocalizationType;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.NavigationType;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.PhysicalParameters;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.ProtocolTimestamp;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.ProtocolVersion;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.TypeSpecification;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.ZoneType;
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
        if (type.hasRawClass(TypeSpecification.class)) {
            return Optional.of(typeSpecificationProperties(mapper));
        }
        if (type.hasRawClass(PhysicalParameters.class)) {
            return Optional.of(physicalParametersProperties(mapper));
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
}
