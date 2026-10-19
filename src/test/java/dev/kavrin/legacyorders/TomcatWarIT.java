package dev.kavrin.legacyorders;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kavrin.legacyorders.docs.ApiDocsEnabledCondition;
import dev.kavrin.legacyorders.support.LocalTomcat;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import org.apache.catalina.startup.Tomcat;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class TomcatWarIT {
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void packagedWarServesOrdersAndGatesDocsAtTheRealServletPrefix(boolean enabled) throws Exception {
        String original = System.getProperty(ApiDocsEnabledCondition.PROPERTY);
        Tomcat tomcat = null;
        try {
            System.setProperty(ApiDocsEnabledCondition.PROPERTY, Boolean.toString(enabled));
            tomcat = LocalTomcat.start(0);
            String base = "http://127.0.0.1:" + tomcat.getConnector().getLocalPort() + "/legacy-orders/api";
            assertEquals(200, request(base + "/orders", null).status);
            for (String path : new String[]{"/v3/api-docs", "/v3/api-docs.yaml", "/docs/index.html",
                    "/docs/initializer.js", "/docs/vendor/swagger-ui.css", "/docs/vendor/swagger-ui-bundle.js"}) {
                assertEquals(enabled ? 200 : 404, request(base + path, null).status, path);
            }
            if (enabled) {
                JsonNode spec = new ObjectMapper().readTree(request(base + "/v3/api-docs", null).body);
                assertTrue(spec.at("/servers/0/url").asText().endsWith("/legacy-orders/api"));
                assertTrue(spec.path("paths").has("/orders"));
            }
            Response created = request(base + "/orders", "{\"lines\":[{\"productCode\":\"PEN-01\",\"quantity\":3}]}");
            assertEquals(201, created.status);
            assertTrue(created.location.endsWith("/legacy-orders/api/orders/1"));
            assertEquals(200, request(created.location, null).status);
            assertEquals(400, request(base + "/orders", "{\"lines\":[]}").status);
            assertEquals(404, request(base + "/orders/999", null).status);
        } finally {
            if (tomcat != null) { tomcat.stop(); tomcat.destroy(); }
            if (original == null) { System.clearProperty(ApiDocsEnabledCondition.PROPERTY); }
            else { System.setProperty(ApiDocsEnabledCondition.PROPERTY, original); }
        }
    }

    private Response request(String address, String body) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(address).openConnection();
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(15000);
        try {
            if (body != null) {
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json");
                try (java.io.OutputStream output = connection.getOutputStream()) {
                    output.write(body.getBytes(StandardCharsets.UTF_8));
                }
            }
            int status = connection.getResponseCode();
            InputStream stream = status >= 400 ? connection.getErrorStream() : connection.getInputStream();
            String text = "";
            if (stream != null) {
                try (Scanner scanner = new Scanner(stream, "UTF-8").useDelimiter("\\A")) {
                    text = scanner.hasNext() ? scanner.next() : "";
                }
            }
            return new Response(status, text, connection.getHeaderField("Location"));
        } finally { connection.disconnect(); }
    }

    private static final class Response {
        private final int status;
        private final String body;
        private final String location;
        private Response(int status, String body, String location) {
            this.status = status; this.body = body; this.location = location;
        }
    }
}
