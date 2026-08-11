package io.github.cooltheworld.rcs.protocol.vda5050.v3.validation;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.codec.JsonCodecLimits;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.codec.Vda5050JsonCodec;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.internal.Unsigned32;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.common.ProtocolHeader;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.common.ProtocolVersionProfile;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.Factsheet;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.FactsheetContent;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.topic.TopicAddress;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.topic.TopicLayout;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.topic.TopicName;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** 对不可信 {@code factsheet} payload 执行完整前三层入站校验。 */
public final class FactsheetValidator {
    private static final String FACTSHEET_REQUIREMENT =
        "VDA3-FACTSHEET-001";
    private static final String TOPIC_LAYOUT_REQUIREMENT = "VDA3-SHARED-011";

    private final Vda5050JsonCodec codec;
    private final Vda5050SchemaValidator schemaValidator;
    private final TypeSpecificationValidator typeSpecificationValidator;
    private final PhysicalParametersValidator physicalParametersValidator;
    private final ProtocolFeaturesValidator protocolFeaturesValidator;
    private final MobileRobotGeometryValidator geometryValidator;
    private final LoadSpecificationValidator loadSpecificationValidator;
    private final MobileRobotConfigurationValidator configurationValidator;

    private FactsheetValidator(JsonCodecLimits limits) {
        codec = Vda5050JsonCodec.create(limits);
        schemaValidator = Vda5050SchemaValidator.create(limits);
        typeSpecificationValidator = TypeSpecificationValidator.create();
        physicalParametersValidator = PhysicalParametersValidator.create();
        protocolFeaturesValidator = ProtocolFeaturesValidator.create();
        geometryValidator = MobileRobotGeometryValidator.create();
        loadSpecificationValidator = LoadSpecificationValidator.create();
        configurationValidator = MobileRobotConfigurationValidator.create();
    }

    /** @return 使用默认 JSON 资源硬上限的 Factsheet Validator */
    public static FactsheetValidator createDefault() {
        return create(JsonCodecLimits.defaults());
    }

    /**
     * @param limits 在任何协议对象绑定前执行的 JSON 资源硬上限
     * @return 可缓存复用且线程安全的 Factsheet Validator
     */
    public static FactsheetValidator create(JsonCodecLimits limits) {
        return new FactsheetValidator(
            Objects.requireNonNull(limits, "limits")
        );
    }

    /**
     * 校验实际 Topic 路径上的不可信 Factsheet payload。
     *
     * @param topicLayout 部署选用的受控 Topic 布局
     * @param topicPath 实际收到消息的 MQTT Topic 路径
     * @param payload 不可信 UTF-8 JSON 字节
     * @return 成功凭证或只含安全强类型上下文的拒绝数据
     */
    public ValidationResult<Factsheet> validate(
        TopicLayout topicLayout,
        String topicPath,
        byte[] payload
    ) {
        Objects.requireNonNull(topicLayout, "topicLayout");
        Objects.requireNonNull(topicPath, "topicPath");
        Objects.requireNonNull(payload, "payload");

        List<ValidationIssue> schemaIssues = schemaValidator.validate(
            TopicName.FACTSHEET,
            payload
        );
        if (!schemaIssues.isEmpty()) {
            return rejected(schemaIssues);
        }

        DecodingResult<Factsheet> decoding = codec.decode(
            TopicName.FACTSHEET,
            payload,
            Factsheet.class
        );
        if (!decoding.isDecoded()) {
            return (RejectedInboundMessage<Factsheet>) decoding;
        }

        Factsheet factsheet = ((DecodedMessage<Factsheet>) decoding).message();
        List<ValidationIssue> semanticIssues = validateSemantics(
            topicLayout,
            topicPath,
            factsheet
        );
        if (!semanticIssues.isEmpty()) {
            return rejected(factsheet, semanticIssues);
        }
        ProtocolVersionProfile profile = ProtocolVersionProfile.requireSupported(
            factsheet.header().version()
        );
        return new ValidatedMessage<>(factsheet, profile, List.of());
    }

    private List<ValidationIssue> validateSemantics(
        TopicLayout topicLayout,
        String topicPath,
        Factsheet factsheet
    ) {
        ProtocolHeader header = factsheet.header();
        FactsheetContent content = factsheet.content();
        List<ValidationIssue> issues = new ArrayList<>();
        if (!Unsigned32.isValid(header.headerId())) {
            issues.add(issue(
                "UINT32_OUT_OF_RANGE",
                "/headerId",
                "Header ID is outside the uint32 range",
                "VDA3-SHARED-002"
            ));
        }
        if (!ProtocolVersionProfile.supports(header.version())) {
            issues.add(issue(
                "UNSUPPORTED_PROTOCOL_VERSION",
                "/version",
                "Protocol version has no explicit support profile",
                "VDA3-SHARED-003"
            ));
        }
        validateTopic(topicLayout, topicPath, header, issues);
        issues.addAll(typeSpecificationValidator.validate(
            content.typeSpecification()
        ));
        issues.addAll(physicalParametersValidator.validate(
            content.physicalParameters()
        ));
        appendPrefixed(
            "/protocolFeatures",
            protocolFeaturesValidator.validate(content.protocolFeatures()),
            issues
        );
        appendPrefixed(
            "/mobileRobotGeometry",
            geometryValidator.validate(content.mobileRobotGeometry()),
            issues
        );
        appendPrefixed(
            "/loadSpecification",
            loadSpecificationValidator.validate(content.loadSpecification()),
            issues
        );
        if (content.mobileRobotConfiguration() != null) {
            appendPrefixed(
                "/mobileRobotConfiguration",
                configurationValidator.validate(
                    content.mobileRobotConfiguration()
                ),
                issues
            );
        }
        return List.copyOf(issues);
    }

    private static void appendPrefixed(
        String prefix,
        List<ValidationIssue> source,
        List<ValidationIssue> target
    ) {
        for (ValidationIssue issue : source) {
            target.add(new ValidationIssue(
                issue.code(),
                issue.severity(),
                prefix + issue.path(),
                issue.description(),
                issue.requirementId()
            ));
        }
    }

    private static void validateTopic(
        TopicLayout topicLayout,
        String topicPath,
        ProtocolHeader header,
        List<ValidationIssue> issues
    ) {
        final TopicAddress address;
        final TopicName topicName;
        try {
            address = Objects.requireNonNull(
                topicLayout.parse(topicPath),
                "parsedTopicAddress"
            );
            topicName = TopicLayout.parseForRobot(
                topicLayout,
                topicPath,
                address.robotIdentity()
            );
        } catch (IllegalArgumentException exception) {
            issues.add(issue(
                "INVALID_TOPIC_ADDRESS",
                "",
                "Topic path does not satisfy the configured VDA 5050 layout",
                TOPIC_LAYOUT_REQUIREMENT
            ));
            return;
        }

        if (topicName != TopicName.FACTSHEET) {
            issues.add(issue(
                "TOPIC_MESSAGE_TYPE_MISMATCH",
                "",
                "Topic path does not identify a factsheet message",
                FACTSHEET_REQUIREMENT
            ));
        }
        if (!address.robotIdentity().manufacturer().equals(
            header.robotIdentity().manufacturer()
        )) {
            issues.add(topicHeaderMismatch("/manufacturer"));
        }
        if (!address.robotIdentity().serialNumber().equals(
            header.robotIdentity().serialNumber()
        )) {
            issues.add(topicHeaderMismatch("/serialNumber"));
        }
    }

    private static ValidationIssue topicHeaderMismatch(String path) {
        return issue(
            "TOPIC_HEADER_MISMATCH",
            path,
            "Topic identity does not match the message header identity",
            "VDA3-SHARED-004"
        );
    }

    private static ValidationIssue issue(
        String code,
        String path,
        String description,
        String requirementId
    ) {
        return new ValidationIssue(
            code,
            ValidationSeverity.ERROR,
            path,
            description,
            requirementId
        );
    }

    private static RejectedInboundMessage<Factsheet> rejected(
        List<ValidationIssue> issues
    ) {
        return RejectedInboundMessage.<Factsheet>builder(
            TopicName.FACTSHEET,
            issues
        ).build();
    }

    private static RejectedInboundMessage<Factsheet> rejected(
        Factsheet factsheet,
        List<ValidationIssue> issues
    ) {
        ProtocolHeader header = factsheet.header();
        RejectedInboundMessage.Builder<Factsheet> builder =
            RejectedInboundMessage.<Factsheet>builder(
                TopicName.FACTSHEET,
                issues
            )
                .robotIdentity(header.robotIdentity())
                .timestamp(header.timestamp())
                .version(header.version());
        if (Unsigned32.isValid(header.headerId())) {
            builder.headerId(header.headerId());
        }
        return builder.build();
    }
}
