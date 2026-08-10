package io.github.cooltheworld.rcs.protocol.vda5050.v3.model.common;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * VDA 5050 的规范 UTC 时间戳。
 *
 * <p>内部值固定为毫秒精度，文本形式固定为
 * {@code YYYY-MM-DDTHH:mm:ss.SSSZ}。</p>
 */
public final class ProtocolTimestamp {
    private static final Pattern CANONICAL_PATTERN = Pattern.compile(
        "[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}\\.[0-9]{3}Z"
    );
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter
        .ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ROOT)
        .withZone(ZoneOffset.UTC);

    private final Instant instant;

    private ProtocolTimestamp(Instant instant) {
        this.instant = Objects.requireNonNull(instant, "instant").truncatedTo(ChronoUnit.MILLIS);
        String canonicalValue = FORMATTER.format(this.instant);
        if (!CANONICAL_PATTERN.matcher(canonicalValue).matches()) {
            throw new IllegalArgumentException(
                "Protocol timestamp must be representable with a four-digit UTC year: "
                    + canonicalValue
            );
        }
    }

    /**
     * 从明确给出的时间点创建协议时间戳，并截断到毫秒精度。
     *
     * @param instant 明确的时间点
     * @return 协议时间戳
     */
    public static ProtocolTimestamp from(Instant instant) {
        return new ProtocolTimestamp(instant);
    }

    /**
     * 严格解析 UTC、三位毫秒和 {@code Z} 后缀的协议时间戳。
     *
     * @param value 协议时间戳文本
     * @return 协议时间戳
     */
    public static ProtocolTimestamp parse(String value) {
        Objects.requireNonNull(value, "value");
        if (!CANONICAL_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException(
                "Protocol timestamp must use YYYY-MM-DDTHH:mm:ss.SSSZ format"
            );
        }
        try {
            ProtocolTimestamp timestamp = new ProtocolTimestamp(Instant.parse(value));
            if (!timestamp.toString().equals(value)) {
                throw new IllegalArgumentException(
                    "Protocol timestamp must not require calendar normalization"
                );
            }
            return timestamp;
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("Protocol timestamp is not a valid instant", exception);
        }
    }

    /**
     * 返回毫秒精度的时间点。
     *
     * @return 内部时间点
     */
    public Instant instant() {
        return instant;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
            || other instanceof ProtocolTimestamp that && instant.equals(that.instant);
    }

    @Override
    public int hashCode() {
        return instant.hashCode();
    }

    @Override
    public String toString() {
        return FORMATTER.format(instant);
    }
}
