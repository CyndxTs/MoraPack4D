/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       EnvioReporteService.java
 [**/

package com.pucp.dp1.grupo4d.morapack.service;

import com.pucp.dp1.grupo4d.morapack.model.dto.payload.StatusPayload;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.GenericResponse;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.EstadoEjecucion;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.EstadoFinalizacion;
import com.pucp.dp1.grupo4d.morapack.model.exception.G4DException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.file.*;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Reportes (txt / pdf) en la carpeta exports: envío por correo en segundo plano y limpieza.
 * Tras responder Brevo (bien o mal) se borran los dos formatos del reporte para no llenar el disco del server.
 */
@Service
public class EnvioReporteService {
    public static final Path CARPETA = Paths.get("exports");
    private static final Pattern CORREO = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}$");
    private static final Duration MAXIMA_ANTIGUEDAD = Duration.ofHours(12);

    private final CorreoService correoService;
    private final CommunicationService communicationService;
    private final ObjectProvider<EnvioReporteService> self;
    private final ObjectProvider<G4DService> g4dService;

    public EnvioReporteService(CorreoService correoService, CommunicationService communicationService,
                               ObjectProvider<EnvioReporteService> self, ObjectProvider<G4DService> g4dService) {
        this.correoService = correoService;
        this.communicationService = communicationService;
        this.self = self;
        this.g4dService = g4dService;
    }

    // Solo archivos dentro de exports (nunca rutas arbitrarias)
    public static Path resolver(String nombre) {
        Path base = CARPETA.toAbsolutePath().normalize();
        Path ruta = base.resolve(nombre == null ? "" : nombre).normalize();
        if (!ruta.startsWith(base) || ruta.equals(base)) throw new G4DException("Ruta de archivo no permitida.");
        return ruta;
    }

    // "<prefijo>__<id>.txt|pdf" -> los dos formatos del mismo reporte
    private static List<Path> formatosDe(String nombre) {
        String base = nombre.replaceAll("\\.(txt|pdf)$", "");
        return List.of(resolver(base + ".txt"), resolver(base + ".pdf"));
    }

    private static String idDe(String nombre) {
        String base = nombre.replaceAll("\\.(txt|pdf)$", "");
        int i = base.lastIndexOf("__");
        return i >= 0 ? base.substring(i + 2) : null;
    }

    // Borra los dos formatos del reporte (no falla si ya no existen)
    public void eliminarReporte(String nombre) {
        for (Path p : formatosDe(nombre)) {
            try { Files.deleteIfExists(p); } catch (IOException e) { System.out.printf("[*] NO SE PUDO BORRAR '%s': %s%n", p.getFileName(), e.getMessage()); }
        }
        g4dService.getObject().olvidarSimulacion(idDe(nombre));
    }

    public GenericResponse iniciarEnvio(String nombre, String correo) {
        String destino = correo == null ? "" : correo.trim().toLowerCase();
        if (!CORREO.matcher(destino).matches()) throw new G4DException("Escribe un correo válido.");
        Path archivo = resolver(nombre);
        if (!Files.isRegularFile(archivo)) throw new G4DException("El reporte ya no existe en el server. Vuelve a generarlo.");
        if (!correoService.estaConfigurado()) throw new G4DException("El envío de correos no está configurado en el server.");
        GenericResponse response = new GenericResponse(true, String.format("Enviando el reporte a %s...", destino));
        String id = response.getToken().substring(4);
        self.getObject().enviar(id, archivo, destino);
        return response;
    }

    @Async("exportationExecutor")
    public CompletableFuture<Void> enviar(String id, Path archivo, String destino) {
        String estado = String.format("/topic/exportation-send-status-%s", id);
        String nombre = archivo.getFileName().toString();
        try {
            byte[] contenido = Files.readAllBytes(archivo);
            boolean esPdf = nombre.toLowerCase().endsWith(".pdf");
            String titulo = nombre.startsWith("SIMULACION") ? "Reporte de simulación" : "Reporte de operación";
            String html = CorreoService.plantilla(titulo, """
                  <p style="margin:0 0 12px;color:#c9d5f0">Te compartieron un reporte de MoraPack.</p>
                  <p style="margin:0;color:#9fb2d9">Va adjunto en formato <strong style="color:#ffffff">%s</strong>: <span style="color:#7cc4ff">%s</span></p>
                """.formatted(esPdf ? "PDF" : "texto", nombre));
            correoService.enviar(destino, String.format("MoraPack · %s", titulo), html,
                    String.format("%s de MoraPack adjunto (%s).", titulo, nombre), List.of(new CorreoService.Adjunto(nombre, contenido)));
            communicationService.enviar(estado, new StatusPayload(EstadoEjecucion.DETENIDO, EstadoFinalizacion.EXITOSO, String.format("Reporte enviado a %s.", destino)));
        } catch (Exception e) {
            String mensaje = (e instanceof G4DException && e.getMessage() != null) ? e.getMessage() : "No se pudo enviar el reporte por correo.";
            communicationService.enviar(estado, new StatusPayload(EstadoEjecucion.DETENIDO, EstadoFinalizacion.ERRONEO, mensaje));
        } finally {
            // Brevo ya respondió (bien o mal): el reporte deja de ocupar espacio en el server
            this.eliminarReporte(nombre);
        }
        return CompletableFuture.completedFuture(null);
    }

    // Red de seguridad: reportes y parciales olvidados (pestañas cerradas, simulaciones abandonadas)
    @Scheduled(fixedDelay = 3600000, initialDelay = 600000)
    public void limpiarAntiguos() {
        if (!Files.isDirectory(CARPETA)) return;
        Instant limite = Instant.now().minus(MAXIMA_ANTIGUEDAD);
        try (Stream<Path> archivos = Files.list(CARPETA)) {
            archivos.filter(Files::isRegularFile).forEach(p -> {
                try {
                    if (Files.getLastModifiedTime(p).toInstant().isBefore(limite)) {
                        Files.deleteIfExists(p);
                        System.out.printf("[~] EXPORTACIÓN ANTIGUA ELIMINADA: '%s'%n", p.getFileName());
                    }
                } catch (IOException ignored) { }
            });
        } catch (IOException e) {
            System.out.printf("[*] NO SE PUDO REVISAR exports: %s%n", e.getMessage());
        }
    }
}
