/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       SimulationService.java
 [**/

package com.pucp.dp1.grupo4d.morapack.service;

import com.pucp.dp1.grupo4d.morapack.adapter.AeropuertoAdapter;
import com.pucp.dp1.grupo4d.morapack.adapter.PedidoAdapter;
import com.pucp.dp1.grupo4d.morapack.adapter.RutaAdapter;
import com.pucp.dp1.grupo4d.morapack.adapter.VueloAdapter;
import com.pucp.dp1.grupo4d.morapack.algorithm.Solucion;
import com.pucp.dp1.grupo4d.morapack.model.dto.AeropuertoDTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.PedidoDTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.RutaDTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.VueloDTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.SimulationRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.SolutionResponse;
import com.pucp.dp1.grupo4d.morapack.model.entity.AeropuertoEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.PedidoEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.RutaEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.VueloEntity;
import com.pucp.dp1.grupo4d.morapack.service.model.*;
import com.pucp.dp1.grupo4d.morapack.util.G4D;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    @Autowired
    private PedidoAdapter pedidoAdapter;

    @Autowired
    private AeropuertoAdapter aeropuertoAdapter;

    @Autowired
    private VueloAdapter vueloAdapter;

    @Autowired
    private RutaAdapter rutaAdapter;

    public SolutionResponse listarParaSimulacion(SimulationRequest request) {
        try {
            LocalDateTime fechaHoraInicio = G4D.toDateTime(request.getFechaHoraInicio());
            LocalDateTime fechaHoraFin = G4D.toDateTime(request.getFechaHoraFin());
            Integer desfaseDeDias = request.getDesfaseDeDias();
            if(fechaHoraFin.isBefore(fechaHoraInicio)) {
                return new SolutionResponse(false, "Mal formato de rango de fechas.");
            }
            if(desfaseDeDias == null || desfaseDeDias < 0) {
                return new SolutionResponse(false, "Mal formato de tiempo de desfase.");
            }
            return devolverSolucion(fechaHoraInicio, fechaHoraFin, desfaseDeDias);
        } catch (Exception e) {
            e.printStackTrace();
            return new SolutionResponse(false, "ERROR - ENVÍO HACIA SIMULACIÓN: " + e.getMessage());
        }
    }

    private SolutionResponse devolverSolucion(LocalDateTime fechaHoraInicio, LocalDateTime fechaHoraFin, Integer desfaseDeDias) {
        List<PedidoDTO> pedidosDTO = new ArrayList<>();
        List<PedidoEntity> pedidosEntity = pedidoService.findByDateTimeRange(fechaHoraInicio, fechaHoraFin, desfaseDeDias);
        pedidosEntity.forEach(p -> pedidosDTO.add(pedidoAdapter.toDTO(p)));
        List<AeropuertoDTO> aeropuertosDTO = new ArrayList<>();
        List<AeropuertoEntity> aeropuertosEntity = aeropuertoService.findAll();
        aeropuertosEntity.forEach(a ->  aeropuertosDTO.add(aeropuertoAdapter.toDTO(a)));
        List<VueloDTO> vuelosDTO = new ArrayList<>();
        List<VueloEntity> vuelosEntity = vueloService.findByDateTimeRange(fechaHoraInicio, fechaHoraFin, desfaseDeDias);
        vuelosEntity.forEach(v -> vuelosDTO.add(vueloAdapter.toDTO(v)));
        List<RutaDTO> rutasDTO = new ArrayList<>();
        List<RutaEntity> rutasEntity = rutaService.findByDateTimeRange(fechaHoraInicio, fechaHoraFin, desfaseDeDias);
        rutasEntity.forEach(r -> rutasDTO.add(rutaAdapter.toDTO(r)));
        return new SolutionResponse(true, "Simulación correctamente enviada!", pedidosDTO, aeropuertosDTO, vuelosDTO, rutasDTO);
    }
}
