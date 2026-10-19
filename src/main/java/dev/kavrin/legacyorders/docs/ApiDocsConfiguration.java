package dev.kavrin.legacyorders.docs;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springdoc.core.SpringDocConfigProperties;
import org.springdoc.core.SpringDocConfiguration;
import org.springdoc.webmvc.core.SpringDocWebMvcConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

@Configuration(proxyBeanMethods = false)
@Conditional(ApiDocsEnabledCondition.class)
@Import({SpringDocConfiguration.class, SpringDocWebMvcConfiguration.class})
public class ApiDocsConfiguration {
    @Bean
    public SpringDocConfigProperties springDocConfigProperties() {
        SpringDocConfigProperties properties = new SpringDocConfigProperties();
        properties.setPackagesToScan(Arrays.asList("dev.kavrin.legacyorders.api"));
        properties.setPathsToMatch(Arrays.asList("/orders", "/orders/**"));
        properties.setOverrideWithGenericResponse(false);
        return properties;
    }

    @Bean
    public OpenAPI orderOpenApi() {
        // With no hardcoded server, springdoc derives the context and servlet prefix from the request.
        return new OpenAPI().info(new Info().title("Legacy Orders API").version("1.0"));
    }

    @Bean
    public ResourceHttpRequestHandler apiDocsViewer() {
        ResourceHttpRequestHandler handler = new ResourceHttpRequestHandler();
        handler.setLocations(Arrays.asList(new ClassPathResource("api-docs-ui/")));
        return handler;
    }

    @Bean
    public ResourceHttpRequestHandler apiDocsVendor() {
        ResourceHttpRequestHandler handler = new ResourceHttpRequestHandler();
        handler.setLocations(Arrays.asList(new ClassPathResource("META-INF/resources/webjars/swagger-ui/5.17.14/")));
        return handler;
    }

    @Bean
    public SimpleUrlHandlerMapping apiDocsResourceMapping(
            ResourceHttpRequestHandler apiDocsViewer, ResourceHttpRequestHandler apiDocsVendor) {
        // XML MVC does not consume WebMvcConfigurer callbacks. Register these handlers explicitly.
        Map<String, Object> mappings = new LinkedHashMap<>();
        mappings.put("/docs/vendor/**", apiDocsVendor);
        mappings.put("/docs/**", apiDocsViewer);
        SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        mapping.setOrder(Integer.MAX_VALUE - 1);
        mapping.setUrlMap(mappings);
        return mapping;
    }
}
