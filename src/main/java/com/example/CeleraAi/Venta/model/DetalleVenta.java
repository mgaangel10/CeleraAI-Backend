package com.example.CeleraAi.Venta.model;

import com.example.CeleraAi.Producto.model.Producto;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class DetalleVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private double total;

    @ManyToOne
    @JsonIgnore
    private Venta venta;

    @ManyToOne
    private Producto prodcuto;

    private int cantidad;
}
