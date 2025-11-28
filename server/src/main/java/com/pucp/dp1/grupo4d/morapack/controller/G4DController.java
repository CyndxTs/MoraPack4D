/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       G4DController.java
 [**/

package com.pucp.dp1.grupo4d.morapack.controller;

import com.pucp.dp1.grupo4d.morapack.model.dto.request.ExportationRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.ReplanificationRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.SimulationRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.StatusResponse;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.EstadoProceso;
import com.pucp.dp1.grupo4d.morapack.model.exception.G4DException;
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
    public StatusResponse iniciarSimulacion(@RequestBody SimulationRequest request) {
        try {
            return g4dService.iniciarSimulacion(request);
        } catch (G4DException e) {
            return  new StatusResponse(false, e.getMessage(), EstadoProceso.INICIADO);
        } catch (Exception e) {
            return new StatusResponse(false, "ERROR INTERNO: " + e.getMessage());
        }
    }

    @MessageMapping("simulator-stop")
    @SendTo("/topic/simulator-status")
    public StatusResponse detenerSimulacion() {
        try {
            return  g4dService.detenerSimulacion();
        } catch (G4DException e) {
            return new StatusResponse(false, e.getMessage(), EstadoProceso.DETENIDO);
        } catch (Exception e) {
            e.printStackTrace();
            return new StatusResponse(false, "ERROR INTERNO: " + e.getMessage());
        }
    }

    @MessageMapping("operator-replanificate")
    @SendTo("/topic/operator-status")
    public StatusResponse replanificarOperacion(@RequestBody ReplanificationRequest request) {
        try {
            return g4dService.replanificarOperacion(request);
        } catch (G4DException e) {
            return new StatusResponse(false, e.getMessage(), EstadoProceso.INICIADO);
        } catch (Exception e) {
            e.printStackTrace();
            return new StatusResponse(false, "ERROR INTERNO: " + e.getMessage());
        }
    }

    @MessageMapping("generator-export")
    @SendTo("/topic/generator-status")
    public StatusResponse exportarSolucion(@RequestBody ExportationRequest request) {
        try {
            return g4dService.exportarSolucion(request);
        } catch (G4DException e) {
            return  new StatusResponse(false, e.getMessage(), EstadoProceso.INICIADO);
        } catch (Exception e) {
            e.printStackTrace();
            return new StatusResponse(false, "ERROR INTERNO: " + e.getMessage());
        }
    }
}
