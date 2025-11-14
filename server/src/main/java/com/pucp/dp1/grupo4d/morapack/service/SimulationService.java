/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       SimulationService.java
 [**/

package com.pucp.dp1.grupo4d.morapack.service;

import com.pucp.dp1.grupo4d.morapack.adapter.*;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.SimulationRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.SimulationResponse;
import com.pucp.dp1.grupo4d.morapack.model.entity.AeropuertoEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.PedidoEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.RutaEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.VueloEntity;
import com.pucp.dp1.grupo4d.morapack.service.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SimulationService {

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private AeropuertoService aeropuertoService;

    @Autowired
    private RutaService rutaService;

    @Autowired
    private VueloService vueloService;

    public SimulationResponse listarParaSimulacion(SimulationRequest request) {
        try {
            LocalDateTime fechaHoraInicio = request.getFechaHoraInicio();
            LocalDateTime fechaHoraFin = request.getFechaHoraFin();
            Integer desfaseDeDias = request.getDesfaseDeDias();
            if(fechaHoraInicio == null  || fechaHoraFin == null || fechaHoraFin.isBefore(fechaHoraInicio)) {
                return new SimulationResponse(false, "Mal formato de rango de fechas.");
            }
            if(desfaseDeDias == null || desfaseDeDias < 0) {
                return new SimulationResponse(false, "Mal formato de tiempo de desfase.");
            }
            List<PedidoEntity> pedidos = pedidoService.listarParaSimulacion(fechaHoraInicio, fechaHoraFin, desfaseDeDias);
            List<AeropuertoEntity> aeropuertos = aeropuertoService.findAll();
            List<VueloEntity> vuelos = vueloService.listarParaSimulacion(fechaHoraInicio, fechaHoraFin, desfaseDeDias);
            List<RutaEntity> rutas = rutaService.listarParaSimulacion(fechaHoraInicio, fechaHoraFin, desfaseDeDias);
            return new SimulationResponse(true, "Simulación correctamente enviada!", pedidos, aeropuertos, vuelos, rutas);
        } catch (Exception e) {
            e.printStackTrace();
            return new SimulationResponse(false, "ERROR - ENVÍO HACIA SIMULACIÓN: " + e.getMessage());
        }
    }
}
