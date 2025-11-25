package com.milsabores.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // 🔹 origen de tu frontend (Vite)
        config.setAllowedOrigins(List.of(
                "http://localhost:5173"
        ));

        // 🔹 métodos permitidos
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // 🔹 cabeceras permitidas
        config.setAllowedHeaders(List.of("*"));

        // 🔹 si algún día envías cookies/autorización
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // aplica a todos los endpoints /api/**
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
