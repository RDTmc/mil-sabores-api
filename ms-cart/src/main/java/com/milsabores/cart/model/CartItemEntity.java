package com.milsabores.cart.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cart_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    /**
     * Relación muchos-a-uno con CartEntity.
     * Mapea la FK:
     *   public.cart_items.cart_id → public.carts.id
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private CartEntity cart;

    /**
     * Snapshot del producto al momento de agregarlo al carrito.
     */
    @Column(name = "product_id", nullable = false, length = 100)
    private String productId;

    @Column(name = "product_name", nullable = false, length = 255)
    private String productName;

    @Column(name = "image", length = 500)
    private String image;

    @Column(name = "unit_price", nullable = false)
    private Integer unitPrice;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "size", length = 50)
    private String size;

    @Column(name = "flavor", length = 100)
    private String flavor;
}
