package com.example.CeleraAi.Finanzas.service;

import com.example.CeleraAi.Finanzas.Dto.FinanzasDto;
import com.example.CeleraAi.Finanzas.repositorio.FinanzasRepo;
import com.example.CeleraAi.Venta.repositorio.VentaRepo;
import com.example.CeleraAi.users.repositorio.UsuarioRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FinanzasService {

    private final FinanzasRepo finanzasRepo;
    private final UsuarioRepo usuarioRepo;
    private final VentaRepo ventaRepo;




}
