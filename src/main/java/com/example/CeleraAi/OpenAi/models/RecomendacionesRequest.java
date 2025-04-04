package com.example.CeleraAi.OpenAi.models;

import com.example.CeleraAi.Producto.model.Producto;
import com.example.CeleraAi.Venta.model.Venta;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class RecomendacionesRequest {
    private List<Producto> productos;
    private List<Venta> ventas;
}
