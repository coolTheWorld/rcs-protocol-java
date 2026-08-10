package io.github.cooltheworld.rcs.protocol.vda5050.v3.mobilerobot;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.internal.Unsigned32;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.Connection;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.ConnectionState;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.ProtocolVersionProfile;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.RobotIdentity;
import java.util.Objects;

/** Mobile Robot 按 Robot Identity 聚合的不可变协议会话状态。 */
public final class MobileRobotState {
    private final RobotIdentity robotIdentity;
    private final ProtocolVersionProfile versionProfile;
    private final boolean recovering;
    private final Long nextConnectionHeaderId;
    private final Connection lastConnection;
    private final Connection connectionLastWill;

    private MobileRobotState(Builder builder) {
        robotIdentity = Objects.requireNonNull(builder.robotIdentity, "robotIdentity");
        versionProfile = Objects.requireNonNull(builder.versionProfile, "versionProfile");
        recovering = builder.recovering;
        nextConnectionHeaderId = requireHeaderId(builder.nextConnectionHeaderId);
        lastConnection = builder.lastConnection;
        connectionLastWill = builder.connectionLastWill;
        validateConnection(lastConnection, false, "Last Connection");
        validateConnection(connectionLastWill, true, "Connection Last Will");
    }

    /** 创建缺少历史快照时的安全恢复状态。 */
    public static MobileRobotState recovering(
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
            .nextConnectionHeaderId(nextConnectionHeaderId)
            .lastConnection(lastConnection)
            .connectionLastWill(connectionLastWill);
    }

    public RobotIdentity robotIdentity() {
        return robotIdentity;
    }

    public ProtocolVersionProfile versionProfile() {
        return versionProfile;
    }

    public boolean isRecovering() {
        return recovering;
    }

    public Long nextConnectionHeaderId() {
        return nextConnectionHeaderId;
    }

    public Connection lastConnection() {
        return lastConnection;
    }

    public Connection connectionLastWill() {
        return connectionLastWill;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
            || other instanceof MobileRobotState that
                && recovering == that.recovering
                && robotIdentity.equals(that.robotIdentity)
                && versionProfile.version().equals(that.versionProfile.version())
                && nextConnectionHeaderId.equals(that.nextConnectionHeaderId)
                && Objects.equals(lastConnection, that.lastConnection)
                && Objects.equals(connectionLastWill, that.connectionLastWill);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            robotIdentity,
            versionProfile.version(),
            recovering,
            nextConnectionHeaderId,
            lastConnection,
            connectionLastWill
        );
    }

    private Long requireHeaderId(Long headerId) {
        if (!Unsigned32.isValid(headerId)) {
            throw new IllegalArgumentException(
                "Next Connection headerId is outside the uint32 range"
            );
        }
        return headerId;
    }

    private void validateConnection(
        Connection connection,
        boolean lastWill,
        String label
    ) {
        if (connection == null) {
            return;
        }
        if (!robotIdentity.equals(connection.header().robotIdentity())) {
            throw new IllegalArgumentException(label + " identity does not match state");
        }
        if (!versionProfile.version().equals(connection.header().version())) {
            throw new IllegalArgumentException(label + " version does not match state");
        }
        boolean connectionBroken = connection.connectionState()
            == ConnectionState.CONNECTION_BROKEN;
        if (connectionBroken != lastWill) {
            throw new IllegalArgumentException(label + " has an invalid connection state");
        }
    }

    /** 用于构造满足会话、计数器与 Connection 不变量的状态。 */
    public static final class Builder {
        private RobotIdentity robotIdentity;
        private ProtocolVersionProfile versionProfile;
        private boolean recovering = true;
        private Long nextConnectionHeaderId = Unsigned32.initial();
        private Connection lastConnection;
        private Connection connectionLastWill;

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

        public Builder nextConnectionHeaderId(Long nextConnectionHeaderId) {
            this.nextConnectionHeaderId = nextConnectionHeaderId;
            return this;
        }

        public Builder lastConnection(Connection lastConnection) {
            this.lastConnection = lastConnection;
            return this;
        }

        public Builder connectionLastWill(Connection connectionLastWill) {
            this.connectionLastWill = connectionLastWill;
            return this;
        }

        public MobileRobotState build() {
            return new MobileRobotState(this);
        }
    }
}
