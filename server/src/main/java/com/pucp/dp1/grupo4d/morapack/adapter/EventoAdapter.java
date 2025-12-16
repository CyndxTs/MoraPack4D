/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       EventoAdapter.java
 [**/

package com.pucp.dp1.grupo4d.morapack.adapter;

import com.pucp.dp1.grupo4d.morapack.model.algorithm.Evento;
import com.pucp.dp1.grupo4d.morapack.model.entity.EventoEntity;
import com.pucp.dp1.grupo4d.morapack.service.model.EventoService;
import com.pucp.dp1.grupo4d.morapack.util.G4DUtility;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class EventoAdapter {
    private final EventoService eventoService;
    private final Map<String, Evento> poolAlgorithm = new HashMap<>();
    private final Map<String, EventoEntity> poolEntity = new HashMap<>();

    public EventoAdapter(EventoService eventoService) {
        this.eventoService = eventoService;
    }

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
        EventoEntity entity = eventoService.findByCodigo(algorithm.getCodigo()).orElse(null);
        if(entity == null) {
            return null;
        }
        poolEntity.put(algorithm.getCodigo(), entity);
        return entity;
    }

    public void clearPools() {
        poolAlgorithm.clear();
        poolEntity.clear();
    }
}
