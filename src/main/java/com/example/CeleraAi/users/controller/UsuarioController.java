package com.example.CeleraAi.users.controller;


import com.example.CeleraAi.security.jwt.JwtProvider;
import com.example.CeleraAi.users.Dto.*;
import com.example.CeleraAi.users.model.Usuario;
import com.example.CeleraAi.users.service.UsuarioService;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;

    @PostMapping("auth/register/user")
    public ResponseEntity<?> crearUser(@RequestBody PostCrearUserDto postCrearUserDto) {
        try {
            Usuario usuario = usuarioService.createWithRole(postCrearUserDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(PostRegistroDto.Usuario(usuario));
        } catch (ResponseStatusException e) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getReason());
        }
    }



    @PostMapping("/auth/login/user")
    public ResponseEntity<JwtUserResponse> loginUser(@RequestBody PostLogin postLogin){
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        postLogin.email(),
                        postLogin.password()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtProvider.generateToken(authentication);
        Usuario usuario = (Usuario) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(JwtUserResponse.ofUsuario(usuario, token));
    }

    @GetMapping("usuario/informacion/usuario")
    public ResponseEntity<GetUsuario> getUsuario(){
        GetUsuario getUsuario = usuarioService.getUsuario();
        return ResponseEntity.ok(getUsuario);
    }


}
