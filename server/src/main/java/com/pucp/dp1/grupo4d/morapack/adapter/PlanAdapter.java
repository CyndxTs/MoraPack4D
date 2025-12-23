/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       PlanAdapter.java
 [**/

package com.pucp.dp1.grupo4d.morapack.adapter;

import com.pucp.dp1.grupo4d.morapack.model.algorithm.Evento;
import com.pucp.dp1.grupo4d.morapack.model.algorithm.Plan;
import com.pucp.dp1.grupo4d.morapack.model.entity.EventoEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.PlanEntity;
import com.pucp.dp1.grupo4d.morapack.service.model.PlanService;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class PlanAdapter {
    private final PlanService planService;
    private final EventoAdapter eventoAdapter;
    private final AeropuertoAdapter aeropuertoAdapter;
    private final Map<String, Map<String, Plan>> poolAlgorithm = new HashMap<>();
    private final Map<String, Map<String, PlanEntity>> poolEntity = new HashMap<>();

    public PlanAdapter(AeropuertoAdapter aeropuertoAdapter, PlanService planService, EventoAdapter eventoAdapter) {
        this.aeropuertoAdapter = aeropuertoAdapter;
        this.planService = planService;
        this.eventoAdapter = eventoAdapter;
    }

    public Plan toAlgorithm(String idTransaccion, PlanEntity entity) {
        if (poolAlgorithm.containsKey(idTransaccion) && poolAlgorithm.get(idTransaccion).containsKey(entity.getCodigo())) {
            return poolAlgorithm.get(idTransaccion).get(entity.getCodigo());
        }
        Plan algorithm = new Plan();
        algorithm.setCodigo(entity.getCodigo());
        algorithm.setCapacidad(entity.getCapacidad());
        algorithm.setDuracion(entity.getDuracion());
        algorithm.setDistancia(entity.getDistancia());
        algorithm.setHoraSalida(entity.getHoraSalidaUTC());
        algorithm.setHoraLlegada(entity.getHoraLlegadaUTC());
        algorithm.setOrigen(aeropuertoAdapter.toAlgorithm(idTransaccion, entity.getOrigen()));
        algorithm.setDestino(aeropuertoAdapter.toAlgorithm(idTransaccion, entity.getDestino()));
        List<Evento> eventos = new ArrayList<>();
        List<EventoEntity> eventosEntity = entity.getEventos();
        eventosEntity.forEach(e -> eventos.add(eventoAdapter.toAlgorithm(idTransaccion, e)));
        algorithm.setEventos(eventos);
        if(!poolAlgorithm.containsKey(idTransaccion)) {
            poolAlgorithm.put(idTransaccion, new HashMap<>());
        }
        poolAlgorithm.get(idTransaccion).put(algorithm.getCodigo(), algorithm);
        return algorithm;
    }

    public PlanEntity toEntity(String idTransaccion, Plan algorithm) {
        if(poolEntity.containsKey(idTransaccion) && poolEntity.get(idTransaccion).containsKey(algorithm.getCodigo())) {
            return poolEntity.get(idTransaccion).get(algorithm.getCodigo());
        }
        PlanEntity entity = planService.findByCodigo(algorithm.getCodigo()).orElse(null);
        if (entity != null) {
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
        eventoAdapter.clearPools(idTransaccion);
        aeropuertoAdapter.clearPools(idTransaccion);
    }
}
