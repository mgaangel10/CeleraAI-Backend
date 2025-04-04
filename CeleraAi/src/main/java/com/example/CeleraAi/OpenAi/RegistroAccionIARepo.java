package com.example.CeleraAi.OpenAi;

import com.example.CeleraAi.OpenAi.models.RegistroAccionIA;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RegistroAccionIARepo extends JpaRepository<RegistroAccionIA, UUID> {
}
