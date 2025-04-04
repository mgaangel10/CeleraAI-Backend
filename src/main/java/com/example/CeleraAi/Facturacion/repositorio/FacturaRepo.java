package com.example.CeleraAi.Facturacion.repositorio;

import com.example.CeleraAi.Facturacion.model.Factura;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FacturaRepo extends JpaRepository<Factura, UUID> {
}
