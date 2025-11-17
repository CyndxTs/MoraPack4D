/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       VueloService.java
 [**/

package com.pucp.dp1.grupo4d.morapack.service.model;

import com.pucp.dp1.grupo4d.morapack.mapper.VueloMapper;
import com.pucp.dp1.grupo4d.morapack.model.dto.DTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.ListResponse;
import com.pucp.dp1.grupo4d.morapack.model.entity.RutaEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.VueloEntity;
import com.pucp.dp1.grupo4d.morapack.repository.VueloRepository;
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

    public ListResponse listar() {
        try {
            List<DTO> vuelosDTO = new ArrayList<>();
            List<VueloEntity> vuelosEntity = this.findAll();
            vuelosEntity.forEach(v -> vuelosDTO.add(vueloMapper.toDTO(v)));
            return new ListResponse(true, "Vuelos listados correctamente!", vuelosDTO);
        } catch (Exception e) {
            e.printStackTrace();
            return new ListResponse(false, "ERROR - LISTADO: " + e.getMessage());
        }
    }

    public List<VueloEntity> findByDateTimeRange(LocalDateTime fechaHoraInicio, LocalDateTime fechaHoraFin) {
        return  vueloRepository.findByDateTimeRange(fechaHoraInicio, fechaHoraFin);
    }
}
