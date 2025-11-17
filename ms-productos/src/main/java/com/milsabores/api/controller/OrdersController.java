package com.milsabores.api.controller;

import com.milsabores.api.service.OrdersService;
import com.milsabores.api.service.OrdersService.CreateOrderRequest;
import com.milsabores.api.service.OrdersService.OrderResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

@RestController
@RequestMapping("/api/orders")
public class OrdersController {

    private final OrdersService service;

    public OrdersController(OrdersService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> create(@AuthenticationPrincipal Jwt jwt,
        @RequestBody CreateOrderRequest req) {
        try {
            // fuerza el userId desde el JWT (issuer: Supabase)
            if (jwt == null || jwt.getSubject() == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token inválido");
            }
            req.userId = jwt.getSubject(); // UUID de Supabase (auth.uid())
            OrderResponse out = service.createOrder(req);
            return ResponseEntity.status(HttpStatus.CREATED).body(out);
        } catch (IllegalArgumentException iae) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, iae.getMessage());
        } catch (ResponseStatusException rse) {
            throw rse;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo crear la orden");
        }
    }
}
