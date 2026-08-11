package io.github.cooltheworld.rcs.protocol.vda5050.v3.validation;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.LocalizationType;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.MobileRobotClass;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.MobileRobotKinematics;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.NavigationType;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.TypeSpecification;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

final class TypeSpecificationValidatorTest {
    private static final TypeSpecificationValidator VALIDATOR =
        TypeSpecificationValidator.create();

    @Test
    @DisplayName("[VDA3-FACTSHEET-006] 接受零值和有限正最大载荷并保持输入不变")
    void acceptsFiniteNonNegativeMaximumLoadMass() {
        TypeSpecification zero = specification(0.0D);
        TypeSpecification positive = specification(1_250.5D);
        TypeSpecification snapshot = specification(positive.maximumLoadMass());

        List<ValidationIssue> issues = VALIDATOR.validate(positive);

        assertAll(
            () -> assertEquals(List.of(), VALIDATOR.validate(zero)),
            () -> assertEquals(List.of(), issues),
            () -> assertEquals(issues, VALIDATOR.validate(positive)),
            () -> assertEquals(snapshot, positive),
            () -> assertThrows(UnsupportedOperationException.class, issues::clear),
            () -> assertThrows(
                NullPointerException.class,
                () -> VALIDATOR.validate(null)
            )
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-006] 报告全部非有限最大载荷值")
    void rejectsEveryNonFiniteMaximumLoadMass() {
        List<ValidationIssue> nan = VALIDATOR.validate(specification(Double.NaN));
        List<ValidationIssue> positiveInfinity = VALIDATOR.validate(
            specification(Double.POSITIVE_INFINITY)
        );
        List<ValidationIssue> negativeInfinity = VALIDATOR.validate(
            specification(Double.NEGATIVE_INFINITY)
        );

        assertAll(
            () -> assertEquals(nan, positiveInfinity),
            () -> assertEquals(nan, negativeInfinity),
            () -> assertEquals(1, nan.size()),
            () -> assertEquals(
                "NON_FINITE_MAXIMUM_LOAD_MASS",
                nan.getFirst().code()
            ),
            () -> assertEquals(
                "/typeSpecification/maximumLoadMass",
                nan.getFirst().path()
            ),
            () -> assertEquals(
                "Maximum load mass must be finite",
                nan.getFirst().description()
            ),
            () -> assertEquals(
                "VDA3-FACTSHEET-006",
                nan.getFirst().requirementId()
            ),
            () -> assertEquals(
                ValidationSeverity.ERROR,
                nan.getFirst().severity()
            )
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-006] 拒绝负最大载荷但不增加正数上限")
    void rejectsNegativeMaximumLoadMassWithoutInventingAnUpperBound() {
        List<ValidationIssue> issues = VALIDATOR.validate(specification(-0.1D));

        assertAll(
            () -> assertEquals(1, issues.size()),
            () -> assertEquals(
                "NEGATIVE_MAXIMUM_LOAD_MASS",
                issues.getFirst().code()
            ),
            () -> assertEquals(
                "/typeSpecification/maximumLoadMass",
                issues.getFirst().path()
            ),
            () -> assertEquals(
                "Maximum load mass must not be negative",
                issues.getFirst().description()
            ),
            () -> assertTrue(
                VALIDATOR.validate(specification(Double.MAX_VALUE)).isEmpty()
            )
        );
    }

    private static TypeSpecification specification(Double maximumLoadMass) {
        return TypeSpecification.builder()
            .seriesName("Carrier X")
            .mobileRobotKinematics(MobileRobotKinematics.DIFFERENTIAL)
            .mobileRobotClass(MobileRobotClass.CARRIER)
            .maximumLoadMass(maximumLoadMass)
            .localizationTypes(List.of(LocalizationType.NATURAL))
            .navigationTypes(List.of(NavigationType.FREELY_NAVIGATING))
            .build();
    }
}
