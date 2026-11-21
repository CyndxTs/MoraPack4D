/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       AuthController.java
 [**/

package com.pucp.dp1.grupo4d.morapack.controller;

import com.pucp.dp1.grupo4d.morapack.model.dto.request.*;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.AuthenticationResponse;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.GenericResponse;
import com.pucp.dp1.grupo4d.morapack.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    private static String dispositivo(HttpServletRequest http) {
        String agente = http.getHeader("User-Agent");
        return (agente != null) ? agente : "Desconocido";
    }

    @PostMapping("/iniciar-sesion")
    public ResponseEntity<AuthenticationResponse> iniciarSesion(@RequestBody SignInRequest request, HttpServletRequest http) {
        return ResponseEntity.ok(authService.iniciarSesion(request, dispositivo(http)));
    }

    @PostMapping("/google")
    public ResponseEntity<AuthenticationResponse> google(@RequestBody GoogleSignInRequest request, HttpServletRequest http) {
        return ResponseEntity.ok(authService.google(request, dispositivo(http)));
    }

    @PostMapping("/refrescar")
    public ResponseEntity<AuthenticationResponse> refrescar(@RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refrescar(request));
    }

    @PostMapping("/cerrar-sesion")
    public ResponseEntity<GenericResponse> cerrarSesion(@RequestBody SignOutRequest request) {
        return ResponseEntity.ok(authService.cerrarSesion(request));
    }

    @PostMapping("/registrar")
    public ResponseEntity<AuthenticationResponse> registrar(@RequestBody SignUpRequest request) {
        return ResponseEntity.ok(authService.registrar(request));
    }

    @PostMapping("/registrar/verificar")
    public ResponseEntity<AuthenticationResponse> verificarRegistro(@RequestBody CodeVerificationRequest request, HttpServletRequest http) {
        return ResponseEntity.ok(authService.verificarRegistro(request, dispositivo(http)));
    }

    @PostMapping("/codigo/reenviar")
    public ResponseEntity<AuthenticationResponse> reenviarCodigo(@RequestBody CodeResendRequest request) {
        return ResponseEntity.ok(authService.reenviarCodigo(request));
    }

    @PostMapping("/contrasenia/olvido")
    public ResponseEntity<AuthenticationResponse> olvideContrasenia(@RequestBody PasswordForgotRequest request) {
        return ResponseEntity.ok(authService.olvideContrasenia(request));
    }

    @PostMapping("/contrasenia/restablecer")
    public ResponseEntity<AuthenticationResponse> restablecerContrasenia(@RequestBody PasswordResetRequest request, HttpServletRequest http) {
        return ResponseEntity.ok(authService.restablecerContrasenia(request, dispositivo(http)));
    }

    // ---------- Mi cuenta (requiere sesión) ----------
    @PostMapping("/mi-cuenta")
    public ResponseEntity<AuthenticationResponse> miCuenta() {
        return ResponseEntity.ok(authService.miCuenta());
    }

    @PostMapping("/mi-cuenta/perfil")
    public ResponseEntity<AuthenticationResponse> actualizarPerfil(@RequestBody ProfileUpdateRequest request) {
        return ResponseEntity.ok(authService.actualizarPerfil(request));
    }

    @PostMapping("/mi-cuenta/contrasenia")
    public ResponseEntity<AuthenticationResponse> cambiarContrasenia(@RequestBody PasswordChangeRequest request) {
        return ResponseEntity.ok(authService.cambiarContrasenia(request));
    }
}
