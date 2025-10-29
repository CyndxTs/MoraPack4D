/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       LoteAdapter.java
 [**/

package com.pucp.dp1.grupo4d.morapack.adapter;

import com.pucp.dp1.grupo4d.morapack.model.algorithm.Lote;
import com.pucp.dp1.grupo4d.morapack.model.entity.LoteEntity;
import com.pucp.dp1.grupo4d.morapack.service.model.LoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class LoteAdapter {

    @Autowired
    private LoteService loteService;

    private final Map<String, Lote> poolAlgorithm = new HashMap<>();
    private final Map<String, LoteEntity>  poolEntity = new HashMap<>();

    public Lote toAlgorithm(LoteEntity entity) {
        if (entity == null) return null;

        if (poolAlgorithm.containsKey(entity.getCodigo())) {
            return poolAlgorithm.get(entity.getCodigo());
        }
        Lote algorithm = new Lote();
        algorithm.setCodigo(entity.getCodigo());
        algorithm.setTamanio(entity.getTamanio());
        poolAlgorithm.put(algorithm.getCodigo(), algorithm);
        return algorithm;
    }

    public LoteEntity toEntity(Lote algorithm) {
        if (algorithm == null) return null;
        if (poolEntity.containsKey(algorithm.getCodigo())) {
            return poolEntity.get(algorithm.getCodigo());
        }
        LoteEntity entity = loteService.findByCodigo(algorithm.getCodigo()).orElse(null);
        if (entity == null) {
            entity = new LoteEntity();
            entity.setCodigo(algorithm.getCodigo());
        };
        entity.setTamanio(algorithm.getTamanio());
        poolEntity.put(entity.getCodigo(), entity);
        return entity;
    }

    public void clearPools() {
        poolAlgorithm.clear();
        poolEntity.clear();
    }
}
