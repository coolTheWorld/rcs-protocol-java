package io.github.cooltheworld.rcs.protocol.vda5050.v3.fleetcontrol;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.fleetcontrol.effect.FleetControlEffect;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.fleetcontrol.event.FleetControlEvent;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.common.ProtocolHeader;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.common.ProtocolVersion;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.common.ProtocolVersionProfile;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.common.RobotIdentity;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.connection.Connection;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.connection.ConnectionState;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet.Factsheet;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.topic.DefaultTopicLayout;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.topic.TopicAddress;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.topic.TopicLayout;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.topic.TopicName;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.ConnectionValidator;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.FactsheetValidator;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.RejectedInboundMessage;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.ValidatedMessage;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.ValidationResult;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.function.Consumer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

final class FleetControlFactsheetStateMachineTest {
    private static final String FIXTURE =
        "vda5050/v3.0.0/fixtures/factsheet/factsheet-cases.json";
    private static final RobotIdentity ROBOT = new RobotIdentity("ACME", "R-1");
    private static final Instant OCCURRED_AT = Instant.parse(
        "2026-08-11T05:00:00.123Z"
    );
    private static final ObjectMapper TEST_MAPPER = JsonMapper.builder().build();

    private final FleetControlStateMachine stateMachine =
        FleetControlStateMachine.createDefault();

    @Test
    @DisplayName("[VDA3-FACTSHEET-005] 首次能力被保存，完整重复不重复发变化 Effect")
    void recordsFirstFactsheetWithoutRepeatingCapabilityChangeEffect()
        throws IOException {
        ValidatedMessage<Factsheet> message = validatedFactsheet(
            "/boundary",
            ROBOT,
            ignored -> {}
        );

        FleetControlTransition first = transition(recoveringState(), message);
        FleetControlTransition repeated = transition(first.state(), message);

        FleetControlEffect.FactsheetChanged changed = assertInstanceOf(
            FleetControlEffect.FactsheetChanged.class,
            first.effects().getFirst()
        );
        assertAll(
            () -> assertSame(message.message(), first.state().lastFactsheet()),
            () -> assertEquals(1, first.effects().size()),
            () -> assertNull(changed.previousFactsheet()),
            () -> assertSame(message.message(), changed.factsheet()),
            () -> assertEquals(OCCURRED_AT, changed.occurredAt()),
            () -> assertSame(message.message(), repeated.state().lastFactsheet()),
            () -> assertTrue(repeated.effects().isEmpty()),
            () -> assertTrue(repeated.issues().isEmpty())
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-005] 缺失网络的历史允许首次非空网络建立基线")
    void establishesTheFirstNonNullNetworkBaseline() throws IOException {
        FleetControlTransition withoutNetwork = transition(
            recoveringState(),
            validatedFactsheet("/valid", ROBOT, tree -> configuration(tree)
                .remove("network"))
        );
        ValidatedMessage<Factsheet> withNetwork = validatedFactsheet(
            "/valid",
            ROBOT,
            ignored -> {}
        );

        FleetControlTransition established = transition(
            withoutNetwork.state(),
            withNetwork
        );
        FleetControlTransition repeated = transition(
            established.state(),
            withNetwork
        );

        assertAll(
            () -> assertSame(
                withNetwork.message(),
                established.state().lastFactsheet()
            ),
            () -> assertNotNull(network(established.state().lastFactsheet())),
            () -> assertTrue(established.issues().isEmpty()),
            () -> assertEquals(
                1L,
                established.effects().stream()
                    .filter(FleetControlEffect.FactsheetChanged.class::isInstance)
                    .count()
            ),
            () -> assertEquals(
                0L,
                repeated.effects().stream()
                    .filter(FleetControlEffect.FactsheetChanged.class::isInstance)
                    .count()
            ),
            () -> assertTrue(repeated.issues().isEmpty())
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-005] 已冻结网络的缺失或变化执行第四层拒绝")
    void rejectsMissingOrChangedFrozenNetwork() throws IOException {
        FleetControlTransition established = transition(
            recoveringState(),
            validatedFactsheet("/valid", ROBOT, ignored -> {})
        );
        ValidatedMessage<Factsheet> missing = validatedFactsheet(
            "/valid",
            ROBOT,
            tree -> configuration(tree).remove("network")
        );
        ValidatedMessage<Factsheet> changed = validatedFactsheet(
            "/valid",
            ROBOT,
            tree -> networkNode(tree).put("localIpAddress", "10.0.0.99")
        );

        FleetControlTransition missingResult = transition(
            established.state(),
            missing
        );
        FleetControlTransition changedResult = transition(
            established.state(),
            changed
        );

        assertNetworkBaselineRejected(established.state(), missingResult);
        assertNetworkBaselineRejected(established.state(), changedResult);
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-005] Connection OFFLINE 不清除网络基线")
    void keepsNetworkBaselineAcrossOfflineConnection() throws IOException {
        FleetControlTransition established = transition(
            recoveringState(),
            validatedFactsheet("/valid", ROBOT, ignored -> {})
        );
        FleetControlTransition offline = stateMachine.transition(
            established.state(),
            new FleetControlEvent.ConnectionReceived(
                validatedConnection(ConnectionState.OFFLINE),
                OCCURRED_AT
            )
        );
        ValidatedMessage<Factsheet> missing = validatedFactsheet(
            "/valid",
            ROBOT,
            tree -> configuration(tree).remove("network")
        );

        FleetControlTransition rejected = transition(offline.state(), missing);

        assertAll(
            () -> assertSame(
                established.state().lastFactsheet(),
                offline.state().lastFactsheet()
            ),
            () -> assertNetworkBaselineRejected(offline.state(), rejected)
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-003/004] Factsheet 必须匹配会话身份与版本")
    void rejectsFactsheetOutsideTheSessionIdentityOrVersion() throws Exception {
        RobotIdentity other = new RobotIdentity("Other", "R-2");
        ValidatedMessage<Factsheet> otherRobot = validatedFactsheet(
            "/boundary",
            other,
            ignored -> {}
        );
        ValidatedMessage<Factsheet> otherVersion = forgeHeaderVersion(
            validatedFactsheet("/boundary", ROBOT, ignored -> {}),
            "3.1.0"
        );
        FleetControlState initial = recoveringState();

        FleetControlTransition identityResult = transition(
            initial,
            otherRobot
        );
        FleetControlTransition versionResult = transition(
            initial,
            otherVersion
        );

        assertAll(
            () -> assertEquals(
                "SESSION_ROBOT_IDENTITY_MISMATCH",
                identityResult.issues().getFirst().code()
            ),
            () -> assertEquals(
                "SESSION_PROTOCOL_VERSION_MISMATCH",
                versionResult.issues().getFirst().code()
            ),
            () -> assertSame(initial, identityResult.state()),
            () -> assertSame(initial, versionResult.state())
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-007] Factsheet 根级或子级扩展只产生脱敏观察 Effect")
    void reportsRootOrChildExtensionsWithoutExposingNamesOrValues()
        throws IOException {
        ValidatedMessage<Factsheet> rootExtension = validatedFactsheet(
            "/boundary",
            ROBOT,
            tree -> tree.putObject("vendorFactsheet").put("secret", "root")
        );
        ValidatedMessage<Factsheet> childExtension = validatedFactsheet(
            "/valid",
            ROBOT,
            tree -> tree.remove("vendorFactsheet")
        );

        FleetControlTransition rootResult = transition(
            recoveringState(),
            rootExtension
        );
        FleetControlTransition childResult = transition(
            recoveringState(),
            childExtension
        );

        assertUnknownExtension(rootResult, rootExtension.message());
        assertUnknownExtension(childResult, childExtension.message());
    }

    @Test
    @DisplayName("[VDA3-SHARED-007] Factsheet 深层集合扩展也被完整观察")
    void traversesEveryNestedFactsheetExtensionLocation() throws IOException {
        ValidatedMessage<Factsheet> message = validatedFactsheet(
            "/boundary",
            ROBOT,
            FleetControlFactsheetStateMachineTest::addNestedExtensions
        );

        FleetControlTransition transition = transition(
            recoveringState(),
            message
        );

        assertUnknownExtension(transition, message.message());
    }

    @Test
    @DisplayName("[VDA3-SHARED-007] 无扩展的嵌套配置不产生观察 Effect")
    void ignoresKnownNestedConfigurationWithoutExtensions() throws IOException {
        ValidatedMessage<Factsheet> message = validatedFactsheet(
            "/boundary",
            ROBOT,
            FleetControlFactsheetStateMachineTest::addKnownConfiguration
        );
        ValidatedMessage<Factsheet> withoutBattery = validatedFactsheet(
            "/boundary",
            ROBOT,
            tree -> {
                addKnownConfiguration(tree);
                configuration(tree).remove("batteryCharging");
            }
        );

        FleetControlTransition transition = transition(
            recoveringState(),
            message
        );
        FleetControlTransition missingBatteryTransition = transition(
            recoveringState(),
            withoutBattery
        );

        assertAll(
            () -> assertEquals(1, transition.effects().size()),
            () -> assertInstanceOf(
                FleetControlEffect.FactsheetChanged.class,
                transition.effects().getFirst()
            ),
            () -> assertEquals(1, missingBatteryTransition.effects().size()),
            () -> assertInstanceOf(
                FleetControlEffect.FactsheetChanged.class,
                missingBatteryTransition.effects().getFirst()
            )
        );
    }

    @Test
    @DisplayName("[VDA3-SHARED-008] Factsheet 前三层拒绝保持 State 并产生安全 Effect")
    void preservesStateForRejectedFactsheet() throws IOException {
        FleetControlState initial = recoveringState();
        RejectedInboundMessage<Factsheet> rejection = rejectedFactsheet();

        FleetControlTransition result = stateMachine.transition(
            initial,
            new FleetControlEvent.FactsheetRejected(rejection, OCCURRED_AT)
        );

        FleetControlEffect.InboundMessageRejected effect = assertInstanceOf(
            FleetControlEffect.InboundMessageRejected.class,
            result.effects().getFirst()
        );
        assertAll(
            () -> assertSame(initial, result.state()),
            () -> assertEquals(rejection.issues(), result.issues()),
            () -> assertEquals(TopicName.FACTSHEET, effect.topic()),
            () -> assertEquals(rejection.robotIdentity(), effect.robotIdentity()),
            () -> assertEquals(rejection.headerId(), effect.headerId()),
            () -> assertEquals(OCCURRED_AT, effect.occurredAt())
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-005] 相同 State 与 Factsheet Event 重放结果确定")
    void replaysFactsheetDeterministically() throws IOException {
        FleetControlState state = recoveringState();
        FleetControlEvent event = new FleetControlEvent.FactsheetReceived(
            validatedFactsheet("/valid", ROBOT, ignored -> {}),
            OCCURRED_AT
        );

        assertEquals(
            stateMachine.transition(state, event),
            stateMachine.transition(state, event)
        );
    }

    private FleetControlTransition transition(
        FleetControlState state,
        ValidatedMessage<Factsheet> message
    ) {
        return stateMachine.transition(
            state,
            new FleetControlEvent.FactsheetReceived(message, OCCURRED_AT)
        );
    }

    private static FleetControlState recoveringState() {
        return FleetControlState.recovering(
            ROBOT,
            ProtocolVersionProfile.V3_0_0
        );
    }

    private static void assertNetworkBaselineRejected(
        FleetControlState expectedState,
        FleetControlTransition transition
    ) {
        FleetControlEffect.InboundMessageRejected effect = assertInstanceOf(
            FleetControlEffect.InboundMessageRejected.class,
            transition.effects().getFirst()
        );
        assertAll(
            () -> assertSame(expectedState, transition.state()),
            () -> assertEquals(1, transition.issues().size()),
            () -> assertEquals(
                "FACTSHEET_NETWORK_BASELINE_CHANGED",
                transition.issues().getFirst().code()
            ),
            () -> assertEquals(
                "VDA3-FACTSHEET-005",
                transition.issues().getFirst().requirementId()
            ),
            () -> assertEquals(TopicName.FACTSHEET, effect.topic())
        );
    }

    private static void assertUnknownExtension(
        FleetControlTransition transition,
        Factsheet factsheet
    ) {
        List<FleetControlEffect.UnknownExtensionObserved> observations =
            transition.effects().stream()
                .filter(FleetControlEffect.UnknownExtensionObserved.class::isInstance)
                .map(FleetControlEffect.UnknownExtensionObserved.class::cast)
                .toList();
        assertAll(
            () -> assertEquals(1, observations.size()),
            () -> assertEquals(TopicName.FACTSHEET, observations.getFirst().topic()),
            () -> assertEquals(
                factsheet.header().robotIdentity(),
                observations.getFirst().robotIdentity()
            ),
            () -> assertEquals(
                factsheet.header().headerId(),
                observations.getFirst().headerId()
            ),
            () -> assertEquals(OCCURRED_AT, observations.getFirst().occurredAt())
        );
    }

    private static ObjectNode configuration(ObjectNode tree) {
        return (ObjectNode) tree.get("mobileRobotConfiguration");
    }

    private static ObjectNode networkNode(ObjectNode tree) {
        return (ObjectNode) configuration(tree).get("network");
    }

    private static Object network(Factsheet factsheet) {
        return factsheet.content().mobileRobotConfiguration().network();
    }

    private static void addKnownConfiguration(ObjectNode tree) {
        ObjectNode configuration = tree.putObject("mobileRobotConfiguration");
        configuration.putArray("versions")
            .addObject()
            .put("key", "softwareVersion")
            .put("value", "3.0.0");
        configuration.putObject("network");
        configuration.putObject("batteryCharging");
    }

    private static void addNestedExtensions(ObjectNode tree) {
        addProtocolFeatureExtensions((ObjectNode) tree.get("protocolFeatures"));
        addGeometryExtensions((ObjectNode) tree.get("mobileRobotGeometry"));
        addLoadExtensions((ObjectNode) tree.get("loadSpecification"));
        addConfigurationExtensions(tree.putObject("mobileRobotConfiguration"));
    }

    private static void addProtocolFeatureExtensions(ObjectNode features) {
        ArrayNode optionalParameters = features.putArray("optionalParameters");
        optionalParameters.addObject()
            .put("parameter", "order.nodes")
            .put("support", "SUPPORTED");
        optionalParameters.addObject()
            .put("parameter", "order.edges")
            .put("support", "REQUIRED")
            .put("vendorParameter", true);

        ArrayNode actions = features.putArray("mobileRobotActions");
        ObjectNode plainAction = action(actions, "plain");
        actionParameter(plainAction, "plain", false);
        ObjectNode extendedAction = action(actions, "extended");
        actionParameter(extendedAction, "plain", false);
        actionParameter(extendedAction, "extended", true);
        extendedAction.put("vendorAction", true);
    }

    private static ObjectNode action(ArrayNode actions, String actionType) {
        ObjectNode action = actions.addObject();
        action.put("actionType", actionType);
        action.putArray("actionScopes").add("NODE");
        action.put("pauseAllowed", true);
        action.put("cancelAllowed", true);
        action.putArray("actionParameters");
        return action;
    }

    private static void actionParameter(
        ObjectNode action,
        String key,
        boolean withExtension
    ) {
        ObjectNode parameter = ((ArrayNode) action.get("actionParameters"))
            .addObject();
        parameter.put("key", key);
        parameter.put("valueDataType", "STRING");
        if (withExtension) {
            parameter.put("vendorParameter", "opaque");
        }
    }

    private static void addGeometryExtensions(ObjectNode geometry) {
        ArrayNode wheels = geometry.putArray("wheelDefinitions");
        wheel(wheels, false);
        wheel(wheels, true);

        ArrayNode envelopes2d = geometry.putArray("envelopes2d");
        envelope2d(envelopes2d, "plain", false);
        envelope2d(envelopes2d, "extended", true);

        ArrayNode envelopes3d = geometry.putArray("envelopes3d");
        envelope3d(envelopes3d, "plain", false);
        envelope3d(envelopes3d, "extended", true);
    }

    private static void wheel(ArrayNode wheels, boolean withExtension) {
        ObjectNode wheel = wheels.addObject();
        wheel.put("type", "DRIVE");
        wheel.put("isActiveDriven", true);
        wheel.put("isActiveSteered", false);
        ObjectNode position = wheel.putObject("position");
        position.put("x", 0.0D);
        position.put("y", 0.0D);
        wheel.put("diameter", 0.3D);
        wheel.put("width", 0.1D);
        if (withExtension) {
            wheel.put("vendorWheel", true);
            position.put("vendorPosition", true);
        }
    }

    private static void envelope2d(
        ArrayNode envelopes,
        String id,
        boolean withExtension
    ) {
        ObjectNode envelope = envelopes.addObject();
        envelope.put("envelope2dId", id);
        ArrayNode vertices = envelope.putArray("vertices");
        vertex(vertices, 0.0D, 0.0D, false);
        vertex(vertices, 1.0D, 0.0D, withExtension);
        vertex(vertices, 0.0D, 1.0D, false);
        if (withExtension) {
            envelope.put("vendorEnvelope2d", true);
        }
    }

    private static void vertex(
        ArrayNode vertices,
        double x,
        double y,
        boolean withExtension
    ) {
        ObjectNode vertex = vertices.addObject();
        vertex.put("x", x);
        vertex.put("y", y);
        if (withExtension) {
            vertex.put("vendorVertex", true);
        }
    }

    private static void envelope3d(
        ArrayNode envelopes,
        String id,
        boolean withExtension
    ) {
        ObjectNode envelope = envelopes.addObject();
        envelope.put("envelope3dId", id);
        envelope.put("format", "gltf");
        envelope.putObject("data").putArray("mesh").add(1);
        if (withExtension) {
            envelope.put("vendorEnvelope3d", true);
        }
    }

    private static void addLoadExtensions(ObjectNode specification) {
        ArrayNode loadSets = specification.putArray("loadSets");
        loadSet(loadSets, "missing-geometry", false, false);
        loadSet(loadSets, "known-geometry", true, false);
        loadSet(loadSets, "extended-geometry", true, true);
    }

    private static void loadSet(
        ArrayNode loadSets,
        String name,
        boolean withGeometry,
        boolean withExtension
    ) {
        ObjectNode loadSet = loadSets.addObject();
        loadSet.put("setName", name);
        loadSet.put("loadType", "BOX");
        if (withGeometry) {
            ObjectNode reference = loadSet.putObject("boundingBoxReference");
            reference.put("x", 0.0D);
            reference.put("y", 0.0D);
            reference.put("z", 0.0D);
            ObjectNode dimensions = loadSet.putObject("loadDimensions");
            dimensions.put("length", 1.0D);
            dimensions.put("width", 1.0D);
            if (withExtension) {
                reference.put("vendorReference", true);
                dimensions.put("vendorDimensions", true);
            }
        }
        if (withExtension) {
            loadSet.put("vendorLoadSet", true);
        }
    }

    private static void addConfigurationExtensions(ObjectNode configuration) {
        ArrayNode versions = configuration.putArray("versions");
        versions.addObject()
            .put("key", "plain")
            .put("value", "1");
        versions.addObject()
            .put("key", "extended")
            .put("value", "1")
            .put("vendorVersion", true);
        configuration.putObject("network").put("vendorNetwork", true);
        configuration.putObject("batteryCharging")
            .put("vendorCharging", true);
        configuration.put("vendorConfiguration", true);
    }

    @SuppressWarnings("unchecked")
    private static ValidatedMessage<Factsheet> validatedFactsheet(
        String pointer,
        RobotIdentity robotIdentity,
        Consumer<ObjectNode> mutation
    ) throws IOException {
        ObjectNode tree = fixtureTree(pointer);
        tree.put("manufacturer", robotIdentity.manufacturer());
        tree.put("serialNumber", robotIdentity.serialNumber());
        mutation.accept(tree);
        String topic = TopicLayout.format(
            DefaultTopicLayout.standard(),
            new TopicAddress(robotIdentity, TopicName.FACTSHEET)
        );
        ValidationResult<Factsheet> result = FactsheetValidator.createDefault()
            .validate(
                DefaultTopicLayout.standard(),
                topic,
                TEST_MAPPER.writeValueAsBytes(tree)
            );
        return (ValidatedMessage<Factsheet>) assertInstanceOf(
            ValidatedMessage.class,
            result,
            () -> "Unexpected validation result: " + result.issues()
        );
    }

    @SuppressWarnings("unchecked")
    private static RejectedInboundMessage<Factsheet> rejectedFactsheet()
        throws IOException {
        ObjectNode tree = fixtureTree("/invalid/explicitNullConfiguration");
        tree.put("manufacturer", ROBOT.manufacturer());
        tree.put("serialNumber", ROBOT.serialNumber());
        ValidationResult<Factsheet> result = FactsheetValidator.createDefault()
            .validate(
                DefaultTopicLayout.standard(),
                "vda5050/v3/ACME/R-1/factsheet",
                TEST_MAPPER.writeValueAsBytes(tree)
            );
        return (RejectedInboundMessage<Factsheet>) assertInstanceOf(
            RejectedInboundMessage.class,
            result
        );
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static ValidatedMessage<Factsheet> forgeHeaderVersion(
        ValidatedMessage<Factsheet> original,
        String version
    ) throws ReflectiveOperationException {
        Factsheet message = original.message();
        ProtocolHeader header = ProtocolHeader.builder()
            .headerId(message.header().headerId())
            .timestamp(message.header().timestamp())
            .version(ProtocolVersion.parse(version))
            .robotIdentity(message.header().robotIdentity())
            .build();
        Factsheet forged = Factsheet.builder()
            .header(header)
            .content(message.content())
            .extensionFields(message.extensionFields())
            .build();
        Constructor<ValidatedMessage> constructor = ValidatedMessage.class
            .getDeclaredConstructor(Object.class, ProtocolVersionProfile.class, List.class);
        constructor.setAccessible(true);
        return (ValidatedMessage<Factsheet>) constructor.newInstance(
            forged,
            ProtocolVersionProfile.V3_0_0,
            List.of()
        );
    }

    @SuppressWarnings("unchecked")
    private static ValidatedMessage<Connection> validatedConnection(
        ConnectionState state
    ) {
        String payload = """
            {
              "headerId": 9,
              "timestamp": "2026-08-11T04:59:59.123Z",
              "version": "3.0.0",
              "manufacturer": "ACME",
              "serialNumber": "R-1",
              "connectionState": "%s"
            }
            """.formatted(state.name());
        ValidationResult<Connection> result = ConnectionValidator.createDefault()
            .validate(
                DefaultTopicLayout.standard(),
                "vda5050/v3/ACME/R-1/connection",
                payload.getBytes(StandardCharsets.UTF_8)
            );
        return (ValidatedMessage<Connection>) assertInstanceOf(
            ValidatedMessage.class,
            result
        );
    }

    private static ObjectNode fixtureTree(String pointer) throws IOException {
        try (InputStream input = FleetControlFactsheetStateMachineTest.class
            .getClassLoader()
            .getResourceAsStream(FIXTURE)) {
            if (input == null) {
                throw new IllegalArgumentException("Missing fixture: " + FIXTURE);
            }
            JsonNode fixture = TEST_MAPPER.readTree(input).at(pointer);
            if (fixture.isMissingNode()) {
                throw new IllegalArgumentException(
                    "Missing fixture case: " + pointer
                );
            }
            return (ObjectNode) fixture.deepCopy();
        }
    }
}
