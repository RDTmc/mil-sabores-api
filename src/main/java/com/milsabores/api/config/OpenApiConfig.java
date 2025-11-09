package com.milsabores.api.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Mil Sabores API",
                version = "1.0.0",
                description = "API pública de catálogo (CLP, solo lectura). Endpoints alineados al frontend React.",
                contact = @Contact(name = "Mil Sabores")
        ),
        servers = {
                @Server(url = "http://localhost:9090/", description = "Local Dev")
        },
        tags = {
                @Tag(name = "Products", description = "Listado y detalle de productos"),
                @Tag(name = "Categories", description = "Listado de categorías"),
                @Tag(name = "Featured", description = "Productos destacados ordenados por posición")
        }
)
public class OpenApiConfig {
    // Configuración mínima: springdoc detecta automáticamente los endpoints.
}
