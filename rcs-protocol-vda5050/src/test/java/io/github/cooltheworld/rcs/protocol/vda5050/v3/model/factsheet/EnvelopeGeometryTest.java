package io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

final class EnvelopeGeometryTest {
    @Test
    @DisplayName("[VDA3-FACTSHEET-001] 二维与三维包络字段完整且集合不可变")
    void buildsImmutableTwoAndThreeDimensionalEnvelopes() {
        List<Envelope2dVertex> vertices = new ArrayList<>(List.of(
            Envelope2dVertex.builder().x(-1.0D).y(-0.5D).build(),
            Envelope2dVertex.builder().x(1.0D).y(-0.5D).build(),
            Envelope2dVertex.builder().x(0.0D).y(0.8D).build()
        ));
        Envelope2d envelope2d = Envelope2d.builder()
            .envelope2dId("footprint")
            .vertices(vertices)
            .description("Simple footprint")
            .build();
        Envelope3d envelope3d = Envelope3d.builder()
            .envelope3dId("body")
            .format("gltf")
            .data(Envelope3dData.empty())
            .url("https://example.invalid/body.gltf")
            .description("Robot body")
            .build();
        MobileRobotGeometry geometry = MobileRobotGeometry.builder()
            .envelopes2d(List.of(envelope2d))
            .envelopes3d(List.of(envelope3d))
            .build();
        vertices.clear();

        assertAll(
            () -> assertEquals(3, envelope2d.vertices().size()),
            () -> assertThrows(
                UnsupportedOperationException.class,
                () -> envelope2d.vertices().clear()
            ),
            () -> assertEquals(List.of(envelope2d), geometry.envelopes2d()),
            () -> assertEquals(List.of(envelope3d), geometry.envelopes3d()),
            () -> assertTrue(envelope3d.data().isEmpty()),
            () -> assertTrue(envelope2d.extensionFields().isEmpty()),
            () -> assertTrue(envelope3d.extensionFields().isEmpty())
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] 包络可选字段保持缺失语义")
    void preservesMissingEnvelopeFields() {
        Envelope2d envelope2d = Envelope2d.builder()
            .envelope2dId("footprint")
            .vertices(List.of())
            .build();
        Envelope3d envelope3d = Envelope3d.builder()
            .envelope3dId("body")
            .format("obj")
            .build();

        assertAll(
            () -> assertNull(envelope2d.description()),
            () -> assertNull(envelope3d.data()),
            () -> assertNull(envelope3d.url()),
            () -> assertNull(envelope3d.description())
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] 包络值相等覆盖每个标准字段")
    void includesEveryStandardEnvelopeFieldInValueEquality() {
        Envelope2dVertex vertex = Envelope2dVertex.builder()
            .x(-1.0D)
            .y(-0.5D)
            .build();
        Envelope2d envelope2d = Envelope2d.builder()
            .envelope2dId("footprint")
            .vertices(List.of(vertex))
            .description("Simple footprint")
            .build();
        Envelope3d envelope3d = Envelope3d.builder()
            .envelope3dId("body")
            .format("gltf")
            .data(Envelope3dData.empty())
            .url("https://example.invalid/body.gltf")
            .description("Robot body")
            .build();

        assertAll(
            () -> assertEquals(vertex, vertex),
            () -> assertNotEquals(vertex, null),
            () -> assertNotEquals(vertex, "vertex"),
            () -> assertNotEquals(
                vertex,
                Envelope2dVertex.builder().x(1.0D).y(-0.5D).build()
            ),
            () -> assertNotEquals(
                vertex,
                Envelope2dVertex.builder().x(-1.0D).y(0.5D).build()
            ),
            () -> assertEquals(envelope2d, envelope2d),
            () -> assertNotEquals(envelope2d, null),
            () -> assertNotEquals(envelope2d, "envelope"),
            () -> assertNotEquals(
                envelope2d,
                Envelope2d.builder()
                    .envelope2dId("safety")
                    .vertices(List.of(vertex))
                    .description("Simple footprint")
                    .build()
            ),
            () -> assertNotEquals(
                envelope2d,
                Envelope2d.builder()
                    .envelope2dId("footprint")
                    .vertices(List.of())
                    .description("Simple footprint")
                    .build()
            ),
            () -> assertNotEquals(
                envelope2d,
                Envelope2d.builder()
                    .envelope2dId("footprint")
                    .vertices(List.of(vertex))
                    .description("Different")
                    .build()
            ),
            () -> assertEquals(envelope3d, envelope3d),
            () -> assertNotEquals(envelope3d, null),
            () -> assertNotEquals(envelope3d, "envelope"),
            () -> assertNotEquals(
                envelope3d,
                fullEnvelope3d().envelope3dId("safety").build()
            ),
            () -> assertNotEquals(
                envelope3d,
                fullEnvelope3d().format("obj").build()
            ),
            () -> assertNotEquals(
                envelope3d,
                fullEnvelope3d().data(null).build()
            ),
            () -> assertNotEquals(
                envelope3d,
                fullEnvelope3d().url("https://example.invalid/other.gltf").build()
            ),
            () -> assertNotEquals(
                envelope3d,
                fullEnvelope3d().description("Different").build()
            )
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] 包络构造拒绝缺失必填字段和 null 顶点")
    void rejectsMissingRequiredFieldsAndNullVertices() {
        Envelope2dVertex vertex = Envelope2dVertex.builder()
            .x(0.0D)
            .y(0.0D)
            .build();

        assertAll(
            () -> assertThrows(
                NullPointerException.class,
                () -> Envelope2dVertex.builder().y(0.0D).build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> Envelope2d.builder().vertices(List.of(vertex)).build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> Envelope2d.builder()
                    .envelope2dId("footprint")
                    .vertices(Arrays.asList(vertex, null))
                    .build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> Envelope3d.builder().format("gltf").build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> Envelope3d.builder().envelope3dId("body").build()
            )
        );
    }

    private static Envelope3d.Builder fullEnvelope3d() {
        return Envelope3d.builder()
            .envelope3dId("body")
            .format("gltf")
            .data(Envelope3dData.empty())
            .url("https://example.invalid/body.gltf")
            .description("Robot body");
    }
}
