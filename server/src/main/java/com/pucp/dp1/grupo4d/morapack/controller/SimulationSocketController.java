package com.pucp.dp1.grupo4d.morapack.controller;


import com.pucp.dp1.grupo4d.morapack.model.dto.model.AeropuertoResponse;
import com.pucp.dp1.grupo4d.morapack.model.dto.model.VueloResponse;
import com.pucp.dp1.grupo4d.morapack.service.model.AeropuertoService;
import com.pucp.dp1.grupo4d.morapack.service.model.VueloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import java.util.List;

@Controller
public class SimulationSocketController {

    @Autowired
    private AeropuertoService aeropuertoService;

    @Autowired
    private VueloService vueloService;

    @MessageMapping("/simulator")  // Cliente envía a /app/simulator
    @SendTo("/topic/simulator")    // Servidor envía a /topic/simulator
    public List<AeropuertoResponse> listarAeropuertos() {
        System.out.println("🛰️ Mensaje recibido en /app/simulator");

        List<AeropuertoResponse> aeropuertos = aeropuertoService.findAll().stream()
                .map(a -> new AeropuertoResponse(
                        a.getId(),
                        a.getCodigo(),
                        a.getCiudad(),
                        a.getPais(),
                        a.getContinente(),
                        a.getAlias(),
                        a.getHusoHorario(),
                        a.getCapacidad(),
                        a.getLatitudDMS(),
                        a.getLongitudDMS(),
                        a.getLatitudDEC(),
                        a.getLongitudDEC(),
                        a.getEsSede()
                ))
                .toList();

        System.out.println("✅ Enviando lista de aeropuertos: " + aeropuertos.size());
        return aeropuertos;
    }

    @MessageMapping("/simulator/flights")
    @SendTo("/topic/simulator/flights")
    public List<VueloResponse> listarVuelosSimulacion() {
        System.out.println("🛫 Mensaje recibido en /app/simulator/flights");
        var vuelos = vueloService.listarVuelosSimulacion();
        System.out.println("✅ Enviando lista de vuelos: " + vuelos.size());
        return vuelos;
    }
}
