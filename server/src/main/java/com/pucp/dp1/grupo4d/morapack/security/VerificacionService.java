/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       VerificacionService.java
 [**/

package com.pucp.dp1.grupo4d.morapack.security;

import com.pucp.dp1.grupo4d.morapack.model.entity.VerificacionCorreoEntity;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.PropositoVerificacion;
import com.pucp.dp1.grupo4d.morapack.model.exception.G4DException;
import com.pucp.dp1.grupo4d.morapack.repository.VerificacionCorreoRepository;
import com.pucp.dp1.grupo4d.morapack.service.CorreoService;
import com.pucp.dp1.grupo4d.morapack.util.G4DSecurity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.function.Consumer;

/**
 * Códigos de 6 dígitos por correo (registro y recuperación de contraseña).
 *  - Vigencia 10 min, reenvío permitido cada 120 s, máximo 5 intentos por código.
 *  - Solo se guarda el hash (BCrypt) del código.
 */
@Service
public class VerificacionService {
    public static final int SEGUNDOS_REENVIO = 120;
    public static final int MINUTOS_VIGENCIA = 10;
    public static final int MAX_INTENTOS = 5;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final VerificacionCorreoRepository repository;
    private final CorreoService correoService;

    public VerificacionService(VerificacionCorreoRepository repository, CorreoService correoService) {
        this.repository = repository;
        this.correoService = correoService;
    }

    private static int segundosRestantes(VerificacionCorreoEntity v, LocalDateTime ahora) {
        long transcurridos = Duration.between(v.getFechaHoraEnvio(), ahora).getSeconds();
        return (int) Math.max(0, SEGUNDOS_REENVIO - transcurridos);
    }

    /**
     * Crea o actualiza la verificación (con 'datos' del registro pendiente) y envía un código nuevo si ya pasó
     * el tiempo de reenvío. Devuelve los segundos que faltan para poder reenviar.
     */
    @Transactional
    public int solicitar(String correo, PropositoVerificacion proposito, Consumer<VerificacionCorreoEntity> datos) {
        LocalDateTime ahora = LocalDateTime.now();
        VerificacionCorreoEntity v = repository.findByCorreoAndProposito(correo, proposito).orElse(null);
        boolean nueva = (v == null);
        if (nueva) {
            v = new VerificacionCorreoEntity();
            v.setCorreo(correo);
            v.setProposito(proposito);
        }
        if (datos != null) datos.accept(v);
        boolean vencida = !nueva && !v.getFechaHoraExpiracion().isAfter(ahora);
        if (nueva || vencida || segundosRestantes(v, ahora) == 0) {
            this.enviarNuevoCodigo(v, ahora);
            return SEGUNDOS_REENVIO;
        }
        repository.save(v);
        return segundosRestantes(v, ahora);
    }

    // Reenvío explícito ("Reenviar código"): solo si ya pasaron 120 s
    @Transactional
    public int reenviar(String correo, PropositoVerificacion proposito) {
        LocalDateTime ahora = LocalDateTime.now();
        VerificacionCorreoEntity v = repository.findByCorreoAndProposito(correo, proposito).orElse(null);
        if (v == null) {
            if (proposito == PropositoVerificacion.RECUPERACION) return SEGUNDOS_REENVIO; // no revela si el correo existe
            throw new G4DException("No hay un registro pendiente para ese correo. Vuelve a llenar el formulario.");
        }
        int restantes = segundosRestantes(v, ahora);
        if (restantes > 0) throw new G4DException(String.format("Espera %d segundos para reenviar el código.", restantes));
        this.enviarNuevoCodigo(v, ahora);
        return SEGUNDOS_REENVIO;
    }

    private void enviarNuevoCodigo(VerificacionCorreoEntity v, LocalDateTime ahora) {
        String codigo = String.format("%06d", RANDOM.nextInt(1_000_000));
        v.setHashCodigo(G4DSecurity.hashear(codigo));
        v.setIntentos(0);
        v.setFechaHoraEnvio(ahora);
        v.setFechaHoraExpiracion(ahora.plusMinutes(MINUTOS_VIGENCIA));
        repository.save(v);
        boolean esRegistro = v.getProposito() == PropositoVerificacion.REGISTRO;
        correoService.enviarCodigo(v.getCorreo(), codigo,
                esRegistro ? "Tu código de verificación - MoraPack4D" : "Restablece tu contraseña - MoraPack4D",
                esRegistro ? "Usa este código para confirmar tu correo y crear tu cuenta." : "Usa este código para restablecer tu contraseña.");
    }

    /**
     * Comprueba el código. Un intento fallido se guarda aunque se lance el error (noRollbackFor).
     * No consume la verificación: llamar a 'consumir' cuando la operación termine bien.
     */
    @Transactional(noRollbackFor = G4DException.class)
    public VerificacionCorreoEntity verificar(String correo, PropositoVerificacion proposito, String codigo) {
        VerificacionCorreoEntity v = repository.findByCorreoAndProposito(correo, proposito)
                .orElseThrow(() -> new G4DException("El código no es válido o ya expiró. Solicita uno nuevo."));
        if (!v.getFechaHoraExpiracion().isAfter(LocalDateTime.now())) throw new G4DException("El código expiró. Solicita uno nuevo.");
        if (v.getIntentos() >= MAX_INTENTOS) throw new G4DException("Demasiados intentos fallidos. Solicita un nuevo código.");
        String limpio = (codigo != null) ? codigo.replaceAll("\\D", "") : "";
        if (limpio.length() != 6 || !G4DSecurity.verificar(limpio, v.getHashCodigo())) {
            v.setIntentos(v.getIntentos() + 1);
            repository.save(v);
            int quedan = MAX_INTENTOS - v.getIntentos();
            throw new G4DException(quedan > 0 ? String.format("Código incorrecto. Te quedan %d intento(s).", quedan) : "Demasiados intentos fallidos. Solicita un nuevo código.");
        }
        if (v.getPais() != null) v.getPais().getNombre();
        return v;
    }

    @Transactional
    public void consumir(VerificacionCorreoEntity v) {
        repository.findById(v.getId()).ifPresent(repository::delete);
    }
}
