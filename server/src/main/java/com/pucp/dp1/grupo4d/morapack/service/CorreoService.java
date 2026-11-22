/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       CorreoService.java
 [**/

package com.pucp.dp1.grupo4d.morapack.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pucp.dp1.grupo4d.morapack.model.exception.G4DException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Correo transaccional por la API HTTP de Brevo (Render Free bloquea SMTP).
 * Sin BREVO_API_KEY (desarrollo) no se envía nada: el código se imprime en la consola del server.
 */
@Service
public class CorreoService {
    private static final String URL_BREVO = "https://api.brevo.com/v3/smtp/email";

    private final String apiKey;
    private final String remitente;
    private final String nombreRemitente;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    public CorreoService(@Value("${morapack.brevo.api-key:}") String apiKey,
                         @Value("${morapack.correo.remitente:}") String remitente,
                         @Value("${morapack.correo.remitente-nombre:MoraPack4D}") String nombreRemitente,
                         ObjectMapper objectMapper) {
        this.apiKey = apiKey;
        this.remitente = remitente;
        this.nombreRemitente = nombreRemitente;
        this.objectMapper = objectMapper;
    }

    public boolean estaConfigurado() {
        return apiKey != null && !apiKey.isBlank() && remitente != null && !remitente.isBlank();
    }

    // Adjunto de un correo (Brevo lo recibe en base64)
    public record Adjunto(String nombre, byte[] contenido) {}

    // Plantilla común: marca, título, cuerpo HTML y pie
    public static String plantilla(String titulo, String cuerpoHtml) {
        return """
            <div style="font-family:Segoe UI,Arial,sans-serif;max-width:520px;margin:auto;padding:28px;background:#0b1530;color:#e8eefc;border-radius:16px">
              <div style="font-size:12px;font-weight:700;letter-spacing:2px;color:#ffb547;margin-bottom:6px">MORAPACK</div>
              <h2 style="margin:0 0 16px;font-weight:600;color:#ffffff">%s</h2>
              %s
              <p style="margin:26px 0 0;color:#7487b3;font-size:12px">Este correo fue enviado automáticamente por MoraPack. No respondas a este mensaje.</p>
            </div>
            """.formatted(titulo, cuerpoHtml);
    }

    /**
     * Envía un correo por la API de Brevo. Lanza G4DException con un motivo entendible si Brevo lo rechaza
     * (remitente no validado, clave inválida, IP no autorizada, adjunto muy grande...).
     */
    public void enviar(String destinatario, String asunto, String html, String texto, List<Adjunto> adjuntos) {
        if (!estaConfigurado()) {
            System.out.printf("[DEV] Correo no configurado (BREVO_API_KEY / MAIL_REMITENTE). Se omitió '%s' para '%s'.%n", asunto, destinatario);
            throw new G4DException("El envío de correos no está configurado en el server (BREVO_API_KEY / MAIL_REMITENTE).");
        }
        Map<String, Object> cuerpo = new java.util.LinkedHashMap<>();
        cuerpo.put("sender", Map.of("name", nombreRemitente, "email", remitente));
        cuerpo.put("to", List.of(Map.of("email", destinatario)));
        cuerpo.put("subject", asunto);
        cuerpo.put("htmlContent", html);
        if (texto != null) cuerpo.put("textContent", texto);
        if (adjuntos != null && !adjuntos.isEmpty()) {
            cuerpo.put("attachment", adjuntos.stream()
                    .map(a -> Map.of("name", a.nombre(), "content", java.util.Base64.getEncoder().encodeToString(a.contenido())))
                    .toList());
        }
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(URL_BREVO))
                    .timeout(Duration.ofSeconds(adjuntos != null && !adjuntos.isEmpty() ? 60 : 15))
                    .header("api-key", apiKey)
                    .header("accept", "application/json")
                    .header("content-type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofByteArray(objectMapper.writeValueAsBytes(cuerpo)))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 300) {
                System.out.printf("[*] BREVO RECHAZÓ EL CORREO (%d): %s%n", response.statusCode(), response.body());
                throw new G4DException(this.motivoDeBrevo(response.statusCode(), response.body()));
            }
            System.out.printf("[@] CORREO ENVIADO a '%s' (%s)%n", destinatario, asunto);
        } catch (G4DException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new G4DException("Se interrumpió el envío del correo.");
        } catch (Exception e) {
            System.out.printf("[*] ERROR AL ENVIAR CORREO: %s%n", e.getMessage());
            throw new G4DException("No se pudo contactar al servicio de correo. Intenta nuevamente en unos minutos.");
        }
    }

    // Traduce los errores habituales de Brevo a un mensaje útil
    private String motivoDeBrevo(int status, String body) {
        String detalle = "";
        try { detalle = String.valueOf(objectMapper.readTree(body).path("message").asText("")); } catch (Exception ignored) { }
        String d = detalle.toLowerCase();
        if (status == 401 || d.contains("key not found") || d.contains("unauthorized")) return "El servicio de correo rechazó la clave (BREVO_API_KEY inválida).";
        if (d.contains("ip")) return "Brevo bloqueó la IP del server: desactiva la restricción de IP o autoriza la IP en Brevo.";
        if (d.contains("sender")) return "El remitente (MAIL_REMITENTE) no está validado en Brevo.";
        if (status == 413 || d.contains("size") || d.contains("attachment")) return "El archivo es demasiado grande para enviarlo por correo.";
        if (d.contains("email") && d.contains("valid")) return "El correo de destino no es válido.";
        return detalle.isBlank() ? String.format("El servicio de correo respondió con error (%d).", status) : "El servicio de correo rechazó el envío: " + detalle;
    }

    public void enviarCodigo(String destinatario, String codigo, String asunto, String motivo) {
        if (!estaConfigurado()) {
            System.out.printf("[DEV] Correo no configurado (BREVO_API_KEY / MAIL_REMITENTE). Código para '%s': %s%n", destinatario, codigo);
            return;
        }
        String html = plantilla("Tu código de verificación", """
              <p style="margin:0 0 22px;color:#9fb2d9">%s</p>
              <div style="font-size:34px;letter-spacing:10px;font-weight:700;text-align:center;padding:18px;background:#13224a;border-radius:12px;color:#7cc4ff">%s</div>
              <p style="margin:22px 0 0;color:#9fb2d9;font-size:13px">El código vence en 10 minutos. Si no fuiste tú, ignora este correo.</p>
            """.formatted(motivo, codigo));
        try {
            this.enviar(destinatario, asunto, html, String.format("%s%nTu código es: %s%nVence en 10 minutos.", motivo, codigo), null);
        } catch (G4DException e) {
            throw new G4DException("No se pudo enviar el correo de verificación. " + e.getMessage());
        }
    }
}
