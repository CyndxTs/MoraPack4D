/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       ParametrosService.java
 [**/

package com.pucp.dp1.grupo4d.morapack.service.model;

import com.pucp.dp1.grupo4d.morapack.mapper.ParametrosMapper;
import com.pucp.dp1.grupo4d.morapack.model.dto.DTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.ParametrosDTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.ImportRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.GenericResponse;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.ListResponse;
import com.pucp.dp1.grupo4d.morapack.model.entity.AeropuertoEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.ParametrosEntity;
import com.pucp.dp1.grupo4d.morapack.repository.ParametrosRepository;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class ParametrosService {

    private final ParametrosRepository parametrosRepository;
    private final AeropuertoService aeropuertoService;
    private final ParametrosMapper parametrosMapper;

    public ParametrosService(ParametrosRepository parametrosRepository, AeropuertoService aeropuertoService, ParametrosMapper parametrosMapper) {
        this.parametrosRepository = parametrosRepository;
        this.aeropuertoService = aeropuertoService;
        this.parametrosMapper = parametrosMapper;
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
        ParametrosEntity result = parametrosRepository.findById(id).orElse(null);
        if (result != null) {
            List<String> codOrigenes =  new ArrayList<>();
            List<AeropuertoEntity> origenes = aeropuertoService.findByEsSede(true);
            origenes.forEach(a -> codOrigenes.add(a.getCodigo()));
            result.setCodOrigenes(codOrigenes);
        }
        return result;
    }

    public ParametrosEntity save(ParametrosEntity parametros) {
        List<AeropuertoEntity> origenesAntiguos = aeropuertoService.findByEsSede(true);
        origenesAntiguos.forEach(a -> {
            a.setEsSede(false);
            aeropuertoService.save(a);
        });
        List<String> codOrigenes = parametros.getCodOrigenes();
        codOrigenes.forEach(cod -> {
            AeropuertoEntity nuevoOrigen = aeropuertoService.findByCodigo(cod).orElse(null);
            if (nuevoOrigen != null) {
                nuevoOrigen.setEsSede(true);
                aeropuertoService.save(nuevoOrigen);
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
            List<DTO> dtos = new ArrayList<>();
            List<ParametrosEntity> entities = this.findAll();
            entities.forEach(entity -> dtos.add(parametrosMapper.toDTO(entity)));
            return new ListResponse(true, "Parametros listados correctamente!", dtos);
        } finally {
            clearPools();
        }
    }

    public GenericResponse importar(ImportRequest<ParametrosDTO> request) {
        try {
            System.out.println("Importando parametros..");
            ParametrosDTO dto = request.getDto();
            ParametrosEntity parametros = parametrosMapper.toEntity(dto);
            this.save(parametros);
            System.out.println("[<] PARAMETROS IMPORTADOS!");
            return new GenericResponse(true, "Parametros importados correctamente!");
        } finally {
            clearPools();
        }
    }

    public void clearPools() {
        aeropuertoService.clearPools();
    }
}
