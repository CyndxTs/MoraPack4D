/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       PlanController.java
 [**/

package com.pucp.dp1.grupo4d.morapack.controller.model;

import com.pucp.dp1.grupo4d.morapack.model.dto.PlanDTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.ImportRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.ListRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.GenericResponse;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.ListResponse;
import com.pucp.dp1.grupo4d.morapack.service.model.PlanService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/planes")
public class PlanController {
    private final PlanService planService;

    public PlanController(PlanService planService) {
        this.planService = planService;
    }

    @GetMapping
    public ResponseEntity<ListResponse> listar() {
        ListRequest request = new ListRequest(0, 30);
        ListResponse response = planService.listar(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/listar")
    public ResponseEntity<ListResponse> listar(@RequestBody ListRequest request) {
        ListResponse response = planService.listar(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/importar")
    public ResponseEntity<GenericResponse> importar(@RequestBody ImportRequest<PlanDTO> request) {
        GenericResponse response = planService.importar(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/importar-archivo")
    public ResponseEntity<GenericResponse> importar(@RequestParam("file") MultipartFile file) {
        GenericResponse response = planService.importar(file);
        return ResponseEntity.ok(response);
    }
}
