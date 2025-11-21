package com.milsabores.orders.service;

import com.milsabores.orders.dto.OrderDtos;
import com.milsabores.orders.model.OrderEntity;
import com.milsabores.orders.model.OrderItemEntity;
import com.milsabores.orders.repository.OrderItemRepository;
import com.milsabores.orders.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    /**
     * Crea una orden nueva para el usuario a partir del payload recibido.
     * Por ahora asumimos que el frontend manda el snapshot de los ítems
     * (normalmente el contenido del carrito).
     */
    @Transactional
    public OrderDtos.OrderResponse createOrder(String userId, OrderDtos.CreateOrderRequest request) {

        // Calcular total (suma de unitPrice * quantity)
        int totalAmount = request.getItems().stream()
                .mapToInt(i -> i.getUnitPrice() * i.getQuantity())
                .sum();

        OrderEntity order = OrderEntity.builder()
                .userId(userId)
                .status("CREATED") // se podría cambiar a PAID luego de confirmar pago
                .totalAmount(totalAmount)
                .paymentMethod(request.getPaymentMethod())
                .shippingAddress(request.getShippingAddress())
                .build();

        // Mapear ítems de request → entidades
        List<OrderItemEntity> items = request.getItems().stream()
                .map(reqItem -> OrderItemEntity.builder()
                        .order(order)
                        .productId(reqItem.getProductId())
                        .productName(reqItem.getProductName())
                        .image(reqItem.getImage())
                        .unitPrice(reqItem.getUnitPrice())
                        .quantity(reqItem.getQuantity())
                        .size(reqItem.getSize())
                        .flavor(reqItem.getFlavor())
                        .build()
                )
                .toList();

        order.setItems(items);

        OrderEntity saved = orderRepository.save(order);

        return toDto(saved);
    }

    /**
     * Obtener una orden específica del usuario.
     */
    @Transactional(readOnly = true)
    public OrderDtos.OrderResponse getOrder(String userId, String orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada"));

        if (!order.getUserId().equals(userId)) {
            throw new IllegalArgumentException("No tienes permiso para ver esta orden");
        }

        return toDto(order);
    }

    /**
     * Listar todas las órdenes de un usuario (más recientes primero).
     */
    @Transactional(readOnly = true)
    public List<OrderDtos.OrderResponse> listOrders(String userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toDto)
                .toList();
    }

    // ================= Helpers =================

    private OrderDtos.OrderResponse toDto(OrderEntity order) {
        List<OrderDtos.OrderItemResponse> items = order.getItems().stream()
                .map(i -> OrderDtos.OrderItemResponse.builder()
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

        return OrderDtos.OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .paymentMethod(order.getPaymentMethod())
                .shippingAddress(order.getShippingAddress())
                .createdAt(order.getCreatedAt())
                .items(items)
                .build();
    }
}
