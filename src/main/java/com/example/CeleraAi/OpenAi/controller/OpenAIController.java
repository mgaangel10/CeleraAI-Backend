package com.example.CeleraAi.OpenAi.controller;

import com.example.CeleraAi.OpenAi.PreguntaUsuarioDto;
import com.example.CeleraAi.OpenAi.models.Recomendaciones;
import com.example.CeleraAi.OpenAi.models.RecomendacionesRequest;
import com.example.CeleraAi.OpenAi.service.OpenAIService;
import com.example.CeleraAi.OpenAi.service.RecomendacionesService;
import com.example.CeleraAi.Producto.model.Producto;
import com.example.CeleraAi.Venta.model.Venta;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class OpenAIController {

    private final OpenAIService openAIService;
    private final RecomendacionesService recomendacionesService;






    @PostMapping("usuario/generarRecomendaciones/{id}")
    public ResponseEntity<String> generarRecomendacionConIA(@RequestBody PreguntaUsuarioDto preguntaUSuario, @PathVariable UUID id) throws JsonProcessingException {
        String resultado = openAIService.generarRecomendacionConIA(preguntaUSuario,id);
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("usuario/alertas/{id}")
    public ResponseEntity<List<String>> alertas(@PathVariable UUID id){
        List<String> alertas = openAIService.generarAlertas(id);
        return ResponseEntity.ok(alertas);
    }
}
