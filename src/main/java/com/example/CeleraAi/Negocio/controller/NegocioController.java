package com.example.CeleraAi.Negocio.controller;

import com.example.CeleraAi.Negocio.Dto.CrearNegocioDto;
import com.example.CeleraAi.Negocio.Dto.NegocioDto;
import com.example.CeleraAi.Negocio.Dto.ResumenNegocioDto;
import com.example.CeleraAi.Negocio.service.NegocioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class NegocioController {
    private final NegocioService negocioService;

    @PostMapping("usuario/crear/negocio")
    public ResponseEntity<NegocioDto> crearNegocio(@RequestBody CrearNegocioDto crearNegocioDto){
        NegocioDto negocioDto = negocioService.crearNegocio(crearNegocioDto);
        return ResponseEntity.status(201).body(negocioDto);
    }
    @GetMapping("usuario/ver/negocios/usuarios")
    public ResponseEntity<List<NegocioDto>> verNegociosDeLosUsuarios(){
        List<NegocioDto> negocioDtos = negocioService.verLosNegociosDelUsuario();
        return ResponseEntity.ok(negocioDtos);
    }

    @GetMapping("usuario/negocio/{id}")
    public ResponseEntity<NegocioDto> negocioId(@PathVariable UUID id){
        NegocioDto negocioDto = negocioService.findById(id);
        return ResponseEntity.ok(negocioDto);
    }
    @GetMapping("usuario/resumen/diario/{id}")
    public ResponseEntity<ResumenNegocioDto> resumenDIario(@PathVariable UUID id){
        ResumenNegocioDto negocioDto = negocioService.obtenerResumenDiario(id);
        return ResponseEntity.ok(negocioDto);
    }
}
