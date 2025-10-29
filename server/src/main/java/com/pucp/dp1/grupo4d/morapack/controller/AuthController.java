/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       AuthController.java
 [**/

package com.pucp.dp1.grupo4d.morapack.controller;

import com.pucp.dp1.grupo4d.morapack.model.dto.request.SignInRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.SignUpRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.AuthResponse;
import com.pucp.dp1.grupo4d.morapack.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signin")
    public ResponseEntity<AuthResponse> signin(@RequestBody SignInRequest request) {
        try {
            AuthResponse response = authService.signIn(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new AuthResponse(null, "Credenciales inválidas.")
            );
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@RequestBody SignUpRequest request) {
        try {
            AuthResponse response = authService.signUp(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new AuthResponse(null, "Credenciales inválidas.")
            );
        }
    }
}
