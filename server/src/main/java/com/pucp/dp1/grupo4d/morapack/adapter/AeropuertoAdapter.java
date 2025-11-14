/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       AeropuertoAdapter.java
 [**/

package com.pucp.dp1.grupo4d.morapack.adapter;

import com.pucp.dp1.grupo4d.morapack.model.algorithm.Aeropuerto;
import com.pucp.dp1.grupo4d.morapack.model.algorithm.Registro;
import com.pucp.dp1.grupo4d.morapack.model.dto.AeropuertoDTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.RegistroDTO;
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
    private AeropuertoService aeropuertoService;

    private final RegistroAdapter registroAdapter;
    private final Map<String, Aeropuerto> poolAlgorithm = new HashMap<>();
    private final Map<String, AeropuertoEntity> poolEntity = new HashMap<>();
    private final Map<String, AeropuertoDTO> poolDTO = new HashMap<>();

    public AeropuertoAdapter(RegistroAdapter registroAdapter) {
        this.registroAdapter = registroAdapter;
    }

    public Aeropuerto toAlgorithm(AeropuertoEntity entity) {
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
        algorithm.setEsSede(entity.getEsSede());
        List<Registro> registros = new ArrayList<>();
        List<RegistroEntity> registrosEntity = entity.getRegistros();
        for (RegistroEntity registroEntity : registrosEntity) {
            Registro registro = registroAdapter.toAlgorithm(registroEntity);
            registros.add(registro);
        }
        algorithm.setRegistros(registros);
        poolAlgorithm.put(algorithm.getCodigo(), algorithm);
        return algorithm;
    }

    public AeropuertoEntity toEntity(Aeropuerto algorithm) {
        if(poolEntity.containsKey(algorithm.getCodigo())) {
            return poolEntity.get(algorithm.getCodigo());
        }
        AeropuertoEntity entity = aeropuertoService.findByCodigo(algorithm.getCodigo()).orElse(null);
        if (entity == null) {
            return null;
        }
        poolEntity.put(entity.getCodigo(), entity);
        return entity;
    }

    public AeropuertoDTO toDTO(Aeropuerto algorithm) {
        if(poolDTO.containsKey(algorithm.getCodigo())) {
            return poolDTO.get(algorithm.getCodigo());
        }
        AeropuertoDTO dto = new AeropuertoDTO();
        dto.setCodigo(algorithm.getCodigo());
        dto.setCiudad(algorithm.getCiudad());
        dto.setPais(algorithm.getPais());
        dto.setContinente(algorithm.getContinente());
        dto.setAlias(algorithm.getAlias());
        dto.setHusoHorario(algorithm.getHusoHorario());
        dto.setCapacidad(algorithm.getCapacidad());
        dto.setEsSede(algorithm.getEsSede());
        dto.setLatitudDEC(algorithm.getLatitudDEC());
        dto.setLongitudDEC(algorithm.getLongitudDEC());
        List<RegistroDTO> registrosDTO = new ArrayList<>();
        List<Registro> registros = algorithm.getRegistros();
        for (Registro registro : registros) {
            RegistroDTO registroDTO = registroAdapter.toDTO(registro);
            registrosDTO.add(registroDTO);
        }
        dto.setRegistros(registrosDTO);
        poolDTO.put(algorithm.getCodigo(), dto);
        return dto;
    }

    public AeropuertoDTO toDTO(AeropuertoEntity entity) {
        if(poolDTO.containsKey(entity.getCodigo())) {
            return poolDTO.get(entity.getCodigo());
        }
        AeropuertoDTO dto = new AeropuertoDTO();
        dto.setCodigo(entity.getCodigo());
        dto.setCiudad(entity.getCiudad());
        dto.setPais(entity.getPais());
        dto.setContinente(entity.getContinente());
        dto.setAlias(entity.getAlias());
        dto.setHusoHorario(entity.getHusoHorario());
        dto.setCapacidad(entity.getCapacidad());
        dto.setEsSede(entity.getEsSede());
        dto.setLatitudDEC(entity.getLatitudDEC());
        dto.setLongitudDEC(entity.getLongitudDEC());
        List<RegistroDTO> registrosDTO = new ArrayList<>();
        List<RegistroEntity> registrosEntity = entity.getRegistros();
        for (RegistroEntity registroEntity : registrosEntity) {
            RegistroDTO registroDTO = registroAdapter.toDTO(registroEntity);
            registrosDTO.add(registroDTO);
        }
        dto.setRegistros(registrosDTO);
        poolDTO.put(entity.getCodigo(), dto);
        return dto;
    }

    public void clearPools() {
        poolAlgorithm.clear();
        poolEntity.clear();
        poolDTO.clear();
        registroAdapter.clearPools();
    }
}
