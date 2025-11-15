/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       RutaService.java
 [**/

package com.pucp.dp1.grupo4d.morapack.service.model;

import com.pucp.dp1.grupo4d.morapack.model.entity.RutaEntity;
import com.pucp.dp1.grupo4d.morapack.repository.RutaRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class RutaService {

    private final RutaRepository rutaRepository;

    public RutaService(RutaRepository rutaRepository) {
        this.rutaRepository = rutaRepository;
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

    public List<RutaEntity> findByDateTimeRange(LocalDateTime fechaHoraInicio, LocalDateTime fechaHoraFin, Integer desfaseDeDias) {
        return  rutaRepository.findByDateTimeRange(fechaHoraInicio, fechaHoraFin, desfaseDeDias);
    }
}
