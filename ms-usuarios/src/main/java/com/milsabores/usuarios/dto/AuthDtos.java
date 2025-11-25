package com.milsabores.usuarios.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

/**
 * DTOs relacionados a autenticación (register/login) para ms-usuarios.
 * Se agrupan en una única clase contenedora
 */
public class AuthDtos {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegisterRequest {
        @NotBlank
        @Email
        private String email;

        @NotBlank
        @Size(min = 4, max = 50)
        private String password;

        @NotBlank
        private String fullName;

        private String phone;

        /**
         * Fecha de nacimiento del usuario.
         * Formato esperado: "dd-MM-yyyy" (ej: "25-11-1990")
         */
        @JsonFormat(pattern = "dd-MM-yyyy")
        private LocalDate birthDate;

        /**
         * Código de registro, por ejemplo:
         *  - "FELICES50" para la promo 10% de por vida.
         * Puede venir null si el usuario no usa código.
         */
        private String registrationCode;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {
        @NotBlank
        @Email
        private String email;

        @NotBlank
        private String password;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginResponse {
        // En el front este campo se llama "token"
        private String token;    // Ahora es un JWT real
        private String userId;
        private String email;
        private String fullName;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UsuarioPublicDto {
        private String id;
        private String email;
        private String fullName;
        private String phone;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorBody {
        private String message;
    }
}
