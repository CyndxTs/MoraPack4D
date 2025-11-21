/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       MonitorController.java
 [**/

package com.pucp.dp1.grupo4d.morapack.controller;

import com.pucp.dp1.grupo4d.morapack.model.dto.request.PlanificationRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.OperationRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.GenericResponse;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.SolutionResponse;
import com.pucp.dp1.grupo4d.morapack.service.MonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class MonitorController {

    @Autowired
    private MonitorService monitorService;

    @MessageMapping("/simularSemana")
    @SendTo("/topic/simulator")
    public ResponseEntity<SolutionResponse> simularSemana(@RequestBody PlanificationRequest request) {
        try {
            SolutionResponse response = monitorService.ejecutarAlgoritmo(request);
            if (response.getSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new SolutionResponse(false, "ERROR INTERNO: " + e.getMessage()));
        }
    }

    @MessageMapping("/obtenerOperacion")
    @SendTo("/topic/operator")
    public ResponseEntity<SolutionResponse> obtenerOperacion(OperationRequest request) {
        try {
            SolutionResponse response = monitorService.obtenerOperacion(request);
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

    @MessageMapping("/replanificarOperacion")
    public ResponseEntity<GenericResponse> replanificarOperacion(@RequestBody PlanificationRequest request) {
        try {
            SolutionResponse response = monitorService.ejecutarAlgoritmo(request);
            if (response.getSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new SolutionResponse(false, "ERROR INTERNO: " + e.getMessage()));
        }
    }

    @MessageMapping("/simularColapso")
    @SendTo("/topic/simulator")
    public ResponseEntity<SolutionResponse> simularColapso(@RequestBody PlanificationRequest request) {
        try {
            SolutionResponse response = monitorService.ejecutarAlgoritmo(request);
            if (response.getSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new SolutionResponse(false, "ERROR INTERNO: " + e.getMessage()));
        }
    }
}
