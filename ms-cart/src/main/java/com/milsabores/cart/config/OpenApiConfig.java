package com.milsabores.cart.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "ms-cart API",
                version = "v1",
                description = "Microservicio de carrito de compras para Pastelería Mil Sabores",
                contact = @io.swagger.v3.oas.annotations.info.Contact(
                        name = "Equipo Mil Sabores",
                        email = "soporte@milsabores.cl"
                )
        ),
        security = {
                @SecurityRequirement(name = "bearerAuth")
        }
)
@SecuritySchemes({
        @SecurityScheme(
                name = "bearerAuth",
                type = SecuritySchemeType.HTTP,
                scheme = "bearer",
                bearerFormat = "JWT",
                description = "JWT emitido por ms-usuarios (Authorization: Bearer {token})"
        )
})
public class OpenApiConfig {

    @Bean
    public OpenAPI cartOpenAPI() {
        return new OpenAPI()
                .info(new io.swagger.v3.oas.models.info.Info()
                        .title("ms-cart API")
                        .version("v1")
                        .description("Microservicio de carrito de compras para Pastelería Mil Sabores")
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
