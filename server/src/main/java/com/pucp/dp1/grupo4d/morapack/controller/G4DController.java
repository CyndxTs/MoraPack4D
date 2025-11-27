/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       G4DController.java
 [**/

package com.pucp.dp1.grupo4d.morapack.controller;

import com.pucp.dp1.grupo4d.morapack.model.dto.request.ExportationRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.OperationRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.SimulationRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.GenericResponse;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.ProcessStatusResponse;
import com.pucp.dp1.grupo4d.morapack.service.G4DService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller("/")
public class G4DController {

    @Autowired
    private G4DService g4dService;

    @GetMapping
    @ResponseBody
    public String morapack4D() {
        return "SERVER INICIADO \uD83D\uDDE3\uFE0F\uD83D\uDD25\uD83D\uDD25\uD83D\uDD25\n";
    }

    @MessageMapping("simulator-init")
    @SendTo("/topic/simulator-status")
    public ProcessStatusResponse iniciarSimulacion(@RequestBody SimulationRequest request) {
        try {
            return g4dService.iniciarSimulacion(request);
        } catch (Exception e) {
            return new ProcessStatusResponse(false, "ERROR INTERNO: " + e.getMessage());
        }
    }

    @MessageMapping("simulator-stop")
    @SendTo("/topic/simulator-status")
    public ProcessStatusResponse detenerSimulacion() {
        try {
            return  g4dService.detenerSimulacion();
        } catch (Exception e) {
            return new ProcessStatusResponse(false, "ERROR INTERNO: " + e.getMessage());
        }
    }

    @MessageMapping("operator-replanificate")
    @SendTo("/topic/operator-status")
    public ProcessStatusResponse replanificarOperacion(@RequestBody OperationRequest request) {
        try {
            return g4dService.replanificarOperacion(request);
        } catch (Exception e) {
            return new ProcessStatusResponse(false, "ERROR INTERNO: " + e.getMessage());
        }
    }

    @MessageMapping("generator-export")
    @SendTo("/topic/generator-status")
    public ProcessStatusResponse exportarSolucion(@RequestBody ExportationRequest request) {
        try {
            return g4dService.exportarSolucion(request);
        } catch (Exception e) {
            return new ProcessStatusResponse(false, "ERROR INTERNO: " + e.getMessage());
        }
    }
}
