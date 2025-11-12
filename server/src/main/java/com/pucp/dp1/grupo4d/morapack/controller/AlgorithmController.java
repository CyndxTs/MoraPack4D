/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       AlgorithmController.java
 [**/

package com.pucp.dp1.grupo4d.morapack.controller;

import com.pucp.dp1.grupo4d.morapack.model.dto.request.PlanificationRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.ImportResponse;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.PlanificationResponse;
import com.pucp.dp1.grupo4d.morapack.service.AlgorithmService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/algorithm")
public class AlgorithmController {

    private final AlgorithmService algorithmService;

    public AlgorithmController(AlgorithmService algorithmService) {
        this.algorithmService = algorithmService;
    }

    @PostMapping("/importar")
    public ResponseEntity<ImportResponse> importarDesdeArchivo(@RequestParam("file") MultipartFile file, @RequestParam("type") String type) {
        try {
            ImportResponse response = algorithmService.importarDesdeArchivo(file, type);
            if (response.isExito()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ImportResponse(false, "ERROR INTERNO: " + e.getMessage()));
        }
    }

    @PostMapping("/planificar")
    public ResponseEntity<PlanificationResponse> planificar(@RequestBody PlanificationRequest request) {
        try {
            PlanificationResponse response = algorithmService.planificar(request);
            if (response.isExito()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new PlanificationResponse(false, "ERROR INTERNO: " + e.getMessage()));
        }
    }
}
