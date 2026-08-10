package io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.action.ActionParameterDefinition;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.action.ActionScope;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.action.ActionValueDataType;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.action.BlockingType;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.action.MobileRobotAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

final class ProtocolFeaturesTest {
    @Test
    @DisplayName("[VDA3-FACTSHEET-001] 协议能力强类型保存可选参数与 Action 声明")
    void buildsAnImmutableProtocolFeaturesGraph() {
        List<ActionScope> scopes = new ArrayList<>(List.of(
            ActionScope.INSTANT,
            ActionScope.NODE
        ));
        List<ActionParameterDefinition> parameters = new ArrayList<>(List.of(
            fullParameter().build()
        ));
        List<BlockingType> blockingTypes = new ArrayList<>(List.of(
            BlockingType.NONE,
            BlockingType.SOFT
        ));
        MobileRobotAction action = fullAction()
            .actionScopes(scopes)
            .actionParameters(parameters)
            .blockingTypes(blockingTypes)
            .build();
        List<OptionalParameter> optionalParameters = new ArrayList<>(List.of(
            fullOptionalParameter().build()
        ));
        List<MobileRobotAction> actions = new ArrayList<>(List.of(action));

        ProtocolFeatures features = ProtocolFeatures.builder()
            .optionalParameters(optionalParameters)
            .mobileRobotActions(actions)
            .build();
        ProtocolFeatures equalFeatures = ProtocolFeatures.builder()
            .optionalParameters(List.of(fullOptionalParameter().build()))
            .mobileRobotActions(List.of(fullAction().build()))
            .build();

        scopes.clear();
        parameters.clear();
        blockingTypes.clear();
        optionalParameters.clear();
        actions.clear();

        assertAll(
            () -> assertEquals(
                List.of(fullOptionalParameter().build()),
                features.optionalParameters()
            ),
            () -> assertEquals(List.of(fullAction().build()), features.mobileRobotActions()),
            () -> assertThrows(
                UnsupportedOperationException.class,
                () -> features.optionalParameters().clear()
            ),
            () -> assertThrows(
                UnsupportedOperationException.class,
                () -> action.actionScopes().clear()
            ),
            () -> assertTrue(features.extensionFields().isEmpty()),
            () -> assertEquals(features, equalFeatures),
            () -> assertEquals(features.hashCode(), equalFeatures.hashCode())
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] 协议能力公开规范封闭枚举的全部值")
    void exposesEveryClosedProtocolFeatureValue() {
        assertAll(
            () -> assertEquals(
                Set.of(
                    OptionalParameterSupport.SUPPORTED,
                    OptionalParameterSupport.REQUIRED
                ),
                Set.of(OptionalParameterSupport.values())
            ),
            () -> assertEquals(
                Set.of(
                    ActionScope.INSTANT,
                    ActionScope.NODE,
                    ActionScope.EDGE,
                    ActionScope.ZONE
                ),
                Set.of(ActionScope.values())
            ),
            () -> assertEquals(
                Set.of(
                    ActionValueDataType.BOOL,
                    ActionValueDataType.NUMBER,
                    ActionValueDataType.INTEGER,
                    ActionValueDataType.STRING,
                    ActionValueDataType.OBJECT,
                    ActionValueDataType.ARRAY
                ),
                Set.of(ActionValueDataType.values())
            ),
            () -> assertEquals(
                Set.of(
                    BlockingType.NONE,
                    BlockingType.SOFT,
                    BlockingType.SINGLE,
                    BlockingType.HARD
                ),
                Set.of(BlockingType.values())
            )
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] 可选 Action 字段区分缺失、false 与空数组")
    void distinguishesMissingFalseAndEmptyOptionalFields() {
        OptionalParameter optionalParameter = OptionalParameter.builder()
            .parameter("order.nodes.nodePosition.allowedDeviationTheta")
            .support(OptionalParameterSupport.SUPPORTED)
            .build();
        ActionParameterDefinition parameter = ActionParameterDefinition.builder()
            .key("height")
            .valueDataType(ActionValueDataType.NUMBER)
            .isOptional(false)
            .build();
        MobileRobotAction missing = minimalAction().build();
        MobileRobotAction empty = minimalAction()
            .actionParameters(List.of())
            .blockingTypes(List.of())
            .build();

        assertAll(
            () -> assertNull(optionalParameter.description()),
            () -> assertEquals(false, parameter.isOptional()),
            () -> assertNull(missing.actionDescription()),
            () -> assertNull(missing.actionParameters()),
            () -> assertNull(missing.actionResult()),
            () -> assertNull(missing.blockingTypes()),
            () -> assertEquals(List.of(), empty.actionParameters()),
            () -> assertEquals(List.of(), empty.blockingTypes()),
            () -> assertNotEquals(missing, empty)
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] 协议能力值相等覆盖每个标准字段")
    void includesEveryStandardFieldInValueEquality() {
        OptionalParameter optionalParameter = fullOptionalParameter().build();
        ActionParameterDefinition parameter = fullParameter().build();
        MobileRobotAction action = fullAction().build();
        ProtocolFeatures features = fullFeatures().build();

        assertAll(
            () -> assertEquals(optionalParameter, optionalParameter),
            () -> assertNotEquals(optionalParameter, null),
            () -> assertNotEquals(
                optionalParameter,
                fullOptionalParameter().parameter("order.edges.trajectory").build()
            ),
            () -> assertNotEquals(
                optionalParameter,
                fullOptionalParameter().support(OptionalParameterSupport.REQUIRED).build()
            ),
            () -> assertNotEquals(
                optionalParameter,
                fullOptionalParameter().description("Different").build()
            ),
            () -> assertEquals(parameter, parameter),
            () -> assertNotEquals(parameter, null),
            () -> assertNotEquals(parameter, fullParameter().key("speed").build()),
            () -> assertNotEquals(
                parameter,
                fullParameter().valueDataType(ActionValueDataType.STRING).build()
            ),
            () -> assertNotEquals(
                parameter,
                fullParameter().description("Different").build()
            ),
            () -> assertNotEquals(parameter, fullParameter().isOptional(false).build()),
            () -> assertEquals(action, action),
            () -> assertNotEquals(action, null),
            () -> assertNotEquals(action, fullAction().actionType("drop").build()),
            () -> assertNotEquals(
                action,
                fullAction().actionDescription("Different").build()
            ),
            () -> assertNotEquals(
                action,
                fullAction().actionScopes(List.of(ActionScope.EDGE)).build()
            ),
            () -> assertNotEquals(
                action,
                fullAction().actionParameters(List.of()).build()
            ),
            () -> assertNotEquals(action, fullAction().actionResult("Different").build()),
            () -> assertNotEquals(
                action,
                fullAction().blockingTypes(List.of(BlockingType.HARD)).build()
            ),
            () -> assertNotEquals(action, fullAction().pauseAllowed(false).build()),
            () -> assertNotEquals(action, fullAction().cancelAllowed(false).build()),
            () -> assertEquals(features, features),
            () -> assertNotEquals(features, null),
            () -> assertNotEquals(
                features,
                fullFeatures().optionalParameters(List.of()).build()
            ),
            () -> assertNotEquals(
                features,
                fullFeatures().mobileRobotActions(List.of()).build()
            )
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] 协议能力拒绝缺失必填字段和含 null 的列表")
    void rejectsMissingRequiredFieldsAndNullListElements() {
        assertAll(
            () -> assertThrows(
                NullPointerException.class,
                () -> fullOptionalParameter().parameter(null).build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> fullOptionalParameter().support(null).build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> fullParameter().key(null).build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> fullParameter().valueDataType(null).build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> fullAction().actionType(null).build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> fullAction().actionScopes(null).build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> fullAction().pauseAllowed(null).build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> fullAction().cancelAllowed(null).build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> fullAction().actionScopes(Arrays.asList(ActionScope.NODE, null)).build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> fullAction()
                    .actionParameters(Arrays.asList(fullParameter().build(), null))
                    .build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> fullFeatures().optionalParameters(null).build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> fullFeatures().mobileRobotActions(null).build()
            )
        );
    }

    private static OptionalParameter.Builder fullOptionalParameter() {
        return OptionalParameter.builder()
            .parameter("order.nodes.nodePosition.allowedDeviationTheta")
            .support(OptionalParameterSupport.SUPPORTED)
            .description("Supported within configured tolerance");
    }

    private static ActionParameterDefinition.Builder fullParameter() {
        return ActionParameterDefinition.builder()
            .key("height")
            .valueDataType(ActionValueDataType.NUMBER)
            .description("Lift height in metres")
            .isOptional(true);
    }

    private static MobileRobotAction.Builder minimalAction() {
        return MobileRobotAction.builder()
            .actionType("pick")
            .actionScopes(List.of(ActionScope.INSTANT, ActionScope.NODE))
            .pauseAllowed(true)
            .cancelAllowed(true);
    }

    private static MobileRobotAction.Builder fullAction() {
        return minimalAction()
            .actionDescription("Pick a load")
            .actionParameters(List.of(fullParameter().build()))
            .actionResult("Picked load identifier")
            .blockingTypes(List.of(BlockingType.NONE, BlockingType.SOFT));
    }

    private static ProtocolFeatures.Builder fullFeatures() {
        return ProtocolFeatures.builder()
            .optionalParameters(List.of(fullOptionalParameter().build()))
            .mobileRobotActions(List.of(fullAction().build()));
    }
}
