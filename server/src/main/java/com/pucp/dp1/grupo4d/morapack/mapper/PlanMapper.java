/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       PlanMapper.java
 [**/

package com.pucp.dp1.grupo4d.morapack.mapper;

import com.pucp.dp1.grupo4d.morapack.model.algorithm.Aeropuerto;
import com.pucp.dp1.grupo4d.morapack.model.algorithm.Evento;
import com.pucp.dp1.grupo4d.morapack.model.algorithm.Plan;
import com.pucp.dp1.grupo4d.morapack.model.dto.EventoDTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.PlanDTO;
import com.pucp.dp1.grupo4d.morapack.model.entity.AeropuertoEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.EventoEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.PlanEntity;
import com.pucp.dp1.grupo4d.morapack.model.exception.G4DException;
import com.pucp.dp1.grupo4d.morapack.service.model.AeropuertoService;
import com.pucp.dp1.grupo4d.morapack.util.G4DUtility;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PlanMapper {

    private final Map<String, PlanDTO> poolDTO = new HashMap<>();
    private final EventoMapper eventoMapper;
    private final AeropuertoService aeropuertoService;

    public PlanMapper(EventoMapper eventoMapper, AeropuertoService aeropuertoService) {
        this.eventoMapper = eventoMapper;
        this.aeropuertoService = aeropuertoService;
    }

    public PlanDTO toDTO(Plan algorithm) {
        if (poolDTO.containsKey(algorithm.getCodigo())) {
            return poolDTO.get(algorithm.getCodigo());
        }
        PlanDTO planDTO = new PlanDTO();
        planDTO.setCodigo(algorithm.getCodigo());
        Aeropuerto origen = algorithm.getOrigen();
        planDTO.setCodOrigen(origen.getCodigo());
        Aeropuerto destino = algorithm.getDestino();
        planDTO.setCodDestino(destino.getCodigo());
        planDTO.setHoraSalida(G4DUtility.Convertor.toDisplayString(algorithm.getHoraSalida()));
        planDTO.setHoraLlegada(G4DUtility.Convertor.toDisplayString(algorithm.getHoraLlegada()));
        planDTO.setCapacidad(algorithm.getCapacidad());
        planDTO.setDuracion(algorithm.getDuracion());
        planDTO.setDistancia(algorithm.getDistancia());
        List<EventoDTO> eventosDTO = new ArrayList<>();
        List<Evento> eventos = algorithm.getEventos();
        for (Evento evento : eventos) {
            EventoDTO eventoDTO = eventoMapper.toDTO(evento);
            eventosDTO.add(eventoDTO);
        }
        planDTO.setEventos(eventosDTO);
        poolDTO.put(algorithm.getCodigo(), planDTO);
        return planDTO;
    }

    public PlanDTO toDTO(PlanEntity entity) {
        if (poolDTO.containsKey(entity.getCodigo())) {
            return poolDTO.get(entity.getCodigo());
        }
        PlanDTO planDTO = new PlanDTO();
        planDTO.setCodigo(entity.getCodigo());
        AeropuertoEntity origenEntity = entity.getOrigen();
        planDTO.setCodOrigen(origenEntity.getCodigo());
        AeropuertoEntity destinoEntity = entity.getDestino();
        planDTO.setCodDestino(destinoEntity.getCodigo());
        planDTO.setHoraSalida(G4DUtility.Convertor.toDisplayString(entity.getHoraSalidaUTC()));
        planDTO.setHoraLlegada(G4DUtility.Convertor.toDisplayString(entity.getHoraLlegadaUTC()));
        planDTO.setCapacidad(entity.getCapacidad());
        planDTO.setDuracion(entity.getDuracion());
        planDTO.setDistancia(entity.getDistancia());
        List<EventoDTO> eventosDTO = new ArrayList<>();
        List<EventoEntity> eventosEntity = entity.getEventos();
        for (EventoEntity eventoEntity : eventosEntity) {
            EventoDTO eventoDTO = eventoMapper.toDTO(eventoEntity);
            eventosDTO.add(eventoDTO);
        }
        planDTO.setEventos(eventosDTO);
        poolDTO.put(entity.getCodigo(), planDTO);
        return planDTO;
    }

    public PlanEntity toEntity(PlanDTO dto) {
        if(poolDTO.containsKey(dto.getCodigo())) {
            return null;
        }
        PlanEntity entity =  new PlanEntity();
        entity.setCodigo(dto.getCodigo());
        entity.setDistancia(dto.getDistancia());
        entity.setDuracion(dto.getDuracion());
        String codOrigen = dto.getCodOrigen();
        AeropuertoEntity origen = aeropuertoService.obtenerPorCodigo(codOrigen);
        if(origen != null) {
            String codDestino = dto.getCodDestino();
            AeropuertoEntity destino = aeropuertoService.obtenerPorCodigo(codDestino);
            if(destino != null) {
                entity.setOrigen(origen);
                entity.setDestino(destino);
                entity.setHoraSalidaUTC(G4DUtility.Convertor.toTime(dto.getHoraSalida()));
                entity.setHoraSalidaLocal(G4DUtility.Convertor.toLocal(entity.getHoraSalidaUTC(), origen.getHusoHorario()));
                entity.setHoraLlegadaUTC(G4DUtility.Convertor.toTime(dto.getHoraLlegada()));
                entity.setHoraLlegadaLocal(G4DUtility.Convertor.toLocal(entity.getHoraLlegadaUTC(), destino.getHusoHorario()));
                return entity;
            } else throw new G4DException(String.format("El destino ('%s') del plan es inválido.", codDestino));
        } else throw new G4DException(String.format("El origen ('%s') del plan es inválido.", codOrigen));
    }

    public void clearPools() {
        poolDTO.clear();
        eventoMapper.clearPools();
        aeropuertoService.clearPools();
    }
}
