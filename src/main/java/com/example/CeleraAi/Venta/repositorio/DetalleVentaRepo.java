package com.example.CeleraAi.Venta.repositorio;

import com.example.CeleraAi.Negocio.model.Negocio;
import com.example.CeleraAi.Venta.model.DetalleVenta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface DetalleVentaRepo extends JpaRepository<DetalleVenta, UUID> {
    List<DetalleVenta> findByVenta_NegocioAndVenta_FechaAfter(Negocio negocio, LocalDate fecha);

}
