package dev.kavrin.legacyorders.api;

import org.springframework.stereotype.Component;

/** Scan guard: ordinary components in the API package must not become MVC beans. */
@Component
public class UnintendedApiComponent {
}
