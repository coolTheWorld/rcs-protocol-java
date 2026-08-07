package io.github.cooltheworld.rcs.protocol.vda5050.v3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import org.junit.jupiter.api.Test;

final class SchemaResourcesTest {
    private static final String RESOURCE_ROOT = "/vda5050/v3.0.0/";

    private static final Map<String, String> EXPECTED_SHA_256 = Map.ofEntries(
        Map.entry("connection.schema", "ee91d4233003ca854b7e200091fe0f3c48196b3702814a55863055489ab978d6"),
        Map.entry("factsheet.schema", "b8cda3b30016bf244a8b9856e3570bc41e232daceec8136149b15aabbcafd83f"),
        Map.entry("instantActions.schema", "216ac2a18c7bd8d3396e19c369418f99dfbc382be712e8be1162829f96b03518"),
        Map.entry("order.schema", "93288542b98aef22b2d9dbb68d618a43ed72860b29335fd18cec6577904dd9fd"),
        Map.entry("responses.schema", "30135290ef1794b8390310c0e3275f172d9bc939f4c8d4e977810b2951116dd2"),
        Map.entry("state.schema", "82c4c58495654c03f6723faec87310824f90e58d374a6899b8ed056604d5ed27"),
        Map.entry("visualization.schema", "ba34b38a676ded850586c11125aa700c96887984c670ead3f7f93d04ca24decf"),
        Map.entry("zoneSet.schema", "1251f685ff211c9fb0b91289a77912cfbd13158f9a1606ec47dbe082c0ec1ec4"),
        Map.entry("LICENSE.txt", "491a28425667fb2309173ef83bc1c39174398888fe6242828b127ed3f78325e3")
    );

    @Test
    void packagesUpstreamSchemasAndLicenseWithoutModification() throws IOException {
        for (Map.Entry<String, String> resource : EXPECTED_SHA_256.entrySet()) {
            assertEquals(resource.getValue(), sha256(resource.getKey()), resource.getKey());
        }
    }

    private static String sha256(String resourceName) throws IOException {
        try (InputStream resource = SchemaResourcesTest.class.getResourceAsStream(
            RESOURCE_ROOT + resourceName
        )) {
            assertNotNull(resource, () -> "Missing classpath resource: " + resourceName);
            return hex(MessageDigest.getInstance("SHA-256").digest(resource.readAllBytes()));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 must be available on JDK 21", exception);
        }
    }

    private static String hex(byte[] bytes) {
        return java.util.HexFormat.of().formatHex(bytes);
    }
}
