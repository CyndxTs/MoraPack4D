/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       LoteAdapter.java
 [**/

package com.pucp.dp1.grupo4d.morapack.adapter;

import com.pucp.dp1.grupo4d.morapack.model.algorithm.Lote;
import com.pucp.dp1.grupo4d.morapack.model.entity.LoteEntity;
import com.pucp.dp1.grupo4d.morapack.service.model.LoteService;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class LoteAdapter {
    private final LoteService loteService;
    private final Map<String, Map<String, Lote>> poolAlgorithm = new HashMap<>();
    private final Map<String, Map<String, LoteEntity>> poolEntity = new HashMap<>();

    public LoteAdapter(LoteService loteService) {
        this.loteService = loteService;
    }

    public Lote toAlgorithm(String idTransaccion, LoteEntity entity) {
        if (poolAlgorithm.containsKey(idTransaccion) && poolAlgorithm.get(idTransaccion).containsKey(entity.getCodigo())) {
            return poolAlgorithm.get(idTransaccion).get(entity.getCodigo());
        }
        Lote algorithm = new Lote();
        algorithm.setCodigo(entity.getCodigo());
        algorithm.setTamanio(entity.getTamanio());
        algorithm.setEstado(entity.getEstado());
        if(!poolAlgorithm.containsKey(idTransaccion)) {
            poolAlgorithm.put(idTransaccion, new HashMap<>());
        }
        poolAlgorithm.get(idTransaccion).put(algorithm.getCodigo(), algorithm);
        return algorithm;
    }

    public LoteEntity toEntity(String idTransaccion, Lote algorithm) {
        if (poolEntity.containsKey(idTransaccion) && poolEntity.get(idTransaccion).containsKey(algorithm.getCodigo())) {
            return poolEntity.get(idTransaccion).get(algorithm.getCodigo());
        }
        LoteEntity entity = loteService.findByCodigo(algorithm.getCodigo()).orElse(new LoteEntity());
        entity.setCodigo(algorithm.getCodigo());
        entity.setTamanio(algorithm.getTamanio());
        entity.setEstado(algorithm.getEstado());
        if(!poolEntity.containsKey(idTransaccion)) {
            poolEntity.put(idTransaccion, new HashMap<>());
        }
        poolEntity.get(idTransaccion).put(entity.getCodigo(), entity);
        return entity;
    }

    public void clearPools(String idTransaccion) {
        poolAlgorithm.remove(idTransaccion);
        poolEntity.remove(idTransaccion);
    }
}
