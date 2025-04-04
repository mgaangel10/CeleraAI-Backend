package com.example.CeleraAi.Negocio.repositorio;

import com.example.CeleraAi.Negocio.model.Negocio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NegocioRepo extends JpaRepository<Negocio, UUID> {
}
