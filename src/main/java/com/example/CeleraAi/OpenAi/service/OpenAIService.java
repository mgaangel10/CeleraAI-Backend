package com.example.CeleraAi.OpenAi.service;

import com.example.CeleraAi.Negocio.model.Negocio;
import com.example.CeleraAi.Negocio.repositorio.NegocioRepo;
import com.example.CeleraAi.OpenAi.PreguntaUsuarioDto;
import com.example.CeleraAi.OpenAi.RegistroAccionIARepo;
import com.example.CeleraAi.OpenAi.models.*;


import com.example.CeleraAi.Producto.model.Producto;
import com.example.CeleraAi.Producto.repositorio.ProductoRepo;
import com.example.CeleraAi.Venta.model.DetalleVenta;
import com.example.CeleraAi.Venta.model.Venta;
import com.example.CeleraAi.Venta.repositorio.VentaRepo;
import com.example.CeleraAi.Venta.service.VentaService;
import com.example.CeleraAi.users.model.Usuario;
import com.example.CeleraAi.users.repositorio.UsuarioRepo;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

import org.apache.poi.sl.draw.geom.GuideIf;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OpenAIService {
//url
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";

    private final UsuarioRepo usuarioRepo;
    private final VentaService ventaService;
    private final VentaRepo ventaRepo;
    private final NegocioRepo negocioRepo;
    private final ProductoRepo productoRepo;
    private final RegistroAccionIARepo registroAccionIARepo;
    @Value("${openai.api.key}")
    private String API_KEY;

    private final Map<UUID, List<MensajeIA>> historialPorUsuario = new HashMap<>();
    private final Map<UUID, AccionIA> ultimaSugerenciaPendiente = new HashMap<>();
    private List<ProductoStockUpdate> productos; // si no lo tienes, lo creamos

    public String generarRecomendacionConIA(PreguntaUsuarioDto pregunta, UUID idNegocio) {
        Optional<Usuario> usuario = obtenerUsuarioAutenticado();
        Optional<Negocio> negocio = negocioRepo.findById(idNegocio);

        if (usuario.isEmpty() || negocio.isEmpty()) return "❌ Usuario o negocio no encontrados.";

        String promptSistema = "Eres un asistente inteligente para negocios pequeños. Analiza los datos y responde de forma útil y profesional.";
        UUID userId = usuario.get().getId();

        List<MensajeIA> historial = historialPorUsuario.computeIfAbsent(userId, k -> new ArrayList<>());
        String mensajeUsuario = pregunta.pregunta().toLowerCase().trim();
        System.out.println("mensaje del usuario: "+mensajeUsuario);

        if (mensajeUsuario.equals("sí") || mensajeUsuario.equals("si") || mensajeUsuario.contains("adelante")) {
            AccionIA sugerencia = ultimaSugerenciaPendiente.get(userId);
            if (sugerencia != null) {
                ultimaSugerenciaPendiente.remove(userId);
                return confirmarYEjecutarAccion(sugerencia, idNegocio);
            } else {
                return "⚠️ No hay ninguna sugerencia pendiente para confirmar.";
            }
        }

        if (mensajeUsuario.contains("añademe") || mensajeUsuario.equals("creame") || mensajeUsuario.contains("haz")) {
            AccionIA sugerencia = ultimaSugerenciaPendiente.get(userId);
            System.out.println("eentra en mi if");
            return confirmarYEjecutarAccion(sugerencia, idNegocio);
        }

        if (mensajeUsuario.equals("no") || mensajeUsuario.contains("mejor no")) {
            ultimaSugerenciaPendiente.remove(userId);
            return "👍 Vale, no he añadido nada. Si quieres otra sugerencia, dímelo.";
        }

        if (historial.isEmpty()) {
            String contexto = construirPromptUniversal(negocio.get(), pregunta.pregunta());
            historial.add(new MensajeIA("user", contexto));
        } else {
            historial.add(new MensajeIA("user", pregunta.pregunta()));
        }

        String respuesta = consultarOpenAIConHistorial(promptSistema, historial);

        String contenido = new JSONObject(respuesta)
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content");

        historial.add(new MensajeIA("assistant", contenido));

        try {
            int indexJson = contenido.indexOf("{");
            if (indexJson != -1) {
                String texto = contenido.substring(0, indexJson).trim();
                String jsonSolo = contenido.substring(indexJson).trim();
                JSONObject json = new JSONObject(jsonSolo);

                String tipo = json.optString("tipo", "");

                if ("accion".equalsIgnoreCase(tipo)) {
                    AccionIA accion = new ObjectMapper().readValue(jsonSolo, AccionIA.class);
                    return confirmarYEjecutarAccion(accion, idNegocio);
                } else if ("sugerencia".equalsIgnoreCase(tipo)) {
                    JSONObject accionJson = json.getJSONObject("accion");
                    AccionIA sugerencia = fromJson(accionJson);
                    ultimaSugerenciaPendiente.put(userId, sugerencia);
                }

                json.put("response", texto);
                return json.toString(2); // ✅ Devuelve JSON bonito con texto en campo "response"
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        JSONObject simpleJson = new JSONObject();
        simpleJson.put("tipo", "respuesta_simple");
        simpleJson.put("response", contenido);
        return simpleJson.toString(2);
    }



    public static AccionIA fromJson(JSONObject json) {
        AccionIA accion = new AccionIA();

        accion.setAccion(json.optString("accion"));

        JSONObject datos = json.optJSONObject("datos");
        if (datos != null) {

            // 🔍 Recuperamos el nombre desde "nombre" o "producto"
            String nombre = datos.optString("nombre", null);
            if (nombre == null || nombre.isBlank()) {
                nombre = datos.optString("producto", null);
            }
            accion.setNombre(nombre);

            // ✅ Recuperamos otros datos con seguridad
            accion.setPrecio(datos.has("precio_venta") ? datos.optDouble("precio_venta") : null);
            accion.setPrecioProveedor(datos.has("precio_proveedor") ? datos.optDouble("precio_proveedor") : null);
            accion.setStock(datos.has("stock") ? datos.optInt("stock") : null);
            accion.setProducto(datos.optString("producto", null));
            accion.setCategoria(datos.optString("categoria", null));
            accion.setDescuento(datos.has("descuento") ? datos.optInt("descuento") : null);

            // 📦 Guardamos todo el JSON en el map genérico por si hace falta después
            Map<String, Object> datosMap = new HashMap<>();
            for (String key : datos.keySet()) {
                datosMap.put(key, datos.get(key));
            }
            accion.setDatos(datosMap);
        }

        return accion;
    }






    private Optional<Usuario> obtenerUsuarioAutenticado() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            return usuarioRepo.findByEmailIgnoreCase(userDetails.getUsername());
        }
        return Optional.empty();
    }

    private String consultarOpenAIConHistorial(String promptSistema, List<MensajeIA> historial) {
        RestTemplate client = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + API_KEY);

        JSONArray messages = new JSONArray();
        messages.put(new JSONObject().put("role", "system").put("content", promptSistema));
        for (MensajeIA m : historial) {
            messages.put(new JSONObject().put("role", m.getRole()).put("content", m.getContent()));
        }

        JSONObject request = new JSONObject();
        request.put("model", "gpt-4o-mini");
        request.put("messages", messages);
        request.put("stream", false);

        HttpEntity<String> entity = new HttpEntity<>(request.toString(), headers);
        ResponseEntity<String> response = client.exchange(API_URL, HttpMethod.POST, entity, String.class);

        return response.getBody();
    }

    private String construirPromptUniversal(Negocio negocio, String pregunta) {
        String categoria = negocio.getCategorias().getNombre();

        StringBuilder productosStr = new StringBuilder();
        for (Producto p : negocio.getProdcutos()) {
            productosStr.append("- %s, %.2f€, stock: %d, proveedor: %.2f€\n"
                    .formatted(p.getNombre(), p.getPrecio(), p.getStock(), p.getPrecioProveedor()));
        }

        StringBuilder ventasStr = new StringBuilder();
        for (Venta v : negocio.getVentas()) {
            for (DetalleVenta d : v.getDetalleVentas()) {
                ventasStr.append("- %s: %d uds x %.2f€ (%s)\n"
                        .formatted(d.getProdcuto().getNombre(), d.getCantidad(), d.getProdcuto().getPrecio(), v.getFecha()));
            }
        }

        StringBuilder facturasStr = new StringBuilder();
        negocio.getFacturas().forEach(f -> {
            facturasStr.append("- Nº %s | Cliente: %s | Total: %.2f€ | Impuestos: %.2f€ | Subtotal: %.2f€\n"
                    .formatted(f.getNumeroFactura(), f.getCliente(), f.getTotal(), f.getImpuestos(), f.getSubtotal()));
        });

        StringBuilder ventasSinFacturaStr = new StringBuilder();
        negocio.getVentas().stream()
                .filter(v -> v.getFactura() == null)
                .forEach(v -> {
                    ventasSinFacturaStr.append("- Venta del %s:\n".formatted(v.getFecha()));
                    v.getDetalleVentas().forEach(d -> {
                        ventasSinFacturaStr.append("   • %s: %d uds a %.2f€\n"
                                .formatted(d.getProdcuto().getNombre(), d.getCantidad(), d.getProdcuto().getPrecio()));
                    });
                });

        return """
                Eres un asistente inteligente especializado en la gestión de negocios pequeños.

                📌 La categoría de este negocio es: **%s**
                Utiliza esta información para adaptar tus respuestas, recomendaciones y acciones al tipo de negocio. 
                No sugieras productos o decisiones que no tengan relación con esta categoría.

                Tu trabajo es analizar la información del negocio y ayudar al usuario con:

                ✅ Consultas sobre sus datos (ventas, productos, stock, facturas…)
                ✅ Acciones directas (crear productos, aplicar promociones, etc.)
                ✅ Sugerencias inteligentes (nuevos productos, ideas para vender más…)

                ---

                📌 SIEMPRE RESPONDE EN DOS PARTES:

                1️⃣ Un mensaje corto, profesional y claro (máximo 3 líneas).
                2️⃣ Justo debajo, un JSON con la acción o respuesta estructurada.

                ---

                📋 FORMATO DEL JSON:

                {
                  "tipo": "consulta" | "accion" | "sugerencia",
                  "accion": "ver_ventas" | "crear_producto" | "sugerir_producto_nuevo" | "alertar_stock_bajo" | "crear_promocion" | etc,
                  "datos": {
                    ...
                  }
                }

                ---

                ⚠️ FORMATO ESTRICTO según el tipo:

                🔧 Si el tipo es `"accion"`:
                - El campo `"accion"` debe ser **un texto** (string), no un objeto.
                - Todos los datos necesarios deben ir dentro de `"datos"`.

                ✅ EJEMPLO CORRECTO (acción):
                {
                  "tipo": "accion",
                  "accion": "crear_producto",
                  "datos": {
                    "nombre": "Croissant",
                    "precio_venta": 1.50,
                    "precio_proveedor": 0.40,
                    "stock": 50
                  }
                }

                ❌ INCORRECTO:
                {
                  "tipo": "accion",
                  "accion": { "accion": "crear_producto", "datos": { ... } }
                }

                💡 Si el tipo es `"sugerencia"`:
                - En ese caso SÍ puedes poner `"accion"` como un objeto anidado con `"accion"` y `"datos"` dentro.
                - Este tipo de respuesta **no se ejecuta** hasta que el usuario diga "sí", "hazlo", "adelante", etc.

                ✅ EJEMPLO CORRECTO (sugerencia):
                {
                  "tipo": "sugerencia",
                  "accion": {
                    "accion": "sugerir_producto_nuevo",
                    "datos": {
                      "nombre": "Pan integral",
                      "precio_venta": 2.0,
                      "precio_proveedor": 0.80,
                      "stock": 30
                    }
                  }
                }

                ---

                🔍 SI EL USUARIO HACE UNA PREGUNTA:
                Responde con tipo = "consulta"

                🔧 SI EL USUARIO DA UNA ORDEN (como "añádelo", "hazlo", "actualiza stock", etc.):
                Responde con tipo = "accion" y ejecuta directamente sin pedir confirmación.
                Es obligatorio incluir estos campos en `"datos"`:
                - nombre (string)
                - precio_venta (decimal)
                - precio_proveedor (decimal)
                - stock (entero)
                🔧 SI EL USUARIO DA UNA ORDEN (como  "actualiza stock"):
                Responde con tipo = "accion" y ejecuta directamente sin pedir confirmación.
                Es obligatorio incluir estos campos en `"datos"`:
                - nombre (string)
                - stock (entero)

                💡 SI ES UNA IDEA O ANÁLISIS:
                Responde con tipo = "sugerencia" y espera confirmación del usuario.

                ---

                ⚠️ MUY IMPORTANTE:
                - NO INVENTES DATOS. Usa solo la información proporcionada.
                - Si no tienes suficiente información, dilo educadamente.
                - El JSON debe estar bien cerrado y sin etiquetas ```json.

                ---

                📦 DATOS DEL NEGOCIO:

                PRODUCTOS:
                %s

                VENTAS:
                %s

                FACTURAS:
                %s

                VENTAS SIN FACTURA:
                %s

                ---

                ❓PREGUNTA DEL USUARIO:
                %s
                """.formatted(
                categoria,
                productosStr,
                ventasStr,
                facturasStr,
                ventasSinFacturaStr,
                pregunta
        );
    }

    private String procesarRespuestaSinEjecutar(String respuestaIA, UUID idNegocio) {
        Optional<Usuario> usuario = obtenerUsuarioAutenticado();
        try {
            String content = new JSONObject(respuestaIA)
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");

            int indexJson = content.indexOf("{");
            if (indexJson == -1) return "⚠ La IA no devolvió un JSON válido.\n" + content;

            String texto = content.substring(0, indexJson).trim();
            String jsonSolo = content.substring(indexJson).trim();
            JSONObject json = new JSONObject(jsonSolo);
            String tipo = json.optString("tipo");

            if ("sugerencia".equalsIgnoreCase(tipo)) {
                AccionIA sugerencia = new ObjectMapper().readValue(jsonSolo, AccionIA.class);
                ultimaSugerenciaPendiente.put(usuario.get().getId(), sugerencia);
            } else if ("accion".equalsIgnoreCase(tipo)) {
                // Ejecutar directamente la acción
                AccionIA accion = new ObjectMapper().readValue(jsonSolo, AccionIA.class);
                String resultado = confirmarYEjecutarAccion(accion, idNegocio);

                JSONObject respuestaFinal = new JSONObject();
                respuestaFinal.put("mensaje", texto);
                respuestaFinal.put("accion", new JSONObject(jsonSolo));
                respuestaFinal.put("ejecutado", true);
                respuestaFinal.put("resultado", resultado);

                return respuestaFinal.toString();
            }

            JSONObject resultado = new JSONObject();
            resultado.put("mensaje", texto);
            resultado.put("accion", new JSONObject(jsonSolo));

            return resultado.toString();
        } catch (Exception e) {
            return "⚠ Error procesando la respuesta IA: " + e.getMessage();
        }
    }

    public String confirmarYEjecutarAccion(AccionIA accion, UUID idNegocio) {
        Optional<Negocio> negocioOpt = negocioRepo.findById(idNegocio);
        if (negocioOpt.isEmpty()) return "❌ Negocio no encontrado.";

        Negocio negocio = negocioOpt.get();

        switch (accion.getAccion()) {

            case "crear_producto", "sugerir_producto_nuevo" -> {
                if (accion.getNombre() == null || accion.getPrecio() == null || accion.getPrecioProveedor() == null || accion.getStock() == null) {
                    JSONObject error = new JSONObject();
                    error.put("tipo", "error");
                    error.put("response", "❌ Faltan datos obligatorios para crear el producto. Asegúrate de que nombre, precio, stock y proveedor estén definidos.");
                    return error.toString(2);
                }

                System.out.println("🧠 Ejecutando creación de producto con:");
                System.out.println("➡ Nombre: " + accion.getNombre());
                System.out.println("➡ Precio venta: " + accion.getPrecio());
                System.out.println("➡ Precio proveedor: " + accion.getPrecioProveedor());
                System.out.println("➡ Stock: " + accion.getStock());

                Producto prodcuto = new Producto();
                prodcuto.setNombre(accion.getNombre());
                prodcuto.setPrecio(accion.getPrecio());
                prodcuto.setStock(accion.getStock());
                prodcuto.setDisponible(true);
                prodcuto.setPrecioProveedor(accion.getPrecioProveedor());
                prodcuto.setNegocio(negocio);

                productoRepo.save(prodcuto);
                negocio.getProdcutos().add(prodcuto);
                negocioRepo.save(negocio);
                guardarRegistro(accion.getAccion(), "Producto creado: " + prodcuto.getNombre(), negocio.getUsuario());

                JSONObject json = new JSONObject();
                json.put("tipo", "accion");
                json.put("accion", "crear_producto");
                json.put("datos", new JSONObject(Map.of(
                        "nombre", prodcuto.getNombre(),
                        "precio_venta", prodcuto.getPrecio(),
                        "precio_proveedor", prodcuto.getPrecioProveedor(),
                        "stock", prodcuto.getStock()
                )));
                json.put("response", "✅ Producto añadido con éxito.");

                return json.toString(2);
            }

            case "actualizar_stock" -> {
                System.out.println("👉 Ejecutando actualización de stock...");

                StringBuilder resultado = new StringBuilder();

                // Intentamos sacar desde una lista
                if (accion.getProductos() != null && !accion.getProductos().isEmpty()) {
                    for (ProductoStockUpdate update : accion.getProductos()) {
                        actualizarProducto(update, negocio, resultado);
                    }
                } else {
                    // 🟨 Este es el bloque donde está tu código
                    Map<String, Object> datos = accion.getDatos();

// Aceptamos "producto" o "nombre"
                    String nombre = null;
                    if (datos.containsKey("producto")) {
                        nombre = (String) datos.get("producto");
                    } else if (datos.containsKey("nombre")) {
                        nombre = (String) datos.get("nombre");
                    }

// Aceptamos "nuevo_stock" o "stock"
                    Integer stock = null;
                    if (datos.containsKey("nuevo_stock")) {
                        stock = ((Number) datos.get("nuevo_stock")).intValue();
                    } else if (datos.containsKey("stock")) {
                        stock = ((Number) datos.get("stock")).intValue();
                    }

                    System.out.println("🔍 Datos recibidos: " + datos);
                    System.out.println("🧪 Nombre detectado: " + nombre);
                    System.out.println("🧪 Stock detectado: " + stock);

                    if (nombre != null && stock != null) {
                        ProductoStockUpdate update = new ProductoStockUpdate(nombre, stock);
                        actualizarProducto(update, negocio, resultado);
                    } else {
                        resultado.append("❌ Datos incompletos para actualizar stock.");
                    }

                }

                guardarRegistro("actualizar_stock", resultado.toString(), negocio.getUsuario());

                JSONObject json = new JSONObject();
                json.put("tipo", "accion");
                json.put("accion", "actualizar_stock");
                json.put("datos", accion.getDatos()); // puedes ajustar si quieres que solo devuelva producto y nuevo_stock
                json.put("response", resultado.toString().replace("\\n", "").trim()); // eliminamos \n si los hay

                return json.toString(2); // JSON bonito

            }


            default -> {
                return "⚠ Acción no reconocida: " + accion.getAccion();
            }
        }
    }
    private void actualizarProducto(ProductoStockUpdate update, Negocio negocio, StringBuilder resultado) {
        Optional<Producto> prodOpt = productoRepo.findByNombreIgnoreCaseAndNegocio(update.getNombre(), negocio);
        if (prodOpt.isPresent()) {
            Producto p = prodOpt.get();
            p.setStock(update.getNuevo_stock());
            productoRepo.save(p);
            resultado.append("✅ ").append(p.getNombre())
                    .append(": stock actualizado a ").append(update.getNuevo_stock()).append("\\n");
        } else {
            resultado.append("❌ No se encontró el producto: ").append(update.getNombre()).append("\\n");
        }
    }

    private void guardarRegistro(String accion, String resultado, Usuario usuario) {
        RegistroAccionIA registro = new RegistroAccionIA();
        registro.setAccion(accion);
        registro.setResultado(resultado);
        registro.setFecha(LocalDateTime.now());
        registro.setUsuario(usuario);
        registroAccionIARepo.save(registro);
    }


    //alertas ia
    public List<String> generarAlertas(UUID idNegocio) {
        Optional<Negocio> negocioOpt = negocioRepo.findById(idNegocio);
        if (negocioOpt.isEmpty()) return List.of("❌ Negocio no encontrado.");

        Negocio negocio = negocioOpt.get();
        List<String> alertas = new ArrayList<>();

        // 1️⃣ Alerta por stock bajo
        for (Producto p : negocio.getProdcutos()) {
            if (p.getStock() <= 5) {
                alertas.add("⚠️ El producto '" + p.getNombre() + "' tiene un stock bajo: " + p.getStock() + " unidades.");
            }
        }

        // 2️⃣ Caída de ventas esta semana vs anterior
        List<Venta> ventas = negocio.getVentas();
        LocalDate hoy = LocalDate.now();
        LocalDate inicioSemana = hoy.minusDays(hoy.getDayOfWeek().getValue() - 1);
        LocalDate inicioSemanaAnterior = inicioSemana.minusWeeks(1);

        double totalSemana = ventas.stream()
                .filter(v -> v.getFecha().isAfter(inicioSemana.minusDays(1)))
                .mapToDouble(Venta::getTotalVenta)
                .sum();

        double totalSemanaAnterior = ventas.stream()
                .filter(v -> !v.getFecha().isBefore(inicioSemanaAnterior) && v.getFecha().isBefore(inicioSemana))
                .mapToDouble(Venta::getTotalVenta)
                .sum();

        if (totalSemanaAnterior > 0) {
            double caida = ((totalSemanaAnterior - totalSemana) / totalSemanaAnterior) * 100;
            if (caida >= 25) {
                alertas.add("📉 Tus ventas han caído un " + String.format("%.1f", caida) + "% respecto a la semana pasada.");
            }
        }

        // 3️⃣ Productos sin ventas en 30 días
        LocalDate hace30Dias = hoy.minusDays(30);

        for (Producto p : negocio.getProdcutos()) {
            // Solo revisar si el producto fue creado hace más de 30 días
            if (p.getFechaCrecaion() != null && p.getFechaCrecaion().isBefore(hoy.minusDays(30))) {
                boolean seVendio = ventas.stream()
                        .flatMap(v -> v.getDetalleVentas().stream())
                        .anyMatch(d -> d.getProdcuto().getId().equals(p.getId()) &&
                                d.getVenta().getFecha().isAfter(hace30Dias));

                if (!seVendio) {
                    alertas.add("😴 El producto '" + p.getNombre() + "' no ha tenido ventas en los últimos 30 días.");
                }
            }
        }


        // 4️⃣ Previsión de ventas para la próxima semana (promedio simple de las últimas 4 semanas)
        LocalDate hace4Semanas = hoy.minusWeeks(4);
        double totalUltimas4Semanas = ventas.stream()
                .filter(v -> v.getFecha().isAfter(hace4Semanas.minusDays(1)))
                .mapToDouble(Venta::getTotalVenta)
                .sum();

        // 4️⃣ Previsión de ventas para la próxima semana (promedio simple de las últimas 4 semanas)
        double promedioSemanal = totalUltimas4Semanas / 4.0;
        LocalDate proximaSemana = inicioSemana.plusWeeks(1);
        alertas.add("🔮 Si todo sigue igual, podrías vender aproximadamente " +
                String.format("%.2f", promedioSemanal) + " € en la semana del " +
                proximaSemana.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ".");


        return alertas;
    }

    private LocalDate toLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private Date toDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    // resumen del dia
    public String generarResumenTexto(UUID idNegocio) {
        // Usamos las nuevas consultas separadas para evitar errores con múltiples bags
        Optional<Negocio> negocioVentasOpt = negocioRepo.cargarConVentas(idNegocio);
        Optional<Negocio> negocioProductosOpt = negocioRepo.cargarConProductos(idNegocio);

        if (negocioVentasOpt.isEmpty() || negocioProductosOpt.isEmpty()) {
            return "❌ No se encontró el negocio o no se pudieron cargar los datos correctamente.";
        }

        Negocio negocioVentas = negocioVentasOpt.get();
        Negocio negocioProductos = negocioProductosOpt.get();

        List<Venta> ventasHoy = ventaRepo.findVentasConDetallesByNegocio(idNegocio)
                .stream()
                .filter(v -> v.getFecha().isEqual(LocalDate.now()))
                .toList();


        if (ventasHoy.isEmpty()){
            System.out.println("no hay ventas");
            return "📭 Hoy no se han registrado ventas."  ;
        }

        double totalVentas = ventasHoy.stream().mapToDouble(Venta::getTotalVenta).sum();

        // Agrupar productos vendidos
        Map<String, Integer> productosVendidos = new HashMap<>();
        for (Venta venta : ventasHoy) {
            for (DetalleVenta d : venta.getDetalleVentas()) {
                String nombre = d.getProdcuto().getNombre();
                productosVendidos.put(nombre,
                        productosVendidos.getOrDefault(nombre, 0) + d.getCantidad());
            }
        }

        // Ordenar por más vendidos
        List<Map.Entry<String, Integer>> topProductos = productosVendidos.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(3)
                .collect(Collectors.toList());

        // Gastos estimados (precio proveedor * cantidad vendida)
        double gastos = ventasHoy.stream()
                .flatMap(v -> v.getDetalleVentas().stream())
                .mapToDouble(d -> {
                    Producto p = d.getProdcuto();
                    return p.getPrecioProveedor() * d.getCantidad();
                }).sum();

        double beneficio = totalVentas - gastos;

        // Crear resumen en texto
        StringBuilder resumen = new StringBuilder();
        resumen.append("\uD83D\uDCCA Resumen del día - ").append(LocalDate.now()).append("\n\n");
        resumen.append("\uD83D\uDED9 Ventas totales: ").append(String.format("%.2f€", totalVentas)).append("\n");

        if (!topProductos.isEmpty()) {
            resumen.append("\uD83C\uDFC6 Productos más vendidos:\n");
            topProductos.forEach(entry ->
                    resumen.append("   - ").append(entry.getKey()).append(" (x").append(entry.getValue()).append(")\n")
            );
        }

        resumen.append("\n\uD83D\uDCB8 Gastos estimados: ").append(String.format("%.2f€", gastos)).append("\n");
        resumen.append("\uD83D\uDCB0 Beneficio estimado: ").append(String.format("%.2f€", beneficio)).append("\n\n");
        resumen.append("¡Buen trabajo hoy! \uD83D\uDCAA");

        return resumen.toString();
    }

}
