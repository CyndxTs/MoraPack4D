/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       EventoMapper.java
 [**/

package com.pucp.dp1.grupo4d.morapack.mapper;

import com.pucp.dp1.grupo4d.morapack.model.algorithm.Evento;
import com.pucp.dp1.grupo4d.morapack.model.dto.EventoDTO;
import com.pucp.dp1.grupo4d.morapack.model.entity.EventoEntity;
import com.pucp.dp1.grupo4d.morapack.util.G4DUtility;
import org.springframework.stereotype.Component;

@Component
public class EventoMapper {

    public EventoDTO toDTO(Evento algorithm) {
        EventoDTO dto = new EventoDTO();
        dto.setCodigo(algorithm.getCodigo());
        dto.setTipo(algorithm.getTipo().toString());
        dto.setFechaHoraInicio(G4DUtility.Convertor.toDisplayString(algorithm.getFechaHoraInicio()));
        dto.setFechaHoraFin(G4DUtility.Convertor.toDisplayString(algorithm.getFechaHoraFin()));
        dto.setFechaHoraNuevaSalida(G4DUtility.Convertor.toDisplayString(algorithm.getFechaHoraSalida()));
        dto.setFechaHoraNuevaLlegada(G4DUtility.Convertor.toDisplayString(algorithm.getFechaHoraLlegada()));
        return dto;
    }

    public EventoDTO toDTO(EventoEntity entity) {
        EventoDTO dto = new EventoDTO();
        dto.setCodigo(entity.getCodigo());
        dto.setTipo(entity.getTipo().toString());
        dto.setFechaHoraInicio(G4DUtility.Convertor.toDisplayString(entity.getFechaHoraInicio()));
        dto.setFechaHoraFin(G4DUtility.Convertor.toDisplayString(entity.getFechaHoraFin()));
        dto.setFechaHoraNuevaSalida(G4DUtility.Convertor.toDisplayString(entity.getFechaHoraNuevaSalida()));
        dto.setFechaHoraNuevaLlegada(G4DUtility.Convertor.toDisplayString(entity.getFechaHoraNuevaLlegada()));
        dto.setNuevaCapacidad(entity.getNuevaCapacidad());
        dto.setCodPlan(entity.getPlan().getCodigo());
        return dto;
    }
}
