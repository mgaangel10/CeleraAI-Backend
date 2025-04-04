package com.example.CeleraAi.Venta.Dto;

import com.example.CeleraAi.Venta.model.DetalleVenta;
import com.example.CeleraAi.Venta.model.Venta;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public record VentaDto(UUID id,
                       List<DetalleVentaDTo> detalleVentas,
                       LocalDate fecha,
                       double total,
                       String metodoPago,
                       boolean activo,
                       boolean terminado,
                       boolean factura) {
    public static VentaDto of(Venta v) {
        if (v == null) {
            // Devuelve un DTO vacío o un DTO con valores predeterminados en caso de que la venta sea null
            return new VentaDto(
                    null, // id es null
                    List.of(), // Lista vacía de detalleVentas
                    null, // fecha es null
                    0.0, // total es 0
                    "", // metodoPago es vacío
                    false, // activo es false
                    false, // terminado es false
                    false // factura es false
            );
        }

        return new VentaDto(
                v.getId(),
                v.getDetalleVentas().stream().map(DetalleVentaDTo::of).collect(Collectors.toList()),
                v.getFecha(),
                v.getTotalVenta(),
                v.getMetodoPago(),
                v.isActivo(),
                v.isTerminado(),
                v.isTieneFactura()
        );
    }

}
