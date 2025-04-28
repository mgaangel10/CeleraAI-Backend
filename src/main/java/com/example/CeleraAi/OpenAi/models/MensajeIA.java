package com.example.CeleraAi.OpenAi.models;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MensajeIA {
    private String role;    // "user" o "assistant"
    private String content; // mensaje de texto
}