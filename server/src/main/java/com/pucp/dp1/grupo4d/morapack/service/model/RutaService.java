/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       RutaService.java
 [**/

package com.pucp.dp1.grupo4d.morapack.service.model;

import com.pucp.dp1.grupo4d.morapack.mapper.RutaMapper;
import com.pucp.dp1.grupo4d.morapack.model.dto.DTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.ListResponse;
import com.pucp.dp1.grupo4d.morapack.model.entity.RegistroEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.RutaEntity;
import com.pucp.dp1.grupo4d.morapack.repository.RutaRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class RutaService {

    private final RutaRepository rutaRepository;
    private final RutaMapper rutaMapper;

    public RutaService(RutaRepository rutaRepository, RutaMapper rutaMapper) {
        this.rutaRepository = rutaRepository;
        this.rutaMapper = rutaMapper;
    }

    public List<RutaEntity> findAll() {
        return rutaRepository.findAll();
    }

    public Optional<RutaEntity> findById(Integer id) {
        return rutaRepository.findById(id);
    }

    public RutaEntity save(RutaEntity ruta) {
        return rutaRepository.save(ruta);
    }

    public void deleteById(Integer id) {
        rutaRepository.deleteById(id);
    }

    public boolean existsById(Integer id) {
        return rutaRepository.existsById(id);
    }

    public Optional<RutaEntity> findByCodigo(String codigo) {
        return rutaRepository.findByCodigo(codigo);
    }

    public boolean existsByCodigo(String codigo) {
        return rutaRepository.findByCodigo(codigo).isPresent();
    }

    public ListResponse listar() {
        try {
            List<DTO> rutasDTO = new ArrayList<>();
            List<RutaEntity> rutasEntity = this.findAll();
            rutasEntity.forEach(r -> rutasDTO.add(rutaMapper.toDTO(r)));
            return new ListResponse(true, "Rutas listadas correctamente!", rutasDTO);
        } catch (Exception e) {
            e.printStackTrace();
            return new ListResponse(false, "ERROR - LISTADO: " + e.getMessage());
        }
    }

    public List<RutaEntity> findByDateTimeRange(LocalDateTime fechaHoraInicio, LocalDateTime fechaHoraFin) {
        return  rutaRepository.findByDateTimeRange(fechaHoraInicio, fechaHoraFin);
    }
}
