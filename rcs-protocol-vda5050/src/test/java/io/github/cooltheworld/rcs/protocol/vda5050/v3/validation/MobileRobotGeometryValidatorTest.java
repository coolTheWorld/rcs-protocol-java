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
