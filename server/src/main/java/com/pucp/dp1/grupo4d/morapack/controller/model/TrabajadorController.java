/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       TrabajadorController.java
 [**/

package com.pucp.dp1.grupo4d.morapack.controller.model;

import com.pucp.dp1.grupo4d.morapack.model.dto.UsuarioDTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.*;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.ListResponse;
import com.pucp.dp1.grupo4d.morapack.service.model.TrabajadorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trabajadores")
public class TrabajadorController {
    private final TrabajadorService trabajadorService;

    public TrabajadorController(TrabajadorService trabajadorService) {
        this.trabajadorService = trabajadorService;
    }

    @GetMapping
    public ResponseEntity<ListResponse> listar() {
        ListRequest request = new ListRequest(0, 30);
        ListResponse response = trabajadorService.listar(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/listar")
    public ResponseEntity<ListResponse> listar(@RequestBody ListRequest request) {
        ListResponse response = trabajadorService.listar(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/filtrar")
    public ResponseEntity<ListResponse> filtrar(@RequestBody FilterRequest<UsuarioDTO> request) {
        ListResponse response = trabajadorService.filtrar(request);
        return ResponseEntity.ok(response);
    }
}
