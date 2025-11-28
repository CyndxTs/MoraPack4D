/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       LoteService.java
 [**/

package com.pucp.dp1.grupo4d.morapack.service.model;

import com.pucp.dp1.grupo4d.morapack.mapper.LoteMapper;
import com.pucp.dp1.grupo4d.morapack.model.dto.DTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.ListRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.ListResponse;
import com.pucp.dp1.grupo4d.morapack.model.entity.LoteEntity;
import com.pucp.dp1.grupo4d.morapack.repository.LoteRepository;
import com.pucp.dp1.grupo4d.morapack.util.G4D;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    public List<LoteEntity> findAll(Pageable pageable) {
        return loteRepository.findAll(pageable).getContent();
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

    public ListResponse listar(ListRequest request) {
        try {
            int page = G4D.toAdmissibleValue(request.getPage(), 0);
            int size = G4D.toAdmissibleValue(request.getSize(), 10);
            Pageable pageable = PageRequest.of(page, size, Sort.by("codigo").ascending());
            List<DTO> dtos = new ArrayList<>();
            List<LoteEntity> entities = this.findAll(pageable);
            entities.forEach(entity -> dtos.add(loteMapper.toDTO(entity)));
            return new ListResponse(true, "Lotes listados correctamente!", dtos);
        } catch (Exception e) {
            return new ListResponse(false, "ERROR - LISTADO: " + e.getMessage());
        } finally {
            clearPools();
        }
    }

    public void clearPools() {
        loteMapper.clearPools();
    }
}
