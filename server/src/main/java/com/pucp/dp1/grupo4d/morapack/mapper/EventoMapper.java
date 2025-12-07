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
import java.util.HashMap;
import java.util.Map;

@Component
public class EventoMapper {

    public EventoDTO toDTO(Evento algorithm) {
        EventoDTO dto = new EventoDTO();
        dto.setCodigo(algorithm.getCodigo());
        dto.setTipo(algorithm.getTipo().toString());
        dto.setFechaHoraInicio(G4DUtility.Convertor.toDisplayString(algorithm.getFechaHoraInicio()));
        dto.setFechaHoraFin(G4DUtility.Convertor.toDisplayString(algorithm.getFechaHoraFin()));
        return dto;
    }

    public EventoDTO toDTO(EventoEntity entity) {
        EventoDTO dto = new EventoDTO();
        dto.setCodigo(entity.getCodigo());
        dto.setTipo(entity.getTipo().toString());
        dto.setFechaHoraInicio(G4DUtility.Convertor.toDisplayString(entity.getFechaHoraInicio()));
        dto.setFechaHoraFin(G4DUtility.Convertor.toDisplayString(entity.getFechaHoraFin()));
        dto.setFechaHoraSalida(G4DUtility.Convertor.toDisplayString(entity.getFechaHoraSalidaUTC()));
        dto.setFechaHoraLlegada(G4DUtility.Convertor.toDisplayString(entity.getFechaHoraLlegadaUTC()));
        return dto;
    }
}
