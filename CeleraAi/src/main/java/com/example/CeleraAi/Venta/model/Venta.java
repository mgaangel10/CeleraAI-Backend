package com.example.CeleraAi.Venta.model;

import com.example.CeleraAi.Facturacion.model.Factura;
import com.example.CeleraAi.Finanzas.model.Finanzas;
import com.example.CeleraAi.Negocio.model.Negocio;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@ToString(exclude = {"negocio", "detalleVentas"})  // Excluye las relaciones recursivas

public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private String metodoPago;
    private LocalDate fecha;
    private double totalVenta;
    private boolean activo;
    private boolean terminado;
    private boolean tieneFactura;

    @ManyToOne
    private Finanzas finanzas;

    @ManyToOne
    @JsonIgnore
    private Factura factura;

    @ManyToOne
    @JoinColumn(name = "negocio_id")
    @JsonIgnore
    private Negocio negocio;

    @OneToMany
    @JsonIgnore
    private List<DetalleVenta> detalleVentas;


}
