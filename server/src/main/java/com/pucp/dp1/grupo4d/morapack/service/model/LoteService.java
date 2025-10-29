/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       LoteService.java
 [**/

package com.pucp.dp1.grupo4d.morapack.service.model;

import com.pucp.dp1.grupo4d.morapack.model.entity.LoteEntity;
import com.pucp.dp1.grupo4d.morapack.repository.LoteRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class LoteService {

    private final LoteRepository loteRepository;

    public LoteService(LoteRepository loteRepository) {
        this.loteRepository = loteRepository;
    }

    public List<LoteEntity> findAll() {
        return loteRepository.findAll();
    }

    public Optional<LoteEntity> findById(Integer id) {
        return loteRepository.findById(id);
    }

    public Optional<LoteEntity> findByCodigo(String codigo) {
        return loteRepository.findByCodigo(codigo);
    }

    public LoteEntity save(LoteEntity lote) {
        return loteRepository.save(lote);
    }

    public void deleteById(Integer id) {
        loteRepository.deleteById(id);
    }

    public boolean existsById(Integer id) {
        return loteRepository.existsById(id);
    }

    public boolean existsByCodigo(String codigo) {
        return loteRepository.findByCodigo(codigo).isPresent();
    }
}
