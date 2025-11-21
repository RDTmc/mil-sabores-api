package com.milsabores.orders.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTOs relacionados con órdenes (requests/responses) para ms-orders.
 */
public class OrderDtos {

    // --------- REQUESTS ---------

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateOrderItemRequest {
        @NotBlank
        private String productId;

        @NotBlank
        private String productName;

        private String image;

        @NotNull
        @Min(1)
        private Integer unitPrice;

        @NotNull
        @Min(1)
        private Integer quantity;

        private String size;
        private String flavor;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateOrderRequest {

        /**
         * Método de pago (ej. "CARD", "TRANSFER", "CASH").
         */
        @NotBlank
        private String paymentMethod;

        /**
         * Dirección de envío o retiro.
         */
        @NotBlank
        private String shippingAddress;

        /**
         * Ítems que el usuario está comprando.
         * Normalmente corresponderán al carrito actual.
         */
        @NotEmpty
        private List<CreateOrderItemRequest> items;
    }

    // --------- RESPONSES ---------

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemResponse {
        private String id;
        private String productId;
        private String productName;
        private String image;
        private Integer unitPrice;
        private Integer quantity;
        private String size;
        private String flavor;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderResponse {
        private String id;
        private String userId;
        private String status;
        private Integer totalAmount;
        private String paymentMethod;
        private String shippingAddress;
        private LocalDateTime createdAt;
        private List<OrderItemResponse> items;
    }

    // --------- ERROR SIMPLE ---------

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorBody {
        private String message;
    }
}
