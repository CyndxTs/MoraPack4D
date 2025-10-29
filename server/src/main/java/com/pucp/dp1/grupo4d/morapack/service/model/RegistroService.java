/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       RegistroService.java
 [**/

package com.pucp.dp1.grupo4d.morapack.service.model;

import com.pucp.dp1.grupo4d.morapack.model.entity.RegistroEntity;
import com.pucp.dp1.grupo4d.morapack.repository.RegistroRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class RegistroService {

    private final RegistroRepository registroRepository;

    public RegistroService(RegistroRepository registroRepository) {
        this.registroRepository = registroRepository;
    }

    public List<RegistroEntity> findAll() {
        return registroRepository.findAll();
    }

    public Optional<RegistroEntity> findById(Integer id) {
        return registroRepository.findById(id);
    }

    public Optional<RegistroEntity> findByCodigo(String codigo) {
        return registroRepository.findByCodigo(codigo);
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

    public boolean existsByCodigo(String codigo) {
        return registroRepository.findByCodigo(codigo).isPresent();
    }
}
