package com.milsabores.api.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "ms-productos API",
                version = "v1",
                description = "Microservicio de catálogo de productos para Pastelería Mil Sabores",
                contact = @Contact(
                        name = "Equipo Mil Sabores",
                        email = "soporte@milsabores.cl"
                )
        ),
        security = {
                @SecurityRequirement(name = "bearerAuth")
        }
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "JWT emitido por ms-usuarios (Authorization: Bearer {token})"
)
public class OpenApiConfig {

        @Bean
        public OpenAPI productsOpenAPI() {
                return new OpenAPI()
                        .info(new io.swagger.v3.oas.models.info.Info()
                                .title("ms-productos API")
                                .version("v1")
                                .description("Catálogo de productos de Pastelería Mil Sabores")
                        )
                        .components(new Components()
                                .addSecuritySchemes(
                                        "bearerAuth",
                                        new io.swagger.v3.oas.models.security.SecurityScheme()
                                                .type(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                )
                        )
                        .addSecurityItem(
                                new io.swagger.v3.oas.models.security.SecurityRequirement()
                                        .addList("bearerAuth")
                        );
        }
}
