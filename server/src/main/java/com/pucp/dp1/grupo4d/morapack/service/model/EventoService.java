/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       EventoService.java
 [**/

package com.pucp.dp1.grupo4d.morapack.service.model;

import com.pucp.dp1.grupo4d.morapack.mapper.EventoMapper;
import com.pucp.dp1.grupo4d.morapack.model.dto.DTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.ListRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.ListResponse;
import com.pucp.dp1.grupo4d.morapack.model.entity.EventoEntity;
import com.pucp.dp1.grupo4d.morapack.repository.EventoRepository;
import com.pucp.dp1.grupo4d.morapack.util.G4D;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class EventoService {

    private final EventoRepository eventoRepository;
    private final EventoMapper eventoMapper;
    private final HashMap<String, EventoEntity> eventos = new HashMap<>();

    public EventoService(EventoRepository eventoRepository, EventoMapper eventoMapper) {
        this.eventoRepository = eventoRepository;
        this.eventoMapper = eventoMapper;
    }

    public List<EventoEntity> findAll() {
        return eventoRepository.findAll();
    }

    public List<EventoEntity> findAll(Pageable pageable) {
        return eventoRepository.findAll(pageable).getContent();
    }

    public Optional<EventoEntity> findById(Integer id) {
        return eventoRepository.findById(id);
    }

    public EventoEntity save(EventoEntity administrador) {
        return eventoRepository.save(administrador);
    }

    public void deleteById(Integer id) {
        eventoRepository.deleteById(id);
    }

    public boolean existsById(Integer id) {
        return eventoRepository.existsById(id);
    }

    public Optional<EventoEntity> findByCodigo(String codigo) {
        return eventoRepository.findByCodigo(codigo);
    }

    public boolean existsByCodigo(String codigo) {
        return eventoRepository.findByCodigo(codigo).isPresent();
    }

    public ListResponse listar(ListRequest request) {
        try {
            int page = G4D.toAdmissibleValue(request.getPage(), 0);
            int size = G4D.toAdmissibleValue(request.getSize(), 10);
            Pageable pageable = PageRequest.of(page, size, Sort.by("codigo").ascending());
            List<DTO> dtos = new ArrayList<>();
            List<EventoEntity> entities = this.findAll(pageable);
            entities.forEach(entity -> dtos.add(eventoMapper.toDTO(entity)));
            return new ListResponse(true, "Eventos listados correctamente!", dtos);
        } catch (Exception e) {
            return new ListResponse(false, "ERROR - LISTADO: " + e.getMessage());
        } finally {
            clearPools();
        }
    }

    public void clearPools() {
        eventos.clear();
        eventoMapper.clearPools();
    }
}
