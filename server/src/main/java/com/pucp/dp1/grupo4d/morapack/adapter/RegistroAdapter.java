/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       RegistroAdapter.java
 [**/

package com.pucp.dp1.grupo4d.morapack.adapter;

import com.pucp.dp1.grupo4d.morapack.model.algorithm.Registro;
import com.pucp.dp1.grupo4d.morapack.model.entity.RegistroEntity;
import com.pucp.dp1.grupo4d.morapack.service.model.RegistroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class RegistroAdapter {

    @Autowired
    private RegistroService registroService;

    private final LoteAdapter loteAdapter;

    private final Map<String, Registro> poolAlgorithm = new HashMap<>();
    private final Map<String, RegistroEntity> poolEntity = new HashMap<>();

    public RegistroAdapter(LoteAdapter loteAdapter) {
        this.loteAdapter = loteAdapter;
    }

    public Registro toAlgorithm(RegistroEntity entity) {
        if (entity == null) return null;
        if (poolAlgorithm.containsKey(entity.getCodigo())) {
            return poolAlgorithm.get(entity.getCodigo());
        }
        Registro algorithm = new Registro();
        algorithm.setCodigo(entity.getCodigo());
        algorithm.setFechaHoraIngresoLocal(entity.getFechaHoraIngresoLocal());
        algorithm.setFechaHoraIngresoUTC(entity.getFechaHoraIngresoUTC());
        algorithm.setFechaHoraEgresoLocal(entity.getFechaHoraEgresoLocal());
        algorithm.setFechaHoraEgresoUTC(entity.getFechaHoraEgresoUTC());
        algorithm.setLote(loteAdapter.toAlgorithm(entity.getLote()));
        poolAlgorithm.put(algorithm.getCodigo(), algorithm);
        return algorithm;
    }

    public RegistroEntity toEntity(Registro algorithm) {
        if (algorithm == null) return null;
        if (poolEntity.containsKey(algorithm.getCodigo())) {
            return poolEntity.get(algorithm.getCodigo());
        }
        RegistroEntity entity = registroService.findByCodigo(algorithm.getCodigo()).orElse(null);
        if (entity == null) {
            entity = new RegistroEntity();
            entity.setCodigo(algorithm.getCodigo());
        }
        entity.setFechaHoraIngresoLocal(algorithm.getFechaHoraIngresoLocal());
        entity.setFechaHoraIngresoUTC(algorithm.getFechaHoraIngresoUTC());
        entity.setFechaHoraEgresoLocal(algorithm.getFechaHoraEgresoLocal());
        entity.setFechaHoraEgresoUTC(algorithm.getFechaHoraEgresoUTC());
        entity.setLote(loteAdapter.toEntity(algorithm.getLote()));
        if (entity.getLote() != null) entity.getLote().getRegistros().add(entity);
        poolEntity.put(entity.getCodigo(), entity);
        return entity;
    }

    public void clearPools() {
        poolAlgorithm.clear();
        poolEntity.clear();
        loteAdapter.clearPools();
    }
}
