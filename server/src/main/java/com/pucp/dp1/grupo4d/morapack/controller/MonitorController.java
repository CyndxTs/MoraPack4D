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

    @MessageMapping("/obtenerSimulacion")
    @SendTo("/topic/simulator")
    public SolutionResponse obtenerSimulacion(@RequestBody PlanificationRequest request) {
        try {
            return monitorService.ejecutarAlgoritmo(request);
        } catch (Exception e) {
            return new SolutionResponse(false, "ERROR INTERNO: " + e.getMessage());
        }
    }

    @MessageMapping("/obtenerOperacion")
    @SendTo("/topic/operator")
    public SolutionResponse obtenerOperacion(OperationRequest request) {
        try {
            return  monitorService.obtenerOperacion(request);
        } catch (Exception e) {
            return new SolutionResponse(false, "ERROR EN EL ENVÍO.");
        }
    }

    @MessageMapping("/replanificarOperacion")
    public GenericResponse replanificarOperacion(@RequestBody PlanificationRequest request) {
        try {
            return monitorService.ejecutarAlgoritmo(request);
        } catch (Exception e) {
            return new SolutionResponse(false, "ERROR INTERNO: " + e.getMessage());
        }
    }
}
