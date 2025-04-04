package com.example.CeleraAi.Negocio.Dto;

import com.example.CeleraAi.Negocio.model.Categorias;

import java.util.UUID;

public record CategoriasDto(UUID id,
                            String nombre) {
    public static CategoriasDto of (Categorias c){
        return new CategoriasDto(c.getId(),
                c.getNombre());
    }
}
