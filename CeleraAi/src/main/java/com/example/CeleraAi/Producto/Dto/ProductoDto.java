package com.example.CeleraAi.Producto.Dto;

import com.example.CeleraAi.Producto.model.Producto;

import java.util.UUID;

public record ProductoDto(UUID id,
                          String nombre,
                          double precio,
                          int stock,
                          double precioProveedor) {
    public static ProductoDto of(Producto p){
        return new ProductoDto(
                p.getId(),
                p.getNombre(),
                p.getPrecio(),
                p.getStock(),
                p.getPrecioProveedor()
        );
    }
}
