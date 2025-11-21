package com.milsabores.cart.service;

import com.milsabores.cart.dto.CartDtos;
import com.milsabores.cart.model.CartEntity;
import com.milsabores.cart.model.CartItemEntity;
import com.milsabores.cart.repository.CartItemRepository;
import com.milsabores.cart.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private static final String STATUS_ACTIVE = "ACTIVE";

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    /**
     * Obtiene el carrito ACTIVO del usuario o lo crea si no existe.
     */
    @Transactional
    public CartEntity getOrCreateActiveCart(String userId) {
        return cartRepository.findByUserIdAndStatus(userId, STATUS_ACTIVE)
                .orElseGet(() -> {
                    CartEntity c = CartEntity.builder()
                            .userId(userId)
                            .status(STATUS_ACTIVE)
                            .build();
                    return cartRepository.save(c);
                });
    }

    /**
     * Devuelve el carrito activo del usuario como DTO.
     * Si no existe, devuelve uno vacío (lo crea).
     */
    @Transactional(readOnly = true)
    public CartDtos.CartResponse getActiveCart(String userId) {
        CartEntity cart = cartRepository.findByUserIdAndStatus(userId, STATUS_ACTIVE)
                .orElse(null);

        if (cart == null) {
            // Carrito activo vacío (sin crearlo físicamente si no quieres)
            return CartDtos.CartResponse.builder()
                    .id(null)
                    .userId(userId)
                    .status(STATUS_ACTIVE)
                    .items(List.of())
                    .totalItems(0)
                    .totalAmount(0)
                    .build();
        }

        return toDto(cart);
    }

    /**
     * Agrega un item al carrito activo del usuario.
     * Si ya existe mismo productId + size + flavor, acumula quantity.
     */
    @Transactional
    public CartDtos.CartResponse addItem(String userId, CartDtos.AddItemRequest req) {
        CartEntity cart = getOrCreateActiveCart(userId);

        // Buscar si ya existe un item similar en el carrito
        CartItemEntity existing = cart.getItems().stream()
                .filter(i ->
                        i.getProductId().equals(req.getProductId()) &&
                                eq(i.getSize(), req.getSize()) &&
                                eq(i.getFlavor(), req.getFlavor())
                )
                .findFirst()
                .orElse(null);

        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + req.getQuantity());
        } else {
            CartItemEntity item = CartItemEntity.builder()
                    .cart(cart)
                    .productId(req.getProductId())
                    .productName(req.getProductName())
                    .image(req.getImage())
                    .unitPrice(req.getUnitPrice())
                    .quantity(req.getQuantity())
                    .size(req.getSize())
                    .flavor(req.getFlavor())
                    .build();
            cart.getItems().add(item);
        }

        CartEntity saved = cartRepository.save(cart);
        return toDto(saved);
    }

    /**
     * Actualiza la cantidad de un item.
     * Si quantity == 0, elimina el item.
     */
    @Transactional
    public CartDtos.CartResponse updateItemQuantity(String userId, String itemId, int quantity) {
        CartEntity cart = cartRepository.findByUserIdAndStatus(userId, STATUS_ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("No existe carrito activo para el usuario"));

        CartItemEntity item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Item no encontrado en el carrito"));

        if (quantity <= 0) {
            cart.getItems().remove(item);
            cartItemRepository.delete(item);
        } else {
            item.setQuantity(quantity);
        }

        CartEntity saved = cartRepository.save(cart);
        return toDto(saved);
    }

    /**
     * Elimina un item específico del carrito.
     */
    @Transactional
    public CartDtos.CartResponse removeItem(String userId, String itemId) {
        CartEntity cart = cartRepository.findByUserIdAndStatus(userId, STATUS_ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("No existe carrito activo para el usuario"));

        CartItemEntity item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Item no encontrado en el carrito"));

        cart.getItems().remove(item);
        cartItemRepository.delete(item);

        CartEntity saved = cartRepository.save(cart);
        return toDto(saved);
    }

    /**
     * Vacía el carrito activo del usuario.
     */
    @Transactional
    public CartDtos.CartResponse clearCart(String userId) {
        CartEntity cart = cartRepository.findByUserIdAndStatus(userId, STATUS_ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("No existe carrito activo para el usuario"));

        cartItemRepository.deleteByCart_Id(cart.getId());
        cart.getItems().clear();

        CartEntity saved = cartRepository.save(cart);
        return toDto(saved);
    }

    // ================= Helpers =================

    private CartDtos.CartResponse toDto(CartEntity cart) {
        List<CartDtos.CartItemResponse> items = cart.getItems().stream()
                .map(i -> CartDtos.CartItemResponse.builder()
                        .id(i.getId())
                        .productId(i.getProductId())
                        .productName(i.getProductName())
                        .image(i.getImage())
                        .unitPrice(i.getUnitPrice())
                        .quantity(i.getQuantity())
                        .size(i.getSize())
                        .flavor(i.getFlavor())
                        .build()
                )
                .toList();

        int totalItems = items.stream()
                .mapToInt(CartDtos.CartItemResponse::getQuantity)
                .sum();

        int totalAmount = items.stream()
                .mapToInt(i -> i.getQuantity() * i.getUnitPrice())
                .sum();

        return CartDtos.CartResponse.builder()
                .id(cart.getId())
                .userId(cart.getUserId())
                .status(cart.getStatus())
                .items(items)
                .totalItems(totalItems)
                .totalAmount(totalAmount)
                .build();
    }

    private boolean eq(String a, String b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }
}
