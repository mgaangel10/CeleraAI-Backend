package com.example.CeleraAi.Producto.controller;

import com.example.CeleraAi.Producto.Dto.CrearProductoDto;
import com.example.CeleraAi.Producto.Dto.ProductoDto;
import com.example.CeleraAi.Producto.service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ProductoController {
    private final ProductoService productoService;

    @PostMapping("usuario/crear/producto/{idNegocio}")
    public ResponseEntity<ProductoDto> crearProducto(@RequestBody CrearProductoDto crearProductoDto, @PathVariable UUID idNegocio){
        ProductoDto productoDto = productoService.crearProducto(crearProductoDto,idNegocio);
        return ResponseEntity.status(201).body(productoDto);
    }

    @PostMapping("usuario/importar/productos/excel/{idNegocio}")
    public ResponseEntity<List<ProductoDto>> importarProductos(@RequestParam("file") MultipartFile file,
                                                               @PathVariable UUID idNegocio) {
        try {
            List<ProductoDto> productos = productoService.importarProductosDesdeExcel(file, idNegocio);
            return ResponseEntity.ok(productos);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @GetMapping("usuario/ver/productos/{id}")
    public ResponseEntity<List<ProductoDto>> verLosProductos(@PathVariable UUID id){
        List<ProductoDto> productoDtos = productoService.verTodosLosProductos(id);
        return ResponseEntity.ok(productoDtos);
    }

    @PostMapping("usuario/poner/no/disponible/{id}")
    public ResponseEntity<ProductoDto> ponerComoNoDisponible(@PathVariable UUID id){
        ProductoDto productoDto = productoService.ponerNoDisponible(id);
        return ResponseEntity.status(201).body(productoDto);
    }

    @GetMapping("usuario/productos/bajo/stock/{id}")
    public ResponseEntity<List<ProductoDto>> bajoStock(@PathVariable UUID id){
        List<ProductoDto> productoDtos = productoService.productosEnBajoStock(id);
        return ResponseEntity.ok(productoDtos);
    }

    @GetMapping("usuario/producto/mas/vendido/semana/{id}")
    public ResponseEntity<List<ProductoDto>> masVendidoSemana(@PathVariable UUID id){
        List<ProductoDto> productoDtos = productoService.masVendidos(id);
        return ResponseEntity.ok(productoDtos);
    }

    @GetMapping("usuario/producto/mas/vendido/mes/{id}")
    public ResponseEntity<List<ProductoDto>> masVendidoMes(@PathVariable UUID id){
        List<ProductoDto> productoDtos = productoService.masVendidosMes(id);
        return ResponseEntity.ok(productoDtos);
    }

    @GetMapping("usuario/producto/alfabeticamente/{id}")
    public ResponseEntity<List<ProductoDto>> ordenarAlfabeticamente(@PathVariable UUID id){
        List<ProductoDto> productoDtos = productoService.ordenarAlfabeticamente(id);
        return ResponseEntity.ok(productoDtos);
    }

    @GetMapping("usuario/producto/mayor/precio/{id}")
    public ResponseEntity<List<ProductoDto>> precioMasAlto(@PathVariable UUID id){
        List<ProductoDto> productoDtos = productoService.ordenarPorPrecioMasAlto(id);
        return ResponseEntity.ok(productoDtos);
    }

    @GetMapping("usuario/producto/mayor/stock/{id}")
    public ResponseEntity<List<ProductoDto>> mayorStock(@PathVariable UUID id){
        List<ProductoDto> productoDtos = productoService.ordenarPorMayorStock(id);
        return ResponseEntity.ok(productoDtos);
    }

    @GetMapping("usuario/producto/menor/stock/{id}")
    public ResponseEntity<List<ProductoDto>> menorStock(@PathVariable UUID id){
        List<ProductoDto> productoDtos = productoService.ordenarPorMenorStock(id);
        return ResponseEntity.ok(productoDtos);
    }

    @PostMapping("usuario/editar/producto/{id}")
    public ResponseEntity<ProductoDto> editarProducto(@PathVariable UUID id, @RequestBody CrearProductoDto crearProductoDto){
        ProductoDto productoDto = productoService.editarProdeucto(id, crearProductoDto);
        return ResponseEntity.status(201).body(productoDto);
    }


}
