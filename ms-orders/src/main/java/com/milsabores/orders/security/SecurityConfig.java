package com.milsabores.orders.security;

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
                // Para este microservicio dejamos TODO abierto
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )
                // Desactivamos CSRF para poder probar fácil con POST/PUT/DELETE desde front
                .csrf(csrf -> csrf.disable())
                // Sin login por formulario ni HTTP Basic
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(form -> form.disable());

        return http.build();
    }
}
