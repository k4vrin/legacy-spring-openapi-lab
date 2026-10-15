package dev.example.legacyorders;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.example.legacyorders.config.ApiDocsPropertyInitializer;
import dev.example.legacyorders.docs.ApiDocsEnabledCondition;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.core.env.MapPropertySource;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.support.XmlWebApplicationContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class DocumentationIntegrationTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void packagedDefaultDisablesBeansAndEveryDocumentationResource() throws Exception {
        try (Contexts contexts = new Contexts(null)) {
            assertFalse(contexts.child.containsBean("apiDocsConfiguration"));
            assertFalse(contexts.child.containsBean("springDocConfigProperties"));
            assertFalse(contexts.child.containsBean("apiDocsResourceMapping"));
            assertFalse(contexts.root.containsBean("orderController"));
            for (String path : new String[]{"/v3/api-docs", "/v3/api-docs.yaml", "/docs/index.html",
                    "/docs/initializer.js", "/docs/vendor/swagger-ui.css", "/docs/vendor/swagger-ui-bundle.js"}) {
                contexts.mvc.perform(get(path)).andExpect(status().isNotFound());
            }
            contexts.mvc.perform(get("/orders")).andExpect(status().isOk());
        }
    }

    @Test
    void enabledSpecInfersTypesWithoutCallingOrderCreation() throws Exception {
        try (Contexts contexts = new Contexts("true")) {
            assertTrue(contexts.child.containsBean("apiDocsConfiguration"));
            assertFalse(contexts.root.containsBean("apiDocsConfiguration"));
            String json = contexts.mvc.perform(get("/v3/api-docs"))
                    .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
            JsonNode spec = mapper.readTree(json);
            assertEquals("3.0.1", spec.path("openapi").asText());
            assertEquals(2, spec.path("paths").size());
            assertTrue(spec.at("/paths/~1orders/get").isObject());
            assertTrue(spec.at("/paths/~1orders/post").isObject());
            assertTrue(spec.at("/paths/~1orders~1{id}/get").isObject());
            assertTrue(spec.at("/components/schemas/CreateOrderRequest/required").toString().contains("lines"));
            assertEquals(1, spec.at("/components/schemas/CreateOrderRequest/properties/lines/minItems").asInt());
            assertTrue(spec.at("/components/schemas/OrderLineRequest/required").toString().contains("quantity"));
            assertTrue(spec.at("/components/schemas/OrderLineRequest/properties/quantity/minimum").isNumber());
            // Inference is deliberately not advertised as a complete error contract.
            assertTrue(spec.at("/paths/~1orders~1{id}/get/responses/404").isMissingNode());
            contexts.mvc.perform(get("/orders")).andExpect(content().string("[]"));
            contexts.mvc.perform(get("/v3/api-docs.yaml")).andExpect(status().isOk());
            for (String path : new String[]{"/docs/index.html", "/docs/initializer.js",
                    "/docs/vendor/swagger-ui.css", "/docs/vendor/swagger-ui-bundle.js"}) {
                contexts.mvc.perform(get(path)).andExpect(status().isOk());
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void apiBehaviorIsTheSameInBothModes(String enabled) throws Exception {
        try (Contexts contexts = new Contexts(enabled)) {
            contexts.mvc.perform(post("/orders").contentType("application/json")
                    .content("{\"lines\":[{\"productCode\":\"BOOK-01\",\"quantity\":2}]}"))
                    .andExpect(status().isCreated()).andExpect(header().string("Location", "http://localhost/orders/1"));
            JsonNode order = mapper.readTree(contexts.mvc.perform(get("/orders/1"))
                    .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
            assertEquals(2, order.at("/lines/0/quantity").asInt());
            contexts.mvc.perform(get("/orders/99")).andExpect(status().isNotFound());
            contexts.mvc.perform(post("/orders").contentType("application/json").content("{\"lines\":[]}"))
                    .andExpect(status().isBadRequest());
            contexts.mvc.perform(post("/orders").contentType("application/json")
                    .content("{\"lines\":[{\"productCode\":\" \",\"quantity\":0}]}"))
                    .andExpect(status().isBadRequest());
            contexts.mvc.perform(post("/orders").contentType("application/json").content("{\"lines\":[null]}"))
                    .andExpect(status().isBadRequest());
            contexts.mvc.perform(post("/orders").contentType("application/json").content("{"))
                    .andExpect(status().isBadRequest());
            contexts.mvc.perform(get("/orders/not-a-number")).andExpect(status().isBadRequest());
        }
    }

    @Test
    void changingAPropertyAfterStartupDoesNotCreateDocumentationBeans() throws Exception {
        try (Contexts contexts = new Contexts("false")) {
            contexts.child.getEnvironment().getPropertySources().addFirst(new MapPropertySource(
                    "laterEdit", Collections.singletonMap(ApiDocsEnabledCondition.PROPERTY, "true")));
            contexts.mvc.perform(get("/v3/api-docs")).andExpect(status().isNotFound());
            assertFalse(contexts.child.containsBean("apiDocsConfiguration"));
        }
    }

    @Test
    void malformedFlagFailsStartupInsteadOfSilentlyDisablingDocs() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> new Contexts("yes"));
        Throwable cause = exception;
        while (cause.getCause() != null) { cause = cause.getCause(); }
        assertTrue(cause.getMessage().contains("must be true or false"), exception.toString() + " root: " + cause);
    }

    private static final class Contexts implements AutoCloseable {
        private final XmlWebApplicationContext root = new XmlWebApplicationContext();
        private final XmlWebApplicationContext child = new XmlWebApplicationContext();
        private MockMvc mvc;

        private Contexts(String override) {
            MockServletContext servlet = new MockServletContext("src/main/webapp",
                    new org.springframework.core.io.FileSystemResourceLoader());
            root.setServletContext(servlet);
            root.setConfigLocation("/WEB-INF/root-context.xml");
            try {
                root.refresh();
                child.setParent(root);
                child.setServletContext(servlet);
                child.setConfigLocation("/WEB-INF/orders-servlet.xml");
                if (override != null) {
                    child.getEnvironment().getPropertySources().replace("systemProperties", new MapPropertySource(
                            "systemProperties", Collections.singletonMap(ApiDocsEnabledCondition.PROPERTY, override)));
                }
                new ApiDocsPropertyInitializer().initialize(child);
                child.refresh();
                mvc = MockMvcBuilders.webAppContextSetup(child).build();
            } catch (RuntimeException exception) {
                try { child.close(); } catch (RuntimeException cleanup) { exception.addSuppressed(cleanup); }
                root.close();
                throw exception;
            }
        }

        @Override
        public void close() {
            if (child.isActive()) { child.close(); }
            if (root.isActive()) { root.close(); }
        }
    }
}
