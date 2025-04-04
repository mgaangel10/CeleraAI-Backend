package com.example.CeleraAi.Finanzas.model;

import com.example.CeleraAi.Venta.model.Venta;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data

public class Finanzas {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private double beneficio;

    private double gastos;

    @OneToMany
    private List<Venta> ventas;

}
