package com.example.CeleraAi.users.repositorio;

import com.example.CeleraAi.users.Dto.GetUsuario;
import com.example.CeleraAi.users.model.Usuario;
import org.hibernate.type.descriptor.converter.spi.JpaAttributeConverter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;


import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepo extends JpaRepository<Usuario, UUID> {
    Optional<Usuario> findFirstByEmail(String email);
    Usuario findByEmail(String email);
    boolean existsByEmailIgnoreCase(String email);
    List<Usuario> findByEnabledFalse();
    Optional<Usuario> findByEmailIgnoreCase(String nombre);
    List<Usuario> findByEnabledTrue();

    @Query("""
            select new com.example.CeleraAi.users.Dto.GetUsuario(
            u.id,
            u.username,
              u.name,
              u.lastName,
              u.phoneNumber,
              u.fotoUrl
            
            )
            from Usuario u
            where u.id = ?1
            """)
    GetUsuario getUsuario(UUID uuid);


}