/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       SesionService.java
 [**/

package com.pucp.dp1.grupo4d.morapack.security;

import com.pucp.dp1.grupo4d.morapack.model.entity.SesionEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.UsuarioEntity;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.EstadoUsuario;
import com.pucp.dp1.grupo4d.morapack.model.exception.G4DAuthException;
import com.pucp.dp1.grupo4d.morapack.repository.SesionRepository;
import com.pucp.dp1.grupo4d.morapack.repository.UsuarioRepository;
import com.pucp.dp1.grupo4d.morapack.repository.VerificacionCorreoRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sesiones por dispositivo (independientes entre sí): duran 12 h desde el inicio de sesión, sin extenderse.
 * El token de refresco es aleatorio y solo se guarda su hash. Los tokens de acceso (10 min) se pueden
 * invalidar antes de tiempo (cierre de sesión, cambio de rol, cuenta deshabilitada): el filtro los rechaza
 * y el front pide uno nuevo con el token de refresco (que falla si la sesión ya no es vigente).
 */
@Service
public class SesionService {
    public static final Duration DURACION_SESION = Duration.ofHours(12);
    private static final SecureRandom RANDOM = new SecureRandom();

    public record SesionIniciada(SesionEntity sesion, String tokenRefresco) {}

    private final SesionRepository sesionRepository;
    private final UsuarioRepository usuarioRepository;
    private final VerificacionCorreoRepository verificacionCorreoRepository;
    // idSesion -> instante (epoch s) desde el cual sus tokens de acceso ya no valen
    private final Map<Long, Long> invalidadasDesde = new ConcurrentHashMap<>();

    public SesionService(SesionRepository sesionRepository, UsuarioRepository usuarioRepository, VerificacionCorreoRepository verificacionCorreoRepository) {
        this.sesionRepository = sesionRepository;
        this.usuarioRepository = usuarioRepository;
        this.verificacionCorreoRepository = verificacionCorreoRepository;
    }

    public static String hash(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public static long epochMs(LocalDateTime fechaHoraUTC) {
        return fechaHoraUTC.toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    @Transactional
    public SesionIniciada iniciar(UsuarioEntity usuario, String dispositivo) {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String tokenRefresco = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        LocalDateTime ahora = LocalDateTime.now();
        SesionEntity sesion = new SesionEntity();
        sesion.setUsuario(usuario);
        sesion.setHashToken(hash(tokenRefresco));
        sesion.setDispositivo((dispositivo != null && dispositivo.length() > 255) ? dispositivo.substring(0, 255) : dispositivo);
        sesion.setFechaHoraInicio(ahora);
        sesion.setFechaHoraExpiracion(ahora.plus(DURACION_SESION));
        sesionRepository.save(sesion);
        if (usuario.getEstado() == EstadoUsuario.OFFLINE) {
            usuario.setEstado(EstadoUsuario.ONLINE);
            usuarioRepository.save(usuario);
        }
        return new SesionIniciada(sesion, tokenRefresco);
    }

    // Sesión vigente del token de refresco (con su usuario cargado) o 401 SESION_EXPIRADA
    @Transactional(readOnly = true)
    public SesionEntity validarRefresco(String tokenRefresco) {
        if (tokenRefresco == null || tokenRefresco.isBlank()) {
            throw new G4DAuthException(G4DAuthException.SESION_EXPIRADA, "Tu sesión expiró. Vuelve a iniciar sesión.");
        }
        SesionEntity sesion = sesionRepository.findByHashToken(hash(tokenRefresco.trim()))
                .orElseThrow(() -> new G4DAuthException(G4DAuthException.SESION_EXPIRADA, "Tu sesión expiró. Vuelve a iniciar sesión."));
        if (!sesion.estaVigente(LocalDateTime.now())) {
            throw new G4DAuthException(G4DAuthException.SESION_EXPIRADA, "Tu sesión expiró. Vuelve a iniciar sesión.");
        }
        UsuarioEntity usuario = sesion.getUsuario();
        usuario.getCodigo(); // inicializa el proxy dentro de la transacción
        if (usuario.getPais() != null) usuario.getPais().getNombre();
        if (usuario.getEstado() == EstadoUsuario.DISABLED) {
            throw new G4DAuthException(G4DAuthException.SESION_EXPIRADA, "Tu cuenta fue deshabilitada.");
        }
        return sesion;
    }

    @Transactional
    public void revocar(String tokenRefresco) {
        if (tokenRefresco == null || tokenRefresco.isBlank()) return;
        sesionRepository.findByHashToken(hash(tokenRefresco.trim())).ifPresent(sesion -> {
            if (sesion.getFechaHoraRevocacion() == null) {
                sesion.setFechaHoraRevocacion(LocalDateTime.now());
                sesionRepository.save(sesion);
            }
            this.invalidar(sesion.getId());
            this.actualizarEstado(sesion.getUsuario());
        });
    }

    // Cierra todas las sesiones del usuario (opcionalmente menos una)
    @Transactional
    public void revocarTodas(UsuarioEntity usuario, Long exceptoIdSesion) {
        LocalDateTime ahora = LocalDateTime.now();
        List<Long> ids = sesionRepository.findIdsVigentes(usuario.getId(), ahora);
        for (Long id : ids) {
            if (id.equals(exceptoIdSesion)) continue;
            sesionRepository.findById(id).ifPresent(s -> {
                s.setFechaHoraRevocacion(ahora);
                sesionRepository.save(s);
            });
            this.invalidar(id);
        }
        this.actualizarEstado(usuario);
    }

    // Los tokens de acceso actuales del usuario dejan de valer (el front los renueva y recibe el rol/código nuevo)
    @Transactional(readOnly = true)
    public void invalidarTokens(UsuarioEntity usuario) {
        sesionRepository.findIdsVigentes(usuario.getId(), LocalDateTime.now()).forEach(this::invalidar);
    }

    private void invalidar(Long idSesion) {
        invalidadasDesde.put(idSesion, System.currentTimeMillis() / 1000);
    }

    public boolean tokenInvalidado(Long idSesion, long emitidoEnSeg) {
        Long desde = invalidadasDesde.get(idSesion);
        return desde != null && emitidoEnSeg < desde;
    }

    private void actualizarEstado(UsuarioEntity usuario) {
        if (usuario.getEstado() == EstadoUsuario.DISABLED) return;
        boolean tieneSesiones = !sesionRepository.findIdsVigentes(usuario.getId(), LocalDateTime.now()).isEmpty();
        EstadoUsuario estado = tieneSesiones ? EstadoUsuario.ONLINE : EstadoUsuario.OFFLINE;
        if (usuario.getEstado() != estado) {
            usuario.setEstado(estado);
            usuarioRepository.save(usuario);
        }
    }

    // Limpieza cada 20 min: sesiones y códigos caducados; marcas de invalidación más viejas que un token de acceso
    @Scheduled(cron = "0 */20 * * * *", zone = "UTC")
    @Transactional
    public void limpiar() {
        LocalDateTime corte = LocalDateTime.now().minusDays(1);
        int sesiones = sesionRepository.eliminarCaducadas(corte);
        sesionRepository.marcarDesconectados(LocalDateTime.now());
        int codigos = verificacionCorreoRepository.eliminarCaducadas(LocalDateTime.now());
        long limite = System.currentTimeMillis() / 1000 - (JwtService.DURACION_ACCESO_MS / 1000) - 60;
        invalidadasDesde.values().removeIf(v -> v < limite);
        if (sesiones + codigos > 0) System.out.printf("[~] SESIONES/CÓDIGOS CADUCADOS ELIMINADOS ('%d'/'%d')%n", sesiones, codigos);
    }
}
