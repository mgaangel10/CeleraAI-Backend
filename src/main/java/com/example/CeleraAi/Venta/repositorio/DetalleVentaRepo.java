package com.example.CeleraAi.Venta.repositorio;

import com.example.CeleraAi.Venta.model.DetalleVenta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DetalleVentaRepo extends JpaRepository<DetalleVenta, UUID> {
}
