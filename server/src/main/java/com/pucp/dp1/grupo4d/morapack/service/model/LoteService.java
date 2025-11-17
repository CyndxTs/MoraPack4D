/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       LoteService.java
 [**/

package com.pucp.dp1.grupo4d.morapack.service.model;

import com.pucp.dp1.grupo4d.morapack.mapper.LoteMapper;
import com.pucp.dp1.grupo4d.morapack.model.dto.DTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.ListResponse;
import com.pucp.dp1.grupo4d.morapack.model.entity.LoteEntity;
import com.pucp.dp1.grupo4d.morapack.repository.LoteRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class LoteService {

    private final LoteRepository loteRepository;
    private final LoteMapper loteMapper;

    public LoteService(LoteRepository loteRepository, LoteMapper loteMapper) {
        this.loteRepository = loteRepository;
        this.loteMapper = loteMapper;
    }

    public List<LoteEntity> findAll() {
        return loteRepository.findAll();
    }

    public Optional<LoteEntity> findById(Integer id) {
        return loteRepository.findById(id);
    }

    public LoteEntity save(LoteEntity lote) {
        return loteRepository.save(lote);
    }

    public void deleteById(Integer id) {
        loteRepository.deleteById(id);
    }

    public boolean existsById(Integer id) {
        return loteRepository.existsById(id);
    }

    public Optional<LoteEntity> findByCodigo(String codigo) {
        return loteRepository.findByCodigo(codigo);
    }

    public boolean existsByCodigo(String codigo) {
        return loteRepository.findByCodigo(codigo).isPresent();
    }

    public ListResponse listar() {
        try {
            List<DTO> lotesDTO = new ArrayList<>();
            List<LoteEntity> lotesEntity = loteRepository.findAll();
            lotesEntity.forEach(l -> lotesDTO.add(loteMapper.toDTO(l)));
            return new ListResponse(true, "Lotes listados correctamente!", lotesDTO);
        } catch (Exception e) {
            e.printStackTrace();
            return new ListResponse(false, "ERROR - LISTADO: " + e.getMessage());
        }
    }
}
