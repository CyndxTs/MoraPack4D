/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       EventoAdapter.java
 [**/

package com.pucp.dp1.grupo4d.morapack.adapter;

import com.pucp.dp1.grupo4d.morapack.model.algorithm.Evento;
import com.pucp.dp1.grupo4d.morapack.model.entity.EventoEntity;
import com.pucp.dp1.grupo4d.morapack.service.model.EventoService;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class EventoAdapter {
    private final EventoService eventoService;
    private final Map<String, Map<String, Evento>> poolAlgorithm = new HashMap<>();
    private final Map<String, Map<String, EventoEntity>> poolEntity = new HashMap<>();

    public EventoAdapter(EventoService eventoService) {
        this.eventoService = eventoService;
    }

    public Evento toAlgorithm(String idTransaccion, EventoEntity entity) {
        if(poolAlgorithm.containsKey(idTransaccion) && poolAlgorithm.get(idTransaccion).containsKey(entity.getCodigo())) {
            return poolAlgorithm.get(idTransaccion).get(entity.getCodigo());
        }
        Evento algorithm = new Evento();
        algorithm.setCodigo(entity.getCodigo());
        algorithm.setTipo(entity.getTipo());
        algorithm.setFechaHoraInicio(entity.getFechaHoraInicio());
        algorithm.setFechaHoraFin(entity.getFechaHoraFin());
        algorithm.setFechaHoraSalida(entity.getFechaHoraSalidaUTC());
        algorithm.setFechaHoraLlegada(entity.getFechaHoraLlegadaUTC());
        if(!poolAlgorithm.containsKey(idTransaccion)) {
            poolAlgorithm.put(idTransaccion, new HashMap<>());
        }
        poolAlgorithm.get(idTransaccion).put(algorithm.getCodigo(), algorithm);
        return algorithm;
    }

    public EventoEntity toEntity(String idTransaccion, Evento algorithm) {
        if(poolEntity.containsKey(idTransaccion) && poolEntity.get(idTransaccion).containsKey(algorithm.getCodigo())) {
            return poolEntity.get(idTransaccion).get(algorithm.getCodigo());
        }
        EventoEntity entity = eventoService.findByCodigo(algorithm.getCodigo()).orElse(null);
        if(entity != null) {
            if(!poolEntity.containsKey(idTransaccion)) {
                poolEntity.put(idTransaccion, new HashMap<>());
            }
            poolEntity.get(idTransaccion).put(entity.getCodigo(), entity);
        }
        return entity;
    }

    public void clearPools(String idTransaccion) {
        poolAlgorithm.remove(idTransaccion);
        poolEntity.remove(idTransaccion);
    }
}
