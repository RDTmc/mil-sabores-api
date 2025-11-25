package com.milsabores.usuarios.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users") // Tabla en Supabase
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "role", nullable = false, length = 50)
    private String role; // Ej: "CUSTOMER", "ADMIN"

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Fecha de nacimiento del usuario.
     * Se usa para:
     *  - Calcular si es adulto mayor (50+)
     *  - Detectar cumpleaños (promo Duoc)
     */
    @Column(name = "birth_date")
    private LocalDate birthDate;

    /**
     * Código con el que se registró, por ejemplo "FELICES50".
     * Sirve para saber si aplica la promo 10% de por vida.
     */
    @Column(name = "registration_code", length = 50)
    private String registrationCode;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (role == null) {
            role = "CUSTOMER";
        }
    }
}
