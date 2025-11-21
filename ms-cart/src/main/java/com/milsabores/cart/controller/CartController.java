package com.milsabores.cart.controller;

import com.milsabores.cart.dto.CartDtos;
import com.milsabores.cart.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints del carrito.
 *
 * Ahora:
 * - El userId se obtiene principalmente desde el JWT (claim "sub"),
 *   emitido por ms-usuarios.
 * - Mantiene compatibilidad temporal con header "X-User-Id" como respaldo.
 *
 * Rutas reales con context-path /api:
 *  - GET    /api/cart
 *  - POST   /api/cart/items
 *  - PUT    /api/cart/items/{itemId}
 *  - DELETE /api/cart/items/{itemId}
 *  - DELETE /api/cart
 */
@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /**
     * Resuelve el userId en este orden:
     *  1) JWT (claim "sub")
     *  2) Header: X-User-Id  (respaldo mientras migras el front)
     */
    private String resolveUserId(Jwt jwt, String userIdHeader) {
        // 1) Desde el JWT (lo ideal)
        if (jwt != null && jwt.getSubject() != null && !jwt.getSubject().isBlank()) {
            return jwt.getSubject();
        }

        // 2) Desde header (modo legado)
        if (userIdHeader != null && !userIdHeader.isBlank()) {
            return userIdHeader;
        }

        throw new IllegalArgumentException(
                "Debe especificarse el userId (JWT sub o header X-User-Id)."
        );
    }

    /**
     * Obtener el carrito activo del usuario.
     */
    @GetMapping
    public ResponseEntity<?> getCart(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(name = "X-User-Id", required = false) String userIdHeader
    ) {
        try {
            String userId = resolveUserId(jwt, userIdHeader);
            var cart = cartService.getActiveCart(userId);
            return ResponseEntity.ok(cart);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new CartDtos.ErrorBody(e.getMessage()));
        }
    }

    /**
     * Agregar un item al carrito.
     */
    @PostMapping("/items")
    public ResponseEntity<?> addItem(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(name = "X-User-Id", required = false) String userIdHeader,
            @Valid @RequestBody CartDtos.AddItemRequest request
    ) {
        try {
            String userId = resolveUserId(jwt, userIdHeader);
            var cart = cartService.addItem(userId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(cart);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new CartDtos.ErrorBody(e.getMessage()));
        }
    }

    /**
     * Actualizar cantidad de un item.
     */
    @PutMapping("/items/{itemId}")
    public ResponseEntity<?> updateItem(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(name = "X-User-Id", required = false) String userIdHeader,
            @PathVariable("itemId") String itemId,
            @Valid @RequestBody CartDtos.UpdateItemRequest request
    ) {
        try {
            String userId = resolveUserId(jwt, userIdHeader);
            var cart = cartService.updateItemQuantity(userId, itemId, request.getQuantity());
            return ResponseEntity.ok(cart);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new CartDtos.ErrorBody(e.getMessage()));
        }
    }

    /**
     * Eliminar un item del carrito.
     */
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<?> removeItem(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(name = "X-User-Id", required = false) String userIdHeader,
            @PathVariable("itemId") String itemId
    ) {
        try {
            String userId = resolveUserId(jwt, userIdHeader);
            var cart = cartService.removeItem(userId, itemId);
            return ResponseEntity.ok(cart);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new CartDtos.ErrorBody(e.getMessage()));
        }
    }

    /**
     * Vaciar completamente el carrito activo.
     */
    @DeleteMapping
    public ResponseEntity<?> clearCart(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(name = "X-User-Id", required = false) String userIdHeader
    ) {
        try {
            String userId = resolveUserId(jwt, userIdHeader);
            var cart = cartService.clearCart(userId);
            return ResponseEntity.ok(cart);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new CartDtos.ErrorBody(e.getMessage()));
        }
    }
}
