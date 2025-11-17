/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       SimulationController.java
 [**/

package com.pucp.dp1.grupo4d.morapack.controller;

import com.pucp.dp1.grupo4d.morapack.model.dto.request.SimulationRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.SolutionResponse;
import com.pucp.dp1.grupo4d.morapack.service.SimulationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class SimulationController {

    @Autowired
    private SimulationService simulationService;

    @MessageMapping("/simulator")
    @SendTo("/topic/simulator")
    public ResponseEntity<SolutionResponse> listarParaSimulacion(SimulationRequest request) {
        try {
            SolutionResponse response = simulationService.listarParaSimulacion(request);
            if (response.getSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(new SolutionResponse(false, "ERROR EN EL ENVÍO."));
        }
    }
}
