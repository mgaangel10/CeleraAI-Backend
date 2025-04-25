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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class OpenAIController {

    private final OpenAIService openAIService;
    private final RecomendacionesService recomendacionesService;

    @PostMapping("usuario/generarTexto")
    public String generarTexto(@RequestBody String mensajeUsuario) {
        return openAIService.generarTextoConIA(mensajeUsuario);
    }

    @PostMapping("usuario/ia/spring")
    public ResponseEntity<String> recibirDesdePython(@RequestBody String mensajeUsuario) {
        String respuesta = openAIService.generarTextoConIA(mensajeUsuario);
        return ResponseEntity.ok(respuesta);
    }


    @PostMapping("usuario/generarRecomendaciones/{id}")
    public ResponseEntity<String> generarRecomendaciones(@RequestBody PreguntaUsuarioDto preguntaUSuario, @PathVariable UUID id) throws JsonProcessingException {
        String resultado = openAIService.generarRecomendaciones(preguntaUSuario,id);
        return ResponseEntity.ok(resultado);
    }

}
