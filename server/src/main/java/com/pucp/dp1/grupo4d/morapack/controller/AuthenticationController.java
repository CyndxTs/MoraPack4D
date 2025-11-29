/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       AuthenticationController.java
 [**/

package com.pucp.dp1.grupo4d.morapack.controller;

import com.pucp.dp1.grupo4d.morapack.model.dto.request.SignInRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.SignOutRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.SignUpRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.AuthenticationResponse;
import com.pucp.dp1.grupo4d.morapack.model.exception.G4DException;
import com.pucp.dp1.grupo4d.morapack.service.AuthenticactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/authentication")
public class AuthenticationController {
    private final AuthenticactionService authenticactionService;

    public AuthenticationController(AuthenticactionService authenticactionService) {
        this.authenticactionService = authenticactionService;
    }

    @PostMapping("/sign-in")
    public ResponseEntity<AuthenticationResponse> signIn(@RequestBody SignInRequest request) {
        try {
            AuthenticationResponse response = authenticactionService.signIn(request);
            return ResponseEntity.ok(response);
        } catch (G4DException e) {
            return ResponseEntity.badRequest().body(new AuthenticationResponse(false, e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(new AuthenticationResponse(false, "ERROR INTERNO: " + e.getMessage()));
        }
    }

    @PostMapping("/sign-up")
    public ResponseEntity<AuthenticationResponse> signUp(@RequestBody SignUpRequest request) {
        try {
            AuthenticationResponse response = authenticactionService.signUp(request);
            return ResponseEntity.ok(response);
        } catch (G4DException e) {
            return ResponseEntity.badRequest().body(new AuthenticationResponse(false, e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(new AuthenticationResponse(false, "ERROR INTERNO: " + e.getMessage()));
        }
    }

    @PostMapping("/sign-out")
    public ResponseEntity<AuthenticationResponse> signOut(@RequestBody SignOutRequest request) {
        try {
            AuthenticationResponse response = authenticactionService.signOut(request);
            return ResponseEntity.ok(response);
        } catch (G4DException e) {
            return ResponseEntity.badRequest().body(new AuthenticationResponse(false, e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(new AuthenticationResponse(false, "ERROR INTERNO: " + e.getMessage()));
        }
    }
}
