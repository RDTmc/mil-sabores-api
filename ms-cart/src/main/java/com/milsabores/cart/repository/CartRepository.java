package com.milsabores.cart.repository;

import com.milsabores.cart.model.CartEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<CartEntity, String> {

    /**
     * Busca el carrito ACTIVO de un usuario.
     * Se usará para:
     *  - obtener el carrito actual
     *  - crear uno nuevo si no existe
     */
    Optional<CartEntity> findByUserIdAndStatus(String userId, String status);

    /**
     * Por si en el futuro quieres listar históricos (CHECKED_OUT, ABANDONED, etc.).
     */
    List<CartEntity> findByUserIdOrderByCreatedAtDesc(String userId);
}
