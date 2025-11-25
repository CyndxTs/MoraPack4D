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
import com.pucp.dp1.grupo4d.morapack.util.G4D;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SegmentacionAdapter {

    private final LoteAdapter loteAdapter;
    private final RutaAdapter rutaAdapter;
    private final Map<String, Segmentacion> poolAlgorithm = new HashMap<>();
    private final Map<String, SegmentacionEntity> poolEntity = new HashMap<>();

    public SegmentacionAdapter(LoteAdapter loteAdapter, RutaAdapter rutaAdapter) {
        this.loteAdapter = loteAdapter;
        this.rutaAdapter = rutaAdapter;
    }

    public Segmentacion toAlgorithm(SegmentacionEntity entity) {
        if (poolAlgorithm.containsKey(entity.getId())) {
            return poolAlgorithm.get(entity.getId());
        }
        Segmentacion algorithm = new Segmentacion();
        algorithm.setCodigo(entity.getCodigo());
        algorithm.setFechaHoraAplicacion(entity.getFechaHoraAplicacionUTC());
        algorithm.setFechaHoraSustitucion(entity.getFechaHoraSustitucionUTC());
        Map<Ruta, Lote> lotesPorRuta = new HashMap<>();
        List<LoteEntity> lotesEntity = entity.getLotes();
        for (LoteEntity loteEntity : lotesEntity) {
            Lote lote = loteAdapter.toAlgorithm(loteEntity);
            RutaEntity rutaEntity = loteEntity.getRuta();
            Ruta ruta = rutaAdapter.toAlgorithm(rutaEntity);
            lotesPorRuta.put(ruta, lote);
        }
        algorithm.setLotesPorRuta(lotesPorRuta);
        poolAlgorithm.put(algorithm.getCodigo(), algorithm);
        return algorithm;
    }

    public SegmentacionEntity toEntity(Segmentacion algorithm) {
        if (poolEntity.containsKey(algorithm.getCodigo())) {
            return poolEntity.get(algorithm.getCodigo());
        }
        SegmentacionEntity entity = new SegmentacionEntity();
        entity.setCodigo(algorithm.getCodigo());
        entity.setFechaHoraAplicacionUTC(algorithm.getFechaHoraAplicacion());
        entity.setFechaHoraAplicacionLocal(G4D.toLocal(algorithm.getFechaHoraAplicacion(), entity.getPedido().getDestino().getHusoHorario()));
        entity.setFechaHoraSustitucionUTC(algorithm.getFechaHoraSustitucion());
        entity.setFechaHoraSustitucionLocal(G4D.toLocal(algorithm.getFechaHoraSustitucion(), entity.getPedido().getDestino().getHusoHorario()));
        List<LoteEntity> lotesEntity = new ArrayList<>();
        Map<Ruta, Lote> lotesPorRuta = algorithm.getLotesPorRuta();
        for (Map.Entry<Ruta, Lote> entry : lotesPorRuta.entrySet()) {
            Ruta ruta = entry.getKey();
            RutaEntity rutaEntity = rutaAdapter.toEntity(ruta);
            Lote lote = entry.getValue();
            LoteEntity loteEntity = loteAdapter.toEntity(lote);
            loteEntity.setRuta(rutaEntity);
            lotesEntity.add(loteEntity);
        }
        entity.setLotes(lotesEntity);
        poolEntity.put(algorithm.getCodigo(), entity);
        return entity;
    }

    public void clearPools() {
        poolAlgorithm.clear();
        poolEntity.clear();
        loteAdapter.clearPools();
        rutaAdapter.clearPools();
    }
}
