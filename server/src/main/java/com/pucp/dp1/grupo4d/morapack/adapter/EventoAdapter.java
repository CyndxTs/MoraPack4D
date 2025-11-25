/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       EventoAdapter.java
 [**/

package com.pucp.dp1.grupo4d.morapack.adapter;

import com.pucp.dp1.grupo4d.morapack.model.algorithm.Evento;
import com.pucp.dp1.grupo4d.morapack.model.entity.EventoEntity;
import com.pucp.dp1.grupo4d.morapack.util.G4D;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class EventoAdapter {

    private final Map<String, Evento> poolAlgorithm = new HashMap<>();
    private final Map<String, EventoEntity> poolEntity = new HashMap<>();

    public Evento toAlgorithm(EventoEntity entity) {
        if(poolAlgorithm.containsKey(entity.getCodigo())) {
            return poolAlgorithm.get(entity.getCodigo());
        }
        Evento algorithm = new Evento();
        algorithm.setCodigo(entity.getCodigo());
        algorithm.setTipo(entity.getTipo());
        algorithm.setFechaHoraInicio(entity.getFechaHoraInicio());
        algorithm.setFechaHoraFin(entity.getFechaHoraFin());
        algorithm.setFechaHoraSalida(entity.getFechaHoraSalidaUTC());
        algorithm.setFechaHoraLlegada(entity.getFechaHoraLlegadaUTC());
        poolAlgorithm.put(entity.getCodigo(), algorithm);
        return algorithm;
    }

    public EventoEntity toEntity(Evento algorithm) {
        if(poolEntity.containsKey(algorithm.getCodigo())) {
            return poolEntity.get(algorithm.getCodigo());
        }
        EventoEntity entity = new EventoEntity();
        entity.setCodigo(algorithm.getCodigo());
        entity.setTipo(algorithm.getTipo());
        entity.setFechaHoraSalidaUTC(algorithm.getFechaHoraSalida());
        entity.setFechaHoraSalidaLocal(G4D.toLocal(algorithm.getFechaHoraSalida(), entity.getPlan().getOrigen().getHusoHorario()));
        entity.setFechaHoraLlegadaUTC(algorithm.getFechaHoraLlegada());
        entity.setFechaHoraLlegadaLocal(G4D.toLocal(algorithm.getFechaHoraLlegada(), entity.getPlan().getDestino().getHusoHorario()));
        poolEntity.put(algorithm.getCodigo(), entity);
        return entity;
    }

    public void clearPools() {
        poolAlgorithm.clear();
        poolEntity.clear();
    }
}
