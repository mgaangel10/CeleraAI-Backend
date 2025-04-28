package com.example.CeleraAi.OpenAi.models;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SugerenciaIA {
    private String tipo;
    private AccionIA accion; // Ojo: aquí "accion" es un objeto, no String
    // getters y setters
}
