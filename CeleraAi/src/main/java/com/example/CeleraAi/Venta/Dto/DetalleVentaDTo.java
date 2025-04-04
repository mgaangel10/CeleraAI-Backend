package com.example.CeleraAi.Venta.Dto;

import com.example.CeleraAi.Venta.model.DetalleVenta;

import java.util.UUID;

public record DetalleVentaDTo(UUID id,
                              String nombreProducto,
                              double precioProducto,
                              int cantida) {

    public static DetalleVentaDTo of(DetalleVenta d){
        return new DetalleVentaDTo(
                d.getId(),
                d.getProdcuto().getNombre(),
                d.getProdcuto().getPrecio(),
                d.getCantidad()
        );
    }
}
