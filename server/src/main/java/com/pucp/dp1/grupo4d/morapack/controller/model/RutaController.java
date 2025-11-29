/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       RutaController.java
 [**/

package com.pucp.dp1.grupo4d.morapack.controller.model;

import com.pucp.dp1.grupo4d.morapack.model.dto.request.ListRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.ListResponse;
import com.pucp.dp1.grupo4d.morapack.service.model.RutaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rutas")
public class RutaController {
    private final RutaService rutaService;

    public RutaController(RutaService rutaService) {
        this.rutaService = rutaService;
    }

    @GetMapping
    public ResponseEntity<ListResponse> listar() {
        ListRequest request = new ListRequest(0, 30);
        ListResponse response = rutaService.listar(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/listar")
    public ResponseEntity<ListResponse> listar(@RequestBody ListRequest request) {
        ListResponse response = rutaService.listar(request);
        return ResponseEntity.ok(response);
    }
}
