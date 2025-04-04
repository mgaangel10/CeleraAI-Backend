package com.example.CeleraAi.users.controller;


import com.example.CeleraAi.security.jwt.JwtProvider;
import com.example.CeleraAi.users.Dto.JwtUserResponse;
import com.example.CeleraAi.users.Dto.PostCrearUserDto;
import com.example.CeleraAi.users.Dto.PostLogin;
import com.example.CeleraAi.users.Dto.PostRegistroDto;
import com.example.CeleraAi.users.model.Administrador;
import com.example.CeleraAi.users.service.AdministradorService;
import com.example.CeleraAi.users.service.UsuarioService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
public class AdministradorController {

    private final AdministradorService administradorService;
    private final UsuarioService usuarioService;
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;


    @PostMapping("auth/register/admin")
    public ResponseEntity<?> crearAdministrador(@RequestBody PostCrearUserDto postCrearUserDto) {
        try {
            Administrador administrador = administradorService.createWithRole(postCrearUserDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(PostRegistroDto.Administrador(administrador));
        } catch (ResponseStatusException e) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getReason());
        }
    }


    @PostMapping("auth/login/admin")
    public ResponseEntity<JwtUserResponse> loginadmin(@RequestBody PostLogin postLogin){
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        postLogin.email(),
                        postLogin.password()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtProvider.generateToken(authentication);
        Administrador administrador = (Administrador) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(JwtUserResponse.ofAdminsitrador(administrador, token));
    }


}
