package com.example.CeleraAi.Producto.Dto;

public record CrearProductoDto(String nombre,
                               double precio,
                               int stock,
                               double precioProveedor) {
}
