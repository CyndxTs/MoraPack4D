/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       SimulationService.java
 [**/

package com.pucp.dp1.grupo4d.morapack.service;

import com.pucp.dp1.grupo4d.morapack.mapper.AeropuertoMapper;
import com.pucp.dp1.grupo4d.morapack.mapper.PedidoMapper;
import com.pucp.dp1.grupo4d.morapack.mapper.RutaMapper;
import com.pucp.dp1.grupo4d.morapack.mapper.VueloMapper;
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
    private PedidoMapper pedidoMapper;

    @Autowired
    private AeropuertoMapper aeropuertoMapper;

    @Autowired
    private VueloMapper vueloMapper;

    @Autowired
    private RutaMapper rutaMapper;

    public SolutionResponse listarParaSimulacion(SimulationRequest request) {
        try {
            LocalDateTime fechaHoraInicio = (G4D.isAdmissible(request.getFechaHoraInicio())) ? G4D.toDateTime(request.getFechaHoraInicio()) : G4D.toDateTime("1999-12-31 23:59:59");
            LocalDateTime fechaHoraFin = (G4D.isAdmissible(request.getFechaHoraFin())) ? G4D.toDateTime(request.getFechaHoraFin()) : LocalDateTime.now();
            if(fechaHoraFin.isBefore(fechaHoraInicio)) {
                return new SolutionResponse(false, "Rango invalido.");
            }
            Integer desfaseTemporal = G4D.isAdmissible(request.getDesfaseTemporal()) ? request.getDesfaseTemporal() : Integer.valueOf(0);
            fechaHoraInicio = fechaHoraInicio.minusDays(desfaseTemporal);
            fechaHoraFin = fechaHoraFin.plusDays(desfaseTemporal);
            return devolverSolucion(fechaHoraInicio, fechaHoraFin);
        } catch (Exception e) {
            e.printStackTrace();
            return new SolutionResponse(false, "ERROR - ENVÍO HACIA SIMULACIÓN: " + e.getMessage());
        }
    }

    private SolutionResponse devolverSolucion(LocalDateTime fechaHoraInicio, LocalDateTime fechaHoraFin) {
        List<PedidoDTO> pedidosDTO = new ArrayList<>();
        List<PedidoEntity> pedidosEntity = pedidoService.findByDateTimeRange(fechaHoraInicio, fechaHoraFin);
        pedidosEntity.forEach(p -> pedidosDTO.add(pedidoMapper.toDTO(p)));
        List<AeropuertoDTO> aeropuertosDTO = new ArrayList<>();
        List<AeropuertoEntity> aeropuertosEntity = aeropuertoService.findAll();
        aeropuertosEntity.forEach(a ->  aeropuertosDTO.add(aeropuertoMapper.toDTO(a)));
        List<VueloDTO> vuelosDTO = new ArrayList<>();
        List<VueloEntity> vuelosEntity = vueloService.findByDateTimeRange(fechaHoraInicio, fechaHoraFin);
        vuelosEntity.forEach(v -> vuelosDTO.add(vueloMapper.toDTO(v)));
        List<RutaDTO> rutasDTO = new ArrayList<>();
        List<RutaEntity> rutasEntity = rutaService.findByDateTimeRange(fechaHoraInicio, fechaHoraFin);
        rutasEntity.forEach(r -> rutasDTO.add(rutaMapper.toDTO(r)));
        limpiarPools();
        return new SolutionResponse(true, "Simulación correctamente enviada!", pedidosDTO, aeropuertosDTO, vuelosDTO, rutasDTO);
    }

    private void limpiarPools() {
        pedidoMapper.clearPools();
        aeropuertoMapper.clearPools();
        vueloMapper.clearPools();
        rutaMapper.clearPools();
    }
}
