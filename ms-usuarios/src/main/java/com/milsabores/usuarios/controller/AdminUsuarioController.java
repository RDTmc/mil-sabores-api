package com.milsabores.usuarios.controller;

import com.milsabores.usuarios.dto.AuthDtos;
import com.milsabores.usuarios.model.UsuarioEntity;
import com.milsabores.usuarios.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints de administración para gestión de usuarios.
 *
 * Rutas reales con context-path /api:
 *  - GET /api/admin/users  → lista todos los usuarios
 *
 * Protegido por SecurityConfig:
 *  - .requestMatchers("/admin/**").hasRole("ADMIN")
 *    → Sólo tokens con "role": "ADMIN" pueden acceder.
 */
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<List<AuthDtos.UsuarioPublicDto>> getAllUsers() {
        List<UsuarioEntity> entities = usuarioService.findAllUsers();

        List<AuthDtos.UsuarioPublicDto> dtos = entities.stream()
                .map(u -> new AuthDtos.UsuarioPublicDto(
                        u.getId(),
                        u.getEmail(),
                        u.getFullName(),
                        u.getPhone()
                ))
                .toList();

        return ResponseEntity.ok(dtos);
    }
}
