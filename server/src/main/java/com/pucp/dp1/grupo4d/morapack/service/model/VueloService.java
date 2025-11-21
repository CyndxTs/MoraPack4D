/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       VueloService.java
 [**/

package com.pucp.dp1.grupo4d.morapack.service.model;

import com.pucp.dp1.grupo4d.morapack.mapper.VueloMapper;
import com.pucp.dp1.grupo4d.morapack.model.dto.DTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.ListRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.ListResponse;
import com.pucp.dp1.grupo4d.morapack.model.entity.VueloEntity;
import com.pucp.dp1.grupo4d.morapack.repository.VueloRepository;
import com.pucp.dp1.grupo4d.morapack.util.G4D;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class VueloService {

    private final VueloRepository vueloRepository;
    private final VueloMapper vueloMapper;

    public VueloService(VueloRepository vueloRepository, VueloMapper vueloMapper) {
        this.vueloRepository = vueloRepository;
        this.vueloMapper = vueloMapper;
    }

    public List<VueloEntity> findAll() {
        return vueloRepository.findAll();
    }

    public List<VueloEntity> findAll(Pageable pageable) {
        return vueloRepository.findAll(pageable).getContent();
    }

    public Optional<VueloEntity> findById(Integer id) {
        return vueloRepository.findById(id);
    }

    public VueloEntity save(VueloEntity vuelo) {
        return vueloRepository.save(vuelo);
    }

    public void deleteById(Integer id) {
        vueloRepository.deleteById(id);
    }

    public boolean existsById(Integer id) {
        return vueloRepository.existsById(id);
    }

    public Optional<VueloEntity> findByCodigo(String codigo) {
        return vueloRepository.findByCodigo(codigo);
    }

    public boolean existsByCodigo(String codigo) {
        return vueloRepository.findByCodigo(codigo).isPresent();
    }

    public List<VueloEntity> findAllByDateTimeRange(LocalDateTime fechaHoraInicio, LocalDateTime fechaHoraFin) {
        return  vueloRepository.findAllByDateTimeRange(fechaHoraInicio, fechaHoraFin);
    }

    public ListResponse listar(ListRequest request) {
        try {
            int page = (G4D.isAdmissible(request.getPage())) ? request.getPage() : 0;
            int size = (G4D.isAdmissible(request.getSize())) ? request.getSize() : 10;
            Pageable pageable = PageRequest.of(page, size,  Sort.by(Sort.Order.asc("fechaHoraSalidaUTC"), Sort.Order.asc("fechaHoraLlegadaUTC")));
            List<DTO> vuelosDTO = new ArrayList<>();
            List<VueloEntity> vuelosEntity = this.findAll(pageable);
            vuelosEntity.forEach(v -> vuelosDTO.add(vueloMapper.toDTO(v)));
            return new ListResponse(true, "Vuelos listados correctamente!", vuelosDTO);
        } catch (Exception e) {
            e.printStackTrace();
            return new ListResponse(false, "ERROR - LISTADO: " + e.getMessage());
        }
    }
}
