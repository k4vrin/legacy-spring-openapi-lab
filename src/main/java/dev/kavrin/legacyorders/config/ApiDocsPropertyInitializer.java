package dev.kavrin.legacyorders.config;

import java.io.IOException;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.support.ResourcePropertySource;

public final class ApiDocsPropertyInitializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(ConfigurableApplicationContext context) {
        try {
            // Initializers run before scanning/condition evaluation. addLast preserves JVM overrides.
            context.getEnvironment().getPropertySources().addLast(
                    new ResourcePropertySource("apiDocsDefaults", "classpath:api-docs.properties"));
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot load packaged API documentation defaults", exception);
        }
    }
}
