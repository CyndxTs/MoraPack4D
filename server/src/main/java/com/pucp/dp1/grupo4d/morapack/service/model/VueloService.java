/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       VueloService.java
 [**/

package com.pucp.dp1.grupo4d.morapack.service.model;

import com.pucp.dp1.grupo4d.morapack.model.entity.VueloEntity;
import com.pucp.dp1.grupo4d.morapack.repository.VueloRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class VueloService {

    private final VueloRepository vueloRepository;

    public VueloService(VueloRepository vueloRepository) {
        this.vueloRepository = vueloRepository;
    }

    public List<VueloEntity> findAll() {
        return vueloRepository.findAll();
    }

    public Optional<VueloEntity> findById(Integer id) {
        return vueloRepository.findById(id);
    }

    public Optional<VueloEntity> findByCodigo(String codigo) {
        return vueloRepository.findByCodigo(codigo);
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

    public boolean existsByCodigo(String codigo) {
        return vueloRepository.findByCodigo(codigo).isPresent();
    }
}
