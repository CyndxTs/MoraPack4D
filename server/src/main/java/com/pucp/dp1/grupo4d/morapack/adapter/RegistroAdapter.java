/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       RegistroAdapter.java
 [**/

package com.pucp.dp1.grupo4d.morapack.adapter;

import com.pucp.dp1.grupo4d.morapack.model.algorithm.Lote;
import com.pucp.dp1.grupo4d.morapack.model.algorithm.Registro;
import com.pucp.dp1.grupo4d.morapack.model.entity.LoteEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.RegistroEntity;
import com.pucp.dp1.grupo4d.morapack.service.model.LoteService;
import com.pucp.dp1.grupo4d.morapack.service.model.RegistroService;
import com.pucp.dp1.grupo4d.morapack.util.G4DUtility;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class RegistroAdapter {

    private final RegistroService registroService;
    private final LoteService loteService;
    private final LoteAdapter loteAdapter;
    private final Map<String, Registro> poolAlgorithm = new HashMap<>();
    private final Map<String, RegistroEntity> poolEntity = new HashMap<>();

    public RegistroAdapter(LoteAdapter loteAdapter, LoteService loteService, RegistroService registroService) {
        this.loteAdapter = loteAdapter;
        this.loteService = loteService;
        this.registroService = registroService;
    }

    public Registro toAlgorithm(RegistroEntity entity) {
        if (poolAlgorithm.containsKey(entity.getCodigo())) {
            return poolAlgorithm.get(entity.getCodigo());
        }
        Registro algorithm = new Registro();
        algorithm.setCodigo(entity.getCodigo());
        algorithm.setSigueVigente(entity.getSigueVigente());
        algorithm.setFechaHoraIngreso(entity.getFechaHoraIngresoUTC());
        algorithm.setFechaHoraEgreso(entity.getFechaHoraEgresoUTC());
        Lote lote = loteAdapter.toAlgorithm(entity.getLote());
        algorithm.setLote(lote);
        poolAlgorithm.put(algorithm.getCodigo(), algorithm);
        return algorithm;
    }

    public RegistroEntity toEntity(Registro algorithm) {
        if (poolEntity.containsKey(algorithm.getCodigo())) {
            return poolEntity.get(algorithm.getCodigo());
        }
        RegistroEntity entity = registroService.findByCodigo(algorithm.getCodigo()).orElse(null);
        if (entity == null) {
            entity = new RegistroEntity();
            entity.setCodigo(algorithm.getCodigo());
        }
        entity.setSigueVigente(algorithm.getSigueVigente());
        entity.setFechaHoraIngresoUTC(algorithm.getFechaHoraIngreso());
        entity.setFechaHoraIngresoLocal(G4DUtility.Convertor.toLocal(algorithm.getFechaHoraIngreso(), entity.getAeropuerto().getHusoHorario()));
        entity.setFechaHoraEgresoUTC(algorithm.getFechaHoraEgreso());
        entity.setFechaHoraEgresoLocal(G4DUtility.Convertor.toLocal(algorithm.getFechaHoraEgreso(), entity.getAeropuerto().getHusoHorario()));
        String codLote = algorithm.getLote().getCodigo();
        LoteEntity loteEntity = loteService.findByCodigo(codLote).orElse(null);
        entity.setLote(loteEntity);
        poolEntity.put(entity.getCodigo(), entity);
        return entity;
    }

    public void clearPools() {
        poolAlgorithm.clear();
        poolEntity.clear();
        loteService.clearPools();
        loteAdapter.clearPools();
    }
}
