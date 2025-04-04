package com.example.CeleraAi.Producto.model;

import com.example.CeleraAi.Negocio.model.Negocio;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@ToString(exclude = {"categorias", "productos"})  // Evita recursión infinita

public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private String nombre;
    private boolean disponible;
    private double precio;
    private int stock;
    private double precioProveedor;

    @ManyToOne
    @JsonIgnore
    private Negocio negocio;


}
