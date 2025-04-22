package com.example.CeleraAi.Negocio.Dto;



public record ResumenNegocioDto(
        double totalHoy,
        long ventasHoy,
        String topProducto,
        long cantidadTopProducto,
        String productoStockBajo,
        int stockRestante,
        Double caidaVentasPorcentual,
        double previsionSiguienteSemana
) {}