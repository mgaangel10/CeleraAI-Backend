package com.example.CeleraAi.Venta.controller;

import com.example.CeleraAi.Producto.Dto.ProductoDto;
import com.example.CeleraAi.Venta.Dto.CrearVentaDto;
import com.example.CeleraAi.Venta.Dto.FiltrarVentaPorFechaDTo;
import com.example.CeleraAi.Venta.Dto.VentaDto;
import com.example.CeleraAi.Venta.Dto.VentasPorFechaDTO;
import com.example.CeleraAi.Venta.model.Venta;
import com.example.CeleraAi.Venta.service.VentaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class VentaController {
    private final VentaService ventaService;

    @PostMapping("usuario/add/venta/{id}")
    public ResponseEntity<VentaDto> addVenta(@RequestBody CrearVentaDto crearVentaDto, @PathVariable UUID id){
        VentaDto ventaDto = ventaService.crearVenta(crearVentaDto, id);
        return ResponseEntity.status(201).body(ventaDto);
    }

    @PostMapping("usuario/agregar/producto/venta/{id}")
    public ResponseEntity<VentaDto> agregarProductoVenta(@PathVariable UUID id){
        VentaDto ventaDto = ventaService.agregarProductoAVenta(id);
        return ResponseEntity.status(201).body(ventaDto);
    }

    @PostMapping("usuario/terminar/venta/{id}")
    public ResponseEntity<VentaDto> terminarVenta(@PathVariable UUID id){
        VentaDto ventaDto = ventaService.terminarVenta(id);
        return ResponseEntity.status(201).body(ventaDto);
    }

    @GetMapping("usuario/ver/total/{fecha}")
    public ResponseEntity<Double> ObtenerElTotalVenta(@PathVariable LocalDate fecha){
        double total = ventaService.obtenerTotalVentasDelDia(fecha);
        return ResponseEntity.ok(total);
    }

    @GetMapping("usuario/ventas/semana/{id}")
    public ResponseEntity<Map<String, Object>> ventasSemanales(@PathVariable UUID id) {
        Map<String, Object> respuesta = ventaService.ventasSemana(id);
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("usuario/ventas/mes/{id}")
    public ResponseEntity<Map<String, Object>> ventasMes(@PathVariable UUID id) {
        Map<String, Object> respuesta = ventaService.ventasMes(id);
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("usuario/ventas/totales/semanas/{id}")
    public ResponseEntity<List<VentaDto>> ventasSemanal(@PathVariable UUID id){
        List<VentaDto> ventaDtos = ventaService.ventasSemanales(id);
        return ResponseEntity.ok(ventaDtos);
    }

    @GetMapping("usuario/ver/venta/actual/{id}")
    public ResponseEntity<VentaDto> ventaActual(@PathVariable UUID id){
        VentaDto ventaDtos = ventaService.verVentasActivas(id);
        return ResponseEntity.ok(ventaDtos);
    }

    @GetMapping("usuario/ver/ventas/sin/factura/{id}")
    public ResponseEntity<List<VentaDto>> ventasSInFacturas(@PathVariable UUID id){
        List<VentaDto> ventaDtos = ventaService.verVentasSinFacturas(id);
        return ResponseEntity.ok(ventaDtos);
    }

    @GetMapping("usuario/ver/ventas/ya/facturas/{id}")
    public ResponseEntity<List<VentaDto>> ventasFacturadasa(@PathVariable UUID id){
        List<VentaDto> ventaDtos = ventaService.verVentasSinFacturas(id);
        return ResponseEntity.ok(ventaDtos);
    }

    @GetMapping("usuario/ver/todas/ventas/{id}")
    public ResponseEntity<List<VentaDto>> verTodasVentas(@PathVariable UUID id){
        List<VentaDto> ventaDtos = ventaService.verTodasVentas(id);
        return ResponseEntity.ok(ventaDtos);
    }

    @PostMapping("usuario/añadir/mas/productos/ventas/{idV}/{idP}")
    public ResponseEntity<VentaDto> verTodasVentas(@PathVariable UUID idV,@PathVariable UUID idP){
        VentaDto ventaDtos = ventaService.editarVenta(idV, idP);
        return ResponseEntity.status(201).body(ventaDtos);
    }
//whwhw

    @GetMapping("usuario/filtrar/ventas/date/{id}")
    public ResponseEntity<List<VentaDto>> getVetasFecha(@RequestBody FiltrarVentaPorFechaDTo filtrarVentaPorFechaDTo, @PathVariable UUID id){
        List<VentaDto> ventaDtos = ventaService.getVentasByFecha(filtrarVentaPorFechaDTo,id);
        return ResponseEntity.ok(ventaDtos);
    }

    @GetMapping("usuario/filtrar/ventas/factura/{id}")
    public ResponseEntity<List<VentaDto>> getVentasFactura(@PathVariable UUID id){
        List<VentaDto> ventaDtos = ventaService.getVentasConFactura(id);
        return ResponseEntity.ok(ventaDtos);
    }

    @GetMapping("usuario/filtrar/ventas/editable/{id}")
    public ResponseEntity<List<VentaDto>> getVentasNoTerminadas(@PathVariable UUID id){
        List<VentaDto> ventaDtos = ventaService.getVentasNoTerminadas(id);
        return ResponseEntity.ok(ventaDtos);
    }

    @GetMapping("usuario/filtrar/ventas/total/{id}")
    public ResponseEntity<List<VentaDto>> getVentasTotalVenta(@PathVariable UUID id){
        List<VentaDto> ventaDtos = ventaService.getVentasPorMayorTotalVenta(id);
        return ResponseEntity.ok(ventaDtos);
    }

    @DeleteMapping("usuario/eliminar/detalle/{id}")
    public void eliminarDetallesVenta(@PathVariable UUID id){
        ventaService.eliminarDetalleVenta(id);

    }

    @PostMapping("usuario/quitar/cantidad/{id}")
    public ResponseEntity<VentaDto> quitarCantidad(@PathVariable UUID id){
        VentaDto ventaDto = ventaService.quitarUnaCntidad(id);
        return ResponseEntity.status(201).body(ventaDto);
    }

    @PostMapping("usuario/agregar/cantidad/{id}")
    public ResponseEntity<VentaDto> agregarCantidad(@PathVariable UUID id){
        VentaDto ventaDto = ventaService.agregarCantidad(id);
        return ResponseEntity.status(201).body(ventaDto);
    }

    @GetMapping("usuario/ver/ingresos/total/{id}")
    public ResponseEntity<Double> ingresos(@PathVariable UUID id){
        double ventaDtos = ventaService.ingresos(id);
        return ResponseEntity.ok(ventaDtos);
    }

    @GetMapping("usuario/ver/gastos/total/{id}")
    public ResponseEntity<Double> gastos(@PathVariable UUID id){
        double ventaDtos = ventaService.gastos(id);
        return ResponseEntity.ok(ventaDtos);
    }

    @GetMapping("usuario/ver/beneficio/total/{id}")
    public ResponseEntity<Double> beneficio(@PathVariable UUID id){
        double ventaDtos = ventaService.beneficio(id);
        return ResponseEntity.ok(ventaDtos);
    }

    @GetMapping("usuario/ver/detalles/venta/{id}")
    public ResponseEntity<VentaDto> verDetallesVenta(@PathVariable UUID id){
        VentaDto ventaDtos = ventaService.detallesVenta(id);
        return ResponseEntity.ok(ventaDtos);
    }

    @GetMapping("usuario/ventas/hoy/{id}")
    public ResponseEntity<List<VentaDto>> verVentasHoy(@PathVariable UUID id){
        List<VentaDto> ventaDtos = ventaService.verVentasHoy(id);
        return ResponseEntity.ok(ventaDtos);
    }

    @GetMapping("usuario/historico/ventas/{id}")
    public ResponseEntity<List<VentasPorFechaDTO>> historicoVentas(@PathVariable UUID id){
        List<VentasPorFechaDTO> ventasPorFechaDTOS = ventaService.historicoVentas(id);
        return ResponseEntity.ok(ventasPorFechaDTOS);
    }







}
