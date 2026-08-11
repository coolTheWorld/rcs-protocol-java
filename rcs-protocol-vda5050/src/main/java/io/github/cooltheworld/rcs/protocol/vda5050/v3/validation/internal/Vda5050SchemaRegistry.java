package io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.internal;

import com.networknt.schema.Error;
import com.networknt.schema.InputFormat;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SchemaRegistryConfig;
import com.networknt.schema.SpecificationVersion;
import com.networknt.schema.path.PathType;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.topic.TopicName;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * 加载、检查并缓存八份可信 classpath Schema。
 *
 * @see <a href="https://github.com/networknt/json-schema-validator#usage">
 *     NetworkNT JSON Schema Validator usage</a>
 */
final class Vda5050SchemaRegistry {
    private static final String RESOURCE_ROOT = "vda5050/v3.0.0/";
    private static final Map<TopicName, String> RESOURCE_NAMES = Map.of(
        TopicName.CONNECTION, "connection.schema",
        TopicName.FACTSHEET, "factsheet.schema",
        TopicName.INSTANT_ACTIONS, "instantActions.schema",
        TopicName.ORDER, "order.schema",
        TopicName.RESPONSES, "responses.schema",
        TopicName.STATE, "state.schema",
        TopicName.VISUALIZATION, "visualization.schema",
        TopicName.ZONE_SET, "zoneSet.schema"
    );

    private final Map<TopicName, Schema> schemas;

    private Vda5050SchemaRegistry(Map<TopicName, Schema> schemas) {
        this.schemas = Map.copyOf(schemas);
    }

    static Vda5050SchemaRegistry create() {
        SchemaRegistryConfig config = SchemaRegistryConfig.builder()
            .failFast(true)
            .formatAssertionsEnabled(true)
            .locale(Locale.ROOT)
            .pathType(PathType.JSON_POINTER)
            .preloadSchema(true)
            .build();
        SchemaRegistry registry = SchemaRegistry.withDefaultDialect(
            SpecificationVersion.DRAFT_2020_12,
            builder -> builder
                .schemaRegistryConfig(config)
                .schemaLoader(loader -> loader.fetchRemoteResources(false))
        );
        Schema metaSchema = registry.getSchema(
            SchemaLocation.of(SpecificationVersion.DRAFT_2020_12.getDialectId())
        );
        EnumMap<TopicName, Schema> schemas = new EnumMap<>(TopicName.class);
        RESOURCE_NAMES.forEach((topic, resourceName) -> schemas.put(
            topic,
            loadSchema(registry, metaSchema, resourceName)
        ));
        requireComplete(schemas);
        return new Vda5050SchemaRegistry(schemas);
    }

    Schema schemaFor(TopicName topic) {
        Schema schema = schemas.get(Objects.requireNonNull(topic, "topic"));
        if (schema == null) {
            throw new IllegalArgumentException("No schema registered for topic");
        }
        return schema;
    }

    private static Schema loadSchema(
        SchemaRegistry registry,
        Schema metaSchema,
        String resourceName
    ) {
        String resourcePath = RESOURCE_ROOT + resourceName;
        String schemaData = readResource(resourcePath);
        List<Error> schemaErrors = metaSchema.validate(
            schemaData,
            InputFormat.JSON
        );
        requireValidSchema(schemaErrors, resourceName);
        return registry.getSchema(
            SchemaLocation.of("classpath:" + resourcePath),
            schemaData,
            InputFormat.JSON
        );
    }

    static void requireComplete(Map<TopicName, Schema> schemas) {
        if (schemas.size() != TopicName.values().length) {
            throw new IllegalStateException("Not all VDA 5050 schemas are registered");
        }
    }

    static void requireValidSchema(List<Error> schemaErrors, String resourceName) {
        if (!schemaErrors.isEmpty()) {
            throw new IllegalStateException(
                "Bundled schema is not valid Draft 2020-12: " + resourceName
            );
        }
    }

    static String readResource(String resourcePath) {
        ClassLoader classLoader = Vda5050SchemaRegistry.class.getClassLoader();
        try (InputStream input = classLoader.getResourceAsStream(resourcePath)) {
            if (input == null) {
                throw new IllegalStateException(
                    "Bundled schema resource is missing: " + resourcePath
                );
            }
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException(
                "Bundled schema resource cannot be read: " + resourcePath,
                exception
            );
        }
    }
}
