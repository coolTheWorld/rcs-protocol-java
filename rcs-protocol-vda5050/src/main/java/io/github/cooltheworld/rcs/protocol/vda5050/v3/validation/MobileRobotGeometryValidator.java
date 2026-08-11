package io.github.cooltheworld.rcs.protocol.vda5050.v3.validation;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.Envelope2d;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.Envelope2dVertex;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.Envelope3d;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.MobileRobotGeometry;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.WheelDefinition;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.WheelPosition;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.WheelType;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** 对 Factsheet {@code mobileRobotGeometry} 执行上下文无关语义校验。 */
public final class MobileRobotGeometryValidator {
    private static final String REQUIREMENT_ID = "VDA3-FACTSHEET-001";
    private static final String POLYGON_REQUIREMENT_ID =
        "VDA3-FACTSHEET-008";

    private MobileRobotGeometryValidator() {}

    /** @return 可缓存复用且线程安全的几何 Validator */
    public static MobileRobotGeometryValidator create() {
        return new MobileRobotGeometryValidator();
    }

    /**
     * 校验有限数值、固定轮朝向、二维简单多边形和三维包络内容来源。
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
            boolean finiteVertices = true;
            for (int vertexIndex = 0;
                vertexIndex < envelope.vertices().size();
                vertexIndex++) {
                Envelope2dVertex vertex = envelope.vertices().get(vertexIndex);
                finite(vertex.x(), prefix + vertexIndex + "/x", issues);
                finite(vertex.y(), prefix + vertexIndex + "/y", issues);
                if (!Double.isFinite(vertex.x())) {
                    finiteVertices = false;
                }
                if (!Double.isFinite(vertex.y())) {
                    finiteVertices = false;
                }
            }
            if (finiteVertices) {
                validateSimplePolygon(
                    envelope.vertices(),
                    "/envelopes2d/" + envelopeIndex + "/vertices",
                    issues
                );
            }
        }
    }

    private static void validateSimplePolygon(
        List<Envelope2dVertex> vertices,
        String path,
        List<ValidationIssue> issues
    ) {
        if (vertices.size() < 3) {
            issues.add(polygonIssue(
                "TOO_FEW_ENVELOPE2D_VERTICES",
                path,
                "2D envelope must contain at least three vertices"
            ));
            return;
        }
        int duplicateIndex = duplicateIndex(vertices);
        if (duplicateIndex >= 0) {
            issues.add(polygonIssue(
                "DUPLICATE_ENVELOPE2D_VERTEX",
                path + "/" + duplicateIndex,
                "2D envelope vertices must be unique"
            ));
            return;
        }
        if (isDegenerate(vertices)) {
            issues.add(polygonIssue(
                "DEGENERATE_ENVELOPE2D_POLYGON",
                path,
                "2D envelope polygon must not be degenerate"
            ));
            return;
        }
        if (hasNonAdjacentIntersection(vertices)) {
            issues.add(polygonIssue(
                "SELF_INTERSECTING_ENVELOPE2D_POLYGON",
                path,
                "2D envelope polygon must be simple"
            ));
        }
    }

    private static int duplicateIndex(List<Envelope2dVertex> vertices) {
        for (int second = 1; second < vertices.size(); second++) {
            for (int first = 0; first < second; first++) {
                if (samePoint(vertices.get(first), vertices.get(second))) {
                    return second;
                }
            }
        }
        return -1;
    }

    private static boolean samePoint(
        Envelope2dVertex first,
        Envelope2dVertex second
    ) {
        return first.x().doubleValue() == second.x().doubleValue()
            && first.y().doubleValue() == second.y().doubleValue();
    }

    private static boolean isDegenerate(List<Envelope2dVertex> vertices) {
        Envelope2dVertex first = vertices.get(0);
        Envelope2dVertex second = vertices.get(1);
        for (int index = 2; index < vertices.size(); index++) {
            if (orientation(first, second, vertices.get(index)) != 0) {
                return false;
            }
        }
        return true;
    }

    private static boolean hasNonAdjacentIntersection(
        List<Envelope2dVertex> vertices
    ) {
        int size = vertices.size();
        for (int firstEdge = 0; firstEdge < size; firstEdge++) {
            Envelope2dVertex firstStart = vertices.get(firstEdge);
            Envelope2dVertex firstEnd = vertices.get((firstEdge + 1) % size);
            for (int secondEdge = firstEdge + 1;
                secondEdge < size;
                secondEdge++) {
                if (areAdjacent(firstEdge, secondEdge, size)) {
                    continue;
                }
                Envelope2dVertex secondStart = vertices.get(secondEdge);
                Envelope2dVertex secondEnd = vertices.get(
                    (secondEdge + 1) % size
                );
                if (segmentsIntersect(
                    firstStart,
                    firstEnd,
                    secondStart,
                    secondEnd
                )) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean areAdjacent(
        int firstEdge,
        int secondEdge,
        int size
    ) {
        return secondEdge == firstEdge + 1
            || firstEdge == 0 && secondEdge == size - 1;
    }

    private static boolean segmentsIntersect(
        Envelope2dVertex firstStart,
        Envelope2dVertex firstEnd,
        Envelope2dVertex secondStart,
        Envelope2dVertex secondEnd
    ) {
        if (!rangesOverlap(
            firstStart.x(),
            firstEnd.x(),
            secondStart.x(),
            secondEnd.x()
        )) {
            return false;
        }
        if (!rangesOverlap(
            firstStart.y(),
            firstEnd.y(),
            secondStart.y(),
            secondEnd.y()
        )) {
            return false;
        }
        int firstToSecondStart = orientation(
            firstStart,
            firstEnd,
            secondStart
        );
        int firstToSecondEnd = orientation(
            firstStart,
            firstEnd,
            secondEnd
        );
        int secondToFirstStart = orientation(
            secondStart,
            secondEnd,
            firstStart
        );
        int secondToFirstEnd = orientation(
            secondStart,
            secondEnd,
            firstEnd
        );
        return firstToSecondStart * firstToSecondEnd <= 0
            && secondToFirstStart * secondToFirstEnd <= 0;
    }

    private static boolean rangesOverlap(
        double firstStart,
        double firstEnd,
        double secondStart,
        double secondEnd
    ) {
        double lower = Math.max(
            Math.min(firstStart, firstEnd),
            Math.min(secondStart, secondEnd)
        );
        double upper = Math.min(
            Math.max(firstStart, firstEnd),
            Math.max(secondStart, secondEnd)
        );
        return lower <= upper;
    }

    private static int orientation(
        Envelope2dVertex first,
        Envelope2dVertex second,
        Envelope2dVertex third
    ) {
        double left = (second.x() - first.x()) * (third.y() - first.y());
        double right = (second.y() - first.y()) * (third.x() - first.x());
        double determinant = left - right;
        if (Double.isFinite(determinant) && determinant != 0.0D) {
            return determinant > 0.0D ? 1 : -1;
        }
        return exactOrientation(first, second, third);
    }

    private static int exactOrientation(
        Envelope2dVertex first,
        Envelope2dVertex second,
        Envelope2dVertex third
    ) {
        BigDecimal firstX = BigDecimal.valueOf(first.x());
        BigDecimal firstY = BigDecimal.valueOf(first.y());
        BigDecimal left = BigDecimal.valueOf(second.x())
            .subtract(firstX)
            .multiply(BigDecimal.valueOf(third.y()).subtract(firstY));
        BigDecimal right = BigDecimal.valueOf(second.y())
            .subtract(firstY)
            .multiply(BigDecimal.valueOf(third.x()).subtract(firstX));
        return left.subtract(right).signum();
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

    private static ValidationIssue polygonIssue(
        String code,
        String path,
        String description
    ) {
        return new ValidationIssue(
            code,
            ValidationSeverity.ERROR,
            path,
            description,
            POLYGON_REQUIREMENT_ID
        );
    }
}
