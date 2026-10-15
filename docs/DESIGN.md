# Integration decisions

See [application architecture](ARCHITECTURE.md) for Mermaid diagrams and the recorded decision to retain a separate initializer. The [learning documents](README.md) explain the lifecycle in more detail.

## Preserve the established MVC configuration

XML remains the owner of MVC. The documentation configuration does not use `@EnableWebMvc`. Both the MVC infrastructure and springdoc can use the explicitly named conversion service.

The root context scans only services and storage. The child scans controllers and the conditional documentation class. This prevents accidental documentation registration in the root context and keeps servlet infrastructure with the handlers it describes.

## Load the flag before registration

A context initializer loads defaults into the servlet Environment before XML scanning. A custom Spring Framework condition can then evaluate the flag without querying a database or looking up application beans during definition processing.

Placing the only property loader inside the conditional configuration would make registration depend on settings that have not been loaded yet. A placeholder configurer intended for later bean-property substitution is also not a substitute for making settings available to an early condition.

A system with an established settings table could supply an equivalent value before this point. This project intentionally substitutes a packaged resource and JVM override; it does not reproduce a particular settings schema or its loader.

## Gate resources with the generator

The same conditional configuration owns specification-generation imports and viewer resource mappings. Turning off the feature removes both. UI execution settings alone would leave the specification and API available.

Resource handlers are explicit beans because the application uses XML MVC, which does not consume Java MVC configuration callbacks. Their mapping order places them after annotated controllers.

## Let the request establish the local base URL

The UI fetches `../v3/api-docs`. Springdoc derives the server URL from the incoming request, including context and servlet prefixes. A deployed WAR test checks `/legacy-orders/api` so we do not accidentally duplicate `/api` in operation paths.

Reverse-proxy deployments and forwarded-header trust require deployment-specific configuration. The local test makes no claim about a production gateway.

## Keep generated metadata separate from verified behavior

Nested validation is executed by MVC, not by OpenAPI. The test suite checks actual invalid requests as well as generated schema metadata. The 404 error example demonstrates a behavior that is not automatically complete in the generated description.

The sample uses original order terminology and synthetic data. It contains no enterprise identifier mapping, internal source, addresses, credentials, deployment details, or copied dependency inventory.
