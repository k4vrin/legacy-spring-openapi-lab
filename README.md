# Legacy Spring OpenAPI Lab

A fictional orders API showing how to add OpenAPI to an XML-configured Spring MVC WAR without changing to Spring Boot application startup.

The application uses `web.xml`, a root service context, a DispatcherServlet child context, and an optional documentation configuration. All code and example data are original. There is no database or connection to an enterprise system.

The integration demonstrates keeping controller discovery narrow and adding OpenAPI through an explicit XML bean declaration, without expanding the component scan to unrelated configuration or infrastructure.

## Build and run

Use JDK 17 for the documented build. Application classes are compiled with `--release 8`; running the project on Java 8 itself has not been verified.

```sh
./mvnw verify
./mvnw -Ddemo.api-docs.enabled=true exec:java
```

The second command runs the packaged `target/legacy-orders.war` in a local Tomcat 9 instance, bound to `127.0.0.1:8080`. Run `verify` first to create the WAR and test launcher. Stop it with Ctrl+C. The launcher uses test dependencies; they are not packaged in the WAR.

If port 8080 is occupied, add `-Ddemo.http.port=8081`. Build dependencies and wrapper downloads require network access on first use.

- [Swagger UI](http://localhost:8080/legacy-orders/api/docs/index.html)
- [OpenAPI JSON](http://localhost:8080/legacy-orders/api/v3/api-docs)
- [OpenAPI YAML](http://localhost:8080/legacy-orders/api/v3/api-docs.yaml)
- [Orders](http://localhost:8080/legacy-orders/api/orders)

```sh
curl -i http://localhost:8080/legacy-orders/api/orders \
  -H 'Content-Type: application/json' \
  -d '{"lines":[{"productCode":"BOOK-01","quantity":2}]}'

curl http://localhost:8080/legacy-orders/api/orders/1
```

Records are held in memory and disappear on restart. This is a local teaching application without authentication.

## Learn the startup model

The [architecture guide](docs/ARCHITECTURE.md) includes Mermaid flowcharts for context ownership, startup, HTTP requests, and the flag lifecycle. It records why this sample keeps property loading in the initializer and registration decisions in the condition.

Follow the [three educational lessons](docs/README.md) on initializers, the Environment, and conditions. Each includes a source walkthrough, an experiment, and recall questions. A [Persian LinkedIn draft](docs/linkedin-post.fa.md) explains the same choices through the sample.

## Documentation switch

`src/main/resources/api-docs.properties` contains:

```properties
demo.api-docs.enabled=false
```

Plain Spring does not automatically load this file. The servlet's `ApiDocsPropertyInitializer` loads it before XML scanning and condition evaluation. It adds the packaged defaults after the standard property sources, allowing the JVM system property to override them.

`ApiDocsEnabledCondition` accepts `true` or `false`, ignoring case and surrounding whitespace. Missing values mean disabled; malformed values fail startup. The condition controls processing of `ApiDocsConfiguration` and registration of its springdoc imports and viewer resource beans. The XML-declared configuration object itself remains in both states; with false, it contributes no documentation beans.

To run with documentation disabled:

```sh
./mvnw -Ddemo.api-docs.enabled=false exec:java
```

The API keeps working; documentation JSON, YAML, HTML, and assets return 404. The flag is evaluated at context startup. Changing a file, environment value, or database record after startup would not automatically create or remove these beans.

In a deployment to an external Tomcat, put `-Ddemo.api-docs.enabled=true` in the **Tomcat JVM options** and deploy `target/legacy-orders.war`. A `-D` option passed only to Maven during packaging is not baked into the WAR. The packaged default remains false. To use a different context path, deploy under the desired WAR name; the specification server URL is derived from the incoming context and servlet prefix.

## Architecture and dependencies

```text
Tomcat reads web.xml
  ContextLoaderListener → root-context.xml → order service + repository
  DispatcherServlet /api/*
    initializer loads documentation defaults
    orders-servlet.xml → controllers + one XML MVC registration
    conditional documentation configuration → springdoc + local Swagger UI
```

The root scan covers only the order service package. The child scans only `dev.kavrin.legacyorders.api`, with default filters disabled and explicit `Controller` and `ControllerAdvice` filters. These include the composed `@RestController` and existing `@RestControllerAdvice` annotations, preserving JSON errors. Ordinary components and configuration classes in that package are excluded.

`ApiDocsConfiguration` lives in the sibling `docs` package, outside the scan hierarchy, and is registered explicitly in `orders-servlet.xml`:

```xml
<context:component-scan base-package="dev.kavrin.legacyorders.api" use-default-filters="false">
  <context:include-filter type="annotation" expression="org.springframework.stereotype.Controller"/>
  <context:include-filter type="annotation" expression="org.springframework.web.bind.annotation.ControllerAdvice"/>
</context:component-scan>
<bean id="apiDocsConfiguration" class="dev.kavrin.legacyorders.docs.ApiDocsConfiguration"/>
```

`@Configuration` marks a class for configuration processing; it does not make an undiscovered class register itself. Explicit XML registration lets us add documentation without broadening which components are discovered. The scan also enables annotation processing, so the XML-declared configuration's `@Conditional`, `@Import`, and `@Bean` annotations are processed. With the flag false, configuration processing skips its imports and bean methods; it does not undo the XML declaration of the configuration object itself.

`web.xml` declares the listener, servlet, initializer, and `/api/*` routing. `orders-servlet.xml` defines beans inside that servlet's child context. Servlet mapping routes HTTP requests; component scanning discovers beans; springdoc's `packagesToScan` and `pathsToMatch` filter the generated specification, without registering controllers or changing request routing. The child resolves services from the root; the root cannot resolve beans defined only in the child.

XML MVC and springdoc share the named `mvcConversionService`. The optional configuration explicitly registers resource handlers because XML MVC does not invoke `WebMvcConfigurer` callbacks.

This deliberately legacy baseline pins Spring 5.3.39, springdoc 1.8.0, Jackson 2.17.3, and Swagger UI 5.17.14. The local test container is Tomcat 9.0.113. These choices illustrate compatibility work; they are not a recommendation for a new application stack.

Springdoc uses Boot 2.7.18 support libraries. The application has no Boot starter, Boot application entry point, Boot MVC auto-configuration, or Boot packaging plugin. Saying it has no Boot libraries would be inaccurate.

```sh
./mvnw dependency:tree
unzip -l target/legacy-orders.war
```

The properties file is packaged at `WEB-INF/classes/api-docs.properties`; local viewer assets are alongside it. Documentation dependency jars remain in the WAR even when the feature is disabled. Disabling registration does not shrink the artifact or provide authorization.

## Contract limits and checks

The three operations are `POST /orders`, `GET /orders`, and `GET /orders/{id}` relative to the servlet. Controllers use no OpenAPI-specific annotations. Springdoc infers schemas from mappings, types, Jackson metadata, and supported validation annotations.

An intentional example of the limit: retrieving a missing order returns a JSON 404 error, but with generic advice-response injection disabled, that 404 is not automatically part of the generated operation. An integration test checks both facts. The generated description needs review before it becomes a client contract.

`verify` runs Spring Test/MockMvc checks plus Failsafe tests deploying the actual WAR in Tomcat on ephemeral loopback ports. See [verification notes](docs/VERIFICATION.md) for the recorded results and limits.

## Sources

- [Springdoc v1 non-Boot integration FAQ](https://springdoc.org/v1/faq.html)
- [Spring MVC context hierarchy](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-servlet/context-hierarchy.html)
- [Spring property sources](https://docs.spring.io/spring-framework/reference/core/beans/environment.html)
- [Maven WAR resources](https://maven.apache.org/plugins/maven-war-plugin/examples/adding-filtering-webresources.html)
