package io.github.cooltheworld.rcs.protocol.vda5050.v3.fleetcontrol;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.fleetcontrol.effect.FleetControlEffect;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.fleetcontrol.event.FleetControlEvent;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.connection.Connection;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.connection.ConnectionState;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.common.ProtocolHeader;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.common.ProtocolTimestamp;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.common.ProtocolVersion;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.common.ProtocolVersionProfile;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.common.RobotIdentity;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.Factsheet;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.topic.DefaultTopicLayout;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.topic.TopicName;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.FactsheetValidator;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.RejectedInboundMessage;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.ValidatedMessage;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.ValidationIssue;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.ValidationResult;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.ValidationSeverity;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

final class FleetControlStateTest {
    private static final String FIXTURE =
        "vda5050/v3.0.0/fixtures/factsheet/factsheet-cases.json";
    private static final RobotIdentity ROBOT = new RobotIdentity("ACME", "R-1");
    private static final Instant OCCURRED_AT = Instant.parse(
        "2026-08-11T04:00:00.123Z"
    );
    private static final ObjectMapper TEST_MAPPER = JsonMapper.builder().build();

    @Test
    void enforcesConnectionSessionIdentityAndVersionInvariants() {
        Connection otherRobot = connection(
            new RobotIdentity("Other", "R-2"),
            ProtocolVersion.parse("3.0.0")
        );
        Connection unsupportedVersion = connection(
            ROBOT,
            ProtocolVersion.parse("3.1.0")
        );

        assertAll(
            () -> assertThrows(
                IllegalArgumentException.class,
                () -> stateBuilder().lastConnection(otherRobot).build()
            ),
            () -> assertThrows(
                IllegalArgumentException.class,
                () -> stateBuilder().lastConnection(unsupportedVersion).build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> FleetControlState.builder()
                    .versionProfile(ProtocolVersionProfile.V3_0_0)
                    .build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> FleetControlState.builder()
                    .robotIdentity(ROBOT)
                    .build()
            )
        );
    }

    @Test
    void startsWithoutFactsheetAndEnforcesItsIdentityAndVersion() throws Exception {
        FleetControlState initial = stateBuilder().build();
        Factsheet valid = validatedFactsheet().message();

        assertAll(
            () -> assertNull(initial.lastFactsheet()),
            () -> assertThrows(
                IllegalArgumentException.class,
                () -> stateBuilder().lastFactsheet(reheader(
                    valid,
                    new RobotIdentity("Other", "R-2"),
                    ProtocolVersion.parse("3.0.0")
                )).build()
            ),
            () -> assertThrows(
                IllegalArgumentException.class,
                () -> stateBuilder().lastFactsheet(reheader(
                    valid,
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
    void preservesFactsheetThroughBuilderAndCompleteValueSemantics()
        throws Exception {
        FleetControlState first = stateBuilder().build();
        FleetControlState same = first.toBuilder().build();
        FleetControlState ready = first.toBuilder().recovering(false).build();
        FleetControlState differentRobot = FleetControlState.builder()
            .robotIdentity(new RobotIdentity("Other", "R-2"))
            .versionProfile(ProtocolVersionProfile.V3_0_0)
            .build();
        FleetControlState differentVersion = FleetControlState.builder()
            .robotIdentity(ROBOT)
            .versionProfile(versionProfile("3.1.0"))
            .build();
        FleetControlState withConnection = first.toBuilder()
            .lastConnection(connection(
                ROBOT,
                ProtocolVersion.parse("3.0.0")
            ))
            .build();
        FleetControlState withFactsheet = first.toBuilder()
            .lastFactsheet(validatedFactsheet().message())
            .build();
        FleetControlState copiedFactsheet = withFactsheet.toBuilder().build();

        assertAll(
            () -> assertEquals(first, first),
            () -> assertEquals(first, same),
            () -> assertEquals(first.hashCode(), same.hashCode()),
            () -> assertNotEquals(first, ready),
            () -> assertNotEquals(first, differentRobot),
            () -> assertNotEquals(first, differentVersion),
            () -> assertNotEquals(first, withConnection),
            () -> assertNotEquals(first, withFactsheet),
            () -> assertEquals(withFactsheet, copiedFactsheet),
            () -> assertSame(
                withFactsheet.lastFactsheet(),
                copiedFactsheet.lastFactsheet()
            ),
            () -> assertNotEquals(first, null),
            () -> assertNotEquals(first, "state")
        );
    }

    @Test
    void distinguishesValidatedAndRejectedFactsheetEvents() throws Exception {
        ValidatedMessage<Factsheet> validated = validatedFactsheet();
        RejectedInboundMessage<Factsheet> rejected = rejectedFactsheet();

        FleetControlEvent.FactsheetReceived received =
            new FleetControlEvent.FactsheetReceived(validated, OCCURRED_AT);
        FleetControlEvent.FactsheetRejected rejection =
            new FleetControlEvent.FactsheetRejected(rejected, OCCURRED_AT);

        assertAll(
            () -> assertSame(validated, received.message()),
            () -> assertSame(rejected, rejection.rejection()),
            () -> assertEquals(OCCURRED_AT, received.occurredAt()),
            () -> assertEquals(OCCURRED_AT, rejection.occurredAt()),
            () -> assertEquals(
                List.of(ValidatedMessage.class, Instant.class),
                Arrays.stream(received.getClass().getRecordComponents())
                    .map(component -> component.getType())
                    .toList()
            ),
            () -> assertEquals(
                List.of(RejectedInboundMessage.class, Instant.class),
                Arrays.stream(rejection.getClass().getRecordComponents())
                    .map(component -> component.getType())
                    .toList()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> new FleetControlEvent.FactsheetReceived(null, OCCURRED_AT)
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> new FleetControlEvent.FactsheetReceived(validated, null)
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> new FleetControlEvent.FactsheetRejected(null, OCCURRED_AT)
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> new FleetControlEvent.FactsheetRejected(rejected, null)
            )
        );
    }

    @Test
    void exposesCapabilityChangeRejectionAndValueFreeExtensionObservation()
        throws Exception {
        Factsheet factsheet = validatedFactsheet().message();
        RejectedInboundMessage<Factsheet> rejected = rejectedFactsheet();
        List<ValidationIssue> mutableIssues = new ArrayList<>(rejected.issues());

        FleetControlEffect.FactsheetChanged changed =
            new FleetControlEffect.FactsheetChanged(null, factsheet, OCCURRED_AT);
        FleetControlEffect.InboundMessageRejected rejection =
            new FleetControlEffect.InboundMessageRejected(
                TopicName.FACTSHEET,
                ROBOT,
                factsheet.header().headerId(),
                mutableIssues,
                OCCURRED_AT
            );
        FleetControlEffect.UnknownExtensionObserved observed =
            new FleetControlEffect.UnknownExtensionObserved(
                TopicName.FACTSHEET,
                ROBOT,
                factsheet.header().headerId(),
                OCCURRED_AT
            );
        mutableIssues.clear();

        assertAll(
            () -> assertNull(changed.previousFactsheet()),
            () -> assertSame(factsheet, changed.factsheet()),
            () -> assertEquals(OCCURRED_AT, changed.occurredAt()),
            () -> assertEquals(TopicName.FACTSHEET, rejection.topic()),
            () -> assertEquals(rejected.issues(), rejection.issues()),
            () -> assertThrows(
                UnsupportedOperationException.class,
                () -> rejection.issues().clear()
            ),
            () -> assertEquals(TopicName.FACTSHEET, observed.topic()),
            () -> assertEquals(
                List.of("topic", "robotIdentity", "headerId", "occurredAt"),
                Arrays.stream(observed.getClass().getRecordComponents())
                    .map(component -> component.getName())
                    .toList()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> new FleetControlEffect.FactsheetChanged(
                    null,
                    null,
                    OCCURRED_AT
                )
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> new FleetControlEffect.FactsheetChanged(
                    null,
                    factsheet,
                    null
                )
            )
        );
    }

    private static FleetControlState.Builder stateBuilder() {
        return FleetControlState.builder()
            .robotIdentity(ROBOT)
            .versionProfile(ProtocolVersionProfile.V3_0_0);
    }

    private static ProtocolVersionProfile versionProfile(String version)
        throws Exception {
        var constructor = ProtocolVersionProfile.class.getDeclaredConstructor(
            ProtocolVersion.class
        );
        constructor.setAccessible(true);
        return constructor.newInstance(ProtocolVersion.parse(version));
    }

    private static Connection connection(
        RobotIdentity robotIdentity,
        ProtocolVersion version
    ) {
        ProtocolHeader header = ProtocolHeader.builder()
            .headerId(7L)
            .timestamp(ProtocolTimestamp.from(OCCURRED_AT))
            .version(version)
            .robotIdentity(robotIdentity)
            .build();
        return Connection.builder()
            .header(header)
            .connectionState(ConnectionState.ONLINE)
            .build();
    }

    private static Factsheet reheader(
        Factsheet source,
        RobotIdentity robotIdentity,
        ProtocolVersion version
    ) {
        ProtocolHeader header = ProtocolHeader.builder()
            .headerId(source.header().headerId())
            .timestamp(ProtocolTimestamp.from(OCCURRED_AT))
            .version(version)
            .robotIdentity(robotIdentity)
            .build();
        return Factsheet.builder()
            .header(header)
            .content(source.content())
            .extensionFields(source.extensionFields())
            .build();
    }

    @SuppressWarnings("unchecked")
    private static ValidatedMessage<Factsheet> validatedFactsheet()
        throws IOException {
        byte[] payload;
        try (InputStream input = FleetControlStateTest.class
            .getClassLoader()
            .getResourceAsStream(FIXTURE)) {
            if (input == null) {
                throw new IllegalArgumentException("Missing fixture: " + FIXTURE);
            }
            JsonNode fixture = TEST_MAPPER.readTree(input).at("/valid");
            payload = TEST_MAPPER.writeValueAsBytes(fixture);
        }
        ValidationResult<Factsheet> result = FactsheetValidator.createDefault()
            .validate(
                DefaultTopicLayout.standard(),
                "vda5050/v3/ACME/R-1/factsheet",
                payload
            );
        return (ValidatedMessage<Factsheet>) assertInstanceOf(
            ValidatedMessage.class,
            result
        );
    }

    private static RejectedInboundMessage<Factsheet> rejectedFactsheet() {
        ValidationIssue error = new ValidationIssue(
            "INVALID_FACTSHEET",
            ValidationSeverity.ERROR,
            "",
            "Factsheet 未通过前三层校验",
            "VDA3-FACTSHEET-001"
        );
        return RejectedInboundMessage.<Factsheet>builder(
            TopicName.FACTSHEET,
            List.of(error)
        ).robotIdentity(ROBOT).headerId(7L).build();
    }
}
