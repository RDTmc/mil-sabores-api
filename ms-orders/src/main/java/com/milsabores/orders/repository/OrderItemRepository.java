package com.milsabores.orders.repository;

import com.milsabores.orders.model.OrderItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItemEntity, String> {

    /**
     * Listar ítems de una orden concreta.
     */
    List<OrderItemEntity> findByOrder_Id(String orderId);
}
