/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       AuthService.java
 [**/

package com.pucp.dp1.grupo4d.morapack.service;

import com.pucp.dp1.grupo4d.morapack.mapper.UsuarioMapper;
import com.pucp.dp1.grupo4d.morapack.model.dto.UsuarioDTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.*;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.AuthenticationResponse;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.GenericResponse;
import com.pucp.dp1.grupo4d.morapack.model.entity.PaisEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.SesionEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.UsuarioEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.VerificacionCorreoEntity;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.EstadoUsuario;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.PropositoVerificacion;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.TipoUsuario;
import com.pucp.dp1.grupo4d.morapack.model.exception.G4DException;
import com.pucp.dp1.grupo4d.morapack.security.*;
import com.pucp.dp1.grupo4d.morapack.service.model.PaisService;
import com.pucp.dp1.grupo4d.morapack.service.model.UsuarioService;
import com.pucp.dp1.grupo4d.morapack.util.G4DSecurity;
import com.pucp.dp1.grupo4d.morapack.util.G4DUtility;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Flujos de autenticación:
 *  - Correo + contraseña, Google (ID token), registro de CLIENTE con código de 6 dígitos, recuperación de contraseña.
 *  - Cada inicio de sesión crea una SESION (12 h, por dispositivo) y devuelve un token de acceso de 10 min + token de refresco.
 */
@Service
public class AuthService {
    private final UsuarioService usuarioService;
    private final PaisService paisService;
    private final SesionService sesionService;
    private final JwtService jwtService;
    private final VerificacionService verificacionService;
    private final GoogleService googleService;
    private final UsuarioMapper usuarioMapper;

    public AuthService(UsuarioService usuarioService, PaisService paisService, SesionService sesionService, JwtService jwtService,
                       VerificacionService verificacionService, GoogleService googleService, UsuarioMapper usuarioMapper) {
        this.usuarioService = usuarioService;
        this.paisService = paisService;
        this.sesionService = sesionService;
        this.jwtService = jwtService;
        this.verificacionService = verificacionService;
        this.googleService = googleService;
        this.usuarioMapper = usuarioMapper;
    }

    // ---------- Sesión ----------
    private AuthenticationResponse abrirSesion(UsuarioEntity usuario, String dispositivo, String mensaje) {
        if (usuario.getEstado() == EstadoUsuario.DISABLED) throw new G4DException("La cuenta está deshabilitada. Contacta a un administrador.");
        SesionService.SesionIniciada iniciada = sesionService.iniciar(usuario, dispositivo);
        AuthenticationResponse response = this.respuestaConTokens(usuario, iniciada.sesion(), mensaje);
        response.setTokenRefresco(iniciada.tokenRefresco());
        return response;
    }

    private AuthenticationResponse respuestaConTokens(UsuarioEntity usuario, SesionEntity sesion, String mensaje) {
        long expiraSesion = SesionService.epochMs(sesion.getFechaHoraExpiracion());
        long expiraAcceso = Math.min(System.currentTimeMillis() + JwtService.DURACION_ACCESO_MS, expiraSesion);
        AuthenticationResponse response = new AuthenticationResponse(true, mensaje, usuarioMapper.toDTO(usuario));
        response.setTokenAcceso(jwtService.emitir(usuario, sesion.getId(), expiraAcceso));
        response.setExpiraAcceso(expiraAcceso);
        response.setExpiraSesion(expiraSesion);
        return response;
    }

    public AuthenticationResponse iniciarSesion(SignInRequest request, String dispositivo) {
        String correo = UsuarioService.normalizarCorreo(request.getCorreo());
        String contrasenia = request.getContrasenia();
        if (correo == null || contrasenia == null || contrasenia.isEmpty()) throw new G4DException("Ingresa tu correo y tu contraseña.");
        UsuarioEntity usuario = usuarioService.findByCorreo(correo).orElse(null);
        if (usuario != null && !usuario.tieneContrasenia()) {
            throw new G4DException("Esta cuenta se creó con Google. Ingresa con Google o usa \"¿Olvidaste tu contraseña?\" para crear una.");
        }
        if (usuario == null || !G4DSecurity.verificar(contrasenia, usuario.getContrasenia())) throw new G4DException("Correo o contraseña incorrectos.");
        return this.abrirSesion(usuario, dispositivo, "Sesión iniciada!");
    }

    // Token de acceso nuevo para la misma sesión (no extiende las 12 h). Lee rol y código actuales de la BD.
    public AuthenticationResponse refrescar(RefreshRequest request) {
        SesionEntity sesion = sesionService.validarRefresco(request.getTokenRefresco());
        AuthenticationResponse response = this.respuestaConTokens(sesion.getUsuario(), sesion, "Acceso renovado!");
        response.setTokenRefresco(request.getTokenRefresco());
        return response;
    }

    public GenericResponse cerrarSesion(SignOutRequest request) {
        sesionService.revocar(request.getTokenRefresco());
        return new GenericResponse(true, "Sesión cerrada!");
    }

    // ---------- Registro (solo CLIENTE) ----------
    public AuthenticationResponse registrar(SignUpRequest request) {
        String nombres = UsuarioService.validarNombre(request.getNombres(), "nombres");
        String apellidos = UsuarioService.validarNombreOpcional(request.getApellidos(), "apellidos");
        String correo = UsuarioService.validarCorreo(request.getCorreo());
        UsuarioService.validarContrasenia(request.getContrasenia(), request.getConfirmacion());
        if (G4DUtility.Convertor.toAdmissible(request.getPais()) == null) throw new G4DException("Selecciona tu país.");
        if (!Boolean.TRUE.equals(request.getAceptaTerminos())) throw new G4DException("Debes aceptar los términos y la política de privacidad.");
        PaisEntity pais = paisService.obtenerParaUsuario(request.getPais());
        if (usuarioService.existsByCorreo(correo)) throw new G4DException("Ese correo ya tiene una cuenta. Inicia sesión o recupera tu contraseña.");
        String hash = G4DSecurity.hashear(request.getContrasenia());
        int segundos = verificacionService.solicitar(correo, PropositoVerificacion.REGISTRO, v -> {
            v.setNombres(nombres);
            v.setApellidos(apellidos);
            v.setContrasenia(hash);
            v.setPais(pais);
        });
        AuthenticationResponse response = new AuthenticationResponse(true, String.format("Te enviamos un código de 6 dígitos a %s.", correo));
        response.setCorreo(correo);
        response.setSegundosParaReenvio(segundos);
        return response;
    }

    // Código correcto: recién aquí se crea el USUARIO (CLIENTE) y se inicia sesión
    public AuthenticationResponse verificarRegistro(CodeVerificationRequest request, String dispositivo) {
        String correo = UsuarioService.validarCorreo(request.getCorreo());
        VerificacionCorreoEntity v = verificacionService.verificar(correo, PropositoVerificacion.REGISTRO, request.getCodigo());
        if (usuarioService.existsByCorreo(correo)) {
            verificacionService.consumir(v);
            throw new G4DException("Ese correo ya tiene una cuenta. Inicia sesión.");
        }
        UsuarioEntity usuario = usuarioService.crear(v.getNombres(), v.getApellidos(), correo, v.getContrasenia(), v.getPais(), TipoUsuario.CLIENTE);
        verificacionService.consumir(v);
        return this.abrirSesion(usuario, dispositivo, "¡Cuenta creada! Bienvenido a MoraPack4D.");
    }

    public AuthenticationResponse reenviarCodigo(CodeResendRequest request) {
        String correo = UsuarioService.validarCorreo(request.getCorreo());
        PropositoVerificacion proposito = G4DUtility.Convertor.toAdmissible(request.getProposito(), PropositoVerificacion.class);
        if (proposito == null) throw new G4DException("Propósito de verificación inválido.");
        if (proposito == PropositoVerificacion.RECUPERACION && !usuarioService.existsByCorreo(correo)) {
            AuthenticationResponse r = new AuthenticationResponse(true, "Si el correo está registrado, te enviamos un código nuevo.");
            r.setCorreo(correo);
            r.setSegundosParaReenvio(VerificacionService.SEGUNDOS_REENVIO);
            return r;
        }
        int segundos = verificacionService.reenviar(correo, proposito);
        AuthenticationResponse response = new AuthenticationResponse(true, "Te enviamos un código nuevo.");
        response.setCorreo(correo);
        response.setSegundosParaReenvio(segundos);
        return response;
    }

    // ---------- Recuperación de contraseña ----------
    public AuthenticationResponse olvideContrasenia(PasswordForgotRequest request) {
        String correo = UsuarioService.validarCorreo(request.getCorreo());
        int segundos = VerificacionService.SEGUNDOS_REENVIO;
        if (usuarioService.existsByCorreo(correo)) {
            segundos = verificacionService.solicitar(correo, PropositoVerificacion.RECUPERACION, null);
        } else {
            System.out.printf("[*] RECUPERACIÓN PEDIDA PARA UN CORREO NO REGISTRADO ('%s')%n", correo);
        }
        AuthenticationResponse response = new AuthenticationResponse(true, "Si el correo está registrado, te enviamos un código de 6 dígitos.");
        response.setCorreo(correo);
        response.setSegundosParaReenvio(segundos);
        return response;
    }

    // Nueva contraseña: cierra todas las sesiones anteriores e inicia una nueva en este dispositivo
    public AuthenticationResponse restablecerContrasenia(PasswordResetRequest request, String dispositivo) {
        String correo = UsuarioService.validarCorreo(request.getCorreo());
        UsuarioService.validarContrasenia(request.getContrasenia(), request.getConfirmacion());
        VerificacionCorreoEntity v = verificacionService.verificar(correo, PropositoVerificacion.RECUPERACION, request.getCodigo());
        UsuarioEntity usuario = usuarioService.findByCorreo(correo).orElseThrow(() -> new G4DException("El código no es válido o ya expiró. Solicita uno nuevo."));
        usuario.setContrasenia(G4DSecurity.hashear(request.getContrasenia()));
        usuarioService.save(usuario);
        verificacionService.consumir(v);
        sesionService.revocarTodas(usuario, null);
        return this.abrirSesion(usuario, dispositivo, "Contraseña actualizada. Sesión iniciada!");
    }

    // ---------- Google ----------
    // Correo existente: entra sin contraseña. Correo nuevo: se crea un CLIENTE (Google no da el país: se pide la primera vez).
    public AuthenticationResponse google(GoogleSignInRequest request, String dispositivo) {
        GoogleService.CuentaGoogle cuenta = googleService.verificar(request.getCredencial());
        String correo = UsuarioService.normalizarCorreo(cuenta.correo());
        UsuarioEntity existente = usuarioService.findByCorreo(correo).orElse(null);
        if (existente != null) return this.abrirSesion(existente, dispositivo, "Sesión iniciada con Google!");
        // Cuenta nueva: Google da nombres (y quizá apellidos). Solo se pide país + aceptar términos; la cuenta se crea al confirmar.
        String nombres = G4DUtility.Convertor.toAdmissible(cuenta.nombres(), correo.substring(0, correo.indexOf('@')));
        String apellidos = cuenta.apellidos();
        String pais = G4DUtility.Convertor.toAdmissible(request.getPais());
        if (pais == null || !Boolean.TRUE.equals(request.getAceptaTerminos())) {
            UsuarioDTO parcial = new UsuarioDTO();
            parcial.setCorreo(correo);
            parcial.setNombres(nombres);
            parcial.setApellidos(apellidos);
            AuthenticationResponse response = new AuthenticationResponse(true, "Elige tu país para crear tu cuenta.", parcial);
            response.setRequiereDatos(true);
            response.setCorreo(correo);
            return response;
        }
        String n = (nombres.length() > 60) ? nombres.substring(0, 60) : nombres;
        String a = (apellidos != null && apellidos.length() > 60) ? apellidos.substring(0, 60) : apellidos;
        UsuarioEntity usuario = usuarioService.crear(n, a, correo, null, paisService.obtenerParaUsuario(pais), TipoUsuario.CLIENTE);
        return this.abrirSesion(usuario, dispositivo, "¡Cuenta creada con Google! Bienvenido a MoraPack4D.");
    }

    // ---------- Mi cuenta ----------
    private UsuarioEntity usuarioActual() {
        UsuarioPrincipal principal = SesionActual.requerir();
        return usuarioService.findById(principal.id()).orElseThrow(() -> new G4DException("La cuenta ya no existe."));
    }

    public AuthenticationResponse miCuenta() {
        return new AuthenticationResponse(true, "Cuenta obtenida!", usuarioMapper.toDTO(this.usuarioActual()));
    }

    @Transactional
    public AuthenticationResponse actualizarPerfil(ProfileUpdateRequest request) {
        UsuarioEntity usuario = this.usuarioActual();
        usuario.setNombres(UsuarioService.validarNombre(request.getNombres(), "nombres"));
        usuario.setApellidos(UsuarioService.validarNombreOpcional(request.getApellidos(), "apellidos"));
        if (G4DUtility.Convertor.toAdmissible(request.getPais()) == null) throw new G4DException("Selecciona tu país.");
        usuario.setPais(paisService.obtenerParaUsuario(request.getPais()));
        usuarioService.save(usuario);
        return new AuthenticationResponse(true, "Datos actualizados!", usuarioMapper.toDTO(usuario));
    }

    // Con contraseña: exige la actual. Sin contraseña (cuenta de Google): la define sin pedir la actual.
    // Las demás sesiones del usuario se cierran; la de este dispositivo sigue.
    public AuthenticationResponse cambiarContrasenia(PasswordChangeRequest request) {
        UsuarioPrincipal principal = SesionActual.requerir();
        UsuarioEntity usuario = this.usuarioActual();
        boolean teniaContrasenia = usuario.tieneContrasenia();
        if (teniaContrasenia && !G4DSecurity.verificar(request.getContraseniaActual(), usuario.getContrasenia())) {
            throw new G4DException("La contraseña actual no es correcta.");
        }
        UsuarioService.validarContrasenia(request.getContrasenia(), request.getConfirmacion());
        if (teniaContrasenia && G4DSecurity.verificar(request.getContrasenia(), usuario.getContrasenia())) {
            throw new G4DException("La nueva contraseña debe ser distinta de la actual.");
        }
        usuario.setContrasenia(G4DSecurity.hashear(request.getContrasenia()));
        usuarioService.save(usuario);
        sesionService.revocarTodas(usuario, principal.idSesion());
        return new AuthenticationResponse(true, teniaContrasenia ? "Contraseña actualizada!" : "Contraseña creada! Ya puedes ingresar también con tu correo.", usuarioMapper.toDTO(usuario));
    }
}
