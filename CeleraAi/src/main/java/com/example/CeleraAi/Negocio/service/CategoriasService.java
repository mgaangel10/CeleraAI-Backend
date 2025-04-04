package com.example.CeleraAi.Negocio.service;

import com.example.CeleraAi.Negocio.Dto.CategoriasDto;
import com.example.CeleraAi.Negocio.Dto.CrearCategoriasDto;
import com.example.CeleraAi.Negocio.Dto.CrearNegocioDto;
import com.example.CeleraAi.Negocio.model.Categorias;
import com.example.CeleraAi.Negocio.repositorio.CategoriasRepo;
import com.example.CeleraAi.users.model.Administrador;
import com.example.CeleraAi.users.repositorio.AdministradorRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoriasService {

    private final CategoriasRepo categoriasRepo;
    private final AdministradorRepo administradorRepo;


    public CategoriasDto crearCategorias(CrearCategoriasDto crearCategoriasDto){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            String nombre= ((UserDetails)principal).getUsername();
            Optional<Administrador> usuario = administradorRepo.findByEmailIgnoreCase(nombre);
            if (usuario.isPresent()){
                Categorias categorias = new Categorias();
                categorias.setNombre(crearCategoriasDto.nombre());
                categoriasRepo.save(categorias);
                return CategoriasDto.of(categorias);
            }
        }

        return null;
    }
}
