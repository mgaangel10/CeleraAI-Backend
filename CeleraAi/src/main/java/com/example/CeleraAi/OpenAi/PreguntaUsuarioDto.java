package com.example.CeleraAi.OpenAi;

import com.example.CeleraAi.OpenAi.models.AccionIA;

public record PreguntaUsuarioDto(String pregunta,
                                 boolean confirmarAccion,
                                 AccionIA accion) {
}
