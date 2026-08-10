package io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.ExtensionFields;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

final class LoadSetTest {
    @Test
    @DisplayName("[VDA3-FACTSHEET-001] 包围盒引用保留必填坐标和可选方向")
    void buildsBoundingBoxReference() {
        BoundingBoxReference reference = BoundingBoxReference.builder()
            .x(0.25D)
            .y(-0.10D)
            .z(0.50D)
            .theta(1.57D)
            .build();
        BoundingBoxReference equalReference = BoundingBoxReference.builder()
            .x(0.25D)
            .y(-0.10D)
            .z(0.50D)
            .theta(1.57D)
            .extensionFields(ExtensionFields.empty())
            .build();
        BoundingBoxReference withoutTheta = BoundingBoxReference.builder()
            .x(0.25D)
            .y(-0.10D)
            .z(0.50D)
            .build();

        assertAll(
            () -> assertEquals(0.25D, reference.x()),
            () -> assertEquals(-0.10D, reference.y()),
            () -> assertEquals(0.50D, reference.z()),
            () -> assertEquals(1.57D, reference.theta()),
            () -> assertNull(withoutTheta.theta()),
            () -> assertTrue(reference.extensionFields().isEmpty()),
            () -> assertEquals(reference, reference),
            () -> assertEquals(reference, equalReference),
            () -> assertNotEquals(reference, withoutTheta),
            () -> assertNotEquals(reference, "bounding box reference"),
            () -> assertNotEquals(
                reference,
                BoundingBoxReference.builder()
                    .x(0.30D)
                    .y(-0.10D)
                    .z(0.50D)
                    .theta(1.57D)
                    .build()
            ),
            () -> assertNotEquals(
                reference,
                BoundingBoxReference.builder()
                    .x(0.25D)
                    .y(-0.20D)
                    .z(0.50D)
                    .theta(1.57D)
                    .build()
            ),
            () -> assertNotEquals(
                reference,
                BoundingBoxReference.builder()
                    .x(0.25D)
                    .y(-0.10D)
                    .z(0.60D)
                    .theta(1.57D)
                    .build()
            ),
            () -> assertEquals(reference.hashCode(), equalReference.hashCode())
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] 包围盒引用拒绝缺失的必填坐标")
    void rejectsMissingRequiredBoundingBoxCoordinates() {
        assertAll(
            () -> assertThrows(
                NullPointerException.class,
                () -> BoundingBoxReference.builder()
                    .y(0.0D)
                    .z(0.0D)
                    .build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> BoundingBoxReference.builder()
                    .x(0.0D)
                    .z(0.0D)
                    .build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> BoundingBoxReference.builder()
                    .x(0.0D)
                    .y(0.0D)
                    .build()
            )
        );
    }

    @Test
    @DisplayName(
        "[VDA3-FACTSHEET-001][VDA3-FACTSHEET-002] 载荷集合完整表达位置几何和能力字段"
    )
    void buildsCompleteLoadSetObjectGraph() {
        BoundingBoxReference reference = BoundingBoxReference.builder()
            .x(0.25D)
            .y(0.0D)
            .z(0.50D)
            .theta(1.57D)
            .build();
        LoadDimensions dimensions = LoadDimensions.builder()
            .length(1.20D)
            .width(0.80D)
            .height(1.00D)
            .build();
        List<String> positions = new ArrayList<>(List.of("front", "back"));
        LoadSet loadSet = completeLoadSet(reference, dimensions, positions)
            .build();
        LoadSet equalLoadSet = completeLoadSet(
            reference,
            dimensions,
            List.copyOf(positions)
        )
            .extensionFields(ExtensionFields.empty())
            .build();
        List<LoadSet> sourceLoadSets = new ArrayList<>(List.of(loadSet));
        LoadSpecification specification = LoadSpecification.builder()
            .loadSets(sourceLoadSets)
            .build();
        List<LoadSet> unequalLoadSets = List.of(
            completeLoadSet(reference, dimensions, positions)
                .setName("SET1")
                .build(),
            completeLoadSet(reference, dimensions, positions)
                .loadType("XLT1200")
                .build(),
            completeLoadSet(reference, dimensions, positions)
                .loadPositions(List.of("front"))
                .build(),
            completeLoadSet(reference, dimensions, positions)
                .boundingBoxReference(
                    BoundingBoxReference.builder()
                        .x(0.30D)
                        .y(0.0D)
                        .z(0.50D)
                        .theta(1.57D)
                        .build()
                )
                .build(),
            completeLoadSet(reference, dimensions, positions)
                .loadDimensions(
                    LoadDimensions.builder()
                        .length(1.30D)
                        .width(0.80D)
                        .height(1.00D)
                        .build()
                )
                .build(),
            completeLoadSet(reference, dimensions, positions)
                .maximumWeight(999.0D)
                .build(),
            completeLoadSet(reference, dimensions, positions)
                .minimumLoadhandlingHeight(0.2D)
                .build(),
            completeLoadSet(reference, dimensions, positions)
                .maximumLoadhandlingHeight(2.1D)
                .build(),
            completeLoadSet(reference, dimensions, positions)
                .minimumLoadhandlingDepth(-0.1D)
                .build(),
            completeLoadSet(reference, dimensions, positions)
                .maximumLoadhandlingDepth(1.3D)
                .build(),
            completeLoadSet(reference, dimensions, positions)
                .minimumLoadhandlingTilt(-0.2D)
                .build(),
            completeLoadSet(reference, dimensions, positions)
                .maximumLoadhandlingTilt(0.3D)
                .build(),
            completeLoadSet(reference, dimensions, positions)
                .maximumSpeed(1.6D)
                .build(),
            completeLoadSet(reference, dimensions, positions)
                .maximumAcceleration(0.9D)
                .build(),
            completeLoadSet(reference, dimensions, positions)
                .maximumDeceleration(0.8D)
                .build(),
            completeLoadSet(reference, dimensions, positions)
                .pickTime(3.1D)
                .build(),
            completeLoadSet(reference, dimensions, positions)
                .dropTime(2.6D)
                .build(),
            completeLoadSet(reference, dimensions, positions)
                .description("alternate pallet")
                .build()
        );
        positions.clear();
        sourceLoadSets.clear();

        assertAll(
            () -> assertEquals("DEFAULT", loadSet.setName()),
            () -> assertEquals("EPAL", loadSet.loadType()),
            () -> assertEquals(List.of("front", "back"), loadSet.loadPositions()),
            () -> assertEquals(reference, loadSet.boundingBoxReference()),
            () -> assertEquals(dimensions, loadSet.loadDimensions()),
            () -> assertEquals(1000.0D, loadSet.maximumWeight()),
            () -> assertEquals(0.1D, loadSet.minimumLoadhandlingHeight()),
            () -> assertEquals(2.0D, loadSet.maximumLoadhandlingHeight()),
            () -> assertEquals(-0.2D, loadSet.minimumLoadhandlingDepth()),
            () -> assertEquals(1.2D, loadSet.maximumLoadhandlingDepth()),
            () -> assertEquals(-0.1D, loadSet.minimumLoadhandlingTilt()),
            () -> assertEquals(0.2D, loadSet.maximumLoadhandlingTilt()),
            () -> assertEquals(1.5D, loadSet.maximumSpeed()),
            () -> assertEquals(0.8D, loadSet.maximumAcceleration()),
            () -> assertEquals(0.7D, loadSet.maximumDeceleration()),
            () -> assertEquals(3.0D, loadSet.pickTime()),
            () -> assertEquals(2.5D, loadSet.dropTime()),
            () -> assertEquals("standard pallet", loadSet.description()),
            () -> assertTrue(loadSet.extensionFields().isEmpty()),
            () -> assertThrows(
                UnsupportedOperationException.class,
                () -> loadSet.loadPositions().clear()
            ),
            () -> assertEquals(List.of(loadSet), specification.loadSets()),
            () -> assertThrows(
                UnsupportedOperationException.class,
                () -> specification.loadSets().clear()
            ),
            () -> assertEquals(loadSet, loadSet),
            () -> assertEquals(loadSet, equalLoadSet),
            () -> assertNotEquals(loadSet, "load set"),
            () -> assertTrue(
                unequalLoadSets.stream().noneMatch(loadSet::equals)
            ),
            () -> assertNotEquals(
                specification,
                LoadSpecification.builder().loadSets(List.of()).build()
            ),
            () -> assertEquals(loadSet.hashCode(), equalLoadSet.hashCode())
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] 可选集合保持缺失空值和不可变语义")
    void preservesMissingEmptyAndImmutableCollections() {
        LoadSet missingPositions = LoadSet.builder()
            .setName("DEFAULT")
            .loadType("EPAL")
            .build();
        LoadSet emptyPositions = LoadSet.builder()
            .setName("DEFAULT")
            .loadType("EPAL")
            .loadPositions(List.of())
            .build();
        LoadSpecification missingSets = LoadSpecification.builder().build();
        LoadSpecification emptySets = LoadSpecification.builder()
            .loadSets(List.of())
            .build();

        assertAll(
            () -> assertNull(missingPositions.loadPositions()),
            () -> assertEquals(List.of(), emptyPositions.loadPositions()),
            () -> assertNull(missingSets.loadSets()),
            () -> assertEquals(List.of(), emptySets.loadSets()),
            () -> assertThrows(
                NullPointerException.class,
                () -> LoadSet.builder()
                    .setName("DEFAULT")
                    .loadType("EPAL")
                    .loadPositions(Arrays.asList("front", null))
                    .build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> LoadSpecification.builder()
                    .loadSets(Arrays.asList(missingPositions, null))
                    .build()
            )
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] 载荷集合只在模型边界强制必填引用")
    void rejectsMissingRequiredLoadSetReferencesButPreservesRawValues() {
        LoadSet rawValues = LoadSet.builder()
            .setName(" DEFAULT ")
            .loadType("EPAL-X")
            .maximumWeight(-1.0D)
            .maximumSpeed(Double.POSITIVE_INFINITY)
            .build();

        assertAll(
            () -> assertEquals(" DEFAULT ", rawValues.setName()),
            () -> assertEquals("EPAL-X", rawValues.loadType()),
            () -> assertEquals(-1.0D, rawValues.maximumWeight()),
            () -> assertEquals(
                Double.POSITIVE_INFINITY,
                rawValues.maximumSpeed()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> LoadSet.builder().loadType("EPAL").build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> LoadSet.builder().setName("DEFAULT").build()
            )
        );
    }

    private static LoadSet.Builder completeLoadSet(
        BoundingBoxReference reference,
        LoadDimensions dimensions,
        List<String> positions
    ) {
        return LoadSet.builder()
            .setName("DEFAULT")
            .loadType("EPAL")
            .loadPositions(positions)
            .boundingBoxReference(reference)
            .loadDimensions(dimensions)
            .maximumWeight(1000.0D)
            .minimumLoadhandlingHeight(0.1D)
            .maximumLoadhandlingHeight(2.0D)
            .minimumLoadhandlingDepth(-0.2D)
            .maximumLoadhandlingDepth(1.2D)
            .minimumLoadhandlingTilt(-0.1D)
            .maximumLoadhandlingTilt(0.2D)
            .maximumSpeed(1.5D)
            .maximumAcceleration(0.8D)
            .maximumDeceleration(0.7D)
            .pickTime(3.0D)
            .dropTime(2.5D)
            .description("standard pallet");
    }
}
