/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       RegistroAdapter.java
 [**/

package com.pucp.dp1.grupo4d.morapack.adapter;

import com.pucp.dp1.grupo4d.morapack.model.algorithm.Registro;
import com.pucp.dp1.grupo4d.morapack.model.entity.RegistroEntity;
import com.pucp.dp1.grupo4d.morapack.service.model.RegistroService;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class RegistroAdapter {
    private final RegistroService registroService;
    private final LoteAdapter loteAdapter;
    private final Map<String, Map<String, Registro>> poolAlgorithm = new HashMap<>();
    private final Map<String, Map<String, RegistroEntity>> poolEntity = new HashMap<>();

    public RegistroAdapter(RegistroService registroService, LoteAdapter loteAdapter) {
        this.registroService = registroService;
        this.loteAdapter = loteAdapter;
    }

    public Registro toAlgorithm(String idTransaccion, RegistroEntity entity) {
        if (poolAlgorithm.containsKey(idTransaccion) && poolAlgorithm.get(idTransaccion).containsKey(entity.getCodigo())) {
            return poolAlgorithm.get(idTransaccion).get(entity.getCodigo());
        }
        Registro algorithm = new Registro();
        algorithm.setCodigo(entity.getCodigo());
        algorithm.setSigueVigente(entity.getSigueVigente());
        algorithm.setFechaHoraIngreso(entity.getFechaHoraIngresoUTC());
        algorithm.setFechaHoraEgreso(entity.getFechaHoraEgresoUTC());
        algorithm.setLote(loteAdapter.toAlgorithm(idTransaccion, entity.getLote()));
        if(!poolAlgorithm.containsKey(idTransaccion)) {
            poolAlgorithm.put(idTransaccion, new HashMap<>());
        }
        poolAlgorithm.get(idTransaccion).put(algorithm.getCodigo(), algorithm);
        return algorithm;
    }

    public RegistroEntity toEntity(String idTransaccion, Registro algorithm) {
        if (poolEntity.containsKey(idTransaccion) && poolEntity.get(idTransaccion).containsKey(algorithm.getCodigo())) {
            return poolEntity.get(idTransaccion).get(algorithm.getCodigo());
        }
        RegistroEntity entity = registroService.findByCodigo(algorithm.getCodigo()).orElse(new RegistroEntity());
        entity.setCodigo(algorithm.getCodigo());
        entity.setSigueVigente(algorithm.getSigueVigente());
        entity.setFechaHoraIngresoUTC(algorithm.getFechaHoraIngreso());
        entity.setFechaHoraEgresoUTC(algorithm.getFechaHoraEgreso());
        entity.setLote(loteAdapter.toEntity(idTransaccion, algorithm.getLote()));
        if(!poolEntity.containsKey(idTransaccion)) {
            poolEntity.put(idTransaccion, new HashMap<>());
        }
        poolEntity.get(idTransaccion).put(algorithm.getCodigo(), entity);
        return entity;
    }

    public void clearPools(String idTransaccion) {
        poolAlgorithm.remove(idTransaccion);
        poolEntity.remove(idTransaccion);
        loteAdapter.clearPools(idTransaccion);
    }
}
