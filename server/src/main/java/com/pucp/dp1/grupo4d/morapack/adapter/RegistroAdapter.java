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

    private final Map<String, Registro> pool = new HashMap<>();

    private final LoteAdapter loteAdapter;

    public RegistroAdapter(LoteAdapter loteAdapter) {
        this.loteAdapter = loteAdapter;
    }

    public Registro toAlgorithm(RegistroEntity entity) {
        if (entity == null) return null;

        if (pool.containsKey(entity.getCodigo())) {
            return pool.get(entity.getCodigo());
        }

        Registro algorithm = new Registro();
        algorithm.setCodigo(entity.getCodigo());
        algorithm.setFechaHoraIngresoLocal(entity.getFechaHoraIngresoLocal());
        algorithm.setFechaHoraIngresoUTC(entity.getFechaHoraIngresoUTC());
        algorithm.setFechaHoraEgresoLocal(entity.getFechaHoraEgresoLocal());
        algorithm.setFechaHoraEgresoUTC(entity.getFechaHoraEgresoUTC());
        algorithm.setLote(loteAdapter.toAlgorithm(entity.getLote()));
        pool.put(algorithm.getCodigo(), algorithm);
        return algorithm;
    }

    public RegistroEntity toEntity(Registro algorithm) {
        if (algorithm == null) return null;
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
        return entity;
    }

    public void clearPool() {
        pool.clear();
    }
}
