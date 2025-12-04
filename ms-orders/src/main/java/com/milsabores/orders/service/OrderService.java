package com.milsabores.orders.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.milsabores.orders.dto.OrderDtos;
import com.milsabores.orders.model.OrderEntity;
import com.milsabores.orders.model.OrderItemEntity;
import com.milsabores.orders.repository.OrderItemRepository;
import com.milsabores.orders.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PromotionService promotionService;

    /**
     * Crea una orden nueva para el usuario a partir del payload recibido.
     * El userId viene resuelto desde el JWT o headers desde el controlador.
     * También recibimos email, birthDate y registrationCode para evaluar promos.
     */
    @Transactional
    public OrderDtos.OrderResponse createOrder(
            String userId,
            String email,
            LocalDate birthDate,
            String registrationCode,
            OrderDtos.CreateOrderRequest request
    ) {

        // 1) Calcular subtotal (suma de unitPrice * quantity)
        int subtotal = request.getItems().stream()
                .mapToInt(i -> i.getUnitPrice() * i.getQuantity())
                .sum();

        // 2) Evaluar promoción según datos del usuario + cupón manual del checkout
        PromotionService.AppliedPromotion promo =
                promotionService.evaluatePromotion(
                        email,
                        birthDate,
                        registrationCode,
                        request.getDiscountCode(), // ⬅️ NUEVO: cupón escrito en /pedido
                        LocalDate.now()
                );

        int discountAmount = 0;
        String discountCode = null;
        String discountDescription = null;

        if (promo != null) {
            discountAmount = subtotal * promo.percentage() / 100;
            if (discountAmount > subtotal) {
                discountAmount = subtotal; // por seguridad
            }
            discountCode = promo.code();
            discountDescription = promo.description();
        }

        int totalAmount = subtotal - discountAmount;

        // 3) Construir la entidad OrderEntity
        OrderEntity order = OrderEntity.builder()
                .userId(userId)
                .status("CREATED") // se podría cambiar a PAID luego de confirmar pago
                .subtotalAmount(subtotal)
                .discountAmount(discountAmount)
                .discountCode(discountCode)
                .discountDescription(discountDescription)
                .totalAmount(totalAmount)
                .paymentMethod(request.getPaymentMethod())
                .shippingAddress(request.getShippingAddress())
                .build();

        // 4) Mapear ítems de request → entidades
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

    /**
     * Lista las últimas órdenes creadas en todo el sistema (global, no por usuario),
     * ordenadas desde la más reciente.
     * Pensado para el dashboard de administración.
     */
    @Transactional(readOnly = true)
    public List<OrderDtos.OrderResponse> listLatestOrders(int limit) {
        int size = Math.max(1, Math.min(limit, 50)); // protegemos el tamaño [1..50]

        Pageable pageable = PageRequest.of(
                0,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return orderRepository.findAll(pageable)
                .stream()
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
                .subtotalAmount(order.getSubtotalAmount())
                .discountAmount(order.getDiscountAmount())
                .discountCode(order.getDiscountCode())
                .discountDescription(order.getDiscountDescription())
                .totalAmount(order.getTotalAmount())
                .paymentMethod(order.getPaymentMethod())
                .shippingAddress(order.getShippingAddress())
                .createdAt(order.getCreatedAt())
                .items(items)
                .build();
    }
}
