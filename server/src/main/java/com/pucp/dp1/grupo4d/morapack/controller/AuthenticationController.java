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

    @PostMapping("/signin")
    public ResponseEntity<AuthenticationResponse> signin(@RequestBody SignInRequest request) {
        try {
            AuthenticationResponse response = authenticactionService.signIn(request);
            if(response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new AuthenticationResponse(false, "ERROR INTERNO: " + e.getMessage()));
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthenticationResponse> signup(@RequestBody SignUpRequest request) {
        try {
            AuthenticationResponse response = authenticactionService.signUp(request);
            if(response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new AuthenticationResponse(false, "ERROR INTERNO: " + e.getMessage()));
        }
    }

    @PostMapping("/signout")
    public ResponseEntity<AuthenticationResponse> signout(@RequestBody SignOutRequest request) {
        try {
            AuthenticationResponse response = authenticactionService.signOut(request);
            if(response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new AuthenticationResponse(false, "ERROR INTERNO: " + e.getMessage()));
        }
    }
}
