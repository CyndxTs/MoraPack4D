/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       UsuarioController.java
 [**/

package com.pucp.dp1.grupo4d.morapack.controller.model;

import com.pucp.dp1.grupo4d.morapack.model.dto.UsuarioDTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.*;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.AuthenticationResponse;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.GenericResponse;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.ListResponse;
import com.pucp.dp1.grupo4d.morapack.service.GestionUsuarioService;
import com.pucp.dp1.grupo4d.morapack.service.model.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Solo ADMINISTRADOR (ver SecurityConfig)
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
    private final UsuarioService usuarioService;
    private final GestionUsuarioService gestionUsuarioService;
    private final com.pucp.dp1.grupo4d.morapack.service.CorreoService correoService;

    public UsuarioController(UsuarioService usuarioService, GestionUsuarioService gestionUsuarioService, com.pucp.dp1.grupo4d.morapack.service.CorreoService correoService) {
        this.usuarioService = usuarioService;
        this.gestionUsuarioService = gestionUsuarioService;
        this.correoService = correoService;
    }

    @PostMapping("/listar")
    public ResponseEntity<ListResponse> listar(@RequestBody ListRequest request) {
        return ResponseEntity.ok(usuarioService.listar(request, null));
    }

    // modelo.tipoUsuario filtra por rol
    @PostMapping("/filtrar")
    public ResponseEntity<ListResponse> filtrar(@RequestBody FilterRequest<UsuarioDTO> request) {
        return ResponseEntity.ok(usuarioService.filtrar(request, null));
    }

    @PostMapping("/crear")
    public ResponseEntity<AuthenticationResponse> crear(@RequestBody UserCreateRequest request) {
        return ResponseEntity.ok(gestionUsuarioService.crear(request));
    }

    @PostMapping("/cambiar-rol")
    public ResponseEntity<AuthenticationResponse> cambiarRol(@RequestBody RoleChangeRequest request) {
        return ResponseEntity.ok(gestionUsuarioService.cambiarRol(request));
    }

    @PostMapping("/cambiar-estado")
    public ResponseEntity<AuthenticationResponse> cambiarEstado(@RequestBody UserStateRequest request) {
        return ResponseEntity.ok(gestionUsuarioService.cambiarEstado(request));
    }

    @PostMapping("/eliminar")
    public ResponseEntity<GenericResponse> eliminar(@RequestBody UserCodeRequest request) {
        return ResponseEntity.ok(gestionUsuarioService.eliminar(request));
    }

    // Prueba del correo (Brevo): envía un correo al propio administrador y devuelve el motivo exacto si falla
    @PostMapping("/correo-prueba")
    public ResponseEntity<GenericResponse> probarCorreo() {
        var principal = com.pucp.dp1.grupo4d.morapack.security.SesionActual.requerir();
        String correo = usuarioService.findById(principal.id()).map(u -> u.getCorreo())
                .orElseThrow(() -> new com.pucp.dp1.grupo4d.morapack.model.exception.G4DException("No se encontró tu cuenta."));
        correoService.enviar(correo, "MoraPack · Prueba de correo",
                com.pucp.dp1.grupo4d.morapack.service.CorreoService.plantilla("El correo funciona",
                        "<p style=\"margin:0;color:#c9d5f0\">Si lees esto, MoraPack puede enviar códigos de verificación y reportes por correo.</p>"),
                "Si lees esto, MoraPack puede enviar correos.", null);
        return ResponseEntity.ok(new GenericResponse(true, String.format("Correo de prueba enviado a %s. Revisa tu bandeja (y spam).", correo)));
    }
}
