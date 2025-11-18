/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       ParametrosService.java
 [**/

package com.pucp.dp1.grupo4d.morapack.service.model;

import com.pucp.dp1.grupo4d.morapack.mapper.ParametrosMapper;
import com.pucp.dp1.grupo4d.morapack.model.dto.DTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.ListResponse;
import com.pucp.dp1.grupo4d.morapack.model.entity.AeropuertoEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.ParametrosEntity;
import com.pucp.dp1.grupo4d.morapack.repository.ParametrosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class ParametrosService {

    @Autowired
    private AeropuertoService aeropuertoService;

    @Autowired
    private ParametrosMapper parametrosMapper;

    private final ParametrosRepository parametrosRepository;

    public ParametrosService(ParametrosRepository parametrosRepository) {
        this.parametrosRepository = parametrosRepository;
    }

    public List<ParametrosEntity> findAll() {
        List<ParametrosEntity> result = new ArrayList<>();
        ParametrosEntity parametros = this.findById(1);
        if (parametros != null) {
            result.add(parametros);
        }
        return result;
    }

    public ParametrosEntity findById(Integer id) {
        List<String> codOrigenes =  new ArrayList<>();
        List<AeropuertoEntity> sedes = aeropuertoService.findByEsSede(true);
        sedes.forEach(a -> codOrigenes.add(a.getCodigo()));
        ParametrosEntity result = parametrosRepository.findById(id).orElse(null);
        if (result != null) {
            result.setCodOrigenes(codOrigenes);
        }
        return result;
    }

    public ParametrosEntity save(ParametrosEntity parametros) {
        List<AeropuertoEntity> sedes = aeropuertoService.findByEsSede(true);
        sedes.forEach(a -> {
            a.setEsSede(false);
            aeropuertoService.save(a);
        });
        List<String> codOrigenes = parametros.getCodOrigenes();
        codOrigenes.forEach(cod -> {
            AeropuertoEntity aeropuerto = aeropuertoService.findByCodigo(cod).orElse(null);
            if (aeropuerto != null) {
                aeropuerto.setEsSede(true);
                aeropuertoService.save(aeropuerto);
            }
        });
        if(this.existsById(1)) {
            parametros.setId(1);
        }
        return parametrosRepository.save(parametros);
    }

    public void deleteById(Integer id) {
        parametrosRepository.deleteById(id);
    }

    public boolean existsById(Integer id) {
        return parametrosRepository.existsById(id);
    }

    public ListResponse listar() {
        try {
            List<DTO> parametrosDTO = new ArrayList<>();
            List<ParametrosEntity> parametrosEntity = this.findAll();
            parametrosEntity.forEach(p -> parametrosDTO.add(parametrosMapper.toDTO(p)));
            return new ListResponse(true, "Parametros listados correctamente!", parametrosDTO);
        } catch (Exception e) {
            e.printStackTrace();
            return new ListResponse(false, "ERROR - LISTADO: " + e.getMessage());
        }
    }
}
