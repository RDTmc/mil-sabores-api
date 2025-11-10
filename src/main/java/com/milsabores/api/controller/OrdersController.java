package com.milsabores.api.controller;

import com.milsabores.api.service.OrdersService;
import com.milsabores.api.service.OrdersService.CreateOrderRequest;
import com.milsabores.api.service.OrdersService.OrderResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/orders")
public class OrdersController {

    private final OrdersService service;

    public OrdersController(OrdersService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> create(@RequestBody CreateOrderRequest req) {
        try {
            // Validación muy básica de payload
            if (req == null || req.items == null || req.items.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El carrito no puede estar vacío.");
            }
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
