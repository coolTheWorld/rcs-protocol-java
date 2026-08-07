package io.github.cooltheworld.rcs.protocol.vda5050.v3.validation;

/** 结构化校验问题的严重级别。 */
public enum ValidationSeverity {
    /** 阻止消息获得成功校验凭证的问题。 */
    ERROR,

    /** 不阻止消息获得凭证、但应由调用方关注的问题。 */
    WARNING
}
