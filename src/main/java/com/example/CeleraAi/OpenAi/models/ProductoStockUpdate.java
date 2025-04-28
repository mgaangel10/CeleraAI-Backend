package com.example.CeleraAi.OpenAi.models;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductoStockUpdate {
    private String nombre;
    private Integer nuevo_stock;
}
