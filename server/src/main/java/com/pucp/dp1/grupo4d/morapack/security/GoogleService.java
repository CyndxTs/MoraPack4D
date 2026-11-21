/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       GoogleService.java
 [**/

package com.pucp.dp1.grupo4d.morapack.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pucp.dp1.grupo4d.morapack.model.exception.G4DException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Verifica el ID token de Google Identity Services con el endpoint oficial 'tokeninfo'
 * (firma, vigencia y emisor los valida Google; aquí se exige audiencia = GOOGLE_CLIENT_ID y correo verificado).
 */
@Service
public class GoogleService {
    private static final String URL_TOKENINFO = "https://oauth2.googleapis.com/tokeninfo?id_token=";

    public record CuentaGoogle(String correo, String nombres, String apellidos) {}

    private final String clientId;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    public GoogleService(@Value("${morapack.google.client-id:}") String clientId, ObjectMapper objectMapper) {
        this.clientId = clientId;
        this.objectMapper = objectMapper;
    }

    public CuentaGoogle verificar(String credencial) {
        if (clientId == null || clientId.isBlank()) throw new G4DException("El inicio de sesión con Google no está configurado (GOOGLE_CLIENT_ID).");
        if (credencial == null || credencial.isBlank()) throw new G4DException("Falta la credencial de Google.");
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(URL_TOKENINFO + URLEncoder.encode(credencial.trim(), StandardCharsets.UTF_8)))
                    .timeout(Duration.ofSeconds(10)).GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) throw new G4DException("La credencial de Google no es válida o expiró. Intenta nuevamente.");
            JsonNode info = objectMapper.readTree(response.body());
            String aud = info.path("aud").asText("");
            String iss = info.path("iss").asText("");
            boolean verificado = "true".equalsIgnoreCase(info.path("email_verified").asText(""));
            if (!clientId.equals(aud) || !(iss.equals("accounts.google.com") || iss.equals("https://accounts.google.com"))) {
                throw new G4DException("La credencial de Google no corresponde a esta aplicación.");
            }
            if (!verificado) throw new G4DException("Tu correo de Google no está verificado.");
            String correo = info.path("email").asText(null);
            if (correo == null || correo.isBlank()) throw new G4DException("Google no devolvió un correo.");
            String nombres = (texto(info, "given_name") != null) ? texto(info, "given_name") : texto(info, "name");
            return new CuentaGoogle(correo, nombres, texto(info, "family_name"));
        } catch (G4DException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new G4DException("No se pudo verificar la cuenta de Google.");
        } catch (Exception e) {
            System.out.printf("[*] ERROR AL VERIFICAR GOOGLE: %s%n", e.getMessage());
            throw new G4DException("No se pudo verificar la cuenta de Google. Intenta nuevamente.");
        }
    }

    private static String texto(JsonNode info, String campo) {
        String v = info.path(campo).asText(null);
        return (v != null && !v.isBlank()) ? v.trim() : null;
    }
}
