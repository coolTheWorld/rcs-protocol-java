package io.github.cooltheworld.rcs.protocol.vda5050.v3.codec.internal;

/** 绑定前流式预检发现的结构化输入问题。 */
final class JsonPreflightException extends Exception {
    private static final long serialVersionUID = 1L;

    private final String code;
    private final String description;
    private final String requirementId;
    private final String path;

    JsonPreflightException(
        String code,
        String description,
        String requirementId,
        String path
    ) {
        super(description);
        this.code = code;
        this.description = description;
        this.requirementId = requirementId;
        this.path = path;
    }

    String code() {
        return code;
    }

    String description() {
        return description;
    }

    String requirementId() {
        return requirementId;
    }

    String path() {
        return path;
    }
}
