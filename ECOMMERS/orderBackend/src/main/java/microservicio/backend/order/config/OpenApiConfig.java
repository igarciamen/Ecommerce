// src/main/java/microservicio/backend/order/config/OpenApiConfig.java
package microservicio.backend.order.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Operation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;

// 👇 OJO al import (con Z)
import org.springdoc.core.customizers.OpenApiCustomizer;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        )
                )
                .info(new Info()
                        .title("Order Microservice API")
                        .version("1.0.0")
                        .description("Documentación Swagger para el microservicio de órdenes")
                );
    }

    /** Inyecta un ejemplo de body JSON para POST /api/orders en Swagger UI */
    @Bean
    public OpenApiCustomizer orderRequestExample() {
        return openApi -> {
            if (openApi.getPaths() == null) return;

            PathItem pathItem = openApi.getPaths().get("/api/orders");
            if (pathItem == null) return;

            Operation post = pathItem.getPost();
            if (post == null) return;

            RequestBody rb = post.getRequestBody();
            if (rb == null) {
                rb = new RequestBody();
                post.setRequestBody(rb);
            }

            Content content = rb.getContent();
            if (content == null) {
                content = new Content();
                rb.setContent(content);
            }

            io.swagger.v3.oas.models.media.MediaType mt = content.get(MediaType.APPLICATION_JSON_VALUE);
            if (mt == null) {
                mt = new io.swagger.v3.oas.models.media.MediaType();
                content.addMediaType(MediaType.APPLICATION_JSON_VALUE, mt);
            }

            String example = "{\n" +
                    "  \"userId\": 1,\n" +
                    "  \"items\": [\n" +
                    "    { \"productId\": 101, \"quantity\": 2 },\n" +
                    "    { \"productId\": 203, \"quantity\": 1 }\n" +
                    "  ]\n" +
                    "}";

            mt.example(example);
        };
    }
}