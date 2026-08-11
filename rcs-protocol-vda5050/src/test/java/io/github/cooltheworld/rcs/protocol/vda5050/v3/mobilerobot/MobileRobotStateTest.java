package io.github.cooltheworld.rcs.protocol.vda5050.v3.mobilerobot;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.cooltheworld.rcs.protocol.vda5050.v3.mobilerobot.effect.MobileRobotEffect;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.mobilerobot.event.MobileRobotEvent;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.connection.Connection;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.connection.ConnectionState;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.common.ProtocolHeader;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.common.ProtocolTimestamp;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.common.ProtocolVersion;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.common.ProtocolVersionProfile;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.common.RobotIdentity;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.Factsheet;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.FactsheetContent;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.LoadSpecification;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.LocalizationType;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.MaximumArrayLengths;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.MaximumStringLengths;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.MobileRobotClass;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.MobileRobotGeometry;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.MobileRobotKinematics;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.NavigationType;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.PhysicalParameters;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.ProtocolFeatures;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.ProtocolLimits;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.ProtocolTiming;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.TypeSpecification;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

final class MobileRobotStateTest {
    private static final RobotIdentity ROBOT = new RobotIdentity("Acme", "R-001");

    @Test
    void enforcesRequiredFieldsAndTheConnectionCounterRange() {
        assertAll(
            () -> assertThrows(
                NullPointerException.class,
                () -> MobileRobotState.builder()
                    .versionProfile(ProtocolVersionProfile.V3_0_0)
                    .build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> MobileRobotState.builder().robotIdentity(ROBOT).build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> stateBuilder().nextConnectionHeaderId(null).build()
            ),
            () -> assertThrows(
                IllegalArgumentException.class,
                () -> stateBuilder().nextConnectionHeaderId(-1L).build()
            ),
            () -> assertThrows(
                IllegalArgumentException.class,
                () -> stateBuilder().nextConnectionHeaderId(4_294_967_296L).build()
            )
        );
    }

    @Test
    void startsWithAnIndependentFactsheetCounterAndNoLastFactsheet() {
        MobileRobotState state = stateBuilder().build();

        assertAll(
            () -> assertEquals(0L, state.nextFactsheetHeaderId()),
            () -> assertNull(state.lastFactsheet())
        );
    }

    @Test
    void enforcesTheFactsheetCounterIdentityAndVersion() {
        Factsheet valid = factsheet(
            ROBOT,
            ProtocolVersion.parse("3.0.0")
        );

        assertAll(
            () -> assertThrows(
                NullPointerException.class,
                () -> stateBuilder().nextFactsheetHeaderId(null).build()
            ),
            () -> assertThrows(
                IllegalArgumentException.class,
                () -> stateBuilder().nextFactsheetHeaderId(-1L).build()
            ),
            () -> assertThrows(
                IllegalArgumentException.class,
                () -> stateBuilder()
                    .nextFactsheetHeaderId(4_294_967_296L)
                    .build()
            ),
            () -> assertThrows(
                IllegalArgumentException.class,
                () -> stateBuilder().lastFactsheet(factsheet(
                    new RobotIdentity("Other", "R-002"),
                    ProtocolVersion.parse("3.0.0")
                )).build()
            ),
            () -> assertThrows(
                IllegalArgumentException.class,
                () -> stateBuilder().lastFactsheet(factsheet(
                    ROBOT,
                    ProtocolVersion.parse("3.1.0")
                )).build()
            ),
            () -> assertSame(
                valid,
                stateBuilder().lastFactsheet(valid).build().lastFactsheet()
            )
        );
    }

    @Test
    void exposesHeaderFreeFactsheetEventAndStronglyTypedPublishEffect() {
        FactsheetContent content = factsheetContent();
        Instant occurredAt = Instant.parse("2026-08-11T03:00:00.123Z");
        Factsheet factsheet = factsheet(
            ROBOT,
            ProtocolVersion.parse("3.0.0")
        );

        MobileRobotEvent.FactsheetPublicationRequested event =
            new MobileRobotEvent.FactsheetPublicationRequested(content, occurredAt);
        MobileRobotEffect.PublishFactsheet effect =
            new MobileRobotEffect.PublishFactsheet(factsheet);

        assertAll(
            () -> assertSame(content, event.content()),
            () -> assertEquals(occurredAt, event.occurredAt()),
            () -> assertEquals(
                List.of(FactsheetContent.class, Instant.class),
                Arrays.stream(event.getClass().getRecordComponents())
                    .map(component -> component.getType())
                    .toList()
            ),
            () -> assertSame(factsheet, effect.factsheet()),
            () -> assertEquals(
                List.of(Factsheet.class),
                Arrays.stream(effect.getClass().getRecordComponents())
                    .map(component -> component.getType())
                    .toList()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> new MobileRobotEvent.FactsheetPublicationRequested(
                    null,
                    occurredAt
                )
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> new MobileRobotEvent.FactsheetPublicationRequested(
                    content,
                    null
                )
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> new MobileRobotEffect.PublishFactsheet(null)
            )
        );
    }

    @Test
    void enforcesConnectionIdentityVersionAndLastWillState() {
        RobotIdentity otherRobot = new RobotIdentity("Other", "R-002");
        Connection validOnline = connection(
            ROBOT,
            ProtocolVersion.parse("3.0.0"),
            ConnectionState.ONLINE
        );
        Connection validBroken = connection(
            ROBOT,
            ProtocolVersion.parse("3.0.0"),
            ConnectionState.CONNECTION_BROKEN
        );

        assertAll(
            () -> assertThrows(
                IllegalArgumentException.class,
                () -> stateBuilder().lastConnection(connection(
                    otherRobot,
                    ProtocolVersion.parse("3.0.0"),
                    ConnectionState.ONLINE
                )).build()
            ),
            () -> assertThrows(
                IllegalArgumentException.class,
                () -> stateBuilder().lastConnection(connection(
                    ROBOT,
                    ProtocolVersion.parse("3.1.0"),
                    ConnectionState.ONLINE
                )).build()
            ),
            () -> assertThrows(
                IllegalArgumentException.class,
                () -> stateBuilder().lastConnection(validBroken).build()
            ),
            () -> assertThrows(
                IllegalArgumentException.class,
                () -> stateBuilder().connectionLastWill(validOnline).build()
            ),
            () -> assertThrows(
                IllegalArgumentException.class,
                () -> stateBuilder().connectionLastWill(connection(
                    otherRobot,
                    ProtocolVersion.parse("3.0.0"),
                    ConnectionState.CONNECTION_BROKEN
                )).build()
            ),
            () -> assertThrows(
                IllegalArgumentException.class,
                () -> stateBuilder().connectionLastWill(connection(
                    ROBOT,
                    ProtocolVersion.parse("3.1.0"),
                    ConnectionState.CONNECTION_BROKEN
                )).build()
            ),
            () -> assertEquals(
                validBroken,
                stateBuilder().connectionLastWill(validBroken).build()
                    .connectionLastWill()
            )
        );
    }

    @Test
    void comparesStatesByTheirCompleteStronglyTypedContent() throws Exception {
        MobileRobotState first = stateBuilder().build();
        MobileRobotState same = first.toBuilder().build();
        MobileRobotState ready = first.toBuilder()
            .recovering(false)
            .build();
        MobileRobotState advanced = first.toBuilder()
            .nextConnectionHeaderId(1L)
            .build();
        MobileRobotState differentRobot = MobileRobotState.builder()
            .robotIdentity(new RobotIdentity("Other", "R-002"))
            .versionProfile(ProtocolVersionProfile.V3_0_0)
            .build();
        MobileRobotState differentVersion = MobileRobotState.builder()
            .robotIdentity(ROBOT)
            .versionProfile(versionProfile("3.1.0"))
            .build();
        MobileRobotState withConnection = first.toBuilder()
            .lastConnection(connection(
                ROBOT,
                ProtocolVersion.parse("3.0.0"),
                ConnectionState.ONLINE
            ))
            .build();
        MobileRobotState withLastWill = first.toBuilder()
            .connectionLastWill(connection(
                ROBOT,
                ProtocolVersion.parse("3.0.0"),
                ConnectionState.CONNECTION_BROKEN
            ))
            .build();
        MobileRobotState withAdvancedFactsheetCounter = first.toBuilder()
            .nextFactsheetHeaderId(1L)
            .build();
        MobileRobotState withFactsheet = first.toBuilder()
            .lastFactsheet(factsheet(
                ROBOT,
                ProtocolVersion.parse("3.0.0")
            ))
            .build();

        assertAll(
            () -> assertEquals(first, first),
            () -> assertEquals(first, same),
            () -> assertEquals(first.hashCode(), same.hashCode()),
            () -> assertNotEquals(first, ready),
            () -> assertNotEquals(first, differentRobot),
            () -> assertNotEquals(first, differentVersion),
            () -> assertNotEquals(first, advanced),
            () -> assertNotEquals(first, withConnection),
            () -> assertNotEquals(first, withLastWill),
            () -> assertNotEquals(first, withAdvancedFactsheetCounter),
            () -> assertNotEquals(first, withFactsheet),
            () -> assertNotEquals(first, null),
            () -> assertNotEquals(first, "state")
        );
    }

    private static ProtocolVersionProfile versionProfile(String version)
        throws Exception {
        var constructor = ProtocolVersionProfile.class.getDeclaredConstructor(
            ProtocolVersion.class
        );
        constructor.setAccessible(true);
        return constructor.newInstance(ProtocolVersion.parse(version));
    }

    private static MobileRobotState.Builder stateBuilder() {
        return MobileRobotState.builder()
            .robotIdentity(ROBOT)
            .versionProfile(ProtocolVersionProfile.V3_0_0);
    }

    private static Connection connection(
        RobotIdentity robotIdentity,
        ProtocolVersion version,
        ConnectionState connectionState
    ) {
        ProtocolHeader header = ProtocolHeader.builder()
            .headerId(7L)
            .timestamp(ProtocolTimestamp.from(
                Instant.parse("2026-08-10T06:00:00.123Z")
            ))
            .version(version)
            .robotIdentity(robotIdentity)
            .build();
        return Connection.builder()
            .header(header)
            .connectionState(connectionState)
            .build();
    }

    private static Factsheet factsheet(
        RobotIdentity robotIdentity,
        ProtocolVersion version
    ) {
        ProtocolHeader header = ProtocolHeader.builder()
            .headerId(9L)
            .timestamp(ProtocolTimestamp.from(
                Instant.parse("2026-08-11T03:00:00.123Z")
            ))
            .version(version)
            .robotIdentity(robotIdentity)
            .build();
        return Factsheet.builder()
            .header(header)
            .content(factsheetContent())
            .build();
    }

    private static FactsheetContent factsheetContent() {
        return FactsheetContent.builder()
            .typeSpecification(TypeSpecification.builder()
                .seriesName("SERIES")
                .mobileRobotKinematics(MobileRobotKinematics.DIFFERENTIAL)
                .mobileRobotClass(MobileRobotClass.CARRIER)
                .maximumLoadMass(100.0D)
                .localizationTypes(List.of(LocalizationType.NATURAL))
                .navigationTypes(List.of(NavigationType.FREELY_NAVIGATING))
                .build())
            .physicalParameters(PhysicalParameters.builder()
                .minimumSpeed(0.1D)
                .maximumSpeed(2.0D)
                .maximumAcceleration(1.0D)
                .maximumDeceleration(1.0D)
                .minimumHeight(0.2D)
                .maximumHeight(1.0D)
                .width(0.8D)
                .length(1.2D)
                .build())
            .protocolLimits(ProtocolLimits.builder()
                .maximumStringLengths(MaximumStringLengths.builder().build())
                .maximumArrayLengths(MaximumArrayLengths.builder().build())
                .timing(ProtocolTiming.builder()
                    .minimumOrderInterval(0.0D)
                    .minimumStateInterval(0.0D)
                    .build())
                .build())
            .protocolFeatures(ProtocolFeatures.builder()
                .optionalParameters(List.of())
                .mobileRobotActions(List.of())
                .build())
            .mobileRobotGeometry(MobileRobotGeometry.builder().build())
            .loadSpecification(LoadSpecification.builder().build())
            .build();
    }
}
