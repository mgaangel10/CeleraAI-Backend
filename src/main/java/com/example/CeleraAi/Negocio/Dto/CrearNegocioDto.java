package com.example.CeleraAi.Negocio.Dto;

public record CrearNegocioDto(String nombre,
                              String categorias,
                              String cid,
                              int numeroEmpleados,
                              String telefono,
                              String email,
                              String ciudad,
                              String pais,
                              String sitioweb) {
}
