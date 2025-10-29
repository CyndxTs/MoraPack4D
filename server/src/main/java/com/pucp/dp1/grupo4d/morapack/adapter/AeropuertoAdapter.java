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

    private final Map<String, Aeropuerto> pool = new HashMap<>();

    private final RegistroAdapter registroAdapter;

    public AeropuertoAdapter(RegistroAdapter registroAdapter) {
        this.registroAdapter = registroAdapter;
    }

    public Aeropuerto toAlgorithm(AeropuertoEntity entity) {
        if (entity == null) return null;

        if (pool.containsKey(entity.getCodigo())) {
            return pool.get(entity.getCodigo());
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
        return algorithm;
    }

    public AeropuertoEntity toEntity(Aeropuerto algorithm) {
        if (algorithm == null) return null;
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
        return entity;
    }

    public void clearPool() {
        pool.clear();
    }
}
