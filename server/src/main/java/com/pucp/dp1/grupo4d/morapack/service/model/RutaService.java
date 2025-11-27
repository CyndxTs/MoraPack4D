/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       RutaService.java
 [**/

package com.pucp.dp1.grupo4d.morapack.service.model;

import com.pucp.dp1.grupo4d.morapack.mapper.RutaMapper;
import com.pucp.dp1.grupo4d.morapack.model.dto.DTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.ListRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.ListResponse;
import com.pucp.dp1.grupo4d.morapack.model.entity.RutaEntity;
import com.pucp.dp1.grupo4d.morapack.repository.RutaRepository;
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

    public List<RutaEntity> findAll(Pageable pageable) {
        return rutaRepository.findAll(pageable).getContent();
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

    public List<RutaEntity> findAllByDateTimeRange(LocalDateTime fechaHoraInicio, LocalDateTime fechaHoraFin, String tipoEscenario) {
        return  rutaRepository.findAllByDateTimeRange(fechaHoraInicio, fechaHoraFin, tipoEscenario);
    }

    public List<RutaEntity> findAllSinceDateTime(LocalDateTime fechaHoraInicio, String tipoEscenario) {
        return  rutaRepository.findAllSinceDateTime(fechaHoraInicio, tipoEscenario);
    }
    
    public ListResponse listar(ListRequest request) {
        try {
            int page = G4D.toAdmissibleValue(request.getPage(), 0);
            int size = G4D.toAdmissibleValue(request.getPage(), 10);
            Pageable pageable = PageRequest.of(page, size,  Sort.by(Sort.Order.asc("fechaHoraSalidaUTC"), Sort.Order.asc("fechaHoraLlegadaUTC")));
            List<DTO> dtos = new ArrayList<>();
            List<RutaEntity> entities = this.findAll(pageable);
            entities.forEach(entity -> dtos.add(rutaMapper.toDTO(entity)));
            return new ListResponse(true, "Rutas listadas correctamente!", dtos);
        } catch (Exception e) {
            return new ListResponse(false, "ERROR - LISTADO: " + e.getMessage());
        } finally {
            clearPools();
        }
    }

    public void clearPools() {
        rutaMapper.clearPools();
    }
}
