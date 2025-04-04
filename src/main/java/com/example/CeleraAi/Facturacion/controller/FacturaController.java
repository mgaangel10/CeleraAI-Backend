package com.example.CeleraAi.Facturacion.controller;

import com.example.CeleraAi.Facturacion.Dto.CrearFacturaDto;
import com.example.CeleraAi.Facturacion.Dto.FacturaDto;
import com.example.CeleraAi.Facturacion.service.FacturaService;
import com.example.CeleraAi.Venta.model.Venta;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.Lifecycle;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class FacturaController {

    private final FacturaService facturaService;

    @PostMapping("usuario/crear/factura/{id}")
    public ResponseEntity<FacturaDto> crearFactura(@PathVariable UUID id,@RequestBody CrearFacturaDto crearFacturaDto){
        FacturaDto facturaDto = facturaService.crearFactura(id,crearFacturaDto);
        return ResponseEntity.status(201).body(facturaDto);
    }

    @GetMapping("usuario/ver/facturas/{id}")
    public ResponseEntity<List<FacturaDto>> verFacturas(@PathVariable UUID id){
        List<FacturaDto> facturaDtos = facturaService.verFacturas(id);
        return ResponseEntity.ok(facturaDtos);
    }
    @GetMapping("usuario/ver/detalles/de/factura/{id}")
    public ResponseEntity<FacturaDto> verFactura(@PathVariable UUID id){
        FacturaDto facturaDtos = facturaService.verFactura(id);
        return ResponseEntity.ok(facturaDtos);
    }

}
