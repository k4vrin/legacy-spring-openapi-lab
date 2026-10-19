package dev.kavrin.legacyorders.docs;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public final class ApiDocsEnabledCondition implements Condition {
    public static final String PROPERTY = "demo.api-docs.enabled";

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String value = context.getEnvironment().getProperty(PROPERTY, "false").trim();
        if (!"true".equalsIgnoreCase(value) && !"false".equalsIgnoreCase(value)) {
            throw new IllegalArgumentException(PROPERTY + " must be true or false");
        }
        return Boolean.parseBoolean(value);
    }
}
