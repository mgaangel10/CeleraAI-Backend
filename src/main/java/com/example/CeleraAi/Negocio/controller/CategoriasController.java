package com.example.CeleraAi.Negocio.controller;

import com.example.CeleraAi.Negocio.Dto.CategoriasDto;
import com.example.CeleraAi.Negocio.Dto.CrearCategoriasDto;
import com.example.CeleraAi.Negocio.service.CategoriasService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CategoriasController {
    private final CategoriasService categoriasService;

    @PostMapping("administrador/crear/categorias")
    public ResponseEntity<CategoriasDto> crearCategorias(@RequestBody CrearCategoriasDto crearCategoriasDto){
        CategoriasDto categoriasDto = categoriasService.crearCategorias(crearCategoriasDto);
        return ResponseEntity.status(201).body(categoriasDto);
    }
}
