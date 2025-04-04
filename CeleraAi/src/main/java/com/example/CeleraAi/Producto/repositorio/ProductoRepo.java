package com.example.CeleraAi.Producto.repositorio;

import com.example.CeleraAi.Producto.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductoRepo extends JpaRepository<Producto, UUID> {
}
