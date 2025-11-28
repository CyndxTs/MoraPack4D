/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       RegistroService.java
 [**/

package com.pucp.dp1.grupo4d.morapack.service.model;

import com.pucp.dp1.grupo4d.morapack.mapper.RegistroMapper;
import com.pucp.dp1.grupo4d.morapack.model.dto.DTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.ListRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.ListResponse;
import com.pucp.dp1.grupo4d.morapack.model.entity.RegistroEntity;
import com.pucp.dp1.grupo4d.morapack.repository.RegistroRepository;
import com.pucp.dp1.grupo4d.morapack.util.G4DUtility;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class RegistroService {

    private final RegistroRepository registroRepository;
    private final RegistroMapper registroMapper;

    public RegistroService(RegistroRepository registroRepository, RegistroMapper registroMapper) {
        this.registroRepository = registroRepository;
        this.registroMapper = registroMapper;
    }

    public List<RegistroEntity> findAll() {
        return registroRepository.findAll();
    }

    public List<RegistroEntity> findAll(Pageable pageable) {
        return registroRepository.findAll(pageable).getContent();
    }

    public Optional<RegistroEntity> findById(Integer id) {
        return registroRepository.findById(id);
    }

    public RegistroEntity save(RegistroEntity registro) {
        return registroRepository.save(registro);
    }

    public void deleteById(Integer id) {
        registroRepository.deleteById(id);
    }

    public boolean existsById(Integer id) {
        return registroRepository.existsById(id);
    }

    public Optional<RegistroEntity> findByCodigo(String codigo) {
        return registroRepository.findByCodigo(codigo);
    }

    public boolean existsByCodigo(String codigo) {
        return registroRepository.findByCodigo(codigo).isPresent();
    }

    public ListResponse listar(ListRequest request) throws Exception{
        try {
            Pageable pageable = G4DUtility.Convertor.toAdmissible(request.getPagina(), request.getTamanio(), Sort.Order.asc("fechaHoraIngresoUTC"), Sort.Order.asc("fechaHoraEgresoUTC"));
            List<DTO> dtos = new ArrayList<>();
            List<RegistroEntity> entities = this.findAll(pageable);
            entities.forEach(entity -> dtos.add(registroMapper.toDTO(entity)));
            return new ListResponse(true, String.format("Registros listados correctamente! ('%d')", dtos.size()), dtos);
        } finally {
            clearPools();
        }
    }

    public void clearPools() {
        registroMapper.clearPools();
    }
}
