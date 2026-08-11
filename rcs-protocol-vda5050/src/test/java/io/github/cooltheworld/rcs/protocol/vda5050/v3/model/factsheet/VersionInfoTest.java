package io.github.cooltheworld.rcs.protocol.vda5050.v3.model.factsheet;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.ExtensionFields;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.extension.internal.ExtensionFieldsJacksonSupport;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

final class VersionInfoTest {
    @Test
    @DisplayName("[VDA3-FACTSHEET-001] 版本信息保留必填原始键值")
    void buildsRequiredRawVersionInformation() {
        VersionInfo info = VersionInfo.builder()
            .key(" softwareVersion ")
            .value("v1.03.2-beta")
            .build();
        VersionInfo equalInfo = VersionInfo.builder()
            .key(" softwareVersion ")
            .value("v1.03.2-beta")
            .extensionFields(ExtensionFields.empty())
            .build();

        assertAll(
            () -> assertEquals(" softwareVersion ", info.key()),
            () -> assertEquals("v1.03.2-beta", info.value()),
            () -> assertTrue(info.extensionFields().isEmpty()),
            () -> assertEquals(info, info),
            () -> assertEquals(info, equalInfo),
            () -> assertNotEquals(info, null),
            () -> assertNotEquals(info, "version"),
            () -> assertNotEquals(
                info,
                VersionInfo.builder()
                    .key("cameraVersion")
                    .value("v1.03.2-beta")
                    .build()
            ),
            () -> assertNotEquals(
                info,
                VersionInfo.builder()
                    .key(" softwareVersion ")
                    .value("v2")
                    .build()
            ),
            () -> assertEquals(info.hashCode(), equalInfo.hashCode())
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] 版本信息拒绝缺失键或值")
    void rejectsMissingRequiredVersionInformation() {
        assertAll(
            () -> assertThrows(
                NullPointerException.class,
                () -> VersionInfo.builder().value("v1").build()
            ),
            () -> assertThrows(
                NullPointerException.class,
                () -> VersionInfo.builder().key("softwareVersion").build()
            )
        );
    }

    @Test
    @DisplayName("[VDA3-FACTSHEET-001] 版本扩展参与值语义")
    void includesExtensionsInVersionValueSemantics() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ExtensionFields extensions = ExtensionFieldsJacksonSupport.capture(
            mapper,
            mapper.createObjectNode().put("vendorVersion", "1"),
            Set.of()
        );
        VersionInfo plain = VersionInfo.builder()
            .key("softwareVersion")
            .value("v1")
            .build();
        VersionInfo extended = VersionInfo.builder()
            .key("softwareVersion")
            .value("v1")
            .extensionFields(extensions)
            .build();

        assertNotEquals(plain, extended);
    }
}
