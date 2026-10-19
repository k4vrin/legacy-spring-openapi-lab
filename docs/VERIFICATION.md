# Verification

Verified locally on 19 October 2026 using Temurin JDK 21.0.12.1 and the pinned Maven 3.9.9 wrapper. Application compilation still uses `--release 8`; runtime execution on Java 8 itself has not been verified.

## Executed command

The local run used project-local Maven caches and a temporary socket directory. In PowerShell, with `JAVA_HOME` pointing at JDK 21:

```powershell
New-Item -ItemType Directory -Path target/socket-tmp -Force | Out-Null
$env:MAVEN_USER_HOME = "$PWD\target\maven-home"
.\mvnw.cmd '-Dmaven.repo.local=target/maven-repository' '-DargLine=-Djdk.net.unixdomain.tmpdir=D:/Projects/legacy-spring-openapi-lab/target/socket-tmp' verify
```

Use the socket directory's actual absolute path if the checkout is elsewhere. The normal command remains `./mvnw verify`. On this Windows machine, the JDK's internal NIO socket failed with `Unable to establish loopback connection` / `Invalid argument: connect` using its default temporary directory, including outside the restricted sandbox. The project-local socket option resolved that failure without changes to the POM, application, or Tomcat launcher. See the [OpenJDK discussion of the temporary socket directory option](https://mail.openjdk.org/pipermail/nio-dev/2023-March/013297.html).

The final run reported **BUILD SUCCESS: 10 cases, zero failures, errors, or skips**:

Verification was rerun after renaming the package prefix and Maven group to `dev.kavrin`, including the Java source directories, XML references, launcher class, Springdoc filter, and documentation examples. All 10 cases passed again.

| Suite | Cases | Evidence |
|---|---:|---|
| `DocumentationIntegrationTest` | 8 | Spring root/child contexts and MockMvc |
| `TomcatWarIT` | 2 | Actual packaged WAR on Tomcat 9.0.113 over loopback HTTP, flag false and true |

Surefire and Failsafe reports are generated under `target/surefire-reports/` and `target/failsafe-reports/`; the local build output is in `target/verify.log`.

## Registration and context checks

- Controller and existing advice definitions are scan-generated. Test-only ordinary `@Component` and `@Configuration` fixtures inside the scanned API package are excluded.
- `apiDocsConfiguration` has an XML definition originating in `orders-servlet.xml`, rather than a scanned definition. Its sibling `docs` package is outside the restricted scan hierarchy.
- Root owns one service and repository. Child has no local copies and resolves the same instances through parent lookup. Root lookups for child controller/advice types fail.
- Application bean names do not overlap across contexts. Spring's internal annotation processors are expected in both contexts and excluded from that comparison.
- Child has one request-mapping handler mapping and adapter; the adapter uses the named `mvcConversionService`.

An XML-specific distinction is asserted explicitly: **the XML-declared configuration object remains when the flag is false**. Its `@Conditional` prevents processing of imports and bean methods, so generator properties and viewer mappings are absent. The previous scan-based expectation that the configuration object itself disappears was updated; endpoint gating remains the same. No additional registrar or post-processor was introduced.

## Behavior checks

- The packaged false default leaves every documentation route unavailable. The initializer loads classpath defaults before configuration processing, and higher-priority system properties enable the feature. The WAR tests use actual JVM system properties for both states.
- When enabled, JSON and YAML specifications, viewer HTML/initializer, and local Swagger UI CSS/JavaScript return 200. When disabled, all those paths return 404.
- Both modes preserve order creation/retrieval, validation, malformed-input handling, and missing-order behavior. MockMvc also checks the existing JSON error codes `INVALID_ORDER`, `INVALID_REQUEST`, and `ORDER_NOT_FOUND`.
- Real WAR requests use `/legacy-orders/api`; the generated server includes the context and servlet prefixes without duplicating them in operation paths.
- Generating the specification leaves the order store empty and describes the three operations and inferred DTO validation metadata. The missing-order JSON 404 is still intentionally not inferred into the operation's documented responses.
- A late property edit does not create documentation contributions or make the endpoint available. A malformed flag fails startup.

## Artifact and documentation checks

WAR inspection confirmed that all 14 application class files have Java 8 bytecode version 52, the packaged default is false, and the restricted XML scan is included. Test fixtures and test/container JARs are excluded from the artifact.

Local Markdown links and code fences, deployment XML parsing, whitespace, the Persian draft's XML snippets against source, and first-person plural phrasing were checked. The draft now includes the caption and all nine carousel slides using repository package/class names.

## Limits and earlier evidence

This run checks HTTP delivery of Swagger UI resources and specification generation; it does not recheck browser rendering or Try it out interactively. The earlier 15 October 2026 verification recorded a browser check with all three operations and four schemas. That earlier UI evidence predates this registration change.

External Tomcat installation, reverse-proxy behavior, authentication, production deployment, and runtime execution on Java 8 remain unverified. Tomcat logged Java module-access warnings during shutdown; all recorded HTTP assertions and test cases passed. No database is involved. These results apply only to this independent sample.
