package com.example.CeleraAi.users.repositorio;

import com.example.CeleraAi.users.model.Administrador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AdministradorRepo extends JpaRepository<Administrador, UUID> {
    Optional<Administrador> findFirstByEmail(String email);
    boolean existsByEmailIgnoreCase(String email);

    Optional<Administrador> findByEmailIgnoreCase(String nombre);
}
