package com.milsabores.usuarios.controller;

import com.milsabores.usuarios.model.UsuarioEntity;
import com.milsabores.usuarios.service.UsuarioService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/auth")
// Con context-path /api, las rutas reales son:
// POST /api/auth/register
// POST /api/auth/login
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioService usuarioService;

    // ========= ENDPOINT: REGISTRO =========

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            UsuarioEntity u = usuarioService.register(
                    request.getEmail(),
                    request.getPassword(),
                    request.getFullName(),
                    request.getPhone()
            );

            UsuarioPublicDto dto = new UsuarioPublicDto();
            dto.setId(u.getId());
            dto.setEmail(u.getEmail());
            dto.setFullName(u.getFullName());
            dto.setPhone(u.getPhone());

            return ResponseEntity.status(HttpStatus.CREATED).body(dto);

        } catch (IllegalStateException e) {
            // Correo ya existe
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorBody(e.getMessage()));
        }
    }

    // ========= ENDPOINT: LOGIN =========

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            UsuarioEntity u = usuarioService.authenticate(
                    request.getEmail(),
                    request.getPassword()
            );

            // Mantenemos un "token" simple para no romper el front.
            // Despues lo reemplazamos por un JWT real usando JwtService.
            String sessionToken = "session-" + UUID.randomUUID();

            LoginResponse response = new LoginResponse();
            response.setToken(sessionToken);
            response.setUserId(u.getId());
            response.setEmail(u.getEmail());
            response.setFullName(u.getFullName());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            // Credenciales inválidas
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorBody("Credenciales inválidas"));
        }
    }

    // ========= DTOs internos =========

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

        public RegisterRequest() {}

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
    }

    public static class LoginRequest {
        @NotBlank
        @Email
        private String email;

        @NotBlank
        private String password;

        public LoginRequest() {}

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class LoginResponse {
        private String token;
        private String userId;
        private String email;
        private String fullName;

        public LoginResponse() {}

        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
    }

    public static class UsuarioPublicDto {
        private String id;
        private String email;
        private String fullName;
        private String phone;

        public UsuarioPublicDto() {}

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
    }

    public static class ErrorBody {
        private String message;

        public ErrorBody() {}
        public ErrorBody(String message) { this.message = message; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}