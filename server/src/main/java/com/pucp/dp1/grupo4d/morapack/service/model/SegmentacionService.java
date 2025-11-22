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
import com.pucp.dp1.grupo4d.morapack.util.G4D;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class SegmentacionService {

    private final SegmentacionRepository segmentacionRepository;
    private final SegmentacionMapper segmentacionMapper;
    private final HashMap<String, SegmentacionEntity> segmentaciones = new HashMap<>();

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

    public ListResponse listar(ListRequest request) {
        try {
            int page = (G4D.isAdmissible(request.getPage())) ? request.getPage() : 0;
            int size = (G4D.isAdmissible(request.getSize())) ? request.getSize() : 10;
            Pageable pageable = PageRequest.of(page, size, Sort.by("codigo").ascending());
            List<DTO> segmentacionesDTO = new ArrayList<>();
            List<SegmentacionEntity> segmentacionesEntity = this.findAll(pageable);
            segmentacionesEntity.forEach(s -> segmentacionesDTO.add(segmentacionMapper.toDTO(s)));
            return new ListResponse(true, "Segmentaciones listadas correctamente!", segmentacionesDTO);
        } catch (Exception e) {
            return new ListResponse(false, "ERROR - LISTADO: " + e.getMessage());
        } finally {
            clearPools();
        }
    }

    public void clearPools() {
        segmentaciones.clear();
        segmentacionMapper.clearPools();
    }
}
