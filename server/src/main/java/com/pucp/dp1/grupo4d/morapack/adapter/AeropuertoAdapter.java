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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class AeropuertoAdapter {

    @Autowired
    AeropuertoService aeropuertoService;

    private final RegistroAdapter registroAdapter;

    private final Map<String, Aeropuerto> poolAlgorithm = new HashMap<>();
    private final Map<String, AeropuertoEntity> poolEntity = new HashMap<>();

    public AeropuertoAdapter(RegistroAdapter registroAdapter) {
        this.registroAdapter = registroAdapter;
    }

    public Aeropuerto toAlgorithm(AeropuertoEntity entity) {
        if (entity == null) return null;

        if (poolAlgorithm.containsKey(entity.getCodigo())) {
            return poolAlgorithm.get(entity.getCodigo());
        }
        Aeropuerto algorithm = new Aeropuerto();
        algorithm.setCodigo(entity.getCodigo());
        algorithm.setCiudad(entity.getCiudad());
        algorithm.setPais(entity.getPais());
        algorithm.setContinente(entity.getContinente());
        algorithm.setAlias(entity.getAlias());
        algorithm.setHusoHorario(entity.getHusoHorario());
        algorithm.setCapacidad(entity.getCapacidad());
        algorithm.setLatitudDMS(entity.getLatitudDMS());
        algorithm.setLatitudDEC(entity.getLatitudDEC());
        algorithm.setLongitudDMS(entity.getLongitudDMS());
        algorithm.setLongitudDEC(entity.getLongitudDEC());
        List<Registro> registros = new ArrayList<>();
        if (entity.getRegistros() != null) {
            for (RegistroEntity registroEntity : entity.getRegistros()) {
                Registro registro = registroAdapter.toAlgorithm(registroEntity);
                registros.add(registro);
            }
        }
        algorithm.setRegistros(registros);
        poolAlgorithm.put(algorithm.getCodigo(), algorithm);
        return algorithm;
    }

    public AeropuertoEntity toEntity(Aeropuerto algorithm) {
        if (algorithm == null) return null;
        if(poolEntity.containsKey(algorithm.getCodigo())) {
            return poolEntity.get(algorithm.getCodigo());
        }
        AeropuertoEntity entity = aeropuertoService.findByCodigo(algorithm.getCodigo()).orElse(null);
        if (entity == null) return null;
        entity.getRegistros().clear();
        if (algorithm.getRegistros() != null) {
            for (Registro registro : algorithm.getRegistros()) {
                RegistroEntity registroEntity = registroAdapter.toEntity(registro);
                registroEntity.setAeropuerto(entity);
                entity.getRegistros().add(registroEntity);
            }
        }
        poolEntity.put(entity.getCodigo(), entity);
        return entity;
    }

    public void clearPools() {
        poolAlgorithm.clear();
        poolEntity.clear();
        registroAdapter.clearPools();
    }
}
