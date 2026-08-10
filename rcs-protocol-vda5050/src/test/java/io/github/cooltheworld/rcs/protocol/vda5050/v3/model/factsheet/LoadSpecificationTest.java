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

final class LoadSpecificationTest {
    @Test
    @DisplayName("[VDA3-FACTSHEET-001] 载荷尺寸保留必填长度宽度和可选高度")
    void buildsLoadDimensionsWithOptionalHeight() {
        LoadDimensions dimensions = LoadDimensions.builder()
            .length(1.2D)
            .width(0.8D)
            .height(1.0D)
            .build();
        LoadDimensions equalDimensions = LoadDimensions.builder()
            .length(1.2D)
            .width(0.8D)
            .height(1.0D)
            .extensionFields(ExtensionFields.empty())
            .build();
        LoadDimensions withoutHeight = LoadDimensions.builder()
            .length(1.2D)
            .width(0.8D)
            .build();

        assertAll(
            () -> assertEquals(1.2D, dimensions.length()),
            () -> assertEquals(0.8D, dimensions.width()),
            () -> assertEquals(1.0D, dimensions.height()),
            () -> assertNull(withoutHeight.height()),
            () -> assertTrue(dimensions.extensionFields().isEmpty()),
            () -> assertEquals(dimensions, dimensions),
            () -> assertEquals(dimensions, equalDimensions),
            () -> assertNotEquals(dimensions, withoutHeight),
            () -> assertNotEquals(dimensions, "load dimensions"),
            () -> assertNotEquals(
                dimensions,
                LoadDimensions.builder()
                    .length(2.0D)
                    .width(0.8D)
                    .height(1.0D)
                    .build()
            ),
            () -> assertNotEquals(
                dimensions,
                LoadDimensions.builder()
                    .length(1.2D)
                    .width(0.9D)
                    .height(1.0D)
                    .build()
            ),
            () -> assertEquals(
                dimensions.hashCode(),
                equalDimensions.hashCode()
            )
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] 载荷尺寸拒绝缺失的必填数值")
    void rejectsMissingRequiredLoadDimensions() {
        assertAll(
            () -> assertThrows(
                NullPointerException.class,
                () -> LoadDimensions.builder().width(0.8D).build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> LoadDimensions.builder().length(1.2D).build()
            )
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] 载荷位置集合保持缺失空值和不可变语义")
    void preservesMissingEmptyAndImmutableLoadPositions() {
        List<String> source = new ArrayList<>(List.of(
            " front ",
            "back"
        ));
        LoadSpecification specification = LoadSpecification.builder()
            .loadPositions(source)
            .build();
        LoadSpecification equalSpecification = LoadSpecification.builder()
            .loadPositions(List.copyOf(source))
            .extensionFields(ExtensionFields.empty())
            .build();
        source.clear();

        assertAll(
            () -> assertEquals(
                List.of(" front ", "back"),
                specification.loadPositions()
            ),
            () -> assertThrows(
                UnsupportedOperationException.class,
                () -> specification.loadPositions().clear()
            ),
            () -> assertNull(
                LoadSpecification.builder().build().loadPositions()
            ),
            () -> assertEquals(
                List.of(),
                LoadSpecification.builder()
                    .loadPositions(List.of())
                    .build()
                    .loadPositions()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> LoadSpecification.builder()
                    .loadPositions(Arrays.asList(
                        "front",
                        null
                    ))
                    .build()
            ),
            () -> assertTrue(specification.extensionFields().isEmpty()),
            () -> assertEquals(specification, specification),
            () -> assertEquals(specification, equalSpecification),
            () -> assertNotEquals(
                specification,
                LoadSpecification.builder().build()
            ),
            () -> assertNotEquals(specification, "load specification"),
            () -> assertEquals(
                specification.hashCode(),
                equalSpecification.hashCode()
            )
        );
    }
}
