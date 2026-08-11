package io.github.cooltheworld.rcs.protocol.vda5050.v3.validation;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.ExtensionFields;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.topic.TopicName;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

final class ResidualExtensionAdmissionTest {
    private static final String SECRET = "credential-do-not-expose";

    @ParameterizedTest(name = "{0} 接收 {1}")
    @MethodSource("receivingTopics")
    @DisplayName("[VDA3-SHARED-012] 八个 Topic 按接收角色处理非空残余扩展")
    void handlesNonEmptyResidualExtensionsByReceivingRole(
        Receiver receiver,
        TopicName topicName
    ) throws ReflectiveOperationException {
        ResidualExtensionAdmission admission = ResidualExtensionAdmission.create();
        ExtensionFields extensionFields = extensionFields();

        ResidualExtensionAdmission.Decision decision = receiver == Receiver.MOBILE_ROBOT
            ? admission.admitForMobileRobot(topicName, extensionFields)
            : admission.admitForFleetControl(topicName, extensionFields);

        if (receiver == Receiver.MOBILE_ROBOT) {
            ResidualExtensionAdmission.Rejected rejected = assertInstanceOf(
                ResidualExtensionAdmission.Rejected.class,
                decision
            );
            assertAll(
                () -> assertFalse(decision.isAccepted()),
                () -> assertFalse(decision.observationRequired()),
                () -> assertEquals("UNSUPPORTED_PARAMETER", rejected.issue().code()),
                () -> assertEquals("", rejected.issue().path()),
                () -> assertEquals(
                    "Unregistered extension is not supported",
                    rejected.issue().description()
                ),
                () -> assertEquals(
                    "VDA3-SHARED-012",
                    rejected.issue().requirementId()
                )
            );
            return;
        }

        assertAll(
            () -> assertInstanceOf(
                ResidualExtensionAdmission.RetainedAndObserved.class,
                decision
            ),
            () -> assertTrue(decision.isAccepted()),
            () -> assertTrue(decision.observationRequired())
        );
    }

    @ParameterizedTest(name = "{0} 接收 {1} 空扩展")
    @MethodSource("receivingTopics")
    @DisplayName("[VDA3-SHARED-012] 八个 Topic 的空残余扩展无需观察")
    void acceptsEmptyResidualExtensionsWithoutObservation(
        Receiver receiver,
        TopicName topicName
    ) {
        ResidualExtensionAdmission admission = ResidualExtensionAdmission.create();

        ResidualExtensionAdmission.Decision decision = receiver == Receiver.MOBILE_ROBOT
            ? admission.admitForMobileRobot(topicName, ExtensionFields.empty())
            : admission.admitForFleetControl(topicName, ExtensionFields.empty());

        assertAll(
            () -> assertInstanceOf(
                ResidualExtensionAdmission.NoResidualExtension.class,
                decision
            ),
            () -> assertTrue(decision.isAccepted()),
            () -> assertFalse(decision.observationRequired())
        );
    }

    @ParameterizedTest(name = "错误角色接收 {1}")
    @MethodSource("receivingTopics")
    @DisplayName("[VDA3-SHARED-012] 反向角色 Topic 调用作为编程错误拒绝")
    void rejectsTopicsThatAreNotReceivedByTheSelectedRole(
        Receiver receiver,
        TopicName topicName
    ) {
        ResidualExtensionAdmission admission = ResidualExtensionAdmission.create();

        IllegalArgumentException error = assertThrows(
            IllegalArgumentException.class,
            () -> {
                if (receiver == Receiver.MOBILE_ROBOT) {
                    admission.admitForFleetControl(
                        topicName,
                        ExtensionFields.empty()
                    );
                } else {
                    admission.admitForMobileRobot(
                        topicName,
                        ExtensionFields.empty()
                    );
                }
            }
        );

        assertEquals("Topic is not received by the selected role", error.getMessage());
    }

    @Test
    @DisplayName("[VDA3-SHARED-012] 准入结果不携带扩展键值或动态读取入口")
    void exposesNoResidualExtensionContentInAdmissionDecisions()
        throws ReflectiveOperationException {
        ResidualExtensionAdmission admission = ResidualExtensionAdmission.create();
        ExtensionFields extensionFields = extensionFields();
        ResidualExtensionAdmission.Decision fleetDecision =
            admission.admitForFleetControl(TopicName.STATE, extensionFields);
        ResidualExtensionAdmission.Decision mobileDecision =
            admission.admitForMobileRobot(TopicName.ORDER, extensionFields);

        assertAll(
            () -> assertEquals(
                0,
                ResidualExtensionAdmission.RetainedAndObserved.class
                    .getRecordComponents()
                    .length
            ),
            () -> assertFalse(fleetDecision.toString().contains(SECRET)),
            () -> assertFalse(mobileDecision.toString().contains(SECRET)),
            () -> assertFalse(extensionFields.isEmpty())
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-012] 相同残余扩展准入确定且不修改输入")
    void replaysDeterministicallyWithoutChangingOpaqueInput()
        throws ReflectiveOperationException {
        ResidualExtensionAdmission admission = ResidualExtensionAdmission.create();
        ExtensionFields extensionFields = extensionFields();

        ResidualExtensionAdmission.Decision first =
            admission.admitForFleetControl(TopicName.FACTSHEET, extensionFields);
        ResidualExtensionAdmission.Decision replay =
            admission.admitForFleetControl(TopicName.FACTSHEET, extensionFields);

        assertAll(
            () -> assertEquals(first, replay),
            () -> assertFalse(extensionFields.isEmpty())
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-012] 缺失 Topic 或扩展引用封闭失败")
    void rejectsMissingRequiredArguments() {
        ResidualExtensionAdmission admission = ResidualExtensionAdmission.create();

        assertAll(
            () -> assertThrows(
                NullPointerException.class,
                () -> admission.admitForMobileRobot(null, ExtensionFields.empty())
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> admission.admitForMobileRobot(TopicName.ORDER, null)
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> admission.admitForFleetControl(null, ExtensionFields.empty())
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> admission.admitForFleetControl(TopicName.STATE, null)
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> new ResidualExtensionAdmission.Rejected(null)
            )
        );
    }

    private static Stream<Arguments> receivingTopics() {
        return Stream.of(
            Arguments.of(Receiver.MOBILE_ROBOT, TopicName.ORDER),
            Arguments.of(Receiver.MOBILE_ROBOT, TopicName.INSTANT_ACTIONS),
            Arguments.of(Receiver.MOBILE_ROBOT, TopicName.ZONE_SET),
            Arguments.of(Receiver.MOBILE_ROBOT, TopicName.RESPONSES),
            Arguments.of(Receiver.FLEET_CONTROL, TopicName.STATE),
            Arguments.of(Receiver.FLEET_CONTROL, TopicName.CONNECTION),
            Arguments.of(Receiver.FLEET_CONTROL, TopicName.FACTSHEET),
            Arguments.of(Receiver.FLEET_CONTROL, TopicName.VISUALIZATION)
        );
    }

    private static ExtensionFields extensionFields()
        throws ReflectiveOperationException {
        Method factory = ExtensionFields.class.getDeclaredMethod(
            "fromJsonBytes",
            byte[].class,
            byte[].class
        );
        factory.setAccessible(true);
        byte[] bytes = ("{\"vendor\":\"" + SECRET + "\"}").getBytes(
            StandardCharsets.UTF_8
        );
        return (ExtensionFields) factory.invoke(null, bytes, bytes);
    }

    private enum Receiver {
        FLEET_CONTROL,
        MOBILE_ROBOT
    }
}
