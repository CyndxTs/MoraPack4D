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
import com.pucp.dp1.grupo4d.morapack.util.G4DUtility;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class PlanMapper {
    private final EventoMapper eventoMapper;

    public PlanMapper(EventoMapper eventoMapper) {
        this.eventoMapper = eventoMapper;
    }

    public PlanDTO toDTO(Plan algorithm) {
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
        return planDTO;
    }

    public PlanDTO toDTO(PlanEntity entity) {
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
        return planDTO;
    }
}
