package com.example.CeleraAi.Venta.service;

import ch.qos.logback.core.read.ListAppender;
import com.example.CeleraAi.Facturacion.model.Factura;
import com.example.CeleraAi.Facturacion.repositorio.FacturaRepo;
import com.example.CeleraAi.Negocio.model.Negocio;
import com.example.CeleraAi.Negocio.repositorio.NegocioRepo;
import com.example.CeleraAi.Producto.model.Producto;
import com.example.CeleraAi.Producto.repositorio.ProductoRepo;
import com.example.CeleraAi.Venta.Dto.CrearVentaDto;
import com.example.CeleraAi.Venta.Dto.DetalleVentaDTo;
import com.example.CeleraAi.Venta.Dto.FiltrarVentaPorFechaDTo;
import com.example.CeleraAi.Venta.Dto.VentaDto;
import com.example.CeleraAi.Venta.model.DetalleVenta;
import com.example.CeleraAi.Venta.model.Venta;
import com.example.CeleraAi.Venta.repositorio.DetalleVentaRepo;
import com.example.CeleraAi.Venta.repositorio.VentaRepo;
import com.example.CeleraAi.users.model.Usuario;
import com.example.CeleraAi.users.repositorio.UsuarioRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VentaService {

    private final VentaRepo ventaRepo;
    private final UsuarioRepo usuarioRepo;
    private final NegocioRepo negocioRepo;
    private final ProductoRepo productoRepo;
    private final DetalleVentaRepo detalleVentaRepo;
    private final FacturaRepo facturaRepo;

    public VentaDto crearVenta (CrearVentaDto crearVentaDto, UUID idNegocio){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            String nombre= ((UserDetails)principal).getUsername();
            Optional<Usuario> usuario = usuarioRepo.findByEmailIgnoreCase(nombre);
            Optional<Negocio> negocio = negocioRepo.findById(idNegocio);
            if (usuario.isPresent()){
                Venta venta = new Venta();
                venta.setFecha(LocalDate.now());
                venta.setTotalVenta(crearVentaDto.total());
                venta.setNegocio(negocio.get());
                venta.setMetodoPago(crearVentaDto.metodoPago());
                ventaRepo.save(venta);
                negocio.get().getVentas().add(venta);
                return VentaDto.of(venta);
            }
        }

        return null;
    }

    public VentaDto agregarProductoAVenta(UUID idProducto) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            String nombre = ((UserDetails) principal).getUsername();
            Optional<Usuario> usuarioOpt = usuarioRepo.findByEmailIgnoreCase(nombre);

            if (usuarioOpt.isPresent()) {


                // Buscar la venta activa del usuario basada en el atributo "activo"
                Optional<Venta> ventaOpt = ventaRepo.findByActivoTrue();
                Optional<Producto> productoOpt = productoRepo.findById(idProducto);

                Venta venta;
                if (ventaOpt.isPresent()) {
                    venta = ventaOpt.get();
                } else {
                    // Si no hay una venta activa, crear una nueva
                    venta = new Venta();

                    venta.setActivo(true);
                    venta.setTerminado(false);
                    venta.setTieneFactura(false);
                    venta.setTotalVenta(0.0);
                    venta.setNegocio(productoOpt.get().getNegocio());
                    venta.setDetalleVentas(new ArrayList<>());


                    venta = ventaRepo.save(venta);
                }


                if (productoOpt.isPresent()) {
                    Producto producto = productoOpt.get();

                    // Buscar si el producto ya está en la venta
                    Optional<DetalleVenta> detalleExistente = venta.getDetalleVentas().stream()
                            .filter(detalle -> detalle.getProdcuto().equals(producto))
                            .findFirst();

                    if (detalleExistente.isPresent()) {
                        // Si el producto ya está en la venta, aumentar la cantidad y recalcular el total
                        DetalleVenta detalle = detalleExistente.get();
                        int nuevaCantidad = detalle.getCantidad() + 1;
                        detalle.setCantidad(nuevaCantidad);
                        detalle.setTotal(detalle.getCantidad() * producto.getPrecio());
                        detalleVentaRepo.save(detalle);
                    } else {
                        // Si no está en la venta, crear un nuevo detalle de venta
                        DetalleVenta nuevoDetalle = new DetalleVenta();
                        nuevoDetalle.setVenta(venta);
                        nuevoDetalle.setProdcuto(producto);
                        nuevoDetalle.setCantidad(1);

                        nuevoDetalle.setTotal(producto.getPrecio());
                        detalleVentaRepo.save(nuevoDetalle);
                        venta.getDetalleVentas().add(nuevoDetalle);
                    }

                    // Recalcular el total de la venta
                    double totalVenta = venta.getDetalleVentas().stream()
                            .mapToDouble(DetalleVenta::getTotal)
                            .sum();
                    venta.setTotalVenta(totalVenta);
                    ventaRepo.save(venta);

                    return VentaDto.of(venta);
                }
            }
        }
        throw new RuntimeException("No autenticado.");
    }

    public void eliminarDetalleVenta(UUID id){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            String nombre= ((UserDetails)principal).getUsername();
            Optional<Usuario> usuario = usuarioRepo.findByEmailIgnoreCase(nombre);
            Optional<DetalleVenta> detalleVenta = detalleVentaRepo.findById(id);
            if (usuario.isPresent()){
              detalleVentaRepo.delete(detalleVenta.get());




            }
        }


    }

    public VentaDto quitarUnaCntidad(UUID id){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            String nombre= ((UserDetails)principal).getUsername();
            Optional<Usuario> usuario = usuarioRepo.findByEmailIgnoreCase(nombre);
            Optional<DetalleVenta> detalleVenta = detalleVentaRepo.findById(id);
            if (usuario.isPresent()){
               DetalleVenta detalleVenta1 = detalleVenta.get();
               int cantidad = detalleVenta1.getCantidad() - 1;
               if (cantidad > 0){
                   detalleVenta1.setCantidad(cantidad);
                   double total = cantidad * detalleVenta1.getProdcuto().getPrecio();
                   detalleVenta1.setTotal(total);
                   detalleVentaRepo.save(detalleVenta1);
               }else {
                   Venta venta = detalleVenta1.getVenta();
                   // 🔥 Eliminar de la lista antes de borrar
                   venta.getDetalleVentas().remove(detalleVenta1);
                   detalleVentaRepo.delete(detalleVenta1);
                   ventaRepo.save(venta);  // Guardar venta actualizada
               }

               Venta venta = detalleVenta1.getVenta();
               double totalCarrito = venta.getDetalleVentas().stream()
                       .mapToDouble(DetalleVenta::getTotal).sum();
               venta.setTotalVenta(totalCarrito);
               ventaRepo.save(venta);
               return VentaDto.of(venta);





            }
        }

        return null;
    }

    public VentaDto agregarCantidad(UUID id){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            String nombre= ((UserDetails)principal).getUsername();
            Optional<Usuario> usuario = usuarioRepo.findByEmailIgnoreCase(nombre);
            Optional<DetalleVenta> detalleVenta = detalleVentaRepo.findById(id);
            if (usuario.isPresent()){
                DetalleVenta detalleVenta1 = detalleVenta.get();
                int cantidad = detalleVenta1.getCantidad() + 1;
               detalleVenta1.setCantidad(cantidad);
               double total = cantidad * detalleVenta1.getProdcuto().getPrecio();
               detalleVenta1.setTotal(total);
               detalleVentaRepo.save(detalleVenta1);
               Venta venta = detalleVenta1.getVenta();
               double totalCarrito = venta.getDetalleVentas().stream().mapToDouble(DetalleVenta::getTotal).sum();
               venta.setTotalVenta(totalCarrito);
               ventaRepo.save(venta);
               return VentaDto.of(venta);

            }
        }

        return null;
    }


    public VentaDto terminarVenta(UUID idVenta){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            String nombre= ((UserDetails)principal).getUsername();
            Optional<Usuario> usuario = usuarioRepo.findByEmailIgnoreCase(nombre);
            Optional<Venta> venta = ventaRepo.findById(idVenta);
            if (usuario.isPresent()){
                venta.get().setActivo(false);
                venta.get().setTieneFactura(false);
                venta.get().setFecha(LocalDate.now());
                venta.get().getDetalleVentas().stream().map(detalleVenta -> {
                    detalleVenta.getProdcuto().setStock(detalleVenta.getProdcuto().getStock()-detalleVenta.getCantidad());
                    return productoRepo.save(detalleVenta.getProdcuto());
                }).collect(Collectors.toList());

                ventaRepo.save(venta.get());
                Negocio negocio = venta.get().getNegocio();
                negocio.getVentas().add(venta.get());
                negocioRepo.save(negocio);



                return VentaDto.of(venta.get());
            }
        }

        return null;
    }

    public List<VentaDto> verVentasFiltrando (UUID idNegocio, LocalDate localDate){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            String nombre= ((UserDetails)principal).getUsername();
            Optional<Usuario> usuario = usuarioRepo.findByEmailIgnoreCase(nombre);
            Optional<Negocio> negocio = negocioRepo.findById(idNegocio);
            if (usuario.isPresent()){
               List<Venta> ventas = negocio.stream().flatMap(negocio1 -> negocio1.getVentas().stream()).collect(Collectors.toList());
               List<Venta> ventas1 = ventas.stream().filter(venta -> venta.getFecha().equals(localDate)).collect(Collectors.toList());
               List<VentaDto> ventaDtos = ventas1.stream().map(VentaDto::of).collect(Collectors.toList());
               return ventaDtos;
            }
        }

        return null;
    }

    public double obtenerTotalVentasDelDia(LocalDate fecha) {
        return ventaRepo.calcularTotalVentasDelDia(fecha);
    }

    public Map<String, Object> ventasSemana(UUID id){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            String nombre= ((UserDetails)principal).getUsername();
            Optional<Usuario> usuario = usuarioRepo.findByEmailIgnoreCase(nombre);

            if (usuario.isPresent()){
                Optional<Negocio> negocio = negocioRepo.findById(id);
                // Obtener el inicio y fin de la semana actual (lunes a domingo)
                LocalDate hoy = LocalDate.now();
                LocalDate inicioSemana = hoy.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                LocalDate finSemana = hoy.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

                // Crear un mapa para almacenar las ventas por día de la semana
                Map<String, Double> ventasPorDia = new LinkedHashMap<>();
                for (DayOfWeek dia : DayOfWeek.values()) {
                    ventasPorDia.put(dia.name(), 0.0);
                }

                // Filtrar ventas dentro de la semana actual y sumarlas por día
                negocio.get().getVentas().stream()
                        .filter(venta -> {
                            LocalDate fechaVenta = venta.getFecha();
                            return !fechaVenta.isBefore(inicioSemana) && !fechaVenta.isAfter(finSemana);
                        })
                        .forEach(venta -> {
                            String diaVenta = venta.getFecha().getDayOfWeek().name();
                            ventasPorDia.put(diaVenta, ventasPorDia.get(diaVenta) + venta.getTotalVenta());
                        });

                // Mapeo de nombres de días en español
                Map<String, String> nombresDias = Map.of(
                        "MONDAY", "Lunes",
                        "TUESDAY", "Martes",
                        "WEDNESDAY", "Miercoles",
                        "THURSDAY", "Jueves",
                        "FRIDAY", "Viernes",
                        "SATURDAY", "Sabado",
                        "SUNDAY", "Domingo"
                );

                // Convertir claves del mapa a nombres en español
                Map<String, Object> resultado = new LinkedHashMap<>();
                resultado.put("semana", inicioSemana + " - " + finSemana);

                Map<String, Double> ventasFormateadas = new LinkedHashMap<>();
                for (Map.Entry<String, Double> entry : ventasPorDia.entrySet()) {
                    ventasFormateadas.put(nombresDias.get(entry.getKey()), entry.getValue());
                }
                resultado.put("ventas", ventasFormateadas);

                return resultado;

            }
        }

        return null;
    }


    public Map<String, Object> ventasMes(UUID id) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            String nombre = ((UserDetails) principal).getUsername();
            Optional<Usuario> usuario = usuarioRepo.findByEmailIgnoreCase(nombre);

            if (usuario.isPresent()) {
                Optional<Negocio> negocio = negocioRepo.findById(id);

                // Obtener el primer y último mes de este año
                LocalDate hoy = LocalDate.now();
                LocalDate inicioMes = hoy.withDayOfMonth(1);  // Primer día del mes
                LocalDate finMes = hoy.withDayOfMonth(hoy.lengthOfMonth());  // Último día del mes

                // Mapeo de nombres de los meses en español
                Map<Integer, String> nombresMeses = new LinkedHashMap<>();
                nombresMeses.put(1, "Enero");
                nombresMeses.put(2, "Febrero");
                nombresMeses.put(3, "Marzo");
                nombresMeses.put(4, "Abril");
                nombresMeses.put(5, "Mayo");
                nombresMeses.put(6, "Junio");
                nombresMeses.put(7, "Julio");
                nombresMeses.put(8, "Agosto");
                nombresMeses.put(9, "Septiembre");
                nombresMeses.put(10, "Octubre");
                nombresMeses.put(11, "Noviembre");
                nombresMeses.put(12, "Diciembre");

                // Mapa para almacenar las ventas por mes
                Map<String, Double> ventasPorMes = new LinkedHashMap<>();

                // Inicializar ventas para cada mes
                for (int mes = 1; mes <= 12; mes++) {
                    ventasPorMes.put(nombresMeses.get(mes), 0.0);
                }

                // Filtrar ventas dentro del año actual y sumarlas por mes
                negocio.get().getVentas().stream()
                        .filter(venta -> {
                            LocalDate fechaVenta = venta.getFecha();
                            return !fechaVenta.isBefore(inicioMes.withMonth(1)) && !fechaVenta.isAfter(finMes.withMonth(12));
                        })
                        .forEach(venta -> {
                            int mesVenta = venta.getFecha().getMonthValue();
                            String nombreMes = nombresMeses.get(mesVenta);
                            ventasPorMes.put(nombreMes, ventasPorMes.get(nombreMes) + venta.getTotalVenta());
                        });

                // Crear la estructura de respuesta con el rango de meses
                Map<String, Object> resultado = new LinkedHashMap<>();
                resultado.put("meses", "2025");  // Año actual

                // Responder con los datos organizados por mes
                resultado.put("ventas", ventasPorMes);

                return resultado;
            }
        }

        return null;
    }



    public List<VentaDto> ventasSemanales(UUID idNegocio) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            String nombre = ((UserDetails) principal).getUsername();
            Optional<Usuario> usuario = usuarioRepo.findByEmailIgnoreCase(nombre);
            Optional<Negocio> negocio = negocioRepo.findById(idNegocio);

            if (usuario.isPresent() && negocio.isPresent()) {
                // Obtener la fecha de inicio y fin de esta semana
                LocalDate today = LocalDate.now();
                // El primer día de la semana (lunes)
                LocalDate startOfWeek = today.with(WeekFields.of(java.util.Locale.getDefault()).dayOfWeek(), 1);
                // El último día de la semana (domingo)
                LocalDate endOfWeek = startOfWeek.plusDays(6);  // Sumar 6 días al lunes para llegar al domingo

                // Filtrar las ventas que ocurren dentro de esta semana
                List<Venta> ventas = negocio.get().getVentas();
                List<VentaDto> ventaDtos = ventas.stream()
                        .filter(venta -> {
                            // Obtener la fecha de la venta y comprobar si está dentro del rango de la semana
                            LocalDate ventaFecha = venta.getFecha();
                            // Filtra ventas que están dentro del rango de la semana (de lunes a domingo)
                            return !ventaFecha.isBefore(startOfWeek) && !ventaFecha.isAfter(endOfWeek);
                        })
                        .map(venta -> VentaDto.of(venta))  // Suponiendo que tienes un constructor de VentaDto que toma una Venta
                        .collect(Collectors.toList());

                return ventaDtos;
            }
        }
        return null;
    }

    public VentaDto verVentasActivas(UUID id){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            String nombre= ((UserDetails)principal).getUsername();
            Optional<Usuario> usuario = usuarioRepo.findByEmailIgnoreCase(nombre);
            Optional<Negocio> negocio = negocioRepo.findById(id);
            if (usuario.isPresent()){
                Optional<Venta> ventaActiva = negocio.get().getVentas().stream()
                        .filter(venta1 -> venta1.isActivo())  // Filtramos solo las ventas activas
                        .findFirst();
                if (ventaActiva.isPresent()){
                    return VentaDto.of(ventaActiva.get());
                }else {
                    return VentaDto.of(null);
                }


            }
        }

        return null;
    }


    public String obtenerDiaConMasVentas(UUID clienteId) {
        // Obtener todas las ventas del cliente
        List<Venta> ventas = ventaRepo.findByNegocioId(clienteId); // Suponiendo que tienes este método en tu repositorio

        // Mapa para almacenar las ventas totales por día de la semana
        Map<DayOfWeek, Double> ventasPorDia = new HashMap<>();

        // Iterar sobre todas las ventas y sumar las ventas por día de la semana
        for (Venta venta : ventas) {
            DayOfWeek diaDeLaSemana = venta.getFecha().getDayOfWeek(); // Obtener el día de la semana de la venta
            ventasPorDia.put(diaDeLaSemana, ventasPorDia.getOrDefault(diaDeLaSemana, 0.0) + venta.getTotalVenta()); // Sumar el monto de la venta
        }

        // Encontrar el día con más ventas
        Map.Entry<DayOfWeek, Double> diaConMasVentas = Collections.max(ventasPorDia.entrySet(), Map.Entry.comparingByValue());

        // Devolver el resultado, que incluye el nombre del día y el total de ventas
        return "El día con más ventas fue: " + diaConMasVentas.getKey() + " con un total de " + diaConMasVentas.getValue() + " en ventas.";
    }

    public List<VentaDto> verVentasSinFacturas(UUID idNegocio){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            String nombre= ((UserDetails)principal).getUsername();
            Optional<Usuario> usuario = usuarioRepo.findByEmailIgnoreCase(nombre);
            Optional<Negocio> negocio = negocioRepo.findById(idNegocio);
            if (usuario.isPresent()){
               List<Venta> ventas = negocio.get().getVentas();
               List<VentaDto> ventaDtos = ventas.stream().filter(venta -> !venta.isTieneFactura()).map(VentaDto::of).collect(Collectors.toList());
               return ventaDtos;

            }
        }

        return null;
    }

    public List<VentaDto> verVentasFacturadas(UUID idNegocio){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            String nombre= ((UserDetails)principal).getUsername();
            Optional<Usuario> usuario = usuarioRepo.findByEmailIgnoreCase(nombre);
            Optional<Negocio> negocio = negocioRepo.findById(idNegocio);
            if (usuario.isPresent()){
                List<Venta> ventas = negocio.get().getVentas();
                List<VentaDto> ventaDtos = ventas.stream().filter(venta -> venta.isTieneFactura()).map(VentaDto::of).collect(Collectors.toList());
                return ventaDtos;

            }
        }

        return null;
    }
    public VentaDto volverAgregarProductoAVenta(UUID idProducto,UUID idV) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            String nombre = ((UserDetails) principal).getUsername();
            Optional<Usuario> usuarioOpt = usuarioRepo.findByEmailIgnoreCase(nombre);

            if (usuarioOpt.isPresent()) {


                // Buscar la venta activa del usuario basada en el atributo "activo"
                Optional<Venta> ventaOpt = ventaRepo.findById(idV);
                Optional<Producto> productoOpt = productoRepo.findById(idProducto);

                Venta venta;
                if (ventaOpt.isPresent()) {
                    ventaOpt.get().setActivo(true);
                    ventaRepo.save(ventaOpt.get());
                    venta = ventaOpt.get();
                } else {
                    // Si no hay una venta activa, crear una nueva
                    venta = new Venta();

                    venta.setActivo(true);
                    venta.setTerminado(false);
                    venta.setTieneFactura(false);
                    venta.setTotalVenta(0.0);
                    venta.setNegocio(productoOpt.get().getNegocio());
                    venta.setDetalleVentas(new ArrayList<>());
                    venta.setActivo(true);

                    venta = ventaRepo.save(venta);
                }


                if (productoOpt.isPresent()) {
                    Producto producto = productoOpt.get();

                    // Buscar si el producto ya está en la venta
                    Optional<DetalleVenta> detalleExistente = venta.getDetalleVentas().stream()
                            .filter(detalle -> detalle.getProdcuto().equals(producto))
                            .findFirst();

                    if (detalleExistente.isPresent()) {
                        // Si el producto ya está en la venta, aumentar la cantidad y recalcular el total
                        DetalleVenta detalle = detalleExistente.get();
                        int nuevaCantidad = detalle.getCantidad() + 1;
                        detalle.setCantidad(nuevaCantidad);
                        detalle.setTotal(detalle.getCantidad() * producto.getPrecio());
                        detalleVentaRepo.save(detalle);
                    } else {
                        // Si no está en la venta, crear un nuevo detalle de venta
                        DetalleVenta nuevoDetalle = new DetalleVenta();
                        nuevoDetalle.setVenta(venta);
                        nuevoDetalle.setProdcuto(producto);
                        nuevoDetalle.setCantidad(1);

                        nuevoDetalle.setTotal(producto.getPrecio());
                        detalleVentaRepo.save(nuevoDetalle);
                        venta.getDetalleVentas().add(nuevoDetalle);
                    }

                    // Recalcular el total de la venta
                    double totalVenta = venta.getDetalleVentas().stream()
                            .mapToDouble(DetalleVenta::getTotal)
                            .sum();
                    venta.setTotalVenta(totalVenta);
                    ventaRepo.save(venta);

                    return VentaDto.of(venta);
                }
            }
        }
        throw new RuntimeException("No autenticado.");
    }


    public VentaDto editarVenta(UUID idVenta,UUID idProducto){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            String nombre= ((UserDetails)principal).getUsername();
            Optional<Usuario> usuario = usuarioRepo.findByEmailIgnoreCase(nombre);
            Optional<Venta> venta = ventaRepo.findById(idVenta);
            if (usuario.isPresent()){
               if (!venta.get().isTieneFactura()){
                  VentaDto ventaDto = volverAgregarProductoAVenta(idProducto,idVenta);

                  return ventaDto;
               }

            }
        }

        return null;
    }
    public double ingresos(UUID id){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            String nombre= ((UserDetails)principal).getUsername();
            Optional<Usuario> usuario = usuarioRepo.findByEmailIgnoreCase(nombre);
            Optional<Negocio> negocio = negocioRepo.findById(id);
            if (usuario.isPresent()){
                List<Venta> ventas = negocio.get().getVentas();
                double ingreso = ventas.stream().mapToDouble(Venta::getTotalVenta).sum();
                return ingreso;

            }
        }

        return 0;
    }

    public double gastos(UUID id){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            String nombre= ((UserDetails)principal).getUsername();
            Optional<Usuario> usuario = usuarioRepo.findByEmailIgnoreCase(nombre);
            Optional<Negocio> negocio = negocioRepo.findById(id);
            if (usuario.isPresent()){
                List<Producto> productos = negocio.get().getProdcutos();
                double gastos = productos.stream().mapToDouble(Producto::getPrecioProveedor).sum();
                return gastos;


            }
        }

        return 0;
    }

    public double beneficio(UUID id){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            String nombre= ((UserDetails)principal).getUsername();
            Optional<Usuario> usuario = usuarioRepo.findByEmailIgnoreCase(nombre);
            Optional<Negocio> negocio = negocioRepo.findById(id);
            if (usuario.isPresent()){
            List<Venta> ventas = negocio.get().getVentas();
            return ventas.stream().flatMap(venta -> venta.getDetalleVentas().stream()).mapToDouble(value -> (value.getProdcuto().getPrecio()-value.getProdcuto().getPrecioProveedor())*value.getCantidad()).sum();



            }
        }

        return 0;
    }

    public VentaDto detallesVenta(UUID id){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            String nombre= ((UserDetails)principal).getUsername();
            Optional<Usuario> usuario = usuarioRepo.findByEmailIgnoreCase(nombre);
            Optional<Venta> venta = ventaRepo.findById(id);
            if (usuario.isPresent()){
               return VentaDto.of(venta.get());


            }
        }

        return null;
    }

    public List<VentaDto> verTodasVentas(UUID idNgeocio){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            String nombre= ((UserDetails)principal).getUsername();
            Optional<Usuario> usuario = usuarioRepo.findByEmailIgnoreCase(nombre);
            Optional<Negocio> negocio = negocioRepo.findById(idNgeocio);
            if (usuario.isPresent()){
                List<Venta> ventas = negocio.get().getVentas();
                List<VentaDto> ventaDtos = ventas.stream().filter(venta -> !venta.isActivo()).map(VentaDto::of).collect(Collectors.toList());
                return ventaDtos;

            }
        }

        return null;
    }


    public List<VentaDto> getVentasByFecha(FiltrarVentaPorFechaDTo fecha, UUID idNgeocio) {

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            String nombre= ((UserDetails)principal).getUsername();
            Optional<Usuario> usuario = usuarioRepo.findByEmailIgnoreCase(nombre);
            Optional<Negocio> negocio = negocioRepo.findById(idNgeocio);
            if (usuario.isPresent()){
                List<Venta> ventas = negocio.get().getVentas();
                return ventas.stream()
                        .filter(venta -> venta.getFecha().equals(fecha.fecha()))
                        .map(VentaDto::of)
                        .collect(Collectors.toList());

            }
        }

        return null;

    }

    public List<VentaDto> getVentasConFactura(UUID idNgeocio) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            String nombre= ((UserDetails)principal).getUsername();
            Optional<Usuario> usuario = usuarioRepo.findByEmailIgnoreCase(nombre);
            Optional<Negocio> negocio = negocioRepo.findById(idNgeocio);
            if (usuario.isPresent()){
                List<Venta> ventas = negocio.get().getVentas();
                return ventas.stream()
                        .filter(Venta::isTieneFactura)
                        .map(VentaDto::of)
                        .collect(Collectors.toList());

            }
        }

        return null;

    }

    public List<VentaDto> getVentasNoTerminadas(UUID idNgeocio) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            String nombre= ((UserDetails)principal).getUsername();
            Optional<Usuario> usuario = usuarioRepo.findByEmailIgnoreCase(nombre);
            Optional<Negocio> negocio = negocioRepo.findById(idNgeocio);
            if (usuario.isPresent()){
                List<Venta> ventas = negocio.get().getVentas();
                return ventas.stream()
                        .filter(venta -> !venta.isTerminado())
                        .map(VentaDto::of)
                        .collect(Collectors.toList());

            }
        }

        return null;

    }

    public List<VentaDto> getVentasPorMayorTotalVenta(UUID idNegocio) {

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            String nombre= ((UserDetails)principal).getUsername();
            Optional<Usuario> usuario = usuarioRepo.findByEmailIgnoreCase(nombre);
            Optional<Negocio> negocio = negocioRepo.findById(idNegocio);
            if (usuario.isPresent()){

                List<Venta> ventas = negocio.get().getVentas();
                return ventas.stream()
                        .sorted((v1, v2) -> Double.compare(v2.getTotalVenta(), v1.getTotalVenta()))
                        .map(VentaDto::of)
                        .collect(Collectors.toList());

            }
        }

        return null;

    }










}
