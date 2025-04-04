package com.example.CeleraAi.OpenAi.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccionIA {
    private String accion;
    private String nombre;
    private String producto;
    private Double precio;
    private Double precioProveedor;
    private Integer stock;
    private List<String> productos;
    private Integer descuento;
    private String categoria;
    // Puedes meter más si luego los necesitas
}

