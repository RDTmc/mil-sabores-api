package com.milsabores.orders.controller;

import com.milsabores.orders.dto.OrderDtos;
import com.milsabores.orders.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints de administración de órdenes.
 *
 * Rutas reales con context-path /api:
 *   - GET /api/admin/orders/latest
 */
@RestController
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class AdminOrdersController {

    private final OrderService orderService;

    /**
     * Verifica si el JWT pertenece a un usuario con rol ADMIN.
     * Lee el claim "role" del token (ej: "ADMIN", "CUSTOMER").
     */
    private boolean isAdmin(Jwt jwt) {
        if (jwt == null) return false;

        Object roleClaim = jwt.getClaim("role");
        if (roleClaim instanceof String role) {
            // Aceptamos "ADMIN" o "ROLE_ADMIN" por seguridad
            return "ADMIN".equalsIgnoreCase(role) || "ROLE_ADMIN".equalsIgnoreCase(role);
        }
        return false;
    }

    /**
     * GET /api/admin/orders/latest?limit=5
     *
     * Devuelve las últimas órdenes creadas en el sistema (de cualquier usuario),
     * pensadas para el dashboard de administración.
     */
    @GetMapping("/latest")
    public ResponseEntity<?> listLatestOrdersForAdmin(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(name = "limit", defaultValue = "5") int limit
    ) {
        if (!isAdmin(jwt)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new OrderDtos.ErrorBody("Acceso restringido a administradores."));
        }

        int safeLimit = Math.max(1, limit);
        List<OrderDtos.OrderResponse> latest = orderService.listLatestOrders(safeLimit);

        return ResponseEntity.ok(latest);
    }
}
