package com.example.CeleraAi.OpenAi.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccionIA {
    private String accion;
    private String nombre;
    private String producto;
    private Double precio;
    private Double precioProveedor;
    private Integer stock;
    private List<ProductoStockUpdate> productos;
    private Integer descuento;
    private String categoria;
    private Map<String, Object> datos; // para cualquier estructura dinámica

    // Puedes meter más si luego los necesitas
}

