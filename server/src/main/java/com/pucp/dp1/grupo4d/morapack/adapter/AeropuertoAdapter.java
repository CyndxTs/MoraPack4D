/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       AeropuertoAdapter.java
 [**/

package com.pucp.dp1.grupo4d.morapack.adapter;

import com.pucp.dp1.grupo4d.morapack.model.algorithm.Aeropuerto;
import com.pucp.dp1.grupo4d.morapack.model.algorithm.Registro;
import com.pucp.dp1.grupo4d.morapack.model.entity.AeropuertoEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.RegistroEntity;
import com.pucp.dp1.grupo4d.morapack.service.model.AeropuertoService;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class AeropuertoAdapter {
    private final AeropuertoService aeropuertoService;
    private final RegistroAdapter registroAdapter;
    private final Map<String, Map<String, Aeropuerto>> poolAlgorithm = new HashMap<>();
    private final Map<String, Map<String, AeropuertoEntity>> poolEntity = new HashMap<>();

    public AeropuertoAdapter(RegistroAdapter registroAdapter, AeropuertoService aeropuertoService) {
        this.registroAdapter = registroAdapter;
        this.aeropuertoService = aeropuertoService;
    }

    public Aeropuerto toAlgorithm(String idTransaccion, AeropuertoEntity entity) {
        if (poolAlgorithm.containsKey(idTransaccion) && poolAlgorithm.get(idTransaccion).containsKey(entity.getCodigo())) {
            return poolAlgorithm.get(idTransaccion).get(entity.getCodigo());
        }
        Aeropuerto algorithm = new Aeropuerto();
        algorithm.setCodigo(entity.getCodigo());
        algorithm.setCiudad(entity.getCiudad());
        algorithm.setPais(entity.getPais());
        algorithm.setContinente(entity.getContinente());
        algorithm.setAlias(entity.getAlias());
        algorithm.setHusoHorario(entity.getHusoHorario());
        algorithm.setCapacidad(entity.getCapacidad());
        algorithm.setLatitud(entity.getLatitudDEC());
        algorithm.setLongitud(entity.getLongitudDEC());
        algorithm.setEsSede(entity.getEsSede());
        List<Registro> registros = new ArrayList<>();
        List<RegistroEntity> registrosEntity = entity.getRegistros();
        registrosEntity.forEach(e -> registros.add(registroAdapter.toAlgorithm(idTransaccion, e)));
        algorithm.setRegistros(registros);
        if(!poolAlgorithm.containsKey(idTransaccion)) {
            poolAlgorithm.put(idTransaccion, new HashMap<>());
        }
        poolAlgorithm.get(idTransaccion).put(algorithm.getCodigo(), algorithm);
        return algorithm;
    }

    public AeropuertoEntity toEntity(String idTransaccion, Aeropuerto algorithm) {
        if(poolEntity.containsKey(idTransaccion) && poolEntity.get(idTransaccion).containsKey(algorithm.getCodigo())) {
            return poolEntity.get(idTransaccion).get(algorithm.getCodigo());
        }
        AeropuertoEntity entity = aeropuertoService.findByCodigo(algorithm.getCodigo()).orElse(null);
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
        registroAdapter.clearPools(idTransaccion);
    }
}
