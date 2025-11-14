/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       RegistroAdapter.java
 [**/

package com.pucp.dp1.grupo4d.morapack.adapter;

import com.pucp.dp1.grupo4d.morapack.model.algorithm.Lote;
import com.pucp.dp1.grupo4d.morapack.model.algorithm.Registro;
import com.pucp.dp1.grupo4d.morapack.model.dto.RegistroDTO;
import com.pucp.dp1.grupo4d.morapack.model.entity.LoteEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.RegistroEntity;
import com.pucp.dp1.grupo4d.morapack.service.model.LoteService;
import com.pucp.dp1.grupo4d.morapack.service.model.RegistroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class RegistroAdapter {

    @Autowired
    private RegistroService registroService;

    @Autowired
    private LoteService loteService;

    private final LoteAdapter loteAdapter;
    private final Map<String, Registro> poolAlgorithm = new HashMap<>();
    private final Map<String, RegistroEntity> poolEntity = new HashMap<>();
    private final Map<String, RegistroDTO>  poolDTO = new HashMap<>();

    public RegistroAdapter(LoteAdapter loteAdapter) {
        this.loteAdapter = loteAdapter;
    }

    public Registro toAlgorithm(RegistroEntity entity) {
        if (poolAlgorithm.containsKey(entity.getCodigo())) {
            return poolAlgorithm.get(entity.getCodigo());
        }
        Registro algorithm = new Registro();
        algorithm.setCodigo(entity.getCodigo());
        algorithm.setFechaHoraIngresoLocal(entity.getFechaHoraIngresoLocal());
        algorithm.setFechaHoraIngresoUTC(entity.getFechaHoraIngresoUTC());
        algorithm.setFechaHoraEgresoLocal(entity.getFechaHoraEgresoLocal());
        algorithm.setFechaHoraEgresoUTC(entity.getFechaHoraEgresoUTC());
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
        entity.setFechaHoraIngresoLocal(algorithm.getFechaHoraIngresoLocal());
        entity.setFechaHoraIngresoUTC(algorithm.getFechaHoraIngresoUTC());
        entity.setFechaHoraEgresoLocal(algorithm.getFechaHoraEgresoLocal());
        entity.setFechaHoraEgresoUTC(algorithm.getFechaHoraEgresoUTC());
        String codLote = algorithm.getLote().getCodigo();
        LoteEntity loteEntity = loteService.findByCodigo(codLote).orElse(null);
        entity.setLote(loteEntity);
        poolEntity.put(entity.getCodigo(), entity);
        return entity;
    }

    public RegistroDTO toDTO(Registro algorithm) {
        if (poolDTO.containsKey(algorithm.getCodigo())) {
            return poolDTO.get(algorithm.getCodigo());
        }
        RegistroDTO dto = new RegistroDTO();
        dto.setCodigo(algorithm.getCodigo());
        dto.setFechaHoraIngreso(algorithm.getFechaHoraIngresoUTC());
        dto.setFechaHoraEgreso(algorithm.getFechaHoraEgresoUTC());
        Lote lote = algorithm.getLote();
        dto.setCodLote(lote.getCodigo());
        poolDTO.put(algorithm.getCodigo(), dto);
        return dto;
    }

    public RegistroDTO toDTO(RegistroEntity entity) {
        if (poolDTO.containsKey(entity.getCodigo())) {
            return poolDTO.get(entity.getCodigo());
        }
        RegistroDTO dto = new RegistroDTO();
        dto.setCodigo(entity.getCodigo());
        dto.setFechaHoraIngreso(entity.getFechaHoraIngresoUTC());
        dto.setFechaHoraEgreso(entity.getFechaHoraEgresoUTC());
        LoteEntity loteEntity = entity.getLote();
        dto.setCodLote(loteEntity.getCodigo());
        poolDTO.put(entity.getCodigo(), dto);
        return dto;
    }

    public void clearPools() {
        poolAlgorithm.clear();
        poolEntity.clear();
        poolDTO.clear();
        loteAdapter.clearPools();
    }
}
