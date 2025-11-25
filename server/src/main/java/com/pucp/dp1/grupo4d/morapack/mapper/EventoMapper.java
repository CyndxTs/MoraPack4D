/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       EventoMapper.java
 [**/

package com.pucp.dp1.grupo4d.morapack.mapper;

import com.pucp.dp1.grupo4d.morapack.model.algorithm.Evento;
import com.pucp.dp1.grupo4d.morapack.model.dto.EventoDTO;
import com.pucp.dp1.grupo4d.morapack.model.entity.EventoEntity;
import com.pucp.dp1.grupo4d.morapack.model.enums.TipoEvento;
import com.pucp.dp1.grupo4d.morapack.util.G4D;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class EventoMapper {

    private final Map<String, EventoDTO> poolDTO = new HashMap<>();

    public EventoEntity toEntity(EventoDTO dto) {
        EventoEntity entity = new EventoEntity();
        entity.setCodigo(dto.getCodigo());
        entity.setTipo(TipoEvento.valueOf(dto.getTipo()));
        entity.setFechaHoraInicio(G4D.toDateTime(dto.getFechaHoraInicio()));
        entity.setFechaHoraFin(G4D.toDateTime(dto.getFechaHoraFin()));
        entity.setFechaHoraSalidaUTC(G4D.toDateTime(dto.getFechaHoraSalida()));
        entity.setFechaHoraSalidaLocal(G4D.toLocal(entity.getFechaHoraSalidaUTC(), entity.getPlan().getOrigen().getHusoHorario()));
        entity.setFechaHoraLlegadaUTC(G4D.toDateTime(dto.getFechaHoraLlegada()));
        entity.setFechaHoraLlegadaLocal(G4D.toLocal(entity.getFechaHoraLlegadaUTC(), entity.getPlan().getDestino().getHusoHorario()));
        return entity;
    }

    public EventoDTO toDTO(Evento algorithm) {
        if (poolDTO.containsKey(algorithm.getCodigo())) {
            return poolDTO.get(algorithm.getCodigo());
        }
        EventoDTO dto = new EventoDTO();
        dto.setCodigo(algorithm.getCodigo());
        dto.setTipo(algorithm.getTipo().toString());
        dto.setFechaHoraInicio(G4D.toDisplayString(algorithm.getFechaHoraInicio()));
        dto.setFechaHoraFin(G4D.toDisplayString(algorithm.getFechaHoraFin()));
        poolDTO.put(algorithm.getCodigo(), dto);
        return dto;
    }

    public EventoDTO toDTO(EventoEntity entity) {
        if(poolDTO.containsKey(entity.getCodigo())) {
            return poolDTO.get(entity.getCodigo());
        }
        EventoDTO dto = new EventoDTO();
        dto.setCodigo(entity.getCodigo());
        dto.setTipo(entity.getTipo().toString());
        dto.setFechaHoraInicio(G4D.toDisplayString(entity.getFechaHoraInicio()));
        dto.setFechaHoraFin(G4D.toDisplayString(entity.getFechaHoraFin()));
        dto.setFechaHoraSalida(G4D.toDisplayString(entity.getFechaHoraSalidaUTC()));
        dto.setFechaHoraLlegada(G4D.toDisplayString(entity.getFechaHoraLlegadaUTC()));
        poolDTO.put(entity.getCodigo(), dto);
        return dto;
    }

    public void clearPools() {
        poolDTO.clear();
    }
}
