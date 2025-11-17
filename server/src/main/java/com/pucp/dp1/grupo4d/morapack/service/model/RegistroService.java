/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       RegistroService.java
 [**/

package com.pucp.dp1.grupo4d.morapack.service.model;

import com.pucp.dp1.grupo4d.morapack.mapper.RegistroMapper;
import com.pucp.dp1.grupo4d.morapack.model.dto.DTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.ListResponse;
import com.pucp.dp1.grupo4d.morapack.model.entity.PlanEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.RegistroEntity;
import com.pucp.dp1.grupo4d.morapack.repository.RegistroRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class RegistroService {

    private final RegistroRepository registroRepository;
    private final RegistroMapper registroMapper;

    public RegistroService(RegistroRepository registroRepository, RegistroMapper registroMapper) {
        this.registroRepository = registroRepository;
        this.registroMapper = registroMapper;
    }

    public List<RegistroEntity> findAll() {
        return registroRepository.findAll();
    }

    public Optional<RegistroEntity> findById(Integer id) {
        return registroRepository.findById(id);
    }

    public RegistroEntity save(RegistroEntity registro) {
        return registroRepository.save(registro);
    }

    public void deleteById(Integer id) {
        registroRepository.deleteById(id);
    }

    public boolean existsById(Integer id) {
        return registroRepository.existsById(id);
    }

    public Optional<RegistroEntity> findByCodigo(String codigo) {
        return registroRepository.findByCodigo(codigo);
    }

    public boolean existsByCodigo(String codigo) {
        return registroRepository.findByCodigo(codigo).isPresent();
    }

    public ListResponse listar() {
        try {
            List<DTO> registrosDTO = new ArrayList<>();
            List<RegistroEntity> registrosEntity = this.findAll();
            registrosEntity.forEach(r -> registrosDTO.add(registroMapper.toDTO(r)));
            return new ListResponse(true, "Registros listados correctamente!", registrosDTO);
        } catch (Exception e) {
            e.printStackTrace();
            return new ListResponse(false, "ERROR - LISTADO: " + e.getMessage());
        }
    }
}
