package com.example.CeleraAi.Finanzas.Dto;

import com.example.CeleraAi.Finanzas.model.Finanzas;
import com.example.CeleraAi.Venta.Dto.VentaDto;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public record FinanzasDto(UUID id,
                          double beneficio,
                          double gasto,
                          List<VentaDto> ventaDtos) {
    public static FinanzasDto of(Finanzas f){
        return new FinanzasDto(
                f.getId(),
                f.getBeneficio(),
                f.getGastos(),
                f.getVentas().stream().map(VentaDto::of).collect(Collectors.toList())
        );
    }
}
