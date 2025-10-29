/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       ProductoService.java
 [**/

package com.pucp.dp1.grupo4d.morapack.service.model;

import com.pucp.dp1.grupo4d.morapack.model.entity.ProductoEntity;
import com.pucp.dp1.grupo4d.morapack.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    public List<ProductoEntity> findAll() {
        return productoRepository.findAll();
    }

    public Optional<ProductoEntity> findById(Integer id) {
        return productoRepository.findById(id);
    }

    public Optional<ProductoEntity> findByCodigo(String codigo) {
        return productoRepository.findByCodigo(codigo);
    }

    public ProductoEntity save(ProductoEntity producto) {
        return productoRepository.save(producto);
    }

    public void deleteById(Integer id) {
        productoRepository.deleteById(id);
    }

    public boolean existsById(Integer id) {
        return productoRepository.existsById(id);
    }

    public boolean existsByCodigo(String codigo) {
        return productoRepository.findByCodigo(codigo).isPresent();
    }
}
