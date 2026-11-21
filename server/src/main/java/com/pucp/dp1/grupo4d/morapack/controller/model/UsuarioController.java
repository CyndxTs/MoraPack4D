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

    public UsuarioController(UsuarioService usuarioService, GestionUsuarioService gestionUsuarioService) {
        this.usuarioService = usuarioService;
        this.gestionUsuarioService = gestionUsuarioService;
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
}
