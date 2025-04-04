package com.example.CeleraAi.Finanzas.repositorio;

import com.example.CeleraAi.Finanzas.model.Finanzas;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FinanzasRepo extends JpaRepository<Finanzas, UUID> {
}
