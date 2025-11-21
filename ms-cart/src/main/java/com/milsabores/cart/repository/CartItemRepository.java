package com.milsabores.cart.repository;

import com.milsabores.cart.model.CartItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartItemRepository extends JpaRepository<CartItemEntity, String> {

    /**
     * Listar ítems por ID de carrito.
     * Útil si necesitas operar directo sobre items sin cargar todo el carrito.
     */
    List<CartItemEntity> findByCart_Id(String cartId);

    /**
     * Vaciar el carrito por ID (DELETE FROM cart_items WHERE cart_id = ...).
     */
    void deleteByCart_Id(String cartId);
}
