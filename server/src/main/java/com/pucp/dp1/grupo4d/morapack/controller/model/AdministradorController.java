/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       AdministradorController.java
 [**/

package com.pucp.dp1.grupo4d.morapack.controller.model;

import com.pucp.dp1.grupo4d.morapack.mapper.UsuarioMapper;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.FilterRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.ListResponse;
import com.pucp.dp1.grupo4d.morapack.service.model.AdministradorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/administradores")
public class AdministradorController {
    private final AdministradorService administradorService;
    private final UsuarioMapper usuarioMapper;

    public AdministradorController(AdministradorService administradorService, UsuarioMapper usuarioMapper) {
        this.administradorService = administradorService;
        this.usuarioMapper = usuarioMapper;
    }

    // Listado
    @GetMapping
    public ResponseEntity<ListResponse> listar() {
        try {
            ListResponse response = administradorService.listar();
            if (response.getSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ListResponse(false, "ERROR INTERNO: " + e.getMessage()));
        }
    }

    // Filtrado
    @GetMapping("/filtrar")
    public ResponseEntity<ListResponse> filtrar(@RequestBody FilterRequest request) {
        try {
            ListResponse response = administradorService.filtrar(request);
            if (response.getSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ListResponse(false, "ERROR INTERNO: " + e.getMessage()));
        }
    }
}
