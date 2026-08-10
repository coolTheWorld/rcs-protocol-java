package io.github.cooltheworld.rcs.protocol.vda5050.v3.codec.internal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.ExtensionFields;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.Envelope3dData;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Codec 内部访问公共不透明 JSON 值的唯一桥接点。 */
public final class OpaqueJsonJacksonAccess {
    private static final OpaqueAccess EXTENSION_FIELDS = access(
        ExtensionFields.class
    );
    private static final OpaqueAccess ENVELOPE_3D_DATA = access(
        Envelope3dData.class
    );

    private OpaqueJsonJacksonAccess() {}

    /** 从已执行资源预检的对象创建不透明扩展值。 */
    public static ExtensionFields extensionFields(
        ObjectMapper mapper,
        ObjectNode value
    ) throws JsonProcessingException {
        return create(
            EXTENSION_FIELDS,
            canonicalBytes(mapper, value),
            mapper.writeValueAsBytes(value),
            ExtensionFields.class
        );
    }

    /** 为 Jackson Serializer 取得防御性解析后的扩展对象。 */
    public static ObjectNode object(
        ObjectMapper mapper,
        ExtensionFields value
    ) {
        return object(mapper, bytes(EXTENSION_FIELDS, value));
    }

    /** 从已执行资源预检的对象创建三维包络内联数据。 */
    public static Envelope3dData envelope3dData(
        ObjectMapper mapper,
        ObjectNode value
    ) throws JsonProcessingException {
        return create(
            ENVELOPE_3D_DATA,
            canonicalBytes(mapper, value),
            mapper.writeValueAsBytes(value),
            Envelope3dData.class
        );
    }

    /** 为 Jackson Serializer 取得防御性解析后的三维包络对象。 */
    public static ObjectNode object(
        ObjectMapper mapper,
        Envelope3dData value
    ) {
        return object(mapper, bytes(ENVELOPE_3D_DATA, value));
    }

    private static OpaqueAccess access(Class<?> valueType) {
        try {
            MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(
                valueType,
                MethodHandles.lookup()
            );
            return new OpaqueAccess(
                lookup.findStatic(
                    valueType,
                    "fromJsonBytes",
                    MethodType.methodType(
                        valueType,
                        byte[].class,
                        byte[].class
                    )
                ),
                lookup.findVirtual(
                    valueType,
                    "toJsonBytes",
                    MethodType.methodType(byte[].class)
                )
            );
        } catch (IllegalAccessException | NoSuchMethodException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    private static <T> T create(
        OpaqueAccess access,
        byte[] canonicalJson,
        byte[] wireJson,
        Class<T> valueType
    ) {
        try {
            return valueType.cast(access.factory().invoke(
                canonicalJson,
                wireJson
            ));
        } catch (Throwable throwable) {
            throw bridgeFailure(throwable);
        }
    }

    private static byte[] bytes(OpaqueAccess access, Object value) {
        try {
            return (byte[]) access.reader().invoke(value);
        } catch (Throwable throwable) {
            throw bridgeFailure(throwable);
        }
    }

    private static IllegalStateException bridgeFailure(Throwable throwable) {
        return new IllegalStateException(
            "Opaque JSON value bridge is incompatible with the public model",
            throwable
        );
    }

    private static byte[] canonicalBytes(
        ObjectMapper mapper,
        ObjectNode value
    ) throws JsonProcessingException {
        Objects.requireNonNull(mapper, "mapper");
        Objects.requireNonNull(value, "value");
        return mapper.writeValueAsBytes(canonicalCopy(mapper, value));
    }

    private static JsonNode canonicalCopy(ObjectMapper mapper, JsonNode value) {
        if (value instanceof ObjectNode object) {
            ObjectNode copy = mapper.createObjectNode();
            List<String> names = new ArrayList<>();
            object.fieldNames().forEachRemaining(names::add);
            Collections.sort(names);
            names.forEach(name -> copy.set(
                name,
                canonicalCopy(mapper, object.required(name))
            ));
            return copy;
        }
        if (value instanceof ArrayNode array) {
            ArrayNode copy = mapper.createArrayNode();
            array.forEach(element -> copy.add(canonicalCopy(mapper, element)));
            return copy;
        }
        return value.deepCopy();
    }

    private static ObjectNode object(ObjectMapper mapper, byte[] canonicalJson) {
        Objects.requireNonNull(mapper, "mapper");
        try {
            JsonNode value = mapper.readTree(canonicalJson);
            if (value instanceof ObjectNode object) {
                return object.deepCopy();
            }
            throw new IllegalStateException("Opaque JSON value is not an object");
        } catch (IOException exception) {
            throw new IllegalStateException("Opaque JSON value is invalid", exception);
        }
    }

    private record OpaqueAccess(MethodHandle factory, MethodHandle reader) {}
}
