/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       SegmentacionService.java
 [**/

package com.pucp.dp1.grupo4d.morapack.service.model;

import com.pucp.dp1.grupo4d.morapack.mapper.SegmentacionMapper;
import com.pucp.dp1.grupo4d.morapack.model.dto.DTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.ListRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.ListResponse;
import com.pucp.dp1.grupo4d.morapack.model.entity.SegmentacionEntity;
import com.pucp.dp1.grupo4d.morapack.repository.SegmentacionRepository;
import com.pucp.dp1.grupo4d.morapack.util.G4DUtility;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class SegmentacionService {

    private final SegmentacionRepository segmentacionRepository;
    private final SegmentacionMapper segmentacionMapper;

    public SegmentacionService(SegmentacionRepository segmentacionRepository, SegmentacionMapper segmentacionMapper) {
        this.segmentacionRepository = segmentacionRepository;
        this.segmentacionMapper = segmentacionMapper;
    }

    public List<SegmentacionEntity> findAll() {
        return segmentacionRepository.findAll();
    }

    public List<SegmentacionEntity> findAll(Pageable pageable) {
        return segmentacionRepository.findAll(pageable).getContent();
    }

    public Optional<SegmentacionEntity> findById(Integer id) {
        return segmentacionRepository.findById(id);
    }

    public SegmentacionEntity save(SegmentacionEntity administrador) {
        return segmentacionRepository.save(administrador);
    }

    public void deleteById(Integer id) {
        segmentacionRepository.deleteById(id);
    }

    public boolean existsById(Integer id) {
        return segmentacionRepository.existsById(id);
    }

    public Optional<SegmentacionEntity> findByCodigo(String codigo) {
        return segmentacionRepository.findByCodigo(codigo);
    }

    public boolean existsByCodigo(String codigo) {
        return segmentacionRepository.findByCodigo(codigo).isPresent();
    }

    public ListResponse listar(ListRequest request) throws Exception {
        try {
            Pageable pageable = G4DUtility.Convertor.toAdmissible(request.getPagina(), request.getTamanio(), Sort.Order.asc("fechaHoraAplicacionUTC"));
            List<DTO> dtos = new ArrayList<>();
            List<SegmentacionEntity> entities = this.findAll(pageable);
            entities.forEach(entity -> dtos.add(segmentacionMapper.toDTO(entity)));
            return new ListResponse(true, String.format("Segmentaciones listadas correctamente! ('%d')", dtos.size()), dtos);
        } finally {
            clearPools();
        }
    }

    public void clearPools() {
        segmentacionMapper.clearPools();
    }
}
