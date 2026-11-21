/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       JwtService.java
 [**/

package com.pucp.dp1.grupo4d.morapack.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pucp.dp1.grupo4d.morapack.model.entity.UsuarioEntity;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.TipoUsuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Token de acceso JWT (HS256) de corta duración (10 min). Claims: sub (id), cod, rol, sid (sesión), iat, exp.
 * La clave es SHA-256(JWT_SECRET). Si JWT_SECRET no está definido se genera una al arrancar: los tokens de acceso
 * dejan de valer al reiniciar el server, pero el front los renueva solo con su token de refresco (la sesión sigue).
 */
@Service
public class JwtService {
    public static final long DURACION_ACCESO_MS = 10L * 60 * 1000;
    private static final Base64.Encoder B64 = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder B64D = Base64.getUrlDecoder();
    private static final String CABECERA = B64.encodeToString("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));

    // Token ya validado
    public record TokenAcceso(Integer idUsuario, String codigo, TipoUsuario rol, Long idSesion, long emitidoEnSeg, long expiraEnSeg) {}

    public static class TokenInvalidoException extends Exception {
        private final boolean expirado;
        public TokenInvalidoException(boolean expirado) {
            super(expirado ? "Token expirado" : "Token inválido");
            this.expirado = expirado;
        }
        public boolean isExpirado() { return expirado; }
    }

    private final byte[] clave;
    private final ObjectMapper objectMapper;

    public JwtService(@Value("${morapack.jwt.secreto:}") String secreto, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        byte[] material;
        if (secreto == null || secreto.isBlank()) {
            material = new byte[32];
            new SecureRandom().nextBytes(material);
            System.out.println("[*] JWT_SECRET no definido: se generó una clave temporal (los tokens de acceso no sobreviven a un reinicio).");
        } else {
            material = secreto.getBytes(StandardCharsets.UTF_8);
        }
        try {
            this.clave = MessageDigest.getInstance("SHA-256").digest(material);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    // 'expiraEnMs' (epoch ms) ya viene acotado por el fin de la sesión
    public String emitir(UsuarioEntity usuario, Long idSesion, long expiraEnMs) {
        long ahora = System.currentTimeMillis() / 1000;
        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("sub", String.valueOf(usuario.getId()));
        claims.put("cod", usuario.getCodigo());
        claims.put("rol", usuario.getRol().toString());
        claims.put("sid", idSesion);
        claims.put("iat", ahora);
        claims.put("exp", expiraEnMs / 1000);
        try {
            String cuerpo = B64.encodeToString(objectMapper.writeValueAsBytes(claims));
            String firmable = CABECERA + "." + cuerpo;
            return firmable + "." + B64.encodeToString(firmar(firmable));
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo emitir el token de acceso.", e);
        }
    }

    public TokenAcceso validar(String token) throws TokenInvalidoException {
        if (token == null) throw new TokenInvalidoException(false);
        String[] partes = token.split("\\.");
        if (partes.length != 3 || !CABECERA.equals(partes[0])) throw new TokenInvalidoException(false);
        try {
            byte[] esperada = firmar(partes[0] + "." + partes[1]);
            byte[] recibida = B64D.decode(partes[2]);
            if (!MessageDigest.isEqual(esperada, recibida)) throw new TokenInvalidoException(false);
            JsonNode claims = objectMapper.readTree(B64D.decode(partes[1]));
            long exp = claims.path("exp").asLong(0);
            if (exp <= System.currentTimeMillis() / 1000) throw new TokenInvalidoException(true);
            TipoUsuario rol = TipoUsuario.valueOf(claims.path("rol").asText());
            return new TokenAcceso(Integer.valueOf(claims.path("sub").asText()), claims.path("cod").asText(), rol,
                    claims.path("sid").asLong(), claims.path("iat").asLong(0), exp);
        } catch (TokenInvalidoException e) {
            throw e;
        } catch (Exception e) {
            throw new TokenInvalidoException(false);
        }
    }

    private byte[] firmar(String datos) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(clave, "HmacSHA256"));
        return mac.doFinal(datos.getBytes(StandardCharsets.UTF_8));
    }
}
