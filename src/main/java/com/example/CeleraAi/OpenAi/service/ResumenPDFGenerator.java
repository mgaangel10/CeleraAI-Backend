package com.example.CeleraAi.OpenAi.service;



import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.Map;
@Service
public class ResumenPDFGenerator {
    public static byte[] generarResumenPDF(double totalVentas, double gastos, double beneficio, Map<String, Integer> productos) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4);
        PdfWriter.getInstance(doc, baos);
        doc.open();

        Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD, Color.BLUE);
        Font normalFont = new Font(Font.HELVETICA, 12);

        doc.add(new Paragraph("📊 Resumen Diario - " + LocalDate.now(), titleFont));
        doc.add(Chunk.NEWLINE);
        doc.add(new Paragraph("🛙 Ventas totales: %.2f€".formatted(totalVentas), normalFont));
        doc.add(new Paragraph("💸 Gastos estimados: %.2f€".formatted(gastos), normalFont));
        doc.add(new Paragraph("💰 Beneficio estimado: %.2f€".formatted(beneficio), normalFont));
        doc.add(Chunk.NEWLINE);

        doc.add(new Paragraph("🏆 Productos más vendidos:", titleFont));
        for (Map.Entry<String, Integer> entry : productos.entrySet()) {
            doc.add(new Paragraph(" - %s (x%d)".formatted(entry.getKey(), entry.getValue()), normalFont));
        }

        doc.close();
        return baos.toByteArray();
    }
}