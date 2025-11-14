/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       RutaAdapter.java
 [**/

package com.pucp.dp1.grupo4d.morapack.adapter;

import com.pucp.dp1.grupo4d.morapack.model.algorithm.Ruta;
import com.pucp.dp1.grupo4d.morapack.model.algorithm.Aeropuerto;
import com.pucp.dp1.grupo4d.morapack.model.algorithm.Vuelo;
import com.pucp.dp1.grupo4d.morapack.model.dto.RutaDTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.VueloDTO;
import com.pucp.dp1.grupo4d.morapack.model.entity.RutaEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.AeropuertoEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.VueloEntity;
import com.pucp.dp1.grupo4d.morapack.model.enums.TipoRuta;
import com.pucp.dp1.grupo4d.morapack.service.model.RutaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class RutaAdapter {

    @Autowired
    RutaService rutaService;

    private final AeropuertoAdapter aeropuertoAdapter;
    private final VueloAdapter vueloAdapter;
    private final Map<String, Ruta> poolAlgorithm = new HashMap<>();
    private final Map<String, RutaEntity> poolEntity = new HashMap<>();
    private final Map<String, RutaDTO> poolDTO = new HashMap<>();

    public RutaAdapter(AeropuertoAdapter aeropuertoAdapter, VueloAdapter vueloAdapter) {
        this.aeropuertoAdapter = aeropuertoAdapter;
        this.vueloAdapter = vueloAdapter;
    }

    public Ruta toAlgorithm(RutaEntity entity) {
        if (poolAlgorithm.containsKey(entity.getCodigo())) {
            return poolAlgorithm.get(entity.getCodigo());
        }
        Ruta algorithm = new Ruta();
        algorithm.setCodigo(entity.getCodigo());
        algorithm.setDuracion(entity.getDuracion());
        algorithm.setDistancia(entity.getDistancia());
        algorithm.setFechaHoraSalidaLocal(entity.getFechaHoraSalidaLocal());
        algorithm.setFechaHoraSalidaUTC(entity.getFechaHoraSalidaUTC());
        algorithm.setFechaHoraLlegadaLocal(entity.getFechaHoraLlegadaLocal());
        algorithm.setFechaHoraLlegadaUTC(entity.getFechaHoraLlegadaUTC());
        algorithm.setTipo(entity.getTipo());
        Aeropuerto origen = aeropuertoAdapter.toAlgorithm(entity.getOrigen());
        algorithm.setOrigen(origen);
        Aeropuerto destino = aeropuertoAdapter.toAlgorithm(entity.getDestino());
        algorithm.setDestino(destino);
        List<Vuelo> vuelos = new ArrayList<>();
        List<VueloEntity> vuelosEntity = entity.getVuelos();
        for (VueloEntity vueloEntity : vuelosEntity) {
            Vuelo vuelo = vueloAdapter.toAlgorithm(vueloEntity);
            vuelos.add(vuelo);
        }
        algorithm.setVuelos(vuelos);
        poolAlgorithm.put(algorithm.getCodigo(), algorithm);
        return algorithm;
    }

    public RutaEntity toEntity(Ruta algorithm) {
        if(poolEntity.containsKey(algorithm.getCodigo())) {
            return  poolEntity.get(algorithm.getCodigo());
        }
        RutaEntity entity = rutaService.findByCodigo(algorithm.getCodigo()).orElse(null);
        if (entity == null) {
            entity = new RutaEntity();
            entity.setCodigo(algorithm.getCodigo());
        }
        entity.setDuracion(algorithm.getDuracion());
        entity.setDistancia(algorithm.getDistancia());
        entity.setFechaHoraSalidaLocal(algorithm.getFechaHoraSalidaLocal());
        entity.setFechaHoraSalidaUTC(algorithm.getFechaHoraSalidaUTC());
        entity.setFechaHoraLlegadaLocal(algorithm.getFechaHoraLlegadaLocal());
        entity.setFechaHoraLlegadaUTC(algorithm.getFechaHoraLlegadaUTC());
        entity.setTipo(algorithm.getTipo());
        AeropuertoEntity origenEntity = aeropuertoAdapter.toEntity(algorithm.getOrigen());
        entity.setOrigen(origenEntity);
        AeropuertoEntity destinoEntity = aeropuertoAdapter.toEntity(algorithm.getDestino());
        entity.setDestino(destinoEntity);
        poolEntity.put(entity.getCodigo(), entity);
        return entity;
    }

    public RutaDTO toDTO(Ruta algorithm) {
        if(poolDTO.containsKey(algorithm.getCodigo())) {
            return poolDTO.get(algorithm.getCodigo());
        }
        RutaDTO dto = new RutaDTO();
        dto.setCodigo(algorithm.getCodigo());
        dto.setDuracion(algorithm.getDuracion());
        dto.setDistancia(algorithm.getDistancia());
        dto.setTipo(algorithm.getTipo().toString());
        dto.setFechaHoraSalida(algorithm.getFechaHoraSalidaUTC());
        dto.setFechaHoraLlegada(algorithm.getFechaHoraLlegadaUTC());
        Aeropuerto origen = algorithm.getOrigen();
        dto.setCodOrigen(origen.getCodigo());
        Aeropuerto destino = algorithm.getDestino();
        dto.setCodDestino(destino.getCodigo());
        List<Vuelo> vuelos = algorithm.getVuelos();
        List<String> codVuelos = new ArrayList<>();
        vuelos.forEach(vuelo -> codVuelos.add(vuelo.getCodigo()));
        dto.setCodVuelos(codVuelos);
        poolDTO.put(algorithm.getCodigo(), dto);
        return dto;
    }

    public RutaDTO toDTO(RutaEntity entity) {
        if(poolDTO.containsKey(entity.getCodigo())) {
            return poolDTO.get(entity.getCodigo());
        }
        RutaDTO dto = new RutaDTO();
        dto.setCodigo(entity.getCodigo());
        dto.setDuracion(entity.getDuracion());
        dto.setDistancia(entity.getDistancia());
        dto.setTipo(entity.getTipo().toString());
        dto.setFechaHoraSalida(entity.getFechaHoraSalidaUTC());
        dto.setFechaHoraLlegada(entity.getFechaHoraLlegadaUTC());
        AeropuertoEntity origenEntity = entity.getOrigen();
        dto.setCodOrigen(origenEntity.getCodigo());
        AeropuertoEntity destinoEntity = entity.getDestino();
        dto.setCodDestino(destinoEntity.getCodigo());
        List<VueloEntity> vuelosEntity = entity.getVuelos();
        List<String> codVuelos = new ArrayList<>();
        vuelosEntity.forEach(vueloEntity -> codVuelos.add(vueloEntity.getCodigo()));
        dto.setCodVuelos(codVuelos);
        poolDTO.put(entity.getCodigo(), dto);
        return dto;
    }

    public void clearPools() {
        poolAlgorithm.clear();
        poolEntity.clear();
        poolDTO.clear();
        aeropuertoAdapter.clearPools();
        vueloAdapter.clearPools();
    }
}
