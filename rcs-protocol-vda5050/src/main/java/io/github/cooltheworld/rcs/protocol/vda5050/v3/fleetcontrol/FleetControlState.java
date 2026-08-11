package io.github.cooltheworld.rcs.protocol.vda5050.v3.fleetcontrol;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.connection.Connection;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.connection.ConnectionState;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.common.ProtocolVersionProfile;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.common.RobotIdentity;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.Factsheet;
import java.util.Objects;

/** Fleet Control 按 Robot Identity 聚合的不可变协议会话状态。 */
public final class FleetControlState {
    private final RobotIdentity robotIdentity;
    private final ProtocolVersionProfile versionProfile;
    private final boolean recovering;
    private final Connection lastConnection;
    private final Factsheet lastFactsheet;

    private FleetControlState(Builder builder) {
        robotIdentity = Objects.requireNonNull(
            builder.robotIdentity,
            "robotIdentity"
        );
        versionProfile = Objects.requireNonNull(
            builder.versionProfile,
            "versionProfile"
        );
        recovering = builder.recovering;
        lastConnection = builder.lastConnection;
        lastFactsheet = builder.lastFactsheet;
        validateLastConnection();
        validateLastFactsheet();
    }

    /**
     * 创建缺少历史快照时的安全恢复状态。
     *
     * @param robotIdentity 会话聚合身份
     * @param versionProfile 显式协议版本配置
     * @return 尚未假定任何连接状态的恢复状态
     */
    public static FleetControlState recovering(
        RobotIdentity robotIdentity,
        ProtocolVersionProfile versionProfile
    ) {
        return builder()
            .robotIdentity(robotIdentity)
            .versionProfile(versionProfile)
            .recovering(true)
            .build();
    }

    /** @return 用于构造或恢复强类型状态的 Builder */
    public static Builder builder() {
        return new Builder();
    }

    /** @return 保留当前字段的 Builder */
    public Builder toBuilder() {
        return new Builder()
            .robotIdentity(robotIdentity)
            .versionProfile(versionProfile)
            .recovering(recovering)
            .lastConnection(lastConnection)
            .lastFactsheet(lastFactsheet);
    }

    /** @return 会话聚合身份 */
    public RobotIdentity robotIdentity() {
        return robotIdentity;
    }

    /** @return 会话使用的显式协议版本配置 */
    public ProtocolVersionProfile versionProfile() {
        return versionProfile;
    }

    /** @return 会话仍需恢复其他可观察事实时返回 {@code true} */
    public boolean isRecovering() {
        return recovering;
    }

    /** @return 最近一条已验证 Connection；尚未收到时为 {@code null} */
    public Connection lastConnection() {
        return lastConnection;
    }

    /** @return 最近一条已验证 Factsheet；尚未收到时为 {@code null} */
    public Factsheet lastFactsheet() {
        return lastFactsheet;
    }

    /** @return 最近观察到的连接状态；尚未收到 Connection 时为 {@code null} */
    public ConnectionState connectionState() {
        return lastConnection == null ? null : lastConnection.connectionState();
    }

    @Override
    public boolean equals(Object other) {
        return this == other
            || other instanceof FleetControlState that
                && recovering == that.recovering
                && robotIdentity.equals(that.robotIdentity)
                && versionProfile.version().equals(that.versionProfile.version())
                && Objects.equals(lastConnection, that.lastConnection)
                && Objects.equals(lastFactsheet, that.lastFactsheet);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            robotIdentity,
            versionProfile.version(),
            recovering,
            lastConnection,
            lastFactsheet
        );
    }

    private void validateLastConnection() {
        if (lastConnection == null) {
            return;
        }
        if (!robotIdentity.equals(lastConnection.header().robotIdentity())) {
            throw new IllegalArgumentException(
                "Last Connection identity does not match the Fleet Control state"
            );
        }
        if (!versionProfile.version().equals(lastConnection.header().version())) {
            throw new IllegalArgumentException(
                "Last Connection version does not match the Fleet Control state"
            );
        }
    }

    private void validateLastFactsheet() {
        if (lastFactsheet == null) {
            return;
        }
        if (!robotIdentity.equals(lastFactsheet.header().robotIdentity())) {
            throw new IllegalArgumentException(
                "Last Factsheet identity does not match the Fleet Control state"
            );
        }
        if (!versionProfile.version().equals(lastFactsheet.header().version())) {
            throw new IllegalArgumentException(
                "Last Factsheet version does not match the Fleet Control state"
            );
        }
    }

    /** 用于构造满足会话身份和版本不变量的状态。 */
    public static final class Builder {
        private RobotIdentity robotIdentity;
        private ProtocolVersionProfile versionProfile;
        private boolean recovering = true;
        private Connection lastConnection;
        private Factsheet lastFactsheet;

        private Builder() {}

        public Builder robotIdentity(RobotIdentity robotIdentity) {
            this.robotIdentity = robotIdentity;
            return this;
        }

        public Builder versionProfile(ProtocolVersionProfile versionProfile) {
            this.versionProfile = versionProfile;
            return this;
        }

        public Builder recovering(boolean recovering) {
            this.recovering = recovering;
            return this;
        }

        public Builder lastConnection(Connection lastConnection) {
            this.lastConnection = lastConnection;
            return this;
        }

        public Builder lastFactsheet(Factsheet lastFactsheet) {
            this.lastFactsheet = lastFactsheet;
            return this;
        }

        public FleetControlState build() {
            return new FleetControlState(this);
        }
    }
}
