package dev.kavrin.legacyorders.api;

import org.springframework.context.annotation.Configuration;

/** Scan guard: @Configuration alone must not bypass the controller filters. */
@Configuration(proxyBeanMethods = false)
public class UnintendedApiConfiguration {
}
