/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       PaisController.java
 [**/

package com.pucp.dp1.grupo4d.morapack.controller.model;

import com.pucp.dp1.grupo4d.morapack.model.dto.PaisDTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.*;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.ListResponse;
import com.pucp.dp1.grupo4d.morapack.service.model.PaisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/paises")
public class PaisController {
    private final PaisService paisService;

    public PaisController(PaisService paisService) {
        this.paisService = paisService;
    }

    @GetMapping
    public ResponseEntity<ListResponse> listar() {
        ListRequest request = new ListRequest(0, 30);
        ListResponse response = paisService.listar(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/listar")
    public ResponseEntity<ListResponse> listar(@RequestBody ListRequest request) {
        ListResponse response = paisService.listar(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/filtrar")
    public ResponseEntity<ListResponse> filtrar(@RequestBody FilterRequest<PaisDTO> request) {
        ListResponse response = paisService.filtrar(request);
        return ResponseEntity.ok(response);
    }
}
