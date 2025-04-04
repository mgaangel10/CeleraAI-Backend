package com.example.CeleraAi.Facturacion.Dto;

import com.example.CeleraAi.Facturacion.model.Factura;
import com.example.CeleraAi.Venta.Dto.VentaDto;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public record FacturaDto(UUID id,
                         String numeroFactura,
                         String cliente,
                         double impuestos,
                         double total,
                         double subtotal,
                         String nombreEmpresa,
                         String cid,
                         int numeroAlbaran,

                         VentaDto ventaDto) {
    public static FacturaDto of(Factura f){
        return new FacturaDto(
                f.getId(),
                f.getNumeroFactura(),
                f.getCliente(),
                f.getImpuestos(),
                f.getTotal(),
                f.getSubtotal(),
                f.getNombreEmpresa(),
                f.getCid(),
                f.getNumeroAlbaran(),
                VentaDto.of(f.getVentas())
        );
    }
}
