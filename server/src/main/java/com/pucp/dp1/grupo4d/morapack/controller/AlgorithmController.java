/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       AlgorithmController.java
 [**/

package com.pucp.dp1.grupo4d.morapack.controller;

import com.pucp.dp1.grupo4d.morapack.model.dto.request.FileImportRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.ListImportRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.PlanificationRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.GenericResponse;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.SolutionResponse;
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

    @PostMapping("/importarDesdeArchivo")
    public ResponseEntity<GenericResponse> importarDesdeArchivo(@RequestPart("file") MultipartFile file, @RequestPart("request") FileImportRequest request) {
        try {
            GenericResponse response = algorithmService.importarDesdeArchivo(file, request);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new GenericResponse(false, "ERROR INTERNO: " + e.getMessage()));
        }
    }

    @PostMapping("/importarDesdeLista")
    public ResponseEntity<GenericResponse> importarDesdeArchivo(@RequestBody ListImportRequest request) {
        try {
            GenericResponse response = algorithmService.importarDesdeLista(request);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new GenericResponse(false, "ERROR INTERNO: " + e.getMessage()));
        }
    }

    @PostMapping("/planificar")
    public ResponseEntity<SolutionResponse> planificar(@RequestBody PlanificationRequest request) {
        try {
            SolutionResponse response = algorithmService.planificar(request);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new SolutionResponse(false, "ERROR INTERNO: " + e.getMessage()));
        }
    }
}
