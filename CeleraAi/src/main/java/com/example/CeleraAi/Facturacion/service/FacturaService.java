package com.example.CeleraAi.Facturacion.service;

import com.example.CeleraAi.Facturacion.Dto.CrearFacturaDto;
import com.example.CeleraAi.Facturacion.Dto.FacturaDto;
import com.example.CeleraAi.Facturacion.model.Factura;
import com.example.CeleraAi.Facturacion.repositorio.FacturaRepo;
import com.example.CeleraAi.Negocio.model.Negocio;
import com.example.CeleraAi.Negocio.repositorio.NegocioRepo;
import com.example.CeleraAi.Venta.Dto.VentaDto;
import com.example.CeleraAi.Venta.model.Venta;
import com.example.CeleraAi.Venta.repositorio.VentaRepo;
import com.example.CeleraAi.users.model.Usuario;
import com.example.CeleraAi.users.repositorio.UsuarioRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FacturaService {

    private final FacturaRepo facturaRepo;
    private final UsuarioRepo usuarioRepo;
    private final NegocioRepo negocioRepo;
    private final VentaRepo ventaRepo;

    public FacturaDto crearFactura(UUID idVenta, CrearFacturaDto facturaDto) {

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            String nombre= ((UserDetails)principal).getUsername();
            Optional<Usuario> usuario = usuarioRepo.findByEmailIgnoreCase(nombre);
            Optional<Venta> ventas = ventaRepo.findById(idVenta);
            System.out.println("se encuentra las ventas? " + (ventas.isPresent() ? ventas.get().toString() : "No se encontró la venta"));

            if (usuario.isPresent() && ventas.isPresent()) { // Verifica que ventas no sea null ni vacío


                // Procesa las ventas si existe
                Venta venta = ventas.get(); // Obtiene la venta del Optional
                Factura factura = new Factura();


                factura.setNumeroFactura(facturaDto.numeroFacura()); // Verifica el nombre correcto del método
                factura.setCliente(facturaDto.cliente());
                factura.setImpuestos(facturaDto.impuestos());
                factura.setTotal(venta.getTotalVenta());
                factura.setVentas(venta);
                factura.setSubtotal(venta.getTotalVenta() + ((facturaDto.impuestos()*venta.getTotalVenta())/100));
                factura.setNegocio(venta.getNegocio());
                factura.setNumeroAlbaran(facturaDto.numeroAlbaran());
                factura.setFecha(LocalDate.now());
                facturaRepo.save(factura);
                Negocio negocio = factura.getNegocio();
                negocio.getFacturas().add(factura);
                negocioRepo.save(negocio);
                factura.setNombreEmpresa(negocio.getNombre());
                factura.setCid(negocio.getCid());
                facturaRepo.save(factura);

                venta.setTieneFactura(true);
                venta.setTerminado(true);
                ventaRepo.save(venta);

                return FacturaDto.of(factura); // Retorna la última factura creada
            }
        }

        return null;
    }


    public List<FacturaDto> verFacturas(UUID idNgeocio){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            String nombre= ((UserDetails)principal).getUsername();
            Optional<Usuario> usuario = usuarioRepo.findByEmailIgnoreCase(nombre);
            Optional<Negocio> negocio = negocioRepo.findById(idNgeocio);
            if (usuario.isPresent()){
                System.out.println(negocio.get().getFacturas());
               List<Factura> facturas = negocio.get().getFacturas();
               List<FacturaDto> facturaDtos = facturas.stream().map(FacturaDto::of).collect(Collectors.toList());
               return facturaDtos;
            }
        }

        return null;
    }

    public FacturaDto verFactura(UUID idFactura){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            String nombre= ((UserDetails)principal).getUsername();
            Optional<Usuario> usuario = usuarioRepo.findByEmailIgnoreCase(nombre);
            Optional<Factura> factura = facturaRepo.findById(idFactura);
            if (usuario.isPresent()){
                return FacturaDto.of(factura.get());
            }
        }

        return null;
    }

}
