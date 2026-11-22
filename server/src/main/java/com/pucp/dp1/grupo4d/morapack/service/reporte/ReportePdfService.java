/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       ReportePdfService.java
 [**/

package com.pucp.dp1.grupo4d.morapack.service.reporte;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.stereotype.Service;
import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Reporte PDF de una simulación o de la operación (OpenPDF). Página A4 horizontal, apta para imprimir:
 * resumen con indicadores, tabla de pedidos atendidos con sus rutas y, si hay, los pedidos sin atender.
 * Las fechas se muestran en el huso del usuario que lo pidió (o en UTC).
 */
@Service
public class ReportePdfService {
    private static final Color MARINO = new Color(12, 22, 49);
    private static final Color MARINO_2 = new Color(24, 42, 92);
    private static final Color AMBAR = new Color(255, 181, 71);
    private static final Color CIELO = new Color(79, 141, 255);
    private static final Color TINTA = new Color(28, 34, 52);
    private static final Color TENUE = new Color(104, 116, 146);
    private static final Color LINEA = new Color(222, 228, 240);
    private static final Color FONDO_FILA = new Color(246, 248, 252);
    private static final Color OK = new Color(22, 150, 92);
    private static final Color ALERTA = new Color(206, 52, 72);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public record NoAtendido(String codigo, String cliente, String destino, int cantidad, LocalDateTime generado, String motivo) {}

    public record Resumen(String titulo, String idTransaccion, Integer husoHorario, Double fitness,
                          Double utilizacionTemporal, Double desviacionEspacial, Double disposicionOperacional,
                          int vuelos, int rutas) {}

    private static Font fuente(float tamanio, int estilo, Color color) {
        return FontFactory.getFont(FontFactory.HELVETICA, BaseFont.CP1252, false, tamanio, estilo, color);
    }

    private String zona(Integer huso) {
        if (huso == null || huso == 0) return "UTC";
        return String.format("UTC%s%d", huso > 0 ? "+" : "-", Math.abs(huso));
    }

    private String fecha(LocalDateTime f, Integer huso) {
        if (f == null) return "-";
        return (huso == null ? f : f.plusHours(huso)).format(FMT);
    }

    public void generar(Path destino, Resumen resumen, List<FilaReporte> filas, List<NoAtendido> noAtendidos) throws IOException {
        Document doc = new Document(PageSize.A4.rotate(), 32, 32, 36, 40);
        try (OutputStream out = new FileOutputStream(destino.toFile())) {
            PdfWriter writer = PdfWriter.getInstance(doc, out);
            writer.setPageEvent(new PiePagina(resumen, this.zona(resumen.husoHorario())));
            doc.addTitle(resumen.titulo());
            doc.addAuthor("MoraPack");
            doc.open();
            this.cabecera(doc, resumen, filas, noAtendidos);
            this.indicadores(doc, resumen, filas, noAtendidos);
            this.tablaPedidos(doc, resumen, filas);
            if (!noAtendidos.isEmpty()) this.tablaNoAtendidos(doc, resumen, noAtendidos);
            doc.close();
        } catch (DocumentException e) {
            throw new IOException("No se pudo generar el PDF: " + e.getMessage(), e);
        }
    }

    private void cabecera(Document doc, Resumen r, List<FilaReporte> filas, List<NoAtendido> noAtendidos) throws DocumentException {
        PdfPTable banda = new PdfPTable(new float[]{3f, 2f});
        banda.setWidthPercentage(100);
        PdfPCell izq = new PdfPCell();
        izq.setBackgroundColor(MARINO);
        izq.setBorder(Rectangle.NO_BORDER);
        izq.setPadding(16);
        izq.addElement(new Paragraph("MORAPACK", fuente(9, Font.BOLD, AMBAR)));
        Paragraph titulo = new Paragraph(r.titulo(), fuente(20, Font.BOLD, Color.WHITE));
        titulo.setSpacingBefore(2);
        izq.addElement(titulo);
        izq.addElement(new Paragraph(String.format("Transacción %s", r.idTransaccion()), fuente(9, Font.NORMAL, new Color(167, 182, 218))));
        PdfPCell der = new PdfPCell();
        der.setBackgroundColor(MARINO);
        der.setBorder(Rectangle.NO_BORDER);
        der.setPadding(16);
        der.setHorizontalAlignment(Element.ALIGN_RIGHT);
        Paragraph generado = new Paragraph(String.format("Generado: %s", this.fecha(LocalDateTime.now(), r.husoHorario())), fuente(9, Font.NORMAL, Color.WHITE));
        generado.setAlignment(Element.ALIGN_RIGHT);
        der.addElement(generado);
        Paragraph zona = new Paragraph(String.format("Horas en %s", this.zona(r.husoHorario())), fuente(9, Font.NORMAL, new Color(167, 182, 218)));
        zona.setAlignment(Element.ALIGN_RIGHT);
        der.addElement(zona);
        if (!filas.isEmpty()) {
            LocalDateTime desde = filas.stream().map(FilaReporte::generado).filter(java.util.Objects::nonNull).min(LocalDateTime::compareTo).orElse(null);
            LocalDateTime hasta = filas.stream().map(FilaReporte::generado).filter(java.util.Objects::nonNull).max(LocalDateTime::compareTo).orElse(null);
            Paragraph periodo = new Paragraph(String.format("Pedidos generados del %s al %s", this.fecha(desde, r.husoHorario()), this.fecha(hasta, r.husoHorario())), fuente(9, Font.NORMAL, Color.WHITE));
            periodo.setAlignment(Element.ALIGN_RIGHT);
            der.addElement(periodo);
        }
        banda.addCell(izq);
        banda.addCell(der);
        doc.add(banda);
    }

    private void indicadores(Document doc, Resumen r, List<FilaReporte> filas, List<NoAtendido> noAtendidos) throws DocumentException {
        int paquetes = filas.stream().mapToInt(FilaReporte::cantidad).sum();
        long aTiempo = filas.stream().filter(FilaReporte::aTiempo).count();
        double horasPromedio = filas.stream().filter(f -> f.entrega() != null && f.generado() != null)
                .mapToDouble(f -> java.time.Duration.between(f.generado(), f.entrega()).toMinutes() / 60.0).average().orElse(0);
        String[][] datos = {
                {"Pedidos atendidos", String.format("%,d", filas.size())},
                {"Paquetes enviados", String.format("%,d", paquetes)},
                {"Entregados a tiempo", filas.isEmpty() ? "-" : String.format("%.1f %%", 100.0 * aTiempo / filas.size())},
                {"Entrega promedio", filas.isEmpty() ? "-" : String.format("%.1f h", horasPromedio)},
                {"Sin atender", String.format("%,d", noAtendidos.size())},
                {"Vuelos / rutas", String.format("%,d / %,d", r.vuelos(), r.rutas())},
        };
        PdfPTable tabla = new PdfPTable(datos.length);
        tabla.setWidthPercentage(100);
        tabla.setSpacingBefore(14);
        for (int i = 0; i < datos.length; i++) {
            PdfPCell c = new PdfPCell();
            c.setBorderColor(LINEA);
            c.setPadding(10);
            c.addElement(new Paragraph(datos[i][1], fuente(16, Font.BOLD, i == 4 && !noAtendidos.isEmpty() ? ALERTA : TINTA)));
            c.addElement(new Paragraph(datos[i][0], fuente(8, Font.NORMAL, TENUE)));
            tabla.addCell(c);
        }
        doc.add(tabla);
        if (r.fitness() != null) {
            Paragraph calidad = new Paragraph(String.format("Calidad de la solución (último bloque): fitness %.2f · utilización temporal %.2f %% · desviación espacial %.2f %% · disposición operacional %.2f %%",
                    r.fitness(), 100 * nz(r.utilizacionTemporal()), 100 * nz(r.desviacionEspacial()), 100 * nz(r.disposicionOperacional())), fuente(8, Font.NORMAL, TENUE));
            calidad.setSpacingBefore(6);
            doc.add(calidad);
        }
    }

    private static double nz(Double d) { return d != null ? d : 0; }

    private PdfPCell celda(String texto, Font fuente, boolean par) {
        PdfPCell c = new PdfPCell(new Phrase(texto, fuente));
        c.setBorderColor(LINEA);
        c.setBorderWidthLeft(0);
        c.setBorderWidthRight(0);
        c.setPaddingTop(5);
        c.setPaddingBottom(6);
        c.setPaddingLeft(5);
        c.setPaddingRight(5);
        if (par) c.setBackgroundColor(FONDO_FILA);
        return c;
    }

    private void encabezados(PdfPTable tabla, String... textos) {
        for (String t : textos) {
            PdfPCell c = new PdfPCell(new Phrase(t, fuente(8, Font.BOLD, Color.WHITE)));
            c.setBackgroundColor(MARINO_2);
            c.setBorderColor(MARINO_2);
            c.setPadding(6);
            tabla.addCell(c);
        }
        tabla.setHeaderRows(1);
    }

    private void titulo(Document doc, String texto, int cantidad) throws DocumentException {
        Paragraph p = new Paragraph();
        p.add(new Chunk(texto, fuente(12, Font.BOLD, TINTA)));
        p.add(new Chunk(String.format("  (%,d)", cantidad), fuente(10, Font.NORMAL, TENUE)));
        p.setSpacingBefore(16);
        p.setSpacingAfter(8);
        doc.add(p);
    }

    private void tablaPedidos(Document doc, Resumen r, List<FilaReporte> filas) throws DocumentException {
        this.titulo(doc, "Pedidos atendidos", filas.size());
        if (filas.isEmpty()) {
            doc.add(new Paragraph("No hay pedidos atendidos en este reporte.", fuente(9, Font.ITALIC, TENUE)));
            return;
        }
        PdfPTable tabla = new PdfPTable(new float[]{0.45f, 1.35f, 0.8f, 1.2f, 0.55f, 1.05f, 1.05f, 1.05f, 0.65f, 3.1f});
        tabla.setWidthPercentage(100);
        this.encabezados(tabla, "#", "Pedido", "Cliente", "Destino", "Cant.", "Generado", "Plazo", "Entrega", "Estado", "Rutas (salida › escalas › destino)");
        Font normal = fuente(8, Font.NORMAL, TINTA), tenue = fuente(8, Font.NORMAL, TENUE), negrita = fuente(8, Font.BOLD, TINTA);
        for (int i = 0; i < filas.size(); i++) {
            FilaReporte f = filas.get(i);
            boolean par = i % 2 == 1;
            tabla.addCell(celda(String.valueOf(i + 1), tenue, par));
            tabla.addCell(celda(f.codigo(), negrita, par));
            tabla.addCell(celda(f.cliente(), normal, par));
            tabla.addCell(celda(String.format("%s %s", f.codDestino(), f.ciudadDestino()).trim(), normal, par));
            tabla.addCell(celda(String.valueOf(f.cantidad()), normal, par));
            tabla.addCell(celda(this.fecha(f.generado(), r.husoHorario()), normal, par));
            tabla.addCell(celda(this.fecha(f.plazo(), r.husoHorario()), normal, par));
            tabla.addCell(celda(this.fecha(f.entrega(), r.husoHorario()), normal, par));
            tabla.addCell(celda(f.aTiempo() ? "A tiempo" : (f.entrega() == null ? "-" : "Tarde"), fuente(8, Font.BOLD, f.aTiempo() ? OK : ALERTA), par));
            tabla.addCell(celda(String.join("\n", f.rutas()), normal, par));
        }
        doc.add(tabla);
    }

    private void tablaNoAtendidos(Document doc, Resumen r, List<NoAtendido> lista) throws DocumentException {
        this.titulo(doc, "Pedidos sin atender", lista.size());
        PdfPTable tabla = new PdfPTable(new float[]{1.4f, 0.9f, 1.2f, 0.6f, 1.2f, 3.5f});
        tabla.setWidthPercentage(100);
        this.encabezados(tabla, "Pedido", "Cliente", "Destino", "Cant.", "Generado", "Motivo");
        Font normal = fuente(8, Font.NORMAL, TINTA), negrita = fuente(8, Font.BOLD, ALERTA);
        for (int i = 0; i < lista.size(); i++) {
            NoAtendido n = lista.get(i);
            boolean par = i % 2 == 1;
            tabla.addCell(celda(n.codigo(), negrita, par));
            tabla.addCell(celda(n.cliente(), normal, par));
            tabla.addCell(celda(n.destino(), normal, par));
            tabla.addCell(celda(String.valueOf(n.cantidad()), normal, par));
            tabla.addCell(celda(this.fecha(n.generado(), r.husoHorario()), normal, par));
            tabla.addCell(celda(n.motivo(), normal, par));
        }
        doc.add(tabla);
    }

    // Pie de cada página: marca, transacción y número de página
    private static class PiePagina extends PdfPageEventHelper {
        private final Resumen resumen;
        private final String zona;

        PiePagina(Resumen resumen, String zona) {
            this.resumen = resumen;
            this.zona = zona;
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            float y = document.bottom() - 20;
            cb.setColorStroke(LINEA);
            cb.setLineWidth(0.6f);
            cb.moveTo(document.left(), y + 12);
            cb.lineTo(document.right(), y + 12);
            cb.stroke();
            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, new Phrase(String.format("MoraPack · %s · %s · horas en %s", resumen.titulo(), resumen.idTransaccion(), zona), fuente(7, Font.NORMAL, TENUE)), document.left(), y, 0);
            ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT, new Phrase(String.format("Página %d", writer.getPageNumber()), fuente(7, Font.NORMAL, TENUE)), document.right(), y, 0);
        }
    }
}
