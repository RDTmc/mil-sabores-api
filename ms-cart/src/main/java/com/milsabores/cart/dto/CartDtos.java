package com.milsabores.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

/**
 * DTOs relacionados al carrito (requests/responses) para ms-cart.
 */
public class CartDtos {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddItemRequest {
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
    public static class UpdateItemRequest {
        @NotNull
        @Min(0)
        private Integer quantity;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CartItemResponse {
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
    public static class CartResponse {
        private String id;
        private String userId;
        private String status;
        private List<CartItemResponse> items;
        private Integer totalItems;
        private Integer totalAmount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorBody {
        private String message;
    }
}
