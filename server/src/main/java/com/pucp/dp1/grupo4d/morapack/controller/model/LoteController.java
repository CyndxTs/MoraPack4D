/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       LoteController.java
 [**/

package com.pucp.dp1.grupo4d.morapack.controller.model;

import com.pucp.dp1.grupo4d.morapack.model.dto.request.ListRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.ListResponse;
import com.pucp.dp1.grupo4d.morapack.service.model.LoteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lotes")
public class LoteController {
    private final LoteService loteService;

    public LoteController(LoteService loteService) {
        this.loteService = loteService;
    }

    @GetMapping
    public ResponseEntity<ListResponse> listar() {
        ListRequest request = new ListRequest(0, 30);
        ListResponse response = loteService.listar(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/listar")
    public ResponseEntity<ListResponse> listar(@RequestBody ListRequest request) {
        ListResponse response = loteService.listar(request);
        return ResponseEntity.ok(response);
    }
}
