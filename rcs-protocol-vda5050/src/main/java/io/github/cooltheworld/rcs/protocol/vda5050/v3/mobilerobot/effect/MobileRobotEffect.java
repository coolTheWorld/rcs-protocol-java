package io.github.cooltheworld.rcs.protocol.vda5050.v3.mobilerobot.effect;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.connection.Connection;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.connection.ConnectionState;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.Factsheet;
import java.util.Objects;

/** Mobile Robot 状态机产生的封闭效果集合。 */
public sealed interface MobileRobotEffect
    permits MobileRobotEffect.ConfigureConnectionLastWill,
        MobileRobotEffect.PublishConnection,
        MobileRobotEffect.PublishFactsheet {
    /** 配置由 Broker 在意外断连后发布的 CONNECTION_BROKEN 消息。 */
    record ConfigureConnectionLastWill(Connection connection)
        implements MobileRobotEffect {
        public ConfigureConnectionLastWill {
            connection = Objects.requireNonNull(connection, "connection");
            if (connection.connectionState() != ConnectionState.CONNECTION_BROKEN) {
                throw new IllegalArgumentException(
                    "Connection Last Will must use CONNECTION_BROKEN"
                );
            }
        }
    }

    /** 发布由 Mobile Robot 主动产生的 Connection 消息。 */
    record PublishConnection(Connection connection)
        implements MobileRobotEffect {
        public PublishConnection {
            connection = Objects.requireNonNull(connection, "connection");
            if (connection.connectionState() == ConnectionState.CONNECTION_BROKEN) {
                throw new IllegalArgumentException(
                    "Mobile Robot must not actively publish CONNECTION_BROKEN"
                );
            }
        }
    }

    /** 发布由 Mobile Robot 状态机完整构造的强类型 Factsheet。 */
    record PublishFactsheet(Factsheet factsheet) implements MobileRobotEffect {
        public PublishFactsheet {
            factsheet = Objects.requireNonNull(factsheet, "factsheet");
        }
    }
}
