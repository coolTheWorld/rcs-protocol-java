package io.github.cooltheworld.rcs.protocol.vda5050.v3.validation;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.codec.JsonCodecLimits;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.codec.Vda5050JsonCodec;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.internal.Unsigned32;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.Connection;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.ProtocolHeader;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.ProtocolVersionProfile;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.topic.TopicAddress;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.topic.TopicLayout;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.topic.TopicName;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 对不可信 {@code connection} payload 执行前三层入站校验并铸造成功凭证。
 *
 * <p>本入口先执行有资源上限的 JSON 与 Schema 校验，再解码强类型消息，最后检查
 * {@code uint32}、显式版本配置以及 Topic 与消息头的一致性。普通非法输入只返回
 * {@link RejectedInboundMessage}，不会通过异常表达。</p>
 */
public final class ConnectionValidator {
    private static final String CONNECTION_REQUIREMENT =
        "VDA3-CONNECTION-001";
    private static final String TOPIC_LAYOUT_REQUIREMENT = "VDA3-SHARED-011";

    private final Vda5050JsonCodec codec;
    private final Vda5050SchemaValidator schemaValidator;

    private ConnectionValidator(JsonCodecLimits limits) {
        codec = Vda5050JsonCodec.create(limits);
        schemaValidator = Vda5050SchemaValidator.create(limits);
    }

    /** @return 使用默认 JSON 资源硬上限的 Connection Validator */
    public static ConnectionValidator createDefault() {
        return create(JsonCodecLimits.defaults());
    }

    /**
     * @param limits 在任何协议对象绑定前执行的 JSON 资源硬上限
     * @return 可缓存复用且线程安全的 Connection Validator
     */
    public static ConnectionValidator create(JsonCodecLimits limits) {
        return new ConnectionValidator(Objects.requireNonNull(limits, "limits"));
    }

    /**
     * 校验实际 Topic 路径上的不可信 Connection payload。
     *
     * @param topicLayout 部署选用的受控 Topic 布局
     * @param topicPath 实际收到消息的 MQTT Topic 路径
     * @param payload 不可信 UTF-8 JSON 字节
     * @return 成功凭证或只含安全上下文的拒绝数据
     */
    public ValidationResult<Connection> validate(
        TopicLayout topicLayout,
        String topicPath,
        byte[] payload
    ) {
        Objects.requireNonNull(topicLayout, "topicLayout");
        Objects.requireNonNull(topicPath, "topicPath");
        Objects.requireNonNull(payload, "payload");

        List<ValidationIssue> schemaIssues = schemaValidator.validate(
            TopicName.CONNECTION,
            payload
        );
        if (!schemaIssues.isEmpty()) {
            return rejected(schemaIssues);
        }

        DecodingResult<Connection> decoding = codec.decode(
            TopicName.CONNECTION,
            payload,
            Connection.class
        );
        if (!decoding.isDecoded()) {
            return (RejectedInboundMessage<Connection>) decoding;
        }

        Connection connection = ((DecodedMessage<Connection>) decoding).message();
        List<ValidationIssue> semanticIssues = validateSemantics(
            topicLayout,
            topicPath,
            connection
        );
        if (!semanticIssues.isEmpty()) {
            return rejected(connection, semanticIssues);
        }
        ProtocolVersionProfile profile = ProtocolVersionProfile.requireSupported(
            connection.header().version()
        );
        return new ValidatedMessage<>(connection, profile, List.of());
    }

    private static List<ValidationIssue> validateSemantics(
        TopicLayout topicLayout,
        String topicPath,
        Connection connection
    ) {
        ProtocolHeader header = connection.header();
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
        return List.copyOf(issues);
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

        if (topicName != TopicName.CONNECTION) {
            issues.add(issue(
                "TOPIC_MESSAGE_TYPE_MISMATCH",
                "",
                "Topic path does not identify a connection message",
                CONNECTION_REQUIREMENT
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

    private static RejectedInboundMessage<Connection> rejected(
        List<ValidationIssue> issues
    ) {
        return RejectedInboundMessage.<Connection>builder(
            TopicName.CONNECTION,
            issues
        ).build();
    }

    private static RejectedInboundMessage<Connection> rejected(
        Connection connection,
        List<ValidationIssue> issues
    ) {
        ProtocolHeader header = connection.header();
        RejectedInboundMessage.Builder<Connection> builder =
            RejectedInboundMessage.<Connection>builder(
                TopicName.CONNECTION,
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
