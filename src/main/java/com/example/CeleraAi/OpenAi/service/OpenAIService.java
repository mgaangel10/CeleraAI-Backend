package com.example.CeleraAi.OpenAi.service;

import com.example.CeleraAi.Facturacion.model.Factura;
import com.example.CeleraAi.Negocio.model.Negocio;
import com.example.CeleraAi.Negocio.repositorio.NegocioRepo;
import com.example.CeleraAi.OpenAi.PreguntaUsuarioDto;
import com.example.CeleraAi.OpenAi.RegistroAccionIARepo;
import com.example.CeleraAi.OpenAi.models.AccionIA;
import com.example.CeleraAi.OpenAi.models.RegistroAccionIA;
import com.example.CeleraAi.Producto.model.Producto;
import com.example.CeleraAi.Producto.repositorio.ProductoRepo;
import com.example.CeleraAi.Venta.Dto.VentaDto;
import com.example.CeleraAi.Venta.model.DetalleVenta;
import com.example.CeleraAi.Venta.model.Venta;
import com.example.CeleraAi.Venta.service.VentaService;
import com.example.CeleraAi.users.model.Usuario;
import com.example.CeleraAi.users.repositorio.UsuarioRepo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.poi.sl.draw.geom.GuideIf;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OpenAIService {

    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String API_KEY = "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"; // ¡Recuerda nunca compartir tu API key!
    private final UsuarioRepo usuarioRepo;
    private final VentaService ventaService;
    private final NegocioRepo negocioRepo;
    private final ProductoRepo productoRepo;
    private final RegistroAccionIARepo registroAccionIARepo;

    public String generarTextoConIA(String mensajeUsuario) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            String nombre = ((UserDetails) principal).getUsername();
            Optional<Usuario> usuario = usuarioRepo.findByEmailIgnoreCase(nombre);

            if (usuario.isPresent()) {
                // Llamada al método para consultar OpenAI
                return consultarAPIOpenAI(mensajeUsuario);
            }
        }

        return "No se encontró al usuario.";
    }

    public String generarRecomendaciones(PreguntaUsuarioDto mensajeUsuario, UUID idNegocio) throws JsonProcessingException {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            String nombre = ((UserDetails) principal).getUsername();
            Optional<Usuario> usuario = usuarioRepo.findByEmailIgnoreCase(nombre);
            Optional<Negocio> negocio = negocioRepo.findById(idNegocio);

            if (usuario.isPresent() && negocio.isPresent()) {
                List<Venta> ventas = negocio.get().getVentas();
                List<Producto> productos = negocio.get().getProdcutos();
                List<Factura> facturas = negocio.get().getFacturas();

                // Construir el mensaje con datos
                StringBuilder ventasInfo = new StringBuilder("Ventas del usuario:\n");
                for (Venta venta : ventas) {
                    for (DetalleVenta detalle : venta.getDetalleVentas()) {
                        Producto producto = detalle.getProdcuto();
                        ventasInfo.append("Producto: ").append(producto.getNombre())
                                .append(", Cantidad: ").append(detalle.getCantidad())
                                .append(", Precio: ").append(producto.getPrecio())
                                .append(", Fecha: ").append(venta.getFecha())
                                .append("\n");
                    }
                }
                if (ventas.isEmpty()) ventasInfo.append("El usuario no tiene ventas registradas.");

                StringBuilder productosInfo = new StringBuilder("Productos disponibles:\n");
                for (Producto producto : productos) {
                    productosInfo.append("Producto: ").append(producto.getNombre())
                            .append(", Precio: ").append(producto.getPrecio())
                            .append(", Stock: ").append(producto.getStock())
                            .append(", Precio proveedor: ").append(producto.getPrecioProveedor())
                            .append("\n");
                }
                if (productos.isEmpty()) productosInfo.append("El usuario no tiene productos registrados.");

                StringBuilder facturasInfo = new StringBuilder("Facturas disponibles:\n");
                for (Factura factura : facturas) {
                    productosInfo.append("Factura: ").append(factura.getNumeroFactura())
                            .append(", Cliente: ").append(factura.getCliente())
                            .append(", Impuestos: ").append(factura.getImpuestos())
                            .append(", Total: ").append(factura.getTotal())
                            .append(", Subtotal: ").append(factura.getSubtotal())
                            .append(", Ventas: ").append(factura.getVentas().getFecha())
                            .append(", Detalles de ventas: ").append(factura.getVentas().getDetalleVentas().toString())
                            .append("\n");
                }
                if (facturas.isEmpty()) productosInfo.append("El usuario no tiene productos registrados.");

                String mensaje = "Usuario: " + nombre + "\n" +
                        "Ventas: " + ventasInfo +
                        "Productos: " + productosInfo +
                        "Negocio: " + negocio.get().getCategorias() +
                        "Facturas: " + facturasInfo +
                        "\nBasado en la pregunta: " + mensajeUsuario.pregunta();

                // Llamada a OpenAI
                String respuestaIA = consultarAPIOpenAI(mensaje);
                JSONObject jsonResponse = new JSONObject(respuestaIA);
                String contenidoIA = jsonResponse.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content");

                int startIndex = contenidoIA.indexOf("{");
                String mensajeTexto = contenidoIA.substring(0, startIndex).trim();
                String jsonSolo = contenidoIA.substring(startIndex).trim();

                ObjectMapper mapper = new ObjectMapper();
                AccionIA sugerencia = mapper.readValue(jsonSolo, AccionIA.class);

                // Ejecutar si es una acción automática
                if ("añadir_producto".equals(sugerencia.getAccion())) {
                    ejecutarAccionIA(sugerencia, idNegocio);
                    mensajeTexto = "✅ Producto añadido correctamente. " + mensajeTexto;
                }
                if ("sugerir_producto_nuevo".equals(sugerencia.getAccion())) {
                    ejecutarAccionIA(sugerencia, idNegocio);
                    mensajeTexto = "✅ Producto añadido correctamente. " + mensajeTexto;
                }

                // Devolver respuesta combinada
                JSONObject respuestaFinal = new JSONObject();
                respuestaFinal.put("mensaje", mensajeTexto);
                respuestaFinal.put("accion", new JSONObject(jsonSolo));

                return respuestaFinal.toString();
            }
        }

        return "❌ No se encontró al usuario o negocio.";
    }

    public String generarRecomendacionesV2(String mensajeUsuario, UUID idNegocio) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            String nombre = ((UserDetails) principal).getUsername();
            Optional<Usuario> usuario = usuarioRepo.findByEmailIgnoreCase(nombre);
            Optional<Negocio> negocio = negocioRepo.findById(idNegocio);

            if (usuario.isPresent() && negocio.isPresent()) {
                List<Venta> ventas = negocio.get().getVentas();
                List<Producto> productos = negocio.get().getProdcutos();

                // Construcción del mensaje para OpenAI
                String mensaje = construirMensajeParaIA(nombre, ventas, productos, mensajeUsuario, negocio.get());

                // Consultar OpenAI y obtener la respuesta
                String respuestaIA = consultarAPIOpenAI(mensaje);

                // Procesar la respuesta para detectar acciones
                return procesarRespuestaIAV2(respuestaIA, negocio.get());
            }
        }

        return "No se encontró al usuario o negocio.";
    }


    private String construirMensajeParaIA(String nombre, List<Venta> ventas, List<Producto> productos, String mensajeUsuario, Negocio negocio) {

        StringBuilder ventasInfo = new StringBuilder();
        for (Venta venta : ventas) {
            for (DetalleVenta detalle : venta.getDetalleVentas()) {
                Producto producto = detalle.getProdcuto();
                ventasInfo.append("- ").append(producto.getNombre())
                        .append(": ").append(detalle.getCantidad())
                        .append(" unidades, precio ").append(producto.getPrecio())
                        .append("\n");
            }
        }

        StringBuilder productosInfo = new StringBuilder();
        for (Producto producto : productos) {
            productosInfo.append("- ").append(producto.getNombre())
                    .append(", precio: ").append(producto.getPrecio())
                    .append(", stock: ").append(producto.getStock())
                    .append("\n");
        }

        // 👇 Este es el prompt mágico que le dice a la IA cómo responder
        String promptFinal = """
Eres un asistente de inteligencia artificial que trabaja como backend API. Tu respuesta debe ser solo un objeto JSON. No debes hablar, ni saludar, ni explicar nada.

Responde únicamente con un JSON válido y ejecutable. No uses comillas dobles mal cerradas ni devoluciones de texto largas.

POSIBLES ACCIONES:

- "sugerir_producto_nuevo"
- "crear_producto"
- "crear_promocion"
- "alertar_stock_bajo"

FORMATO DE RESPUESTA ESPERADO:

{
  "accion": "sugerir_producto_nuevo",
  "nombre": "Pan de Calabaza",
  "precio": 3.5,
  "categoria": "Repostería"
}

DATOS DEL USUARIO:

VENTAS:
%s

PRODUCTOS:
%s

PREGUNTA:
%s
""".formatted(ventasInfo.toString(), productosInfo.toString(), mensajeUsuario);


        return promptFinal;
    }


    private String procesarRespuestaIA(String respuestaIA, Negocio negocio) {
        try {
            // Convertir la respuesta de OpenAI en un JSON
            JSONObject jsonResponse = new JSONObject(respuestaIA);

            // Extraer el contenido de "choices[0].message.content"
            String contenidoIA = jsonResponse.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");

            // Convertir el contenido en JSON real
            JSONObject jsonRespuesta = new JSONObject(contenidoIA);

            String accion = jsonRespuesta.getString("accion");

            if ("crear_producto".equals(accion)) {
                String nombre = jsonRespuesta.getString("producto");
                double precio = jsonRespuesta.getDouble("precio");
                int stock = jsonRespuesta.getInt("stock");
                double precioProveedor = jsonRespuesta.getDouble("precioProveedor");

                Producto nuevoProducto = new Producto();
                nuevoProducto.setNombre(nombre);
                nuevoProducto.setStock(stock);
                nuevoProducto.setPrecio(precio);
                nuevoProducto.setPrecioProveedor(precioProveedor);
                nuevoProducto.setNegocio(negocio);
                productoRepo.save(nuevoProducto);

                return "✅ Producto añadido con éxito: " + nombre;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return "⚠ Error al procesar la respuesta de OpenAI.";
        }

        return respuestaIA; // Si no hay una acción específica, devuelve la respuesta de OpenAI
    }
    private String procesarRespuestaIAV2(String respuestaIA, Negocio negocio) {
        try {
            JSONObject jsonResponse = new JSONObject(respuestaIA);
            String contenidoIA = jsonResponse.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");

            // 👇 Mostrar en consola la respuesta bruta de la IA
            System.out.println("🔍 Respuesta de OpenAI: " + contenidoIA);

            // 👇 Validar si empieza como JSON
            if (!contenidoIA.trim().startsWith("{")) {
                return "⚠ La IA no respondió en formato JSON. Intenta de nuevo o afina el prompt.";
            }

            ObjectMapper mapper = new ObjectMapper();
            AccionIA accion = mapper.readValue(contenidoIA, AccionIA.class);

            switch (accion.getAccion()) {
                case "crear_producto":
                    Producto nuevo = new Producto();
                    nuevo.setNombre(accion.getProducto());
                    nuevo.setStock(accion.getStock());
                    nuevo.setPrecio(accion.getPrecio());
                    nuevo.setPrecioProveedor(accion.getPrecioProveedor());
                    nuevo.setDisponible(true);
                    nuevo.setNegocio(negocio);
                    productoRepo.save(nuevo);
                    guardarRegistro("crear_producto", "Producto creado: " + nuevo.getNombre(), negocio.getUsuario());
                    return "✅ Producto añadido con éxito.";

                case "crear_promocion":
                    guardarRegistro("crear_promocion", "Promoción del " + accion.getDescuento() + "% aplicada a: " + accion.getProductos(), negocio.getUsuario());
                    return "🎉 Se ha creado una promoción.";

                case "alertar_stock_bajo":
                    guardarRegistro("alertar_stock_bajo", "Productos con stock bajo: " + accion.getProductos(), negocio.getUsuario());
                    return "⚠ Alerta de stock bajo generada.";

                case "sugerir_producto_nuevo":
                    Producto sugerido = new Producto();
                    sugerido.setNombre(accion.getNombre());
                    sugerido.setPrecio(accion.getPrecio());
                    sugerido.setStock(10); // stock inicial por defecto
                    sugerido.setPrecioProveedor(accion.getPrecioProveedor() != null ? accion.getPrecioProveedor() : 0.0);
                    sugerido.setNegocio(negocio);
                    sugerido.setDisponible(true);
                    productoRepo.save(sugerido);
                    guardarRegistro("sugerir_producto_nuevo", "Producto sugerido: " + sugerido.getNombre(), negocio.getUsuario());
                    return "✨ Producto sugerido añadido.";

                default:
                    return "🤖 Acción no reconocida: " + accion.getAccion();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "⚠ Error al procesar la respuesta de OpenAI.";
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






    public String consultarAPIOpenAI(String mensaje) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + API_KEY);
        headers.setContentType(MediaType.APPLICATION_JSON);

        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "gpt-4o-mini"); // O "gpt-3.5-turbo" si prefieres ahorrar
        requestBody.put("stream", false);

        // 🧠 SYSTEM MESSAGE - ¡clave para que se porte bien!
        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", """
Eres un asistente inteligente que ayuda a negocios a mejorar su tienda.

Tu respuesta debe tener **dos partes**:
1. Un mensaje natural y amable.
2. Si es una accion no preguntes (hazlo)
3. Si es una recomendacion pregunta si quieres ejecutarlo
4. Justo debajo, un JSON con la acción sugerida (sin explicación, sin texto extra).

Ejemplo:
💡 Te recomiendo añadir *Pan de Calabaza* por 3.5€ (Temporada). ¿Quieres que lo añada a tus productos?

{
  "accion": "sugerir_producto_nuevo",
  "nombre": "Pan de Calabaza",
  "precio": 3.5,
  "categoria": "Temporada"
}

Nunca devuelvas más texto fuera del JSON. Solo un texto bonito + el JSON. El JSON debe estar al final.
""");

        // USER MESSAGE (tu prompt generado)
        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        userMessage.put("content", mensaje);

        JSONArray messages = new JSONArray();
        messages.put(systemMessage);
        messages.put(userMessage);

        requestBody.put("messages", messages);

        HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);

        ResponseEntity<String> response = restTemplate.exchange(API_URL, HttpMethod.POST, entity, String.class);

        return response.getBody();
    }


    // ✅ 1. MÉTODO ACTUALIZADO: Solo analiza la respuesta, no ejecuta nada directamente
    // ✅ 1. MÉTODO ACTUALIZADO: Solo analiza la respuesta, extrae el JSON de un mensaje mixto IA (texto + json)
    public AccionIA analizarSugerenciaIA(String respuestaIA) {
        try {
            JSONObject jsonResponse = new JSONObject(respuestaIA);
            String contenidoIA = jsonResponse.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");

            // 👇 Mostrar en consola el mensaje completo de la IA
            System.out.println("📩 Mensaje completo de la IA:\n" + contenidoIA);

            // 👇 Buscar dónde empieza el JSON dentro del texto
            int startIndex = contenidoIA.indexOf("{");
            if (startIndex == -1) {
                throw new RuntimeException("La IA no devolvió un JSON válido en su mensaje.");
            }

            String soloJson = contenidoIA.substring(startIndex);

            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(soloJson, AccionIA.class);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al analizar respuesta IA con formato mixto (texto + JSON).");
        }
    }




    public String ejecutarAccionIA(AccionIA accion,UUID idNegocio) {
        Optional<Negocio> negocio = negocioRepo.findById(idNegocio);
        switch (accion.getAccion()) {
            case "sugerir_producto_nuevo":
                Producto sugerido = new Producto();
                sugerido.setNombre(accion.getNombre());
                sugerido.setPrecio(accion.getPrecio());
                sugerido.setDisponible(true);
                sugerido.setStock(10);
                sugerido.setPrecioProveedor(
                        accion.getPrecioProveedor() != null ? accion.getPrecioProveedor() : 0.0
                );
                // Aquí necesitarías pasar el negocio y el usuario
                // Esto puede venir como parte del AccionIA o de sesión
                sugerido.setNegocio(negocio.get());
                productoRepo.save(sugerido);
                guardarRegistro("sugerir_producto_nuevo", "Producto sugerido: " + sugerido.getNombre(), negocio.get().getUsuario());
                return "✅ Producto añadido con éxito.";
            // Añade aquí los otros casos...

            case "añadir_producto":
                Producto nuevo = new Producto();
                nuevo.setNombre(accion.getNombre());
                nuevo.setPrecio(accion.getPrecio());
                nuevo.setDisponible(true);
                nuevo.setStock(10);
                nuevo.setPrecioProveedor(
                        accion.getPrecioProveedor() != null ? accion.getPrecioProveedor() : 0.0
                );
                // Aquí necesitarías pasar el negocio y el usuario
                // Esto puede venir como parte del AccionIA o de sesión
                nuevo.setNegocio(negocio.get());
                productoRepo.save(nuevo);
                guardarRegistro("sugerir_producto_nuevo", "Producto sugerido: " + nuevo.getNombre(), negocio.get().getUsuario());
                return "✅ Producto añadido con éxito.";
            default:
                return "⚠ Acción no reconocida: " + accion.getAccion();
        }
    }



}
