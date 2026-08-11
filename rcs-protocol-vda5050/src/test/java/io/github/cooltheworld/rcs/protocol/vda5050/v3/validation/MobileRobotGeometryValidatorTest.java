package io.github.cooltheworld.rcs.protocol.vda5050.v3.validation;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.codec.Vda5050JsonCodec;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.Envelope2d;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.Envelope2dVertex;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.Envelope3d;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.MobileRobotGeometry;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.WheelDefinition;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.WheelPosition;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.WheelType;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.topic.TopicName;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unchecked")
final class MobileRobotGeometryValidatorTest {
    private static final MobileRobotGeometryValidator VALIDATOR =
        MobileRobotGeometryValidator.create();

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] 有限几何与有效 3D 内容来源通过语义校验")
    void acceptsFiniteGeometryAndValidEnvelopeSources() {
        Envelope3d dataEnvelope = decodedDataEnvelope();
        MobileRobotGeometry geometry = MobileRobotGeometry.builder()
            .wheelDefinitions(List.of(wheel(
                WheelType.FIXED,
                WheelPosition.builder().x(0.0D).y(0.0D).theta(0.0D).build()
            )))
            .envelopes2d(List.of(Envelope2d.builder()
                .envelope2dId("footprint")
                .vertices(List.of(
                    Envelope2dVertex.builder().x(0.0D).y(0.0D).build(),
                    Envelope2dVertex.builder().x(1.0D).y(0.0D).build(),
                    Envelope2dVertex.builder().x(0.0D).y(1.0D).build()
                ))
                .build()))
            .envelopes3d(List.of(
                envelope3d().url("https://example.invalid/body.gltf").build(),
                dataEnvelope,
                envelope3d()
                    .data(dataEnvelope.data())
                    .url("ftp://example.invalid/body.obj")
                    .build()
            ))
            .build();

        assertEquals(List.of(), VALIDATOR.validate(geometry));
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] Validator 报告每个非有限几何数值")
    void reportsEveryNonFiniteGeometryNumber() {
        MobileRobotGeometry geometry = MobileRobotGeometry.builder()
            .wheelDefinitions(List.of(WheelDefinition.builder()
                .type(WheelType.DRIVE)
                .isActiveDriven(true)
                .isActiveSteered(false)
                .position(WheelPosition.builder()
                    .x(Double.NaN)
                    .y(Double.POSITIVE_INFINITY)
                    .theta(Double.NEGATIVE_INFINITY)
                    .build())
                .diameter(Double.NaN)
                .width(Double.POSITIVE_INFINITY)
                .centerDisplacement(Double.NEGATIVE_INFINITY)
                .build()))
            .envelopes2d(List.of(Envelope2d.builder()
                .envelope2dId("footprint")
                .vertices(List.of(Envelope2dVertex.builder()
                    .x(Double.NaN)
                    .y(Double.POSITIVE_INFINITY)
                    .build()))
                .build()))
            .build();

        List<ValidationIssue> issues = VALIDATOR.validate(geometry);

        assertAll(
            () -> assertEquals(8, issues.size()),
            () -> assertEquals(
                List.of(
                    "/wheelDefinitions/0/position/x",
                    "/wheelDefinitions/0/position/y",
                    "/wheelDefinitions/0/position/theta",
                    "/wheelDefinitions/0/diameter",
                    "/wheelDefinitions/0/width",
                    "/wheelDefinitions/0/centerDisplacement",
                    "/envelopes2d/0/vertices/0/x",
                    "/envelopes2d/0/vertices/0/y"
                ),
                issues.stream().map(ValidationIssue::path).toList()
            ),
            () -> assertEquals(
                List.of("NON_FINITE_GEOMETRY_NUMBER"),
                issues.stream().map(ValidationIssue::code).distinct().toList()
            ),
            () -> assertThrows(
                UnsupportedOperationException.class,
                issues::clear
            )
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] 固定轮必须声明朝向")
    void requiresThetaForFixedWheels() {
        MobileRobotGeometry geometry = MobileRobotGeometry.builder()
            .wheelDefinitions(List.of(wheel(
                WheelType.FIXED,
                WheelPosition.builder().x(0.0D).y(0.0D).build()
            )))
            .build();

        ValidationIssue issue = VALIDATOR.validate(geometry).getFirst();

        assertAll(
            () -> assertEquals("MISSING_FIXED_WHEEL_THETA", issue.code()),
            () -> assertEquals("/wheelDefinitions/0/position/theta", issue.path())
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] 3D 包络要求 Data 或绝对 URL")
    void requiresDataOrAnAbsoluteUrl() {
        MobileRobotGeometry geometry = MobileRobotGeometry.builder()
            .envelopes3d(List.of(
                envelope3d().build(),
                envelope3d().url("models/body.gltf").build(),
                envelope3d().url("not a url").build()
            ))
            .build();

        List<ValidationIssue> issues = VALIDATOR.validate(geometry);

        assertAll(
            () -> assertEquals(3, issues.size()),
            () -> assertEquals("MISSING_ENVELOPE3D_CONTENT", issues.get(0).code()),
            () -> assertEquals("INVALID_ENVELOPE3D_URL", issues.get(1).code()),
            () -> assertEquals("INVALID_ENVELOPE3D_URL", issues.get(2).code()),
            () -> assertEquals("/envelopes3d/0", issues.get(0).path()),
            () -> assertEquals("/envelopes3d/1/url", issues.get(1).path())
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-008] 接受顺逆时针与凹简单多边形")
    void acceptsSimplePolygonsWithoutInventingAnOrientationRule() {
        MobileRobotGeometry geometry = geometry(
            polygon(
                "clockwise",
                vertex(0.0D, 0.0D),
                vertex(0.0D, 2.0D),
                vertex(2.0D, 0.0D)
            ),
            polygon(
                "concave",
                vertex(0.0D, 0.0D),
                vertex(3.0D, 0.0D),
                vertex(1.0D, 1.0D),
                vertex(3.0D, 3.0D),
                vertex(0.0D, 3.0D)
            ),
            polygon(
                "overflow-safe",
                vertex(Double.MAX_VALUE, 0.0D),
                vertex(0.0D, Double.MAX_VALUE),
                vertex(-Double.MAX_VALUE, 0.0D)
            ),
            polygon(
                "underflow-safe",
                vertex(0.0D, 0.0D),
                vertex(Double.MIN_VALUE, 0.0D),
                vertex(0.0D, Double.MIN_VALUE)
            ),
            polygon(
                "parallel-overlapping-boxes",
                vertex(0.0D, 0.0D),
                vertex(2.0D, 2.0D),
                vertex(2.0D, 3.0D),
                vertex(0.0D, 1.0D),
                vertex(-1.0D, 0.0D)
            ),
            polygon(
                "line-crosses-outside-segment",
                vertex(0.0D, 0.0D),
                vertex(1.0D, 0.0D),
                vertex(2.0D, -1.0D),
                vertex(0.5D, 1.0D),
                vertex(-1.0D, 1.0D)
            )
        );

        assertEquals(List.of(), VALIDATOR.validate(geometry));
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-008] 拒绝少于三点、重复点与共线退化包络")
    void rejectsTooFewDuplicateAndDegenerateVertices() {
        MobileRobotGeometry geometry = geometry(
            polygon(
                "too-few",
                vertex(0.0D, 0.0D),
                vertex(1.0D, 0.0D)
            ),
            polygon(
                "explicit-close",
                vertex(0.0D, 0.0D),
                vertex(1.0D, 0.0D),
                vertex(0.0D, 1.0D),
                vertex(0.0D, 0.0D)
            ),
            polygon(
                "signed-zero-duplicate",
                vertex(0.0D, 0.0D),
                vertex(-0.0D, -0.0D),
                vertex(1.0D, 0.0D)
            ),
            polygon(
                "collinear",
                vertex(0.0D, 0.0D),
                vertex(1.0D, 0.0D),
                vertex(2.0D, 0.0D)
            )
        );

        List<ValidationIssue> issues = VALIDATOR.validate(geometry);

        assertAll(
            () -> assertEquals(
                List.of(
                    "TOO_FEW_ENVELOPE2D_VERTICES",
                    "DUPLICATE_ENVELOPE2D_VERTEX",
                    "DUPLICATE_ENVELOPE2D_VERTEX",
                    "DEGENERATE_ENVELOPE2D_POLYGON"
                ),
                issues.stream().map(ValidationIssue::code).toList()
            ),
            () -> assertEquals(
                List.of(
                    "/envelopes2d/0/vertices",
                    "/envelopes2d/1/vertices/3",
                    "/envelopes2d/2/vertices/1",
                    "/envelopes2d/3/vertices"
                ),
                issues.stream().map(ValidationIssue::path).toList()
            ),
            () -> assertEquals(
                List.of("VDA3-FACTSHEET-008"),
                issues.stream()
                    .map(ValidationIssue::requirementId)
                    .distinct()
                    .toList()
            )
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-008] 拒绝非相邻边交叉、接触或重叠")
    void rejectsEveryNonAdjacentEdgeIntersectionForm() {
        MobileRobotGeometry geometry = geometry(
            polygon(
                "crossing",
                vertex(0.0D, 0.0D),
                vertex(2.0D, 2.0D),
                vertex(0.0D, 2.0D),
                vertex(2.0D, 0.0D)
            ),
            polygon(
                "touching",
                vertex(0.0D, 0.0D),
                vertex(2.0D, 0.0D),
                vertex(1.0D, 0.0D),
                vertex(1.0D, 1.0D),
                vertex(0.0D, 1.0D)
            ),
            polygon(
                "overlapping",
                vertex(0.0D, 0.0D),
                vertex(4.0D, 0.0D),
                vertex(5.0D, 1.0D),
                vertex(1.0D, 0.0D),
                vertex(3.0D, 0.0D),
                vertex(0.0D, 2.0D)
            )
        );

        List<ValidationIssue> issues = VALIDATOR.validate(geometry);

        assertAll(
            () -> assertEquals(
                List.of(
                    "SELF_INTERSECTING_ENVELOPE2D_POLYGON",
                    "SELF_INTERSECTING_ENVELOPE2D_POLYGON",
                    "SELF_INTERSECTING_ENVELOPE2D_POLYGON"
                ),
                issues.stream().map(ValidationIssue::code).toList()
            ),
            () -> assertEquals(
                List.of(
                    "/envelopes2d/0/vertices",
                    "/envelopes2d/1/vertices",
                    "/envelopes2d/2/vertices"
                ),
                issues.stream().map(ValidationIssue::path).toList()
            )
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-008] 大边界包络保持确定性且不修改输入")
    void validatesALargeBoundaryPolygonDeterministicallyWithoutMutation() {
        List<Envelope2dVertex> vertices = IntStream.range(0, 512)
            .mapToObj(index -> {
                double angle = 2.0D * Math.PI * index / 512.0D;
                return vertex(Math.cos(angle), Math.sin(angle));
            })
            .toList();
        MobileRobotGeometry geometry = geometry(polygon(
            "large-boundary",
            vertices.toArray(Envelope2dVertex[]::new)
        ));
        MobileRobotGeometry snapshot = geometry(polygon(
            "large-boundary",
            vertices.toArray(Envelope2dVertex[]::new)
        ));

        List<ValidationIssue> issues = VALIDATOR.validate(geometry);

        assertAll(
            () -> assertEquals(List.of(), issues),
            () -> assertEquals(issues, VALIDATOR.validate(geometry)),
            () -> assertEquals(snapshot, geometry)
        );
    }

    private static WheelDefinition wheel(
        WheelType type,
        WheelPosition position
    ) {
        return WheelDefinition.builder()
            .type(type)
            .isActiveDriven(true)
            .isActiveSteered(false)
            .position(position)
            .diameter(0.3D)
            .width(0.1D)
            .build();
    }

    private static Envelope3d.Builder envelope3d() {
        return Envelope3d.builder().envelope3dId("body").format("gltf");
    }

    private static MobileRobotGeometry geometry(Envelope2d... envelopes) {
        return MobileRobotGeometry.builder()
            .envelopes2d(List.of(envelopes))
            .build();
    }

    private static Envelope2d polygon(
        String id,
        Envelope2dVertex... vertices
    ) {
        return Envelope2d.builder()
            .envelope2dId(id)
            .vertices(List.of(vertices))
            .build();
    }

    private static Envelope2dVertex vertex(double x, double y) {
        return Envelope2dVertex.builder().x(x).y(y).build();
    }

    private static Envelope3d decodedDataEnvelope() {
        DecodingResult<MobileRobotGeometry> result = Vda5050JsonCodec
            .createDefault()
            .decode(
                TopicName.FACTSHEET,
                "{\"envelopes3d\":[{\"envelope3dId\":\"body\",\"format\":\"gltf\",\"data\":{\"mesh\":[]}}]}"
                    .getBytes(StandardCharsets.UTF_8),
                MobileRobotGeometry.class
            );
        return ((DecodedMessage<MobileRobotGeometry>) assertInstanceOf(
            DecodedMessage.class,
            result
        )).message().envelopes3d().getFirst();
    }
}
