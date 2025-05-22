package com.example.CeleraAi.Venta.Dto;

import org.apache.catalina.Lifecycle;

import java.util.List;

public record VentasPorFechaDTO (String fecha,
                                 List<VentaDto> ventaDtos){

    public static VentasPorFechaDTO of(String fecha, List<VentaDto> ventaDtos){
        return new VentasPorFechaDTO(
                fecha,
                ventaDtos
        );
    }
}
