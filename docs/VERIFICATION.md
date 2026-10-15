# Verification

Verified locally on 15 October 2026 using JDK 17.0.18 and the Maven 3.9.9 wrapper.

```sh
./mvnw verify
```

The build passed with eight tests: six Spring context/MockMvc cases and two packaged-WAR Tomcat cases. No failures, errors, or skipped tests were reported. Surefire/Failsafe reports are generated under `target/`.

The documented `exec:java` launcher was also exercised separately. Swagger UI rendered all three operations and four schemas in a browser, with the generated server including `/legacy-orders/api`. This browser check caught and led to a fix for the Maven plugin classloader used by the launcher; the full suite passed again after that fix.

WAR inspection confirmed the packaged disabled default, 32 runtime dependency JARs, no packaged test/container JARs, and no duplicate validation/JAXB API classes. Source text was checked for the enterprise identifiers intentionally excluded from this independent example. These checks are not a general secret-scanning or vulnerability audit.

## Scope

- Application source is compiled for Java 8 using JDK 17.
- Spring context/MockMvc tests exercise documentation gating, scope, schema inference, validation, and API behavior in both modes.
- A late property edit checks that startup conditions are not live feature switches.
- A malformed flag checks failure during startup.
- WAR tests start Tomcat 9 on ephemeral loopback ports and exercise actual HTTP requests, resource paths, generated server prefixes, order creation, and validation.

No enterprise source, deployment, API, or database is involved. These results establish behavior only for this independent sample.

## Limits

Execution on Java 8 itself, external Tomcat installation, reverse-proxy behavior, authentication, and production deployment have not been verified. Tomcat can log Java module-access warnings during local shutdown; the recorded HTTP and test results still passed.

The resolved dependency graph is reproducible with `./mvnw dependency:tree`. No before/after framework-version comparison against a separately committed baseline is claimed.

## Publication and documentation follow-up

The existing sample was committed and pushed to the public [legacy-spring-openapi-lab repository](https://github.com/k4vrin/legacy-spring-openapi-lab) on 15 October 2026 after all eight tests passed again. The architecture, lessons, and Persian post draft were added afterward as a separate documentation change. The initializer and condition remain separate; this follow-up changes no application source or runtime configuration.

Documentation checks validated local Markdown links, balanced fences, whitespace, and excluded enterprise identifiers across nine Markdown files. All seven Mermaid diagrams passed parsing with Mermaid 11.4.1. This is a syntax check; diagram layout was not visually inspected. Parser dependencies were installed temporarily outside the repository and are not project dependencies.
