package com.example.CeleraAi.OpenAi.models;

import com.example.CeleraAi.users.model.Usuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegistroAccionIA {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String accion;
    private String resultado;
    private LocalDateTime fecha;

    @ManyToOne
    private Usuario usuario;

}
