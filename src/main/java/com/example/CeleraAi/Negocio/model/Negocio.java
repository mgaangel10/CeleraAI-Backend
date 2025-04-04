package com.example.CeleraAi.Negocio.model;

import com.example.CeleraAi.Facturacion.model.Factura;
import com.example.CeleraAi.Producto.model.Producto;
import com.example.CeleraAi.Venta.model.Venta;
import com.example.CeleraAi.users.model.Usuario;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@ToString(exclude = {"facturas", "prodcutos", "ventas"})

public class Negocio {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private String cid;
    private String nombre;
    private int numeroEmpleados;

    private String telefono;
    private String email;

    private String ciudad;
    private String pais;

    private String sitioweb;

    @ManyToOne
    private Categorias categorias;

    @ManyToOne
    private Usuario usuario;

    @OneToMany
    @JsonIgnore
    private List<Factura> facturas;

    @OneToMany
    @JsonIgnore
    private List<Producto> prodcutos;

    @OneToMany(mappedBy = "negocio")
    private List<Venta> ventas;


}
