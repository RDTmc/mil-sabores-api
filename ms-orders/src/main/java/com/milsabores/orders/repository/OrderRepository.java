package com.milsabores.orders.repository;

import com.milsabores.orders.model.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<OrderEntity, String> {

    /**
     * Listar todas las órdenes de un usuario, más recientes primero.
     * Útil para la página "Mis compras" en React.
     */
    List<OrderEntity> findByUserIdOrderByCreatedAtDesc(String userId);
}
