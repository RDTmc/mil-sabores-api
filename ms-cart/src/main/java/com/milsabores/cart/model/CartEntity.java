package com.milsabores.cart.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    /**
     * ID del usuario dueño del carrito.
     *
     * Se alinea con el claim "sub" del JWT emitido por ms-usuarios.
     * En Supabase está mapeado a la columna:
     *   public.carts.user_id (character varying NOT NULL)
     */
    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;

    /**
     * Estado del carrito:
     *  - ACTIVE: carrito en uso
     *  - CHECKED_OUT: ya convertido en orden
     *  - ABANDONED: opcional para futuro
     *
     * Columna: public.carts.status
     */
    @Column(name = "status", nullable = false, length = 30)
    private String status;

    /**
     * Timestamps de auditoría.
     * En Supabase:
     *   created_at timestamp without time zone NOT NULL
     *   updated_at timestamp without time zone NOT NULL
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Relación 1 → N con cart_items.
     * mappedBy = "cart" coincide con CartItemEntity.cart
     */
    @OneToMany(
            mappedBy = "cart",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<CartItemEntity> items = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
        if (status == null) status = "ACTIVE";
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
