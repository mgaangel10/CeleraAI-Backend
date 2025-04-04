package com.example.CeleraAi.Negocio.Dto;

import com.example.CeleraAi.Facturacion.Dto.FacturaDto;
import com.example.CeleraAi.Negocio.model.Negocio;
import com.example.CeleraAi.Producto.Dto.ProductoDto;
import com.example.CeleraAi.Venta.Dto.VentaDto;
import org.apache.catalina.Lifecycle;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public record NegocioDto(UUID id,
                         String nombre,
                         String categoria,
                         int numeroEmpleados,
                         String telefono,
                         String email,
                         String ciudad,
                         String pais,
                         String sitioweb,
                         List<ProductoDto> productoDtos,
                         List<VentaDto> ventaDtos,
                         List<FacturaDto> facturaDtos) {
    public static NegocioDto of(Negocio n){
        return new NegocioDto(
                n.getId(),
                n.getNombre(),
                n.getCategorias().getNombre(),
                n.getNumeroEmpleados(),
                n.getTelefono(),
                n.getEmail(),
                n.getCiudad(),
                n.getPais(),
                n.getSitioweb(),
                n.getProdcutos() == null ? null : n.getProdcutos().stream().map(ProductoDto::of).collect(Collectors.toList()),
                n.getVentas() == null ? null : n.getVentas().stream().map(VentaDto::of).collect(Collectors.toList()),
                n.getFacturas() == null ? null : n.getFacturas().stream().map(FacturaDto::of).collect(Collectors.toList())
        );
    }
}
