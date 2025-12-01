package com.milsabores.usuarios.controller;

import com.milsabores.usuarios.dto.AuthDtos;
import com.milsabores.usuarios.model.UsuarioEntity;
import com.milsabores.usuarios.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints de administración para gestión de usuarios.
 *
 * Rutas reales con context-path /api:
 *  - GET    /api/admin/users        → lista todos los usuarios
 *  - PUT    /api/admin/users/{id}   → actualiza datos básicos de un usuario
 *  - DELETE /api/admin/users/{id}   → elimina un usuario
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

    /**
     * Listar todos los usuarios para el panel admin.
     */
    @GetMapping
    public ResponseEntity<List<AuthDtos.AdminUserResponse>> getAllUsers() {
        List<UsuarioEntity> entities = usuarioService.findAllUsers();

        List<AuthDtos.AdminUserResponse> dtos = entities.stream()
                .map(u -> new AuthDtos.AdminUserResponse(
                        u.getId(),
                        u.getEmail(),
                        u.getFullName(),
                        u.getPhone(),
                        u.getRole()
                ))
                .toList();

        return ResponseEntity.ok(dtos);
    }

    /**
     * Actualizar datos básicos de un usuario.
     * Body: AdminUserResponse (reutilizamos el DTO, pero sólo se consideran:
     *  - fullName
     *  - phone
     *  - role
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable("id") String id,
            @RequestBody AuthDtos.AdminUserResponse body
    ) {
        try {
            UsuarioEntity updated = usuarioService.updateUser(
                    id,
                    body.getFullName(),
                    body.getPhone(),
                    body.getRole()
            );

            AuthDtos.AdminUserResponse dto = new AuthDtos.AdminUserResponse(
                    updated.getId(),
                    updated.getEmail(),
                    updated.getFullName(),
                    updated.getPhone(),
                    updated.getRole()
            );

            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new AuthDtos.ErrorBody(e.getMessage()));
        }
    }

    /**
     * Eliminar un usuario por id.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable("id") String id) {
        try {
            usuarioService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new AuthDtos.ErrorBody(e.getMessage()));
        }
    }
}
