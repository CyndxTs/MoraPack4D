/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       RegistroController.java
 [**/

package com.pucp.dp1.grupo4d.morapack.controller.model;

import com.pucp.dp1.grupo4d.morapack.model.dto.request.ListRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.ListResponse;
import com.pucp.dp1.grupo4d.morapack.service.model.RegistroService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/registros")
public class RegistroController {
    private final RegistroService registroService;

    public RegistroController(RegistroService registroService) {
        this.registroService = registroService;
    }

    @GetMapping
    public ResponseEntity<ListResponse> listar() {
        ListRequest request = new ListRequest(0, 30);
        ListResponse response = registroService.listar(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/listar")
    public ResponseEntity<ListResponse> listar(@RequestBody ListRequest request) {
        ListResponse response = registroService.listar(request);
        return ResponseEntity.ok(response);
    }
}
