package com.milsabores.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.core.authority.AuthorityUtils;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final SupabaseJwtConfig supabase;

    public SecurityConfig(SupabaseJwtConfig supabase) {
        this.supabase = supabase;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {}) // usa tu configuración CORS existente
                .authorizeHttpRequests(auth -> auth
                        // público: catálogo + swagger + debug
                        .requestMatchers(HttpMethod.GET,
                                "/api/products/**",
                                "/api/categories/**",
                                "/api/featured/**",
                                "/api/_debug/**",
                                "/swagger-ui/**", "/v3/api-docs/**", "/api-docs/**").permitAll()
                        // protegido: crear orden requiere JWT
                        .requestMatchers(HttpMethod.POST, "/api/orders").authenticated()
                        // resto: permitir por ahora
                        .anyRequest().permitAll()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.decoder(jwtDecoder()).jwtAuthenticationConverter(jwtAuthenticationConverter()))
                );
        return http.build();
    }

    @Bean
    JwtDecoder jwtDecoder() {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(supabase.getJwks()).build();

        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(supabase.getIssuer());
        // si más adelante quieres validar "aud", puedes encadenar otro validador acá.
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(withIssuer));

        return decoder;
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        // Por ahora no derivamos roles del token; principal = "sub"
        JwtAuthenticationConverter conv = new JwtAuthenticationConverter();
        conv.setJwtGrantedAuthoritiesConverter(jwt -> AuthorityUtils.NO_AUTHORITIES);
        return conv;
    }
}
