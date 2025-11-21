package com.milsabores.orders.controller;

import com.milsabores.orders.dto.OrderDtos;
import com.milsabores.orders.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestionar Órdenes en ms-orders.
 *
 * Ahora:
 * - El carrito se maneja en el microservicio ms-cart.
 * - Aquí solo creamos y consultamos órdenes persistidas en BD (JPA).
 * - El userId se obtiene principalmente desde el JWT (claim "sub"),
 *   y como respaldo desde header X-User-Id o query param ?userId=...
 */
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrdersController {

    private final OrderService orderService;

    /**
     * Resuelve el userId en este orden:
     * 1) JWT (claim "sub")
     * 2) Header: X-User-Id
     * 3) Query param: ?userId=...
     */
    private String resolveUserId(Jwt jwt, String userIdHeader, String userIdParam) {
        // 1) Desde el JWT (lo ideal)
        if (jwt != null && jwt.getSubject() != null && !jwt.getSubject().isBlank()) {
            return jwt.getSubject();
        }

        // 2) Desde header
        if (userIdHeader != null && !userIdHeader.isBlank()) {
            return userIdHeader;
        }

        // 3) Desde query param
        if (userIdParam != null && !userIdParam.isBlank()) {
            return userIdParam;
        }

        throw new IllegalArgumentException(
                "Debe especificarse el userId (JWT sub, header X-User-Id o query param userId)."
        );
    }

    /**
     * POST /api/orders
     * Crea una nueva orden para el usuario autenticado.
     *
     * El front debe enviar:
     * - Authorization: Bearer <JWT emitido por ms-usuarios>
     * - Body JSON: OrderDtos.CreateOrderRequest
     *   (paymentMethod, shippingAddress, items[])
     *
     * Opcionalmente aún se acepta:
     * - Header: X-User-Id
     * - Query param: ?userId=...
     * como respaldo mientras migras todo a JWT.
     */
    @PostMapping
    public ResponseEntity<?> createOrder(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(name = "X-User-Id", required = false) String userIdHeader,
            @RequestParam(name = "userId", required = false) String userIdParam,
            @Valid @RequestBody OrderDtos.CreateOrderRequest request
    ) {
        try {
            String userId = resolveUserId(jwt, userIdHeader, userIdParam);
            OrderDtos.OrderResponse response = orderService.createOrder(userId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new OrderDtos.ErrorBody(e.getMessage()));
        }
    }

    /**
     * GET /api/orders
     * Lista todas las órdenes del usuario (más recientes primero).
     * Ideal para la página "Mis compras".
     */
    @GetMapping
    public ResponseEntity<?> listOrders(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(name = "X-User-Id", required = false) String userIdHeader,
            @RequestParam(name = "userId", required = false) String userIdParam
    ) {
        try {
            String userId = resolveUserId(jwt, userIdHeader, userIdParam);
            List<OrderDtos.OrderResponse> orders = orderService.listOrders(userId);
            return ResponseEntity.ok(orders);
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new OrderDtos.ErrorBody(e.getMessage()));
        }
    }

    /**
     * GET /api/orders/{orderId}
     * Devuelve el detalle de una orden del usuario.
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrder(
            @PathVariable("orderId") String orderId,
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(name = "X-User-Id", required = false) String userIdHeader,
            @RequestParam(name = "userId", required = false) String userIdParam
    ) {
        try {
            String userId = resolveUserId(jwt, userIdHeader, userIdParam);
            OrderDtos.OrderResponse order = orderService.getOrder(userId, orderId);
            return ResponseEntity.ok(order);
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new OrderDtos.ErrorBody(e.getMessage()));
        }
    }
}
