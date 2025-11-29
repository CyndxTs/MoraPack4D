/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       G4DController.java
 [**/

package com.pucp.dp1.grupo4d.morapack.controller;

import com.pucp.dp1.grupo4d.morapack.model.dto.request.ExportationRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.ReplanificationRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.SimulationRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.GenericResponse;
import com.pucp.dp1.grupo4d.morapack.service.G4DService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class G4DController {
    private final G4DService g4dService;

    public G4DController(G4DService g4dService) {
        this.g4dService = g4dService;
    }

    @GetMapping
    public String morapack4D() {
        return "SERVER INICIADO \uD83D\uDDE3\uFE0F\uD83D\uDD25\uD83D\uDD25\uD83D\uDD25\n";
    }

    @PostMapping("/simulation-init")
    public ResponseEntity<GenericResponse> iniciarSimulacion(@RequestBody SimulationRequest request) {
        GenericResponse response = g4dService.iniciarSimulacion(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/simulation-stop")
    public ResponseEntity<GenericResponse> detenerSimulacion() {
        GenericResponse response = g4dService.detenerSimulacion();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/operation-replanificate")
    public ResponseEntity<GenericResponse> replanificarOperacion(@RequestBody ReplanificationRequest request) {
        GenericResponse response = g4dService.replanificarOperacion(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/solution-export")
    public ResponseEntity<GenericResponse> exportarSolucion(@RequestBody ExportationRequest request) {
        GenericResponse response = g4dService.exportarSolucion(request);
        return ResponseEntity.ok(response);
    }
}
