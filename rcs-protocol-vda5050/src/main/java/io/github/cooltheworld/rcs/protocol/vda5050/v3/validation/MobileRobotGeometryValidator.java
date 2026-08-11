package io.github.cooltheworld.rcs.protocol.vda5050.v3.validation;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.Envelope2d;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.Envelope2dVertex;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.Envelope3d;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.MobileRobotGeometry;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.WheelDefinition;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.WheelPosition;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.WheelType;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** 对 Factsheet {@code mobileRobotGeometry} 执行上下文无关语义校验。 */
public final class MobileRobotGeometryValidator {
    private static final String REQUIREMENT_ID = "VDA3-FACTSHEET-001";

    private MobileRobotGeometryValidator() {}

    /** @return 可缓存复用且线程安全的几何 Validator */
    public static MobileRobotGeometryValidator create() {
        return new MobileRobotGeometryValidator();
    }

    /**
     * 校验有限数值、固定轮朝向和三维包络内容来源。
     *
     * @param geometry 已完成强类型绑定的几何片段
     * @return 按线路遍历顺序排列的不可变问题列表
     */
    public List<ValidationIssue> validate(MobileRobotGeometry geometry) {
        Objects.requireNonNull(geometry, "geometry");
        List<ValidationIssue> issues = new ArrayList<>();
        validateWheels(geometry.wheelDefinitions(), issues);
        validateEnvelopes2d(geometry.envelopes2d(), issues);
        validateEnvelopes3d(geometry.envelopes3d(), issues);
        return List.copyOf(issues);
    }

    private static void validateWheels(
        List<WheelDefinition> wheels,
        List<ValidationIssue> issues
    ) {
        if (wheels == null) {
            return;
        }
        for (int index = 0; index < wheels.size(); index++) {
            WheelDefinition wheel = wheels.get(index);
            String prefix = "/wheelDefinitions/" + index;
            WheelPosition position = wheel.position();
            finite(position.x(), prefix + "/position/x", issues);
            finite(position.y(), prefix + "/position/y", issues);
            finite(position.theta(), prefix + "/position/theta", issues);
            finite(wheel.diameter(), prefix + "/diameter", issues);
            finite(wheel.width(), prefix + "/width", issues);
            finite(
                wheel.centerDisplacement(),
                prefix + "/centerDisplacement",
                issues
            );
            if (WheelType.FIXED.equals(wheel.type()) && position.theta() == null) {
                issues.add(issue(
                    "MISSING_FIXED_WHEEL_THETA",
                    prefix + "/position/theta",
                    "Fixed wheel position must declare theta"
                ));
            }
        }
    }

    private static void validateEnvelopes2d(
        List<Envelope2d> envelopes,
        List<ValidationIssue> issues
    ) {
        if (envelopes == null) {
            return;
        }
        for (int envelopeIndex = 0;
            envelopeIndex < envelopes.size();
            envelopeIndex++) {
            Envelope2d envelope = envelopes.get(envelopeIndex);
            String prefix = "/envelopes2d/" + envelopeIndex + "/vertices/";
            for (int vertexIndex = 0;
                vertexIndex < envelope.vertices().size();
                vertexIndex++) {
                Envelope2dVertex vertex = envelope.vertices().get(vertexIndex);
                finite(vertex.x(), prefix + vertexIndex + "/x", issues);
                finite(vertex.y(), prefix + vertexIndex + "/y", issues);
            }
        }
    }

    private static void validateEnvelopes3d(
        List<Envelope3d> envelopes,
        List<ValidationIssue> issues
    ) {
        if (envelopes == null) {
            return;
        }
        for (int index = 0; index < envelopes.size(); index++) {
            Envelope3d envelope = envelopes.get(index);
            String prefix = "/envelopes3d/" + index;
            if (envelope.data() == null && envelope.url() == null) {
                issues.add(issue(
                    "MISSING_ENVELOPE3D_CONTENT",
                    prefix,
                    "3D envelope must declare data or url"
                ));
            }
            if (envelope.url() != null && !isAbsoluteUri(envelope.url())) {
                issues.add(issue(
                    "INVALID_ENVELOPE3D_URL",
                    prefix + "/url",
                    "3D envelope url must be an absolute URI"
                ));
            }
        }
    }

    private static boolean isAbsoluteUri(String value) {
        try {
            URI uri = new URI(value);
            return uri.isAbsolute();
        } catch (URISyntaxException exception) {
            return false;
        }
    }

    private static void finite(
        Double value,
        String path,
        List<ValidationIssue> issues
    ) {
        if (value != null && !Double.isFinite(value)) {
            issues.add(issue(
                "NON_FINITE_GEOMETRY_NUMBER",
                path,
                "Geometry number must be finite"
            ));
        }
    }

    private static ValidationIssue issue(
        String code,
        String path,
        String description
    ) {
        return new ValidationIssue(
            code,
            ValidationSeverity.ERROR,
            path,
            description,
            REQUIREMENT_ID
        );
    }
}
