package com.milsabores.usuarios.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Desactivamos CSRF para poder probar fácil con React / Postman
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Endpoints de auth públicos (registro/login)
                        .requestMatchers("/api/auth/**").permitAll()
                        // Cualquier otro endpoint en ms-usuarios requeriría auth
                        .anyRequest().authenticated()
                )
                // Por ahora sin formLogin ni httpBasic para no mezclar flujos
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(form -> form.disable());

        return http.build();
    }
}
