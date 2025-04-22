package com.example.CeleraAi.OpenAi.service;



import com.example.CeleraAi.Negocio.model.Negocio;
import com.example.CeleraAi.Negocio.repositorio.NegocioRepo;
import com.example.CeleraAi.Venta.model.Venta;
import com.example.CeleraAi.Venta.repositorio.VentaRepo;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class GeneradorResumenPDF {
    private final NegocioRepo negocioRepo;
    private final VentaRepo ventaRepo;

    public byte[] generarResumenEnPDF(UUID idNegocio) {
        Optional<Negocio> negocioOpt = negocioRepo.cargarConVentas(idNegocio);

        List<Venta> ventasHoy = ventaRepo.findVentasConDetallesByNegocio(negocioOpt.get().getId()).stream()
                .filter(v -> v.getFecha().isEqual(LocalDate.now()))
                .toList();


        double total = ventasHoy.stream().mapToDouble(Venta::getTotalVenta).sum();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document doc = new Document();
            PdfWriter.getInstance(doc, baos);
            doc.open();

            // Título
            Font tituloFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLUE);
            doc.add(new Paragraph("Resumen Diario - " + LocalDate.now(), tituloFont));
            doc.add(new Paragraph("\n"));

            // Tabla
            PdfPTable tabla = new PdfPTable(3);
            tabla.setWidthPercentage(100);
            tabla.setSpacingBefore(10);

            Stream.of("Producto", "Cantidad", "Total").forEach(col -> {
                PdfPCell cell = new PdfPCell(new Phrase(col));
                cell.setBackgroundColor(Color.LIGHT_GRAY);
                tabla.addCell(cell);
            });

            ventasHoy.forEach(v -> v.getDetalleVentas().forEach(d -> {
                tabla.addCell(d.getProdcuto().getNombre());
                tabla.addCell(String.valueOf(d.getCantidad()));
                tabla.addCell(d.getProdcuto().getPrecio() + "€");
            }));

            doc.add(tabla);
            doc.add(new Paragraph("\nTotal del día: " + total + "€"));
            doc.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error al generar PDF", e);
        }
    }
}
