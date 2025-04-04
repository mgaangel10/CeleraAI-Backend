package com.example.CeleraAi.Facturacion.model;

import com.example.CeleraAi.Negocio.model.Negocio;
import com.example.CeleraAi.Venta.model.Venta;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Factura {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private String NombreEmpresa;
    private String cid;
    private int numeroAlbaran;
    private String numeroFactura;
    private LocalDate fecha;
    private String cliente;
    private double subtotal;
    private double impuestos;
    private double total;

    @ManyToOne
    private Venta ventas;

    @ManyToOne
    @JsonIgnore
    private Negocio negocio;
}
