package com.example.CeleraAi.Negocio.repositorio;

import com.example.CeleraAi.Negocio.model.Categorias;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CategoriasRepo extends JpaRepository<Categorias, UUID> {
    Optional<Categorias> findByNombre(String nombre);
}
