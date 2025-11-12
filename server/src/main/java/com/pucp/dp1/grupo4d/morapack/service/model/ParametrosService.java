/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       ParametrosService.java
 [**/

package com.pucp.dp1.grupo4d.morapack.service.model;

import com.pucp.dp1.grupo4d.morapack.model.entity.AeropuertoEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.ParametrosEntity;
import com.pucp.dp1.grupo4d.morapack.repository.ParametrosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class ParametrosService {

    private final ParametrosRepository parametrosRepository;

    @Autowired
    private AeropuertoService aeropuertoService;

    public ParametrosService(ParametrosRepository parametrosRepository) {
        this.parametrosRepository = parametrosRepository;
    }

    public List<ParametrosEntity> findAll() {
        List<AeropuertoEntity> sedes = aeropuertoService.findByEsSede(true);
        List<String> codOrigenes =  new ArrayList<>();
        sedes.forEach(a -> codOrigenes.add(a.getCodigo()));
        List<ParametrosEntity> result = parametrosRepository.findAll();
        result.forEach(p -> p.setCodOrigenes(codOrigenes));
        return result;
    }

    public ParametrosEntity findById(Integer id) {
        List<AeropuertoEntity> sedes = aeropuertoService.findByEsSede(true);
        List<String> codOrigenes =  new ArrayList<>();
        sedes.forEach(a -> codOrigenes.add(a.getCodigo()));
        ParametrosEntity result = parametrosRepository.findById(id).orElse(null);
        if (result != null) {
            result.setCodOrigenes(codOrigenes);
        }
        return result;
    }

    public ParametrosEntity save(ParametrosEntity parametrizacion) {
        return parametrosRepository.save(parametrizacion);
    }

    public void deleteById(Integer id) {
        parametrosRepository.deleteById(id);
    }

    public boolean existsById(Integer id) {
        return parametrosRepository.existsById(id);
    }
}
