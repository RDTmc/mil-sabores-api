package com.milsabores.usuarios.repository;

import com.milsabores.usuarios.model.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<UsuarioEntity, String> {

    Optional<UsuarioEntity> findByEmail(String email);

    boolean existsByEmail(String email);
}
