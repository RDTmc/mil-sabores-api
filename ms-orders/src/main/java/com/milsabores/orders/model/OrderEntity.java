package com.milsabores.orders.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    /**
     * Usuario dueño de la orden.
     * Debe venir del JWT (ms-usuarios) cuando creemos la orden.
     */
    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;

    /**
     * Estado de la orden:
     *  - CREATED
     *  - PAID
     *  - CANCELLED
     *  - etc.
     */
    @Column(name = "status", nullable = false, length = 30)
    private String status;

    /**
     * Monto total (en pesos, entero).
     */
    @Column(name = "total_amount", nullable = false)
    private Integer totalAmount;

    /**
     * Método de pago (ej: "CARD", "TRANSFER", "CASH").
     */
    @Column(name = "payment_method", nullable = false, length = 30)
    private String paymentMethod;

    /**
     * Dirección de envío / retiro.
     */
    @Column(name = "shipping_address", nullable = false, length = 500)
    private String shippingAddress;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<OrderItemEntity> items = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
        if (status == null) status = "CREATED";
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
