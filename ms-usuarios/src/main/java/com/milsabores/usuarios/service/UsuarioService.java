package com.milsabores.usuarios.service;

import com.milsabores.usuarios.model.UsuarioEntity;
import com.milsabores.usuarios.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    // Ahora usamos el PasswordEncoder que viene desde SecurityConfig
    private final PasswordEncoder passwordEncoder;

    /**
     * Registra un usuario nuevo en la BD.
     * Lanza IllegalStateException si el correo ya existe.
     */
    public UsuarioEntity register(String email,
                                  String rawPassword,
                                  String fullName,
                                  String phone,
                                  LocalDate birthDate,
                                  String registrationCode) {

        String normalizedEmail = email.toLowerCase();

        if (usuarioRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalStateException("El correo ya está registrado");
        }

        String hash = passwordEncoder.encode(rawPassword);

        // Normalizar código de registro (por ejemplo "FELICES50")
        String normalizedCode = null;
        if (registrationCode != null && !registrationCode.trim().isEmpty()) {
            normalizedCode = registrationCode.trim().toUpperCase();
        }

        UsuarioEntity entity = UsuarioEntity.builder()
                .email(normalizedEmail)
                .passwordHash(hash)
                .fullName(fullName)
                .phone(phone)
                .role("CUSTOMER")       // valor por defecto, se puede parametrizar más adelante
                .birthDate(birthDate)   // ✅ nuevo
                .registrationCode(normalizedCode) // ✅ nuevo
                .build();

        return usuarioRepository.save(entity);
    }

    /**
     * Autentica al usuario contra la BD.
     * Lanza IllegalArgumentException si las credenciales son inválidas.
     */
    public UsuarioEntity authenticate(String email, String rawPassword) {
        String normalizedEmail = email.toLowerCase();

        UsuarioEntity user = usuarioRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new IllegalArgumentException("Credenciales inválidas"));

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Credenciales inválidas");
        }

        return user;
    }

    public boolean emailExists(String email) {
        return usuarioRepository.existsByEmail(email.toLowerCase());
    }

    public Optional<UsuarioEntity> findById(String id) {
        return usuarioRepository.findById(id);
    }
}
