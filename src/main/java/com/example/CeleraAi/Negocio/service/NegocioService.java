package com.example.CeleraAi.Negocio.service;

import com.example.CeleraAi.Negocio.Dto.CrearNegocioDto;
import com.example.CeleraAi.Negocio.Dto.NegocioDto;
import com.example.CeleraAi.Negocio.Dto.ResumenNegocioDto;
import com.example.CeleraAi.Negocio.model.Categorias;
import com.example.CeleraAi.Negocio.model.Negocio;
import com.example.CeleraAi.Negocio.repositorio.CategoriasRepo;
import com.example.CeleraAi.Negocio.repositorio.NegocioRepo;
import com.example.CeleraAi.Producto.model.Producto;
import com.example.CeleraAi.Venta.model.DetalleVenta;
import com.example.CeleraAi.Venta.model.Venta;
import com.example.CeleraAi.Venta.repositorio.DetalleVentaRepo;
import com.example.CeleraAi.Venta.repositorio.VentaRepo;
import com.example.CeleraAi.users.model.Usuario;
import com.example.CeleraAi.users.repositorio.UsuarioRepo;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.Lifecycle;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NegocioService {

    private final NegocioRepo negocioRepo;
    private final CategoriasRepo categoriasRepo;
    private final UsuarioRepo usuarioRepo;
    private final VentaRepo ventaRepo;
    private final DetalleVentaRepo detalleVentaRepo;

    public NegocioDto crearNegocio(CrearNegocioDto crearNegocioDto){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            String nombre= ((UserDetails)principal).getUsername();
            Optional<Usuario> usuario = usuarioRepo.findByEmailIgnoreCase(nombre);
            Optional<Categorias> categorias = categoriasRepo.findByNombre(crearNegocioDto.categorias());
            if (categorias.isEmpty()){

                throw new RuntimeException("no se encuentra la categoria");
            }
            if (usuario.isPresent()){
                Negocio negocio = new Negocio();
                negocio.setNombre(crearNegocioDto.nombre());
                negocio.setCategorias(categorias.get());

                negocio.setNumeroEmpleados(crearNegocioDto.numeroEmpleados());
                negocio.setTelefono(crearNegocioDto.telefono());
                negocio.setEmail(crearNegocioDto.email());
                negocio.setPais(crearNegocioDto.pais());
                negocio.setCiudad(crearNegocioDto.ciudad());
                negocio.setUsuario(usuario.get());
                negocio.setSitioweb(crearNegocioDto.sitioweb());
                negocioRepo.save(negocio);
                usuario.get().getNegocios().add(negocio);
                usuarioRepo.save(usuario.get());
                categorias.get().getNegocios().add(negocio);
                return NegocioDto.of(negocio);
            }
        }

        return null;
    }

    public List<NegocioDto> verLosNegociosDelUsuario(){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            String nombre= ((UserDetails)principal).getUsername();
            Optional<Usuario> usuario = usuarioRepo.findByEmailIgnoreCase(nombre);
            if (usuario.isPresent()){
                List<Negocio> negocios = usuario.get().getNegocios();
                List<NegocioDto> negocioDtos = negocios.stream().map(NegocioDto::of).collect(Collectors.toList());
                return negocioDtos;
            }
        }

        return null;
    }
    public NegocioDto findById(UUID uuid){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            String nombre= ((UserDetails)principal).getUsername();
            Optional<Usuario> usuario = usuarioRepo.findByEmailIgnoreCase(nombre);
            if (usuario.isPresent()){
                Optional<Negocio> negocio = negocioRepo.findById(uuid);
                return NegocioDto.of(negocio.get());
            }
        }

        return null;
    }

    public ResumenNegocioDto obtenerResumenDiario(UUID idNegocio) {
        Negocio negocio = negocioRepo.findById(idNegocio).orElseThrow();

        LocalDate hoy = LocalDate.now();

        // 1️⃣ Ventas del día
        List<Venta> ventasHoy = ventaRepo.findByNegocioAndFecha(negocio, hoy);
        double totalVentasHoy = ventasHoy.stream().mapToDouble(Venta::getTotalVenta).sum();
        long cantidadVentasHoy = ventasHoy.size();

        // 2️⃣ Top producto por cantidad vendida en últimos 7 días
        LocalDate hace7dias = hoy.minusDays(7);
        List<DetalleVenta> detallesUltimaSemana = detalleVentaRepo.findByVenta_NegocioAndVenta_FechaAfter(negocio, hace7dias);

        Map<String, Long> conteoPorProducto = detallesUltimaSemana.stream()
                .collect(Collectors.groupingBy(
                        d -> d.getProdcuto().getNombre(),
                        Collectors.summingLong(DetalleVenta::getCantidad)
                ));

        String topProducto = conteoPorProducto.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Sin ventas");

        long cantidadTopProducto = conteoPorProducto.getOrDefault(topProducto, 0L);

        // 3️⃣ Producto con stock más bajo
        Producto productoStockBajo = negocio.getProdcutos().stream()
                .filter(p -> p.getStock() <= 5)
                .min(Comparator.comparingInt(Producto::getStock))
                .orElse(null);

        // 4️⃣ Caída de ventas semana actual vs anterior
        LocalDate inicioSemanaActual = hoy.with(DayOfWeek.MONDAY);
        LocalDate finSemanaActual = inicioSemanaActual.plusDays(6);

        LocalDate inicioSemanaAnterior = inicioSemanaActual.minusWeeks(1);
        LocalDate finSemanaAnterior = inicioSemanaAnterior.plusDays(6);

        double totalSemanaActual = ventaRepo.totalVentasEntreFechas(negocio, inicioSemanaActual, finSemanaActual.plusDays(1));
        double totalSemanaAnterior = ventaRepo.totalVentasEntreFechas(negocio, inicioSemanaAnterior, finSemanaAnterior.plusDays(1));

        Double caidaPorcentual = null;
        if (totalSemanaAnterior > 0) {
            caidaPorcentual = ((totalSemanaActual - totalSemanaAnterior) / totalSemanaAnterior) * 100;
        }

        // 5️⃣ Previsión basada en 4 semanas
        LocalDate hace4Semanas = hoy.minusWeeks(4);
        double totalUltimas4 = ventaRepo.totalVentasDesde(negocio, hace4Semanas);
        double prevision = totalUltimas4 / 4.0;

        return new ResumenNegocioDto(
                totalVentasHoy,
                cantidadVentasHoy,
                topProducto,
                cantidadTopProducto,
                productoStockBajo != null ? productoStockBajo.getNombre() : null,
                productoStockBajo != null ? productoStockBajo.getStock() : 0,
                caidaPorcentual,
                prevision
        );
    }



}