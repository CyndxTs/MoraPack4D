/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       AuthController.java
 [**/

package com.pucp.dp1.grupo4d.morapack.controller;

import com.pucp.dp1.grupo4d.morapack.model.dto.request.SignInRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.SignOutRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.AuthenticationResponse;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.GenericResponse;
import com.pucp.dp1.grupo4d.morapack.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/iniciar-sesion")
    public ResponseEntity<AuthenticationResponse> iniciarSesion(@RequestBody SignInRequest request) {
        return ResponseEntity.ok(authService.iniciarSesion(request));
    }

    @PostMapping("/cerrar-sesion")
    public ResponseEntity<GenericResponse> cerrarSesion(@RequestBody SignOutRequest request) {
        return ResponseEntity.ok(authService.cerrarSesion(request));
    }
}
