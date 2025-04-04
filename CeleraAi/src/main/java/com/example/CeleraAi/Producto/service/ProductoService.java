package com.example.CeleraAi.Producto.service;

import com.example.CeleraAi.Negocio.model.Negocio;
import com.example.CeleraAi.Negocio.repositorio.NegocioRepo;
import com.example.CeleraAi.Producto.Dto.CrearProductoDto;
import com.example.CeleraAi.Producto.Dto.ProductoDto;
import com.example.CeleraAi.Producto.model.Producto;
import com.example.CeleraAi.Producto.repositorio.ProductoRepo;
import com.example.CeleraAi.Venta.model.DetalleVenta;
import com.example.CeleraAi.Venta.model.Venta;
import com.example.CeleraAi.users.model.Usuario;
import com.example.CeleraAi.users.repositorio.UsuarioRepo;
import lombok.RequiredArgsConstructor;
import org.apache.poi.sl.draw.geom.GuideIf;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepo productoRepo;
    private final NegocioRepo negocioRepo;
    private final UsuarioRepo usuarioRepo;

    public ProductoDto crearProducto(CrearProductoDto crearProductoDto, UUID idNegocio){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            String nombre= ((UserDetails)principal).getUsername();
            Optional<Usuario> usuario = usuarioRepo.findByEmailIgnoreCase(nombre);
            Optional<Negocio> negocio = negocioRepo.findById(idNegocio);
            if (usuario.isPresent()){
                Producto prodcuto = new Producto();
                prodcuto.setNombre(crearProductoDto.nombre());
                prodcuto.setPrecio(crearProductoDto.precio());
                prodcuto.setStock(crearProductoDto.stock());
                prodcuto.setDisponible(true);
                prodcuto.setPrecioProveedor(crearProductoDto.precioProveedor());
                prodcuto.setNegocio(negocio.get());

                productoRepo.save(prodcuto);
                negocio.get().getProdcutos().add(prodcuto);
                negocioRepo.save(negocio.get());
                return ProductoDto.of(prodcuto);
            }
        }

        return null;
    }
    public List<ProductoDto> importarProductosDesdeExcel(MultipartFile file, UUID idNegocio) throws Exception {

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            String nombre = ((UserDetails) principal).getUsername();
            Optional<Usuario> usuario = usuarioRepo.findByEmailIgnoreCase(nombre);

            if (usuario.isPresent()) {
                List<ProductoDto> productosGuardados = new ArrayList<>();

                try (InputStream inputStream = file.getInputStream();
                     Workbook workbook = new XSSFWorkbook(inputStream)) {

                    Sheet sheet = workbook.getSheetAt(0); // Tomamos la primera hoja del archivo
                    for (Row row : sheet) {
                        if (row.getRowNum() == 0) continue; // Omitimos la primera fila si es un encabezado

                        Producto producto = new Producto();

                        producto.setNombre(getCellValue(row.getCell(0))); // Nombre se mantiene como String
                        producto.setPrecio(parseDouble(getCellValue(row.getCell(1))));
                        producto.setStock(parseInt(getCellValue(row.getCell(2))));
                        producto.setPrecioProveedor(parseDouble(getCellValue(row.getCell(3))));

                        Optional<Negocio> negocioOpt = negocioRepo.findById(idNegocio);
                        if (negocioOpt.isEmpty()) {
                            throw new RuntimeException("Negocio no encontrado.");
                        }

                        producto.setNegocio(negocioOpt.get());
                        producto.setDisponible(true);
                        productoRepo.save(producto);

                        negocioOpt.get().getProdcutos().add(producto);
                        negocioRepo.save(negocioOpt.get());

                        productosGuardados.add(ProductoDto.of(producto));
                    }
                }
                return productosGuardados;
            }
        }
        return null;
    }

    // Método para manejar diferentes tipos de datos en celdas
    private String getCellValue(Cell cell) {
        if (cell == null) {
            return ""; // Si la celda está vacía, devuelve un String vacío
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue()); // Convierte números a String
            default:
                return "";
        }
    }

    private double parseDouble(String value) {
        try {
            return Double.parseDouble(value.replace(",", ".")); // Convierte "5,0" a 5.0
        } catch (NumberFormatException e) {
            throw new RuntimeException("Error al convertir a double: " + value);
        }
    }

    private int parseInt(String value) {
        try {
            return (int) Double.parseDouble(value.replace(",", ".")); // Maneja conversiones de "50.0" a 50
        } catch (NumberFormatException e) {
            throw new RuntimeException("Error al convertir a int: " + value);
        }
    }



    public List<ProductoDto> verTodosLosProductos(UUID idNegocio){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            String nombre= ((UserDetails)principal).getUsername();
            Optional<Usuario> usuario = usuarioRepo.findByEmailIgnoreCase(nombre);

            if (usuario.isPresent()){
                List<Producto> prodcutos = productoRepo.findAll();
                List<Producto> prodcutoList = prodcutos.stream().filter(prodcuto -> prodcuto.getNegocio().getId().equals(idNegocio) && prodcuto.isDisponible()).collect(Collectors.toList());

                List<ProductoDto> productoDtos = prodcutoList.stream().map(ProductoDto::of).collect(Collectors.toList());

                return productoDtos;
            }
        }

        return null;
    }

    public ProductoDto ponerNoDisponible(UUID id){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            String nombre= ((UserDetails)principal).getUsername();
            Optional<Usuario> usuario = usuarioRepo.findByEmailIgnoreCase(nombre);
            Optional<Producto> producto = productoRepo.findById(id);
            if (usuario.isPresent()){
                producto.get().setDisponible(false);
                productoRepo.save(producto.get());
                return ProductoDto.of(producto.get());

            }
        }

        return null;
    }


    public List<ProductoDto> productosEnBajoStock(UUID idNegocio){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            String nombre= ((UserDetails)principal).getUsername();
            Optional<Usuario> usuario = usuarioRepo.findByEmailIgnoreCase(nombre);

            if (usuario.isPresent()){
               Optional<Negocio> negocio = negocioRepo.findById(idNegocio);
               List<Producto> prodcutos = negocio.get().getProdcutos().stream().filter(prodcuto -> prodcuto.getStock()<15 && prodcuto.isDisponible()).collect(Collectors.toList());
               List<ProductoDto> productoDtos = prodcutos.stream().map(ProductoDto::of).collect(Collectors.toList());
               return productoDtos;
            }
        }

        return null;
    }

    public List<ProductoDto> masVendidos(UUID id){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            String nombre= ((UserDetails)principal).getUsername();
            Optional<Usuario> usuario = usuarioRepo.findByEmailIgnoreCase(nombre);

            if (usuario.isPresent()){
                Optional<Negocio> negocio = negocioRepo.findById(id);
                LocalDate hoy = LocalDate.now();
                LocalDate inicioSemana = hoy.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                LocalDate finSemana = hoy.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

                // Filtrar las ventas dentro de la semana actual
                List<Venta> ventasSemana = negocio.get().getVentas().stream()
                        .filter(venta -> {
                            LocalDate fechaVenta = venta.getFecha();
                            return !fechaVenta.isBefore(inicioSemana) && !fechaVenta.isAfter(finSemana);
                        })
                        .toList();

                // Obtener todos los detalles de ventas de la semana
                List<DetalleVenta> detallesVentas = ventasSemana.stream()
                        .flatMap(venta -> venta.getDetalleVentas().stream())
                        .collect(Collectors.toList());

                // Contar la cantidad vendida por producto
                Map<Producto, Integer> conteoProductos = new HashMap<>();
                for (DetalleVenta detalle : detallesVentas) {
                    Producto producto = detalle.getProdcuto();
                    conteoProductos.put(producto, conteoProductos.getOrDefault(producto, 0) + detalle.getCantidad());
                }

                // Ordenar los productos por cantidad vendida en orden descendente
                return conteoProductos.entrySet().stream()
                        .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue())) // Orden descendente
                        .map(Map.Entry::getKey).map(ProductoDto::of)
                        .toList();

            }
        }

        return null;
    }
    public ProductoDto editarProdeucto(UUID id,CrearProductoDto crearProductoDto) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            String nombre = ((UserDetails) principal).getUsername();
            Optional<Usuario> usuario = usuarioRepo.findByEmailIgnoreCase(nombre);
            Optional<Producto> producto = productoRepo.findById(id);
            if (usuario.isPresent()) {
                producto.get().setNombre(crearProductoDto.nombre());
                producto.get().setPrecio(crearProductoDto.precio());
                producto.get().setStock(crearProductoDto.stock());
                producto.get().setPrecioProveedor(crearProductoDto.precioProveedor());
                productoRepo.save(producto.get());
                return ProductoDto.of(producto.get());
            }

        }
        return null;
    }

    public List<ProductoDto> masVendidosMes(UUID id) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            String nombre = ((UserDetails) principal).getUsername();
            Optional<Usuario> usuario = usuarioRepo.findByEmailIgnoreCase(nombre);

            if (usuario.isPresent()) {
                Optional<Negocio> negocio = negocioRepo.findById(id);
                LocalDate hoy = LocalDate.now();
                LocalDate inicioMes = hoy.withDayOfMonth(1);  // Primer día del mes
                LocalDate finMes = hoy.withDayOfMonth(hoy.lengthOfMonth());  // Último día del mes

                // Filtrar las ventas dentro del mes actual
                List<Venta> ventasMes = negocio.get().getVentas().stream()
                        .filter(venta -> {
                            LocalDate fechaVenta = venta.getFecha();
                            return !fechaVenta.isBefore(inicioMes) && !fechaVenta.isAfter(finMes);
                        })
                        .toList();

                // Obtener todos los detalles de ventas del mes
                List<DetalleVenta> detallesVentas = ventasMes.stream()
                        .flatMap(venta -> venta.getDetalleVentas().stream())
                        .collect(Collectors.toList());

                // Contar la cantidad vendida por producto
                Map<Producto, Integer> conteoProductos = new HashMap<>();
                for (DetalleVenta detalle : detallesVentas) {
                    Producto producto = detalle.getProdcuto();
                    conteoProductos.put(producto, conteoProductos.getOrDefault(producto, 0) + detalle.getCantidad());
                }

                // Ordenar los productos por cantidad vendida en orden descendente
                return conteoProductos.entrySet().stream()
                        .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue())) // Orden descendente
                        .map(Map.Entry::getKey)
                        .map(ProductoDto::of)
                        .toList();
            }
        }

        return null;
    }



    public List<ProductoDto> ordenarAlfabeticamente(UUID idNegocio) {
        // Obtener el usuario autenticado
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            String nombre = ((UserDetails) principal).getUsername();
            Optional<Usuario> usuario = usuarioRepo.findByEmailIgnoreCase(nombre);

            // Verificar si el usuario existe
            if (usuario.isPresent()) {
                // Obtener el negocio por ID
                Optional<Negocio> negocio = negocioRepo.findById(idNegocio);

                // Verificar si el negocio existe
                if (negocio.isPresent()) {
                    List<Producto> productos = negocio.get().getProdcutos(); // Asegúrate de que 'getProductos()' es correcto

                    // Verifica si el negocio tiene productos
                    if (productos != null && !productos.isEmpty()) {
                        // Ordenar alfabéticamente por nombre de producto
                        List<ProductoDto> productoDtos = productos.stream().filter(producto -> producto.isDisponible())
                                .sorted(Comparator.comparing(Producto::getNombre)) // Ordena alfabéticamente
                                .map(ProductoDto::of) // Convierte Producto a ProductoDto
                                .collect(Collectors.toList());

                        // 📌 Verifica si la lista está ordenada correctamente
                        productoDtos.forEach(p -> System.out.println(p.nombre()));

                        return productoDtos;
                    } else {
                        System.out.println("No hay productos en este negocio.");
                        return Collections.emptyList(); // Si no hay productos, devuelve lista vacía
                    }
                } else {
                    System.out.println("Negocio no encontrado.");
                }
            } else {
                System.out.println("Usuario no encontrado.");
            }
        }
        return Collections.emptyList(); // Si no se encuentra el usuario, devuelve lista vacía
    }


    public List<ProductoDto> ordenarPorPrecioMasAlto(UUID idNegocio) {
        // Obtener el usuario autenticado
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            String nombre = ((UserDetails) principal).getUsername();
            Optional<Usuario> usuario = usuarioRepo.findByEmailIgnoreCase(nombre);

            // Verificar si el usuario existe
            if (usuario.isPresent()) {
                // Obtener el negocio por ID


                // Verificar si el negocio existe
                Optional<Negocio> negocio = negocioRepo.findById(idNegocio);
                List<Producto> productos = negocio.stream().flatMap(negocio1 -> negocio1.getProdcutos().stream()).collect(Collectors.toList());
                List<Producto> productos1 = productos.stream().sorted((p1, p2) -> Double.compare(p2.getPrecio(), p1.getPrecio())).collect(Collectors.toList());
                List<ProductoDto> productoDtos = productos1.stream().filter(producto -> producto.isDisponible()).map(ProductoDto::of).collect(Collectors.toList());
                System.out.println(productoDtos.toArray());
                return productoDtos;
            } else {
                System.out.println("Usuario no encontrado.");
            }
        }
        return Collections.emptyList(); // Si no se encuentra el usuario, devuelve lista vacía
    }


    public List<ProductoDto> ordenarPorMayorStock(UUID idNegocio) {
        // Obtener el usuario autenticado
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            String nombre = ((UserDetails) principal).getUsername();
            Optional<Usuario> usuario = usuarioRepo.findByEmailIgnoreCase(nombre);

            // Verificar si el usuario existe
            if (usuario.isPresent()) {
                // Obtener el negocio por ID
                Optional<Negocio> negocio = negocioRepo.findById(idNegocio);
                List<Producto> productos = negocio.stream().flatMap(negocio1 -> negocio1.getProdcutos().stream()).collect(Collectors.toList());
                List<Producto> productos1 = productos.stream().sorted((p1, p2) -> Integer.compare(p2.getStock(), p1.getStock())).collect(Collectors.toList());
                List<ProductoDto> productoDtos = productos1.stream().filter(producto -> producto.isDisponible()).map(ProductoDto::of).collect(Collectors.toList());
                System.out.println(productoDtos.toArray());
                return productoDtos;

            } else {
                System.out.println("Usuario no encontrado.");
            }
        }
        return Collections.emptyList(); // Si no se encuentra el usuario, devuelve lista vacía
    }

    public List<ProductoDto> ordenarPorMenorStock(UUID idNegocio) {
        // Obtener el usuario autenticado
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            String nombre = ((UserDetails) principal).getUsername();
            Optional<Usuario> usuario = usuarioRepo.findByEmailIgnoreCase(nombre);

            // Verificar si el usuario existe
            if (usuario.isPresent()) {
                // Obtener el negocio por ID
                Optional<Negocio> negocio = negocioRepo.findById(idNegocio);
                List<Producto> productos = negocio.stream().flatMap(negocio1 -> negocio1.getProdcutos().stream()).collect(Collectors.toList());
                List<Producto> productos1 = productos.stream().sorted((p1, p2) -> Integer.compare(p1.getStock(), p2.getStock())).collect(Collectors.toList());
                List<ProductoDto> productoDtos = productos1.stream().filter(producto -> producto.isDisponible()).map(ProductoDto::of).collect(Collectors.toList());
                System.out.println(productoDtos.toArray());
                return productoDtos;

            } else {
                System.out.println("Usuario no encontrado.");
            }
        }
        return Collections.emptyList(); // Si no se encuentra el usuario, devuelve lista vacía
    }







}
