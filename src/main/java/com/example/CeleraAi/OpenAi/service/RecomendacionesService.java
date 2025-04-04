package com.example.CeleraAi.OpenAi.service;

import com.example.CeleraAi.OpenAi.models.Recomendaciones;
import com.example.CeleraAi.Producto.model.Producto;
import com.example.CeleraAi.Venta.model.Venta;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RecomendacionesService {
    public Recomendaciones generarRecomendaciones(List<Producto> productos, List<Venta> ventas) {
        List<String> recomendaciones = new ArrayList<>();

        // Ejemplo de lógica de recomendaciones basada en productos y ventas
        if (productos != null && !productos.isEmpty()) {
            recomendaciones.add("Ofrecer descuentos por compra de productos relacionados.");
        }

        if (ventas != null && !ventas.isEmpty()) {
            recomendaciones.add("Analizar las ventas de la semana para identificar productos populares.");
        }

        // Cualquier lógica adicional que desees agregar para generar las recomendaciones
        // Asegúrate de devolver un objeto Recomendaciones con la lista de recomendaciones generadas
        Recomendaciones recomendacionResponse = new Recomendaciones();
        recomendacionResponse.setRecomendaciones(recomendaciones);

        return recomendacionResponse;
    }
}
