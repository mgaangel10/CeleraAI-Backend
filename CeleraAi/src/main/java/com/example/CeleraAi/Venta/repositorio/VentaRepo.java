package com.example.CeleraAi.Venta.repositorio;

import com.example.CeleraAi.Venta.model.Venta;
import org.apache.catalina.Lifecycle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VentaRepo extends JpaRepository<Venta, UUID> {
    Optional<Venta> findByActivoTrue();

    @Query("SELECT COALESCE(SUM(v.totalVenta), 0) FROM Venta v WHERE CAST(v.fecha AS DATE) = :fecha AND v.activo = false")
    Double calcularTotalVentasDelDia(@Param("fecha") LocalDate fecha);

    List<Venta> findByNegocioId(UUID id);



}
