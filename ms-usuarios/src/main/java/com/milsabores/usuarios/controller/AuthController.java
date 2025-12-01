package com.milsabores.usuarios.controller;

import com.milsabores.usuarios.dto.AuthDtos;
import com.milsabores.usuarios.model.UsuarioEntity;
import com.milsabores.usuarios.security.JwtService;
import com.milsabores.usuarios.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
// Con context-path /api, las rutas reales son:
// POST /api/auth/register
// POST /api/auth/login
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioService usuarioService;
    private final JwtService jwtService;

    // ========= ENDPOINT: REGISTRO =========

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody AuthDtos.RegisterRequest request) {
        try {
            UsuarioEntity u = usuarioService.register(
                    request.getEmail(),
                    request.getPassword(),
                    request.getFullName(),
                    request.getPhone(),
                    request.getBirthDate(),
                    request.getRegistrationCode()
            );

            AuthDtos.UsuarioPublicDto dto = new AuthDtos.UsuarioPublicDto(
                    u.getId(),
                    u.getEmail(),
                    u.getFullName(),
                    u.getPhone()
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(dto);

        } catch (IllegalStateException e) {
            // Correo ya existe
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new AuthDtos.ErrorBody(e.getMessage()));
        }
    }

    // ========= ENDPOINT: LOGIN =========

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthDtos.LoginRequest request) {
        try {
            UsuarioEntity u = usuarioService.authenticate(
                    request.getEmail(),
                    request.getPassword()
            );

            // Ahora generamos un JWT real
            String jwt = jwtService.generateToken(u);

            // 🔹 Incluimos el rol en la respuesta al frontend
            AuthDtos.LoginResponse response = new AuthDtos.LoginResponse(
                    jwt,              // token (JWT)
                    u.getId(),
                    u.getEmail(),
                    u.getFullName(),
                    u.getRole()       // 👈 NUEVO: rol ("CUSTOMER" / "ADMIN")
            );

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            // Credenciales inválidas
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthDtos.ErrorBody("Credenciales inválidas"));
        }
    }
}
