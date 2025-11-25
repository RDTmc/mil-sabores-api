package com.milsabores.usuarios.security;

import com.milsabores.usuarios.model.UsuarioEntity;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    @Value("${app.jwt.issuer}")
    private String issuer;

    private Key getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(UsuarioEntity user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        // Formato latinoamericano: dd-MM-yyyy
        DateTimeFormatter birthDateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        var builder = Jwts.builder()
                .setSubject(user.getId())
                .setIssuer(issuer)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .claim("email", user.getEmail())
                .claim("role", user.getRole());

        // Claim opcional: birthDate en formato dd-MM-yyyy
        if (user.getBirthDate() != null) {
            String birthDateStr = user.getBirthDate().format(birthDateFormatter);
            builder.claim("birthDate", birthDateStr);
        }

        // Claim opcional: registrationCode normalizado
        if (user.getRegistrationCode() != null && !user.getRegistrationCode().isBlank()) {
            builder.claim("registrationCode", user.getRegistrationCode());
        }

        return builder
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
}
