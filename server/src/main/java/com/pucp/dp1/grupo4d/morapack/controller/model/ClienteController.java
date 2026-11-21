/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       ClienteController.java
 [**/

package com.pucp.dp1.grupo4d.morapack.controller.model;

import com.pucp.dp1.grupo4d.morapack.model.dto.UsuarioDTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.FilterRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.ListRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.ListResponse;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.TipoUsuario;
import com.pucp.dp1.grupo4d.morapack.service.model.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Clientes (lectura) para el trabajador: p. ej. elegir el cliente de un pedido de operación
@RestController
@RequestMapping("/api/clientes")
public class ClienteController {
    private final UsuarioService usuarioService;

    public ClienteController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public ResponseEntity<ListResponse> listar() {
        return ResponseEntity.ok(usuarioService.listar(new ListRequest(0, 30), TipoUsuario.CLIENTE));
    }

    @PostMapping("/listar")
    public ResponseEntity<ListResponse> listar(@RequestBody ListRequest request) {
        return ResponseEntity.ok(usuarioService.listar(request, TipoUsuario.CLIENTE));
    }

    @PostMapping("/filtrar")
    public ResponseEntity<ListResponse> filtrar(@RequestBody FilterRequest<UsuarioDTO> request) {
        return ResponseEntity.ok(usuarioService.filtrar(request, TipoUsuario.CLIENTE));
    }
}
