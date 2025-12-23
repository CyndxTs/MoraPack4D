/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       SegmentacionAdapter.java
 [**/

package com.pucp.dp1.grupo4d.morapack.adapter;

import com.pucp.dp1.grupo4d.morapack.model.algorithm.Lote;
import com.pucp.dp1.grupo4d.morapack.model.algorithm.Ruta;
import com.pucp.dp1.grupo4d.morapack.model.algorithm.Segmentacion;
import com.pucp.dp1.grupo4d.morapack.model.entity.LoteEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.RutaEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.SegmentacionEntity;
import com.pucp.dp1.grupo4d.morapack.service.model.SegmentacionService;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SegmentacionAdapter {
    private final LoteAdapter loteAdapter;
    private final RutaAdapter rutaAdapter;
    private final Map<String, Map<String, Segmentacion>> poolAlgorithm = new HashMap<>();
    private final Map<String, Map<String, SegmentacionEntity>> poolEntity = new HashMap<>();
    private final SegmentacionService segmentacionService;

    public SegmentacionAdapter(LoteAdapter loteAdapter, RutaAdapter rutaAdapter, SegmentacionService segmentacionService, SegmentacionService segmentacionService1) {
        this.loteAdapter = loteAdapter;
        this.rutaAdapter = rutaAdapter;
        this.segmentacionService = segmentacionService1;
    }

    public Segmentacion toAlgorithm(String idTransaccion, SegmentacionEntity entity) {
        if (poolAlgorithm.containsKey(idTransaccion) && poolAlgorithm.get(idTransaccion).containsKey(entity.getCodigo())) {
            return poolAlgorithm.get(idTransaccion).get(entity.getCodigo());
        }
        Segmentacion algorithm = new Segmentacion();
        algorithm.setCodigo(entity.getCodigo());
        algorithm.setFechaHoraAplicacion(entity.getFechaHoraAplicacionUTC());
        algorithm.setFechaHoraSustitucion(entity.getFechaHoraSustitucionUTC());
        Map<Ruta, Lote> lotesPorRuta = new HashMap<>();
        List<LoteEntity> lotesEntity = entity.getLotes();
        lotesEntity.forEach(l -> lotesPorRuta.put(rutaAdapter.toAlgorithm(idTransaccion, l.getRuta()), loteAdapter.toAlgorithm(idTransaccion, l)));
        algorithm.setLotesPorRuta(lotesPorRuta);
        if(!poolAlgorithm.containsKey(idTransaccion)) {
            poolAlgorithm.put(idTransaccion, new HashMap<>());
        }
        poolAlgorithm.get(idTransaccion).put(algorithm.getCodigo(), algorithm);
        return algorithm;
    }

    public SegmentacionEntity toEntity(String idTransaccion, Segmentacion algorithm) {
        if (poolEntity.containsKey(idTransaccion) && poolEntity.get(idTransaccion).containsKey(algorithm.getCodigo())) {
            return poolEntity.get(idTransaccion).get(algorithm.getCodigo());
        }
        SegmentacionEntity entity = segmentacionService.findByCodigo(algorithm.getCodigo()).orElse(new SegmentacionEntity());
        entity.setCodigo(algorithm.getCodigo());
        entity.setFechaHoraAplicacionUTC(algorithm.getFechaHoraAplicacion());
        entity.setFechaHoraSustitucionUTC(algorithm.getFechaHoraSustitucion());
        List<LoteEntity> lotesEntity = new ArrayList<>();
        Map<Ruta, Lote> lotesPorRuta = algorithm.getLotesPorRuta();
        for (Map.Entry<Ruta, Lote> entry : lotesPorRuta.entrySet()) {
            Ruta ruta = entry.getKey();
            RutaEntity rutaEntity = rutaAdapter.toEntity(idTransaccion, ruta);
            Lote lote = entry.getValue();
            LoteEntity loteEntity = loteAdapter.toEntity(idTransaccion, lote);
            loteEntity.setRuta(rutaEntity);
            lotesEntity.add(loteEntity);
        }
        entity.setLotes(lotesEntity);
        if(!poolEntity.containsKey(idTransaccion)) {
            poolEntity.put(idTransaccion, new HashMap<>());
        }
        poolEntity.get(idTransaccion).put(entity.getCodigo(), entity);
        return entity;
    }

    public void clearPools(String idTransaccion) {
        poolAlgorithm.remove(idTransaccion);
        poolEntity.remove(idTransaccion);
        loteAdapter.clearPools(idTransaccion);
        rutaAdapter.clearPools(idTransaccion);
    }
}
