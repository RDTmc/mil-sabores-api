package com.milsabores.orders.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@RestController
@RequestMapping
public class OrdersController {

    // ====== Estado en memoria (por usuario) ======
    private final Map<String, CartResponseDto> carts = new ConcurrentHashMap<>();
    private final Map<String, OrderDetailDto> orders = new ConcurrentHashMap<>();
    private final AtomicInteger orderSequence = new AtomicInteger(1);

    // ====== Helpers internos ======
    private CartResponseDto getOrCreateCart(String userId) {
        return carts.computeIfAbsent(userId, id -> {
            CartResponseDto c = new CartResponseDto();
            c.setUserId(id);
            c.setItems(new ArrayList<>());
            c.setTotalItems(0);
            c.setSubtotal(0);
            return c;
        });
    }

    private void recalcTotals(CartResponseDto cart) {
        int subtotal = cart.getItems().stream()
                .mapToInt(item -> item.getUnitPrice() * item.getQuantity())
                .sum();
        int totalItems = cart.getItems().stream()
                .mapToInt(CartItemDto::getQuantity)
                .sum();
        cart.setSubtotal(subtotal);
        cart.setTotalItems(totalItems);
    }

    private String nextOrderId() {
        int seq = orderSequence.getAndIncrement();
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "ORD-" + date + "-" + String.format("%04d", seq);
    }

    // ======================================================
    // ENDPOINTS DE CARRITO (/api/cart...)
    // ======================================================

    /**
     * GET /api/cart?userId=123
     * Obtiene el carrito actual de un usuario (o uno vacío si no existe).
     */
    @GetMapping("/cart")
    public ResponseEntity<CartResponseDto> getCart(
            @RequestParam("userId") String userId
    ) {
        CartResponseDto cart = getOrCreateCart(userId);
        return ResponseEntity.ok(cart);
    }

    /**
     * POST /api/cart/items
     * Agrega o incrementa un ítem en el carrito.
     */
    @PostMapping("/cart/items")
    public ResponseEntity<CartResponseDto> addItemToCart(
            @Valid @RequestBody AddCartItemRequest request
    ) {
        CartResponseDto cart = getOrCreateCart(request.getUserId());

        // Buscar si ya existe el mismo producto/tamaño/sabor
        Optional<CartItemDto> existing = cart.getItems().stream()
                .filter(i ->
                        Objects.equals(i.getProductId(), request.getProductId()) &&
                                Objects.equals(i.getSize(), request.getSize()) &&
                                Objects.equals(i.getFlavor(), request.getFlavor())
                )
                .findFirst();

        if (existing.isPresent()) {
            CartItemDto item = existing.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
        } else {
            CartItemDto item = new CartItemDto();
            item.setProductId(request.getProductId());
            item.setName(request.getName());
            item.setImage(request.getImage());
            item.setUnitPrice(request.getUnitPrice());
            item.setQuantity(request.getQuantity());
            item.setSize(request.getSize());
            item.setFlavor(request.getFlavor());
            cart.getItems().add(item);
        }

        recalcTotals(cart);
        return ResponseEntity.ok(cart);
    }

    /**
     * PUT /api/cart/items/{productId}
     * Actualiza cantidad (y opcionalmente tamaño/sabor) de un ítem.
     */
    @PutMapping("/cart/items/{productId}")
    public ResponseEntity<CartResponseDto> updateCartItem(
            @PathVariable("productId") String productId,
            @Valid @RequestBody UpdateCartItemRequest request
    ) {
        CartResponseDto cart = carts.get(request.getUserId());
        if (cart == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Optional<CartItemDto> existing = cart.getItems().stream()
                .filter(i -> Objects.equals(i.getProductId(), productId))
                .findFirst();

        if (existing.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        CartItemDto item = existing.get();
        if (request.getQuantity() <= 0) {
            cart.getItems().remove(item);
        } else {
            item.setQuantity(request.getQuantity());
            if (request.getSize() != null) {
                item.setSize(request.getSize());
            }
            if (request.getFlavor() != null) {
                item.setFlavor(request.getFlavor());
            }
        }

        recalcTotals(cart);
        return ResponseEntity.ok(cart);
    }

    /**
     * DELETE /api/cart/items/{productId}?userId=123
     * Elimina un producto del carrito.
     */
    @DeleteMapping("/cart/items/{productId}")
    public ResponseEntity<CartResponseDto> removeItemFromCart(
            @PathVariable("productId") String productId,
            @RequestParam("userId") String userId
    ) {
        CartResponseDto cart = carts.get(userId);
        if (cart == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        cart.setItems(
                cart.getItems().stream()
                        .filter(i -> !Objects.equals(i.getProductId(), productId))
                        .collect(Collectors.toList())
        );

        recalcTotals(cart);
        return ResponseEntity.ok(cart);
    }

    /**
     * DELETE /api/cart?userId=123
     * Vacía por completo el carrito de un usuario.
     */
    @DeleteMapping("/cart")
    public ResponseEntity<CartResponseDto> clearCart(
            @RequestParam("userId") String userId
    ) {
        CartResponseDto empty = new CartResponseDto();
        empty.setUserId(userId);
        empty.setItems(new ArrayList<>());
        empty.setTotalItems(0);
        empty.setSubtotal(0);
        carts.put(userId, empty);
        return ResponseEntity.ok(empty);
    }

    // ======================================================
    // ENDPOINTS DE ÓRDENES (/api/orders...)
    // ======================================================

    /**
     * POST /api/orders
     * Crea una orden a partir de los ítems enviados (y opcionalmente vacía el carrito).
     */
    @PostMapping("/orders")
    public ResponseEntity<OrderDetailDto> createOrder(
            @Valid @RequestBody CreateOrderRequest request
    ) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        String orderId = nextOrderId();
        OrderDetailDto order = new OrderDetailDto();
        order.setOrderId(orderId);
        order.setUserId(request.getUserId());
        order.setStatus("CONFIRMED");
        order.setItems(new ArrayList<>(request.getItems()));
        order.setPaymentMethod(request.getPaymentMethod());
        order.setShippingAddress(request.getShippingAddress());
        order.setNotes(request.getNotes());

        int subtotal = request.getItems().stream()
                .mapToInt(i -> i.getUnitPrice() * i.getQuantity())
                .sum();
        int deliveryFee = 2990; // fijo para el MVP
        order.setSubtotal(subtotal);
        order.setDeliveryFee(deliveryFee);
        order.setTotal(subtotal + deliveryFee);
        order.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        orders.put(orderId, order);

        // Opcional: vaciar carrito del usuario después de confirmar
        if (request.getUserId() != null) {
            carts.remove(request.getUserId());
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    /**
     * GET /api/orders?userId=123
     * Lista órdenes de un usuario (resumen).
     */
    @GetMapping("/orders")
    public ResponseEntity<List<OrderSummaryDto>> listOrders(
            @RequestParam("userId") String userId
    ) {
        List<OrderSummaryDto> result = orders.values().stream()
                .filter(o -> Objects.equals(o.getUserId(), userId))
                .map(o -> {
                    OrderSummaryDto s = new OrderSummaryDto();
                    s.setOrderId(o.getOrderId());
                    s.setStatus(o.getStatus());
                    s.setTotal(o.getTotal());
                    s.setCreatedAt(o.getCreatedAt());
                    return s;
                })
                .sorted(Comparator.comparing(OrderSummaryDto::getCreatedAt).reversed())
                .toList();

        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/orders/{orderId}
     * Detalle de una orden.
     */
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<OrderDetailDto> getOrder(
            @PathVariable("orderId") String orderId
    ) {
        OrderDetailDto order = orders.get(orderId);
        if (order == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(order);
    }

    // ======================================================
    // DTOs internos
    // ======================================================

    public static class CartItemDto {
        @NotBlank
        private String productId;
        @NotBlank
        private String name;
        private String image;
        @Min(0)
        private int unitPrice;
        @Min(1)
        private int quantity;
        private String size;
        private String flavor;

        public CartItemDto() {}

        // Getters y setters
        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getImage() { return image; }
        public void setImage(String image) { this.image = image; }

        public int getUnitPrice() { return unitPrice; }
        public void setUnitPrice(int unitPrice) { this.unitPrice = unitPrice; }

        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }

        public String getSize() { return size; }
        public void setSize(String size) { this.size = size; }

        public String getFlavor() { return flavor; }
        public void setFlavor(String flavor) { this.flavor = flavor; }
    }

    public static class CartResponseDto {
        private String userId;
        private List<CartItemDto> items;
        private int totalItems;
        private int subtotal;

        public CartResponseDto() {}

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }

        public List<CartItemDto> getItems() { return items; }
        public void setItems(List<CartItemDto> items) { this.items = items; }

        public int getTotalItems() { return totalItems; }
        public void setTotalItems(int totalItems) { this.totalItems = totalItems; }

        public int getSubtotal() { return subtotal; }
        public void setSubtotal(int subtotal) { this.subtotal = subtotal; }
    }

    public static class AddCartItemRequest {
        @NotBlank
        private String userId;
        @NotBlank
        private String productId;
        @NotBlank
        private String name;
        private String image;
        @Min(0)
        private int unitPrice;
        @Min(1)
        private int quantity;
        private String size;
        private String flavor;

        public AddCartItemRequest() {}

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }

        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getImage() { return image; }
        public void setImage(String image) { this.image = image; }

        public int getUnitPrice() { return unitPrice; }
        public void setUnitPrice(int unitPrice) { this.unitPrice = unitPrice; }

        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }

        public String getSize() { return size; }
        public void setSize(String size) { this.size = size; }

        public String getFlavor() { return flavor; }
        public void setFlavor(String flavor) { this.flavor = flavor; }
    }

    public static class UpdateCartItemRequest {
        @NotBlank
        private String userId;
        @Min(0)
        private int quantity;
        private String size;
        private String flavor;

        public UpdateCartItemRequest() {}

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }

        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }

        public String getSize() { return size; }
        public void setSize(String size) { this.size = size; }

        public String getFlavor() { return flavor; }
        public void setFlavor(String flavor) { this.flavor = flavor; }
    }

    public static class CreateOrderRequest {
        @NotBlank
        private String userId;
        private String cartId;
        @NotNull
        private List<CartItemDto> items;
        @NotBlank
        private String paymentMethod;
        private String shippingAddress;
        private String notes;

        public CreateOrderRequest() {}

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }

        public String getCartId() { return cartId; }
        public void setCartId(String cartId) { this.cartId = cartId; }

        public List<CartItemDto> getItems() { return items; }
        public void setItems(List<CartItemDto> items) { this.items = items; }

        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

        public String getShippingAddress() { return shippingAddress; }
        public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }

        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    public static class OrderSummaryDto {
        private String orderId;
        private String status;
        private int total;
        private String createdAt;

        public OrderSummaryDto() {}

        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public int getTotal() { return total; }
        public void setTotal(int total) { this.total = total; }

        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    }

    public static class OrderDetailDto {
        private String orderId;
        private String userId;
        private String status;
        private List<CartItemDto> items;
        private String paymentMethod;
        private String shippingAddress;
        private String notes;
        private int subtotal;
        private int deliveryFee;
        private int total;
        private String createdAt;

        public OrderDetailDto() {}

        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public List<CartItemDto> getItems() { return items; }
        public void setItems(List<CartItemDto> items) { this.items = items; }

        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

        public String getShippingAddress() { return shippingAddress; }
        public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }

        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }

        public int getSubtotal() { return subtotal; }
        public void setSubtotal(int subtotal) { this.subtotal = subtotal; }

        public int getDeliveryFee() { return deliveryFee; }
        public void setDeliveryFee(int deliveryFee) { this.deliveryFee = deliveryFee; }

        public int getTotal() { return total; }
        public void setTotal(int total) { this.total = total; }

        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    }
}
