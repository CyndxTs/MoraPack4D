/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       AeropuertoMapper.java
 [**/

package com.pucp.dp1.grupo4d.morapack.mapper;

import com.pucp.dp1.grupo4d.morapack.model.algorithm.Aeropuerto;
import com.pucp.dp1.grupo4d.morapack.model.algorithm.Registro;
import com.pucp.dp1.grupo4d.morapack.model.dto.AeropuertoDTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.RegistroDTO;
import com.pucp.dp1.grupo4d.morapack.model.entity.AeropuertoEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.RegistroEntity;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class AeropuertoMapper {

    private final RegistroMapper registroMapper;
    private final Map<String, AeropuertoDTO> poolDTO = new HashMap<>();

    public AeropuertoMapper(RegistroMapper registroMapper) {
        this.registroMapper = registroMapper;
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
        dto.setLatitud(algorithm.getLatitud());
        dto.setLongitud(algorithm.getLongitud());
        List<RegistroDTO> registrosDTO = new ArrayList<>();
        List<Registro> registros = algorithm.getRegistros();
        for (Registro registro : registros) {
            RegistroDTO registroDTO = registroMapper.toDTO(registro);
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
        dto.setLatitud(entity.getLatitudDEC());
        dto.setLongitud(entity.getLongitudDEC());
        List<RegistroDTO> registrosDTO = new ArrayList<>();
        List<RegistroEntity> registrosEntity = entity.getRegistros();
        for (RegistroEntity registroEntity : registrosEntity) {
            RegistroDTO registroDTO = registroMapper.toDTO(registroEntity);
            registrosDTO.add(registroDTO);
        }
        dto.setRegistros(registrosDTO);
        poolDTO.put(entity.getCodigo(), dto);
        return dto;
    }

    public void clearPools() {
        poolDTO.clear();
        registroMapper.clearPools();
    }
}
