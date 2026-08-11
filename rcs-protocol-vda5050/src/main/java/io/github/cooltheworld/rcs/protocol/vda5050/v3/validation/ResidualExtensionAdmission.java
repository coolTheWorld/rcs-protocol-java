package io.github.cooltheworld.rcs.protocol.vda5050.v3.validation;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.ExtensionFields;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.topic.TopicName;
import java.util.Objects;
import java.util.Set;

/**
 * 八个标准 Topic 的角色感知残余扩展准入。
 *
 * <p>Mobile Robot 对控制 Topic 的非空未注册扩展 fail closed；Fleet Control 对遥测 Topic 的非空
 * 扩展保留原强类型消息并产生不携带扩展内容的观察标记。该服务不提供按键或按值读取扩展的入口。</p>
 */
public final class ResidualExtensionAdmission {
    private static final Set<TopicName> MOBILE_ROBOT_TOPICS = Set.of(
        TopicName.ORDER,
        TopicName.INSTANT_ACTIONS,
        TopicName.ZONE_SET,
        TopicName.RESPONSES
    );
    private static final Set<TopicName> FLEET_CONTROL_TOPICS = Set.of(
        TopicName.STATE,
        TopicName.CONNECTION,
        TopicName.FACTSHEET,
        TopicName.VISUALIZATION
    );
    private static final Decision NO_RESIDUAL_EXTENSION =
        new NoResidualExtension();
    private static final Decision RETAINED_AND_OBSERVED =
        new RetainedAndObserved();
    private static final Decision UNSUPPORTED_PARAMETER = new Rejected(
        new ValidationIssue(
            "UNSUPPORTED_PARAMETER",
            ValidationSeverity.ERROR,
            "",
            "Unregistered extension is not supported",
            "VDA3-SHARED-012"
        )
    );

    private ResidualExtensionAdmission() {
    }

    /** @return 无状态角色感知准入服务 */
    public static ResidualExtensionAdmission create() {
        return new ResidualExtensionAdmission();
    }

    /**
     * 处理 Mobile Robot 接收的控制 Topic 残余扩展。
     *
     * @param topicName 控制 Topic
     * @param extensionFields 已注册 Adapter 之后仍未识别的扩展
     * @return 空扩展成功，非空扩展固定拒绝
     */
    public Decision admitForMobileRobot(
        TopicName topicName,
        ExtensionFields extensionFields
    ) {
        requireReceivedTopic(MOBILE_ROBOT_TOPICS, topicName);
        Objects.requireNonNull(extensionFields, "extensionFields");
        if (extensionFields.isEmpty()) {
            return NO_RESIDUAL_EXTENSION;
        }
        return UNSUPPORTED_PARAMETER;
    }

    /**
     * 处理 Fleet Control 接收的遥测 Topic 残余扩展。
     *
     * @param topicName 遥测 Topic
     * @param extensionFields 已知字段之外的不透明扩展
     * @return 空扩展成功，非空扩展返回无内容观察标记
     */
    public Decision admitForFleetControl(
        TopicName topicName,
        ExtensionFields extensionFields
    ) {
        requireReceivedTopic(FLEET_CONTROL_TOPICS, topicName);
        Objects.requireNonNull(extensionFields, "extensionFields");
        if (extensionFields.isEmpty()) {
            return NO_RESIDUAL_EXTENSION;
        }
        return RETAINED_AND_OBSERVED;
    }

    private static void requireReceivedTopic(
        Set<TopicName> expectedTopics,
        TopicName topicName
    ) {
        Objects.requireNonNull(topicName, "topicName");
        if (!expectedTopics.contains(topicName)) {
            throw new IllegalArgumentException(
                "Topic is not received by the selected role"
            );
        }
    }

    /** 残余扩展准入的封闭结果。 */
    public sealed interface Decision permits
            NoResidualExtension,
            RetainedAndObserved,
            Rejected {
        /** @return 当前消息可继续处理时返回 {@code true} */
        boolean isAccepted();

        /** @return 需要产生脱敏观察时返回 {@code true} */
        boolean observationRequired();
    }

    /** 空残余扩展，无需额外观察。 */
    public record NoResidualExtension() implements Decision {
        @Override
        public boolean isAccepted() {
            return true;
        }

        @Override
        public boolean observationRequired() {
            return false;
        }
    }

    /** Fleet Control 保留原消息并产生脱敏观察。 */
    public record RetainedAndObserved() implements Decision {
        @Override
        public boolean isAccepted() {
            return true;
        }

        @Override
        public boolean observationRequired() {
            return true;
        }
    }

    /**
     * Mobile Robot 的普通协议拒绝。
     *
     * @param issue 固定安全 Issue
     */
    public record Rejected(ValidationIssue issue) implements Decision {
        public Rejected {
            Objects.requireNonNull(issue, "issue");
        }

        @Override
        public boolean isAccepted() {
            return false;
        }

        @Override
        public boolean observationRequired() {
            return false;
        }
    }
}
