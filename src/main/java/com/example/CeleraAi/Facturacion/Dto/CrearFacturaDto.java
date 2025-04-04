package com.example.CeleraAi.Facturacion.Dto;

import com.example.CeleraAi.Venta.model.Venta;

import java.util.List;
import java.util.UUID;

public record CrearFacturaDto(String numeroFacura,
                              String cliente,
                              double impuestos,
                              int numeroAlbaran

                              ) {
}
