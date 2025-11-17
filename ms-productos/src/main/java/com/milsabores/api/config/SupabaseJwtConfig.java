package com.milsabores.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "supabase")
public class SupabaseJwtConfig {
    /**
     * URL del JWKS de tu proyecto Supabase, ej:
     * https://yjjegavoizzmqmpamdyi.supabase.co/auth/v1/keys
     */
    private String jwks;

    /**
     * Issuer esperado del JWT, ej:
     * https://yjjegavoizzmqmpamdyi.supabase.co/auth/v1
     */
    private String issuer;

    public String getJwks() { return jwks; }
    public void setJwks(String jwks) { this.jwks = jwks; }

    public String getIssuer() { return issuer; }
    public void setIssuer(String issuer) { this.issuer = issuer; }
}
