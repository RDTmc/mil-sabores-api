package com.milsabores.orders.security;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
@EnableWebSecurity
@OpenAPIDefinition(
        info = @Info(
                title = "ms-orders API",
                version = "v1",
                description = "Microservicio de órdenes de compra para Pastelería Mil Sabores"
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
public class SecurityConfig {

    /**
     * Misma clave secreta que usa ms-usuarios para firmar los JWT HS256.
     * Debe coincidir EXACTAMENTE con app.jwt.secret de ms-usuarios y ms-cart.
     */
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // API stateless → sin CSRF y sin sesión de servidor
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        // Permitimos OPTIONS para CORS preflight
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Swagger / OpenAPI sin autenticación
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/api-docs/**"
                        ).permitAll()

                        // Todos los endpoints de órdenes requieren JWT válido
                        .anyRequest().authenticated()
                )
                // Nada de login por formulario ni Basic
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(form -> form.disable())
                // Este MS actúa como Resource Server de JWT (Bearer tokens)
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.decoder(jwtDecoder()))
                );

        return http.build();
    }

    /**
     * Configuración CORS para permitir llamadas desde el frontend (React)
     * y otros clientes mientras estamos en desarrollo.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // En dev: aceptar cualquier origen (puedes restringir a http://localhost:5173 más adelante)
        config.setAllowedOriginPatterns(List.of("*"));

        // Métodos permitidos
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Headers permitidos (incluimos Authorization)
        config.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "X-Requested-With"
        ));

        // Para dev no necesitamos cookies
        config.setAllowCredentials(false);

        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Con context-path /api, esto cubre /api/** internamente
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * Decoder para validar JWT HS256 usando la misma secret key
     * que ms-usuarios y ms-cart.
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        SecretKey key = new SecretKeySpec(keyBytes, "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(key).build();
    }
}
