/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       RutaMapper.java
 [**/

package com.pucp.dp1.grupo4d.morapack.mapper;

import com.pucp.dp1.grupo4d.morapack.model.algorithm.Aeropuerto;
import com.pucp.dp1.grupo4d.morapack.model.algorithm.Ruta;
import com.pucp.dp1.grupo4d.morapack.model.algorithm.Vuelo;
import com.pucp.dp1.grupo4d.morapack.model.dto.RutaDTO;
import com.pucp.dp1.grupo4d.morapack.model.entity.AeropuertoEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.RutaEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.VueloEntity;
import com.pucp.dp1.grupo4d.morapack.util.G4DUtility;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class RutaMapper {

    public RutaDTO toDTO(Ruta algorithm) {
        RutaDTO dto = new RutaDTO();
        dto.setCodigo(algorithm.getCodigo());
        dto.setDuracion(algorithm.getDuracion());
        dto.setDistancia(algorithm.getDistancia());
        dto.setTipo(algorithm.getTipo().toString());
        dto.setFechaHoraSalida(G4DUtility.Convertor.toDisplayString(algorithm.getFechaHoraSalida()));
        dto.setFechaHoraLlegada(G4DUtility.Convertor.toDisplayString(algorithm.getFechaHoraLlegada()));
        Aeropuerto origen = algorithm.getOrigen();
        dto.setCodOrigen(origen.getCodigo());
        Aeropuerto destino = algorithm.getDestino();
        dto.setCodDestino(destino.getCodigo());
        List<Vuelo> vuelos = algorithm.getVuelos();
        List<String> codVuelos = new ArrayList<>();
        vuelos.forEach(vuelo -> codVuelos.add(vuelo.getCodigo()));
        dto.setCodVuelos(codVuelos);
        dto.setEstado(algorithm.getEstado().toString());
        return dto;
    }

    public RutaDTO toDTO(RutaEntity entity) {
        RutaDTO dto = new RutaDTO();
        dto.setCodigo(entity.getCodigo());
        dto.setDuracion(entity.getDuracion());
        dto.setDistancia(entity.getDistancia());
        dto.setTipo(entity.getTipo().toString());
        dto.setFechaHoraSalida(G4DUtility.Convertor.toDisplayString(entity.getFechaHoraSalidaUTC()));
        dto.setFechaHoraLlegada(G4DUtility.Convertor.toDisplayString(entity.getFechaHoraLlegadaUTC()));
        AeropuertoEntity origenEntity = entity.getOrigen();
        dto.setCodOrigen(origenEntity.getCodigo());
        AeropuertoEntity destinoEntity = entity.getDestino();
        dto.setCodDestino(destinoEntity.getCodigo());
        List<VueloEntity> vuelosEntity = entity.getVuelos();
        List<String> codVuelos = new ArrayList<>();
        vuelosEntity.forEach(vueloEntity -> codVuelos.add(vueloEntity.getCodigo()));
        dto.setCodVuelos(codVuelos);
        dto.setEstado(entity.getEstado().toString());
        return dto;
    }
}
