package com.example.CeleraAi.Negocio.service;

import com.example.CeleraAi.Negocio.Dto.CrearNegocioDto;
import com.example.CeleraAi.Negocio.Dto.NegocioDto;
import com.example.CeleraAi.Negocio.model.Categorias;
import com.example.CeleraAi.Negocio.model.Negocio;
import com.example.CeleraAi.Negocio.repositorio.CategoriasRepo;
import com.example.CeleraAi.Negocio.repositorio.NegocioRepo;
import com.example.CeleraAi.users.model.Usuario;
import com.example.CeleraAi.users.repositorio.UsuarioRepo;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.Lifecycle;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NegocioService {

    private final NegocioRepo negocioRepo;
    private final CategoriasRepo categoriasRepo;
    private final UsuarioRepo usuarioRepo;

    public NegocioDto crearNegocio(CrearNegocioDto crearNegocioDto){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            String nombre= ((UserDetails)principal).getUsername();
            Optional<Usuario> usuario = usuarioRepo.findByEmailIgnoreCase(nombre);
            Optional<Categorias> categorias = categoriasRepo.findByNombre(crearNegocioDto.categorias());
            if (categorias.isEmpty()){

                throw new RuntimeException("no se encuentra la categoria");
            }
            if (usuario.isPresent()){
                Negocio negocio = new Negocio();
                negocio.setNombre(crearNegocioDto.nombre());
                negocio.setCategorias(categorias.get());

                negocio.setNumeroEmpleados(crearNegocioDto.numeroEmpleados());
                negocio.setTelefono(crearNegocioDto.telefono());
                negocio.setEmail(crearNegocioDto.email());
                negocio.setPais(crearNegocioDto.pais());
                negocio.setCiudad(crearNegocioDto.ciudad());
                negocio.setUsuario(usuario.get());
                negocio.setSitioweb(crearNegocioDto.sitioweb());
                negocioRepo.save(negocio);
                usuario.get().getNegocios().add(negocio);
                categorias.get().getNegocios().add(negocio);
                return NegocioDto.of(negocio);
            }
        }

        return null;
    }

    public List<NegocioDto> verLosNegociosDelUsuario(){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            String nombre= ((UserDetails)principal).getUsername();
            Optional<Usuario> usuario = usuarioRepo.findByEmailIgnoreCase(nombre);
            if (usuario.isPresent()){
                List<Negocio> negocios = negocioRepo.findAll();
                List<NegocioDto> negocioDtos = negocios.stream().map(NegocioDto::of).collect(Collectors.toList());
                return negocioDtos;
            }
        }

        return null;
    }
    public NegocioDto findById(UUID uuid){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            String nombre= ((UserDetails)principal).getUsername();
            Optional<Usuario> usuario = usuarioRepo.findByEmailIgnoreCase(nombre);
            if (usuario.isPresent()){
               Optional<Negocio> negocio = negocioRepo.findById(uuid);
               return NegocioDto.of(negocio.get());
            }
        }

        return null;
    }
}
