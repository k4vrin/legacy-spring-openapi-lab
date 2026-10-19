# Lesson 1: application context initializers

An `ApplicationContextInitializer` is a callback that prepares a configurable Spring context before it is refreshed. Spring Framework provides this mechanism independently of Spring Boot. [Spring 5.3 API](https://docs.spring.io/spring-framework/docs/5.3.39/javadoc-api/org/springframework/context/ApplicationContextInitializer.html)

In this example, preparation means adding a property source before Spring processes the explicitly XML-registered conditional configuration class.

## What refresh means here

A context object existing does not mean that its beans are ready. During refresh, Spring loads and processes definitions, evaluates configuration, runs factory/post-processing stages, and creates the relevant singleton beans. Our property source must be ready before the documentation class is considered for registration.

```mermaid
flowchart LR
    C[Context object created] --> I[Initializer prepares Environment]
    I --> R[Refresh processes XML and configuration]
    R --> B[Application beans become available]
```

This diagram is intentionally simplified. It is not a full description of every refresh phase, nor a promise that all beans, including lazy beans, are instantiated during startup.

## Follow the actual implementation

Open [ApiDocsPropertyInitializer.java](../../src/main/java/dev/kavrin/legacyorders/config/ApiDocsPropertyInitializer.java). Its work is:

```java
context.getEnvironment().getPropertySources().addLast(
        new ResourcePropertySource("apiDocsDefaults", "classpath:api-docs.properties"));
```

`ResourcePropertySource` reads the named classpath resource. `apiDocsDefaults` is the name of this source within the Environment, not the name of a bean or a property key. `addLast` makes it a fallback behind the sources already present.

The servlet declaration in [web.xml](../../src/main/webapp/WEB-INF/web.xml) registers the callback:

```xml
<init-param>
  <param-name>contextInitializerClasses</param-name>
  <param-value>dev.kavrin.legacyorders.config.ApiDocsPropertyInitializer</param-value>
</init-param>
```

This parameter belongs to the DispatcherServlet and applies to its child context. A similarly named context parameter can be used by `ContextLoaderListener` for the root context; that is a different registration location.

## Why not PostConstruct or a normal service?

`@PostConstruct` runs during initialization of an individual managed bean. It is too late to rely on that callback to supply a flag needed while configuration definitions are being processed. An initializer has an explicit earlier role and should not obtain application services whose lifecycle has not completed.

An initializer is also different from `WebApplicationInitializer`, which configures servlet-container integration programmatically. This sample uses `web.xml` for container wiring and an `ApplicationContextInitializer` to prepare the Spring child context.

## Experiment

With JDK 17 selected, run:

```sh
./mvnw verify
./mvnw -Ddemo.api-docs.enabled=true exec:java
```

Put breakpoints in `initialize` and `ApiDocsEnabledCondition.matches`. Observe that the initializer runs first for the servlet context. A condition breakpoint may be reached multiple times; do not interpret it as one invocation per HTTP request.

As a temporary local experiment, remove the initializer registration and start without an override. The condition still defaults to false, so a 404 alone would not prove the file was loaded. Inspect the Environment for `apiDocsDefaults`, or temporarily change the packaged default to true, rebuild, and compare. Restore the files afterward. The existing default-off HTTP test alone does not distinguish a loaded false from a missing value that falls back to false.

## Recall questions

1. What must already exist when an initializer runs, and what should you not assume is ready?
2. Why is `@PostConstruct` a poor place to supply this conditional flag?
3. Which context receives the initializer in this WAR?

### Check your reasoning

1. The configurable context and its Environment exist. Ordinary application services need not have been instantiated or initialized.
2. The flag is needed during definition processing, before an ordinary bean's initialization callback can be relied on.
3. The DispatcherServlet child context, because the initializer is registered as a servlet init parameter.

Continue with [Environment and property sources](02-environment.md).
