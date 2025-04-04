package com.example.CeleraAi.users.service;


import com.example.CeleraAi.Negocio.model.Negocio;
import com.example.CeleraAi.Producto.Dto.ProductoDto;
import com.example.CeleraAi.Producto.model.Producto;
import com.example.CeleraAi.users.Dto.GetUsuario;
import com.example.CeleraAi.users.Dto.PostCrearUserDto;
import com.example.CeleraAi.users.Dto.PostLogin;
import com.example.CeleraAi.users.model.UserRoles;
import com.example.CeleraAi.users.model.Usuario;
import com.example.CeleraAi.users.repositorio.AdministradorRepo;
import com.example.CeleraAi.users.repositorio.UsuarioRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioService {
    private final UsuarioRepo usuarioRepo;
    private final PasswordEncoder passwordEncoder;
    private final AdministradorRepo administradorRepo;


    public Optional<Usuario> findById(UUID id){return usuarioRepo.findById(id);}

    public Optional<Usuario> findByEmail(String email) {
        return usuarioRepo.findFirstByEmail(email);
    }

    public  Usuario crearUsuario(PostCrearUserDto postCrearUserDto, EnumSet<UserRoles> userRoles){
        if (usuarioRepo.existsByEmailIgnoreCase(postCrearUserDto.email())||administradorRepo.existsByEmailIgnoreCase(postCrearUserDto.email())){
            throw new RuntimeException("El email ya ha sido registrado");
        }

        Usuario usuario = Usuario.builder()
                .email(postCrearUserDto.email())
                .name(postCrearUserDto.name())
                .lastName(postCrearUserDto.lastName())
                .password(passwordEncoder.encode(postCrearUserDto.password()))
                .createdAt(LocalDateTime.now())
                .username(postCrearUserDto.name()+postCrearUserDto.lastName())
                .phoneNumber(postCrearUserDto.phoneNumber())
                .birthDate(postCrearUserDto.nacimiento())
                .roles(EnumSet.of(UserRoles.USER))
                .enabled(false)
                .build();

        return usuarioRepo.save(usuario);

    }

    public Usuario createWithRole(PostCrearUserDto postCrearUserDto){
        return crearUsuario(postCrearUserDto,EnumSet.of(UserRoles.USER));
    }
    public GetUsuario getUsuario(){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            String nombre = ((UserDetails) principal).getUsername();
            Optional<Usuario> usuario = usuarioRepo.findByEmailIgnoreCase(nombre);

            // Verificar si el usuario existe
            if (usuario.isPresent()) {
                return GetUsuario.of(usuario.get());


            } else {
                System.out.println("Usuario no encontrado.");
            }
        }
        return null;

    }
    public Usuario setearEnabled(PostLogin postCrearUserDto){
        Optional<Usuario> usuario = usuarioRepo.findByEmailIgnoreCase(postCrearUserDto.email());

        if (usuario.isPresent() || usuario.get().isEnabled()){
            usuario.get().setEnabled(true);
            return usuarioRepo.save(usuario.get());
        }else {
            throw new RuntimeException("No se encuentra el usuario");
        }
    }


}
