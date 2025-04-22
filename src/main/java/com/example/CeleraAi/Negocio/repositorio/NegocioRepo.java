package com.example.CeleraAi.Negocio.repositorio;

import com.example.CeleraAi.Negocio.model.Negocio;
import com.example.CeleraAi.Venta.model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NegocioRepo extends JpaRepository<Negocio, UUID> {
    @Query("SELECT n FROM Negocio n LEFT JOIN FETCH n.facturas WHERE n.id = :id")
    Optional<Negocio> cargarConFacturas(@Param("id") UUID id);

    @Query("SELECT n FROM Negocio n LEFT JOIN FETCH n.prodcutos WHERE n.id = :id")
    Optional<Negocio> cargarConProductos(@Param("id") UUID id);

    @Query("SELECT n FROM Negocio n LEFT JOIN FETCH n.ventas WHERE n.id = :id")
    Optional<Negocio> cargarConVentas(@Param("id") UUID id);

    @Query("SELECT v FROM Venta v LEFT JOIN FETCH v.detalleVentas d LEFT JOIN FETCH d.prodcuto WHERE v.negocio.id = :idNegocio")
    List<Venta> findVentasConDetallesByNegocio(@Param("idNegocio") UUID idNegocio);
}
