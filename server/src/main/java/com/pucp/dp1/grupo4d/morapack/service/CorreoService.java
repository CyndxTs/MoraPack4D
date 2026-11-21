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

    public void enviarCodigo(String destinatario, String codigo, String asunto, String motivo) {
        if (!estaConfigurado()) {
            System.out.printf("[DEV] Correo no configurado (BREVO_API_KEY / MAIL_REMITENTE). Código para '%s': %s%n", destinatario, codigo);
            return;
        }
        String html = """
            <div style="font-family:Segoe UI,Arial,sans-serif;max-width:480px;margin:auto;padding:28px;background:#0b1530;color:#e8eefc;border-radius:16px">
              <h2 style="margin:0 0 6px;font-weight:600;color:#ffffff">MoraPack4D</h2>
              <p style="margin:0 0 22px;color:#9fb2d9">%s</p>
              <div style="font-size:34px;letter-spacing:10px;font-weight:700;text-align:center;padding:18px;background:#13224a;border-radius:12px;color:#7cc4ff">%s</div>
              <p style="margin:22px 0 0;color:#9fb2d9;font-size:13px">El código vence en 10 minutos. Si no fuiste tú, ignora este correo.</p>
            </div>
            """.formatted(motivo, codigo);
        Map<String, Object> cuerpo = Map.of(
            "sender", Map.of("name", nombreRemitente, "email", remitente),
            "to", List.of(Map.of("email", destinatario)),
            "subject", asunto,
            "htmlContent", html,
            "textContent", String.format("%s%nTu código es: %s%nVence en 10 minutos.", motivo, codigo)
        );
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(URL_BREVO))
                    .timeout(Duration.ofSeconds(15))
                    .header("api-key", apiKey)
                    .header("accept", "application/json")
                    .header("content-type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofByteArray(objectMapper.writeValueAsBytes(cuerpo)))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 300) {
                System.out.printf("[*] BREVO RECHAZÓ EL CORREO (%d): %s%n", response.statusCode(), response.body());
                throw new G4DException("No se pudo enviar el correo de verificación. Intente nuevamente en unos minutos.");
            }
        } catch (G4DException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new G4DException("No se pudo enviar el correo de verificación.");
        } catch (Exception e) {
            System.out.printf("[*] ERROR AL ENVIAR CORREO: %s%n", e.getMessage());
            throw new G4DException("No se pudo enviar el correo de verificación. Intente nuevamente en unos minutos.");
        }
    }
}
