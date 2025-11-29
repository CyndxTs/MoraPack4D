/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       EventoController.java
 [**/

package com.pucp.dp1.grupo4d.morapack.controller.model;

import com.pucp.dp1.grupo4d.morapack.model.dto.request.ListRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.ListResponse;
import com.pucp.dp1.grupo4d.morapack.service.model.EventoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/eventos")
public class EventoController {
    private final EventoService eventoService;

    public EventoController(EventoService eventoService) {
        this.eventoService = eventoService;
    }

    @GetMapping
    public ResponseEntity<ListResponse> listar() {
        ListRequest request = new ListRequest(0, 30);
        ListResponse response = eventoService.listar(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/listar")
    public ResponseEntity<ListResponse> listar(@RequestBody ListRequest request) {
        ListResponse response = eventoService.listar(request);
        return ResponseEntity.ok(response);
    }
}
