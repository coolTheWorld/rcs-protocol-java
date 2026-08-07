package io.github.cooltheworld.rcs.protocol.vda5050.v3.codec.internal;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.Connection;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.ConnectionState;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.ProtocolTimestamp;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.ProtocolVersion;
import java.util.Map;
import java.util.Optional;

/** 为自定义反序列化的不可变协议消息提供显式线路字段元数据。 */
final class ProtocolJsonTypeRegistry {
    private ProtocolJsonTypeRegistry() {}

    static Optional<Map<String, JavaType>> findProperties(
        ObjectMapper mapper,
        JavaType type
    ) {
        if (!type.hasRawClass(Connection.class)) {
            return Optional.empty();
        }
        return Optional.of(Map.of(
            "headerId", mapper.constructType(Long.class),
            "timestamp", mapper.constructType(ProtocolTimestamp.class),
            "version", mapper.constructType(ProtocolVersion.class),
            "manufacturer", mapper.constructType(String.class),
            "serialNumber", mapper.constructType(String.class),
            "connectionState", mapper.constructType(ConnectionState.class)
        ));
    }
}
