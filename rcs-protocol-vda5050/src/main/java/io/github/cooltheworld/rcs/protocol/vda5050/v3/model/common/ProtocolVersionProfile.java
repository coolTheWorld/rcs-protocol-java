package io.github.cooltheworld.rcs.protocol.vda5050.v3.model.common;

import java.util.Objects;

/**
 * 当前库显式支持的 VDA 5050 v3 协议版本配置。
 *
 * <p>可表示的 {@link ProtocolVersion} 不一定存在对应配置；只有本类型暴露的配置才能用于
 * 正常状态转换和出站消息。</p>
 */
public final class ProtocolVersionProfile {
    /** VDA 5050 3.0.0 的唯一首版支持配置。 */
    public static final ProtocolVersionProfile V3_0_0 = new ProtocolVersionProfile(
        ProtocolVersion.parse("3.0.0")
    );

    private final ProtocolVersion version;

    private ProtocolVersionProfile(ProtocolVersion version) {
        this.version = version;
    }

    /**
     * 判断版本是否具有显式支持配置。
     *
     * @param version 待检查版本
     * @return 版本受支持时返回 {@code true}
     */
    public static boolean supports(ProtocolVersion version) {
        Objects.requireNonNull(version, "version");
        return V3_0_0.version.equals(version);
    }

    /**
     * 返回版本对应的显式支持配置。
     *
     * @param version 待解析版本
     * @return 对应的支持配置
     * @throws IllegalArgumentException 版本没有显式配置时
     */
    public static ProtocolVersionProfile requireSupported(ProtocolVersion version) {
        if (!supports(version)) {
            throw new IllegalArgumentException("Protocol version has no explicit support profile");
        }
        return V3_0_0;
    }

    /**
     * 返回该配置对应的协议版本。
     *
     * @return 协议版本
     */
    public ProtocolVersion version() {
        return version;
    }

    @Override
    public String toString() {
        return version.toString();
    }
}
