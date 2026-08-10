package io.github.cooltheworld.rcs.protocol.vda5050.v3.architecture;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PublicApiArchitectureTest {
    private static final String ROOT = "io.github.cooltheworld.rcs.protocol.vda5050.v3";
    private static final String LEGACY_MODEL_PACKAGE = ROOT + ".model";
    private static final String JACKSON_CLASS_PREFIX = "com/fasterxml/jackson";

    private static final Map<String, List<String>> MODEL_PACKAGES = modelPackages();
    private static final Map<String, String> ROLE_TYPES = Map.of(
        "FleetControlEvent", ROOT + ".fleetcontrol.event",
        "FleetControlEffect", ROOT + ".fleetcontrol.effect",
        "MobileRobotEvent", ROOT + ".mobilerobot.event",
        "MobileRobotEffect", ROOT + ".mobilerobot.effect"
    );

    @Test
    void publicTypesUseThePackagesSpecifiedByTheContract() {
        List<String> violations = new ArrayList<>();

        MODEL_PACKAGES.forEach((packageName, typeNames) -> typeNames.forEach(typeName -> {
            requirePresent(packageName + "." + typeName, violations);
        }));
        ROLE_TYPES.forEach((typeName, packageName) -> {
            requirePresent(packageName + "." + typeName, violations);
        });

        assertEquals(List.of(), violations);
    }

    @Test
    void protocolModelsAndRoleContractsDoNotReferenceJackson() {
        List<String> violations = new ArrayList<>();
        MODEL_PACKAGES.forEach((packageName, typeNames) -> typeNames.forEach(typeName ->
            requireJacksonFree(packageName + "." + typeName, LEGACY_MODEL_PACKAGE + "." + typeName, violations)
        ));
        requireJacksonFree(ROOT + ".extension.ExtensionFields", null, violations);
        ROLE_TYPES.forEach((typeName, packageName) -> requireJacksonFree(
            packageName + "." + typeName,
            ROOT + "." + roleName(typeName) + "." + typeName,
            violations
        ));
        requireJacksonFree(ROOT + ".fleetcontrol.FleetControlState", null, violations);
        requireJacksonFree(ROOT + ".fleetcontrol.FleetControlStateMachine", null, violations);
        requireJacksonFree(ROOT + ".fleetcontrol.FleetControlTransition", null, violations);
        requireJacksonFree(ROOT + ".mobilerobot.MobileRobotState", null, violations);
        requireJacksonFree(ROOT + ".mobilerobot.MobileRobotStateMachine", null, violations);
        requireJacksonFree(ROOT + ".mobilerobot.MobileRobotTransition", null, violations);

        assertEquals(List.of(), violations);
    }

    private static Map<String, List<String>> modelPackages() {
        Map<String, List<String>> packages = new LinkedHashMap<>();
        packages.put(ROOT + ".model.common", List.of(
            "ProtocolHeader",
            "ProtocolTimestamp",
            "ProtocolVersion",
            "ProtocolVersionProfile",
            "RobotIdentity"
        ));
        packages.put(ROOT + ".model.action", List.of(
            "ActionParameterDefinition",
            "ActionScope",
            "ActionValueDataType",
            "BlockingType",
            "MobileRobotAction"
        ));
        packages.put(ROOT + ".model.connection", List.of(
            "Connection",
            "ConnectionState"
        ));
        packages.put(ROOT + ".model.factsheet", List.of(
            "Envelope2d",
            "Envelope2dVertex",
            "Envelope3d",
            "Envelope3dData",
            "LocalizationType",
            "MaximumArrayLengths",
            "MaximumStringLengths",
            "MobileRobotClass",
            "MobileRobotGeometry",
            "MobileRobotKinematics",
            "NavigationType",
            "OptionalParameter",
            "OptionalParameterSupport",
            "PhysicalParameters",
            "ProtocolFeatures",
            "ProtocolLimits",
            "ProtocolTiming",
            "TypeSpecification",
            "WheelDefinition",
            "WheelPosition",
            "WheelType",
            "ZoneType"
        ));
        return Map.copyOf(packages);
    }

    private static String roleName(String typeName) {
        return typeName.startsWith("FleetControl") ? "fleetcontrol" : "mobilerobot";
    }

    private static void requirePresent(String className, List<String> violations) {
        if (!classExists(className)) {
            violations.add("缺少目标公共类型 " + className);
        }
    }

    private static boolean classExists(String className) {
        try {
            Class.forName(className, false, PublicApiArchitectureTest.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    private static void requireJacksonFree(
        String targetClassName,
        String legacyClassName,
        List<String> violations
    ) {
        String className = classExists(targetClassName) ? targetClassName : legacyClassName;
        if (className == null || !classExists(className)) {
            violations.add("无法检查缺失类型 " + targetClassName);
            return;
        }
        try {
            requireJacksonFree(
                Class.forName(
                    className,
                    false,
                    PublicApiArchitectureTest.class.getClassLoader()
                ),
                violations
            );
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException("无法加载公共类型 " + className, exception);
        } catch (IOException exception) {
            throw new IllegalStateException("无法读取 class 文件 " + className, exception);
        }
    }

    private static void requireJacksonFree(
        Class<?> type,
        List<String> violations
    ) throws IOException {
        if (classFileText(type.getName()).contains(JACKSON_CLASS_PREFIX)) {
            violations.add("公共类型引用 Jackson " + type.getName());
        }
        for (Class<?> nestedType : type.getDeclaredClasses()) {
            requireJacksonFree(nestedType, violations);
        }
    }

    private static String classFileText(String className) throws IOException {
        String resourceName = "/" + className.replace('.', '/') + ".class";
        try (InputStream input = PublicApiArchitectureTest.class.getResourceAsStream(resourceName)) {
            if (input == null) {
                throw new IOException("缺少 classpath 资源 " + resourceName);
            }
            return new String(input.readAllBytes(), StandardCharsets.ISO_8859_1);
        }
    }
}
