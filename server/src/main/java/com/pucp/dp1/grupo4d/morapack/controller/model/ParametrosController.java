/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       ParametrosController.java
 [**/

package com.pucp.dp1.grupo4d.morapack.controller.model;

import com.pucp.dp1.grupo4d.morapack.model.dto.ParametrosDTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.ImportRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.GenericResponse;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.ListResponse;
import com.pucp.dp1.grupo4d.morapack.service.model.ParametrosService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/parametros")
public class ParametrosController {
    private final ParametrosService parametrosService;

    public ParametrosController(ParametrosService parametrosService) {
        this.parametrosService = parametrosService;
    }

    @GetMapping
    public ResponseEntity<ListResponse> listar() {
        ListResponse response = parametrosService.listar();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/importar")
    public ResponseEntity<GenericResponse> importar(@RequestBody ImportRequest<ParametrosDTO> request) {
        GenericResponse response = parametrosService.importar(request);
        return ResponseEntity.ok(response);
    }
}
