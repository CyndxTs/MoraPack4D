/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       ParametrosMapper.java
 [**/

package com.pucp.dp1.grupo4d.morapack.mapper;

import com.pucp.dp1.grupo4d.morapack.algorithm.GVNS;
import com.pucp.dp1.grupo4d.morapack.algorithm.Problematica;
import com.pucp.dp1.grupo4d.morapack.model.dto.ParametrosDTO;
import com.pucp.dp1.grupo4d.morapack.model.entity.ParametrosEntity;
import com.pucp.dp1.grupo4d.morapack.util.G4DUtility;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class ParametrosMapper {

    public void toAlgorithm(Problematica problematica, ParametrosDTO dto) {
        problematica.maxDiasDeEntregaIntracontinental = G4DUtility.Convertor.toAdmissible(dto.getMaxDiasEntregaIntracontinental(), 2);
        problematica.maxDiasDeEntregaIntercontinental = G4DUtility.Convertor.toAdmissible(dto.getMaxDiasEntregaIntercontinental(), 3);
        problematica.maxHorasDeRecojo = G4DUtility.Convertor.toAdmissible(dto.getMaxHorasRecojo(), 2.0);
        problematica.maxHorasDeEstancia = G4DUtility.Convertor.toAdmissible(dto.getMaxHorasEstancia(), 12.0);
        problematica.minHorasDeEstancia = G4DUtility.Convertor.toAdmissible(dto.getMinHorasEstancia(), 1.0);
        problematica.probabilidadDeReplanificacion = G4DUtility.Convertor.toAdmissible(dto.getProbabilidadReplanificacion(), 0.15);
        G4DUtility.Convertor.toAdmissible(dto.getCodOrigenes(),() -> List.of("SPIM", "EBCI", "UBBB")).forEach(cod -> problematica.origenes.put(cod, null));
    }

    public void toAlgorithm(GVNS gvns, ParametrosDTO dto) {
        gvns.dMin = G4DUtility.Convertor.toAdmissible(dto.getDMin(), 0.005);
        gvns.iMax = G4DUtility.Convertor.toAdmissible(dto.getIMax(), 3);
        gvns.eleMin = G4DUtility.Convertor.toAdmissible(dto.getEleMin(), 1);
        gvns.eleMax = G4DUtility.Convertor.toAdmissible(dto.getEleMax(), 2);
        gvns.kMin = G4DUtility.Convertor.toAdmissible(dto.getKMin(), 3);
        gvns.kMax = G4DUtility.Convertor.toAdmissible(dto.getKMax(), 5);
        gvns.nMax = G4DUtility.Convertor.toAdmissible(dto.getNMax(), 6);
        gvns.tMax = G4DUtility.Convertor.toAdmissible(dto.getTMax(), 7);
        gvns.solucion.f_UA = G4DUtility.Convertor.toAdmissible(dto.getFactorDeUmbralDeAberracion(), 1.015);
        gvns.solucion.f_UT = G4DUtility.Convertor.toAdmissible(dto.getFactorDeUtilizacionTemporal(), 5000.0);
        gvns.solucion.f_DE = G4DUtility.Convertor.toAdmissible(dto.getFactorDeDesviacionEspacial(), 2000.0);
        gvns.solucion.f_DO = G4DUtility.Convertor.toAdmissible(dto.getFactorDeDisposicionOperacional(), 3000.0);
    }

    public ParametrosDTO toDTO(ParametrosEntity entity) {
        ParametrosDTO dto = new ParametrosDTO();
        dto.setMaxDiasEntregaIntracontinental(entity.getMaxDiasEntregaIntracontinental());
        dto.setMaxDiasEntregaIntercontinental(entity.getMaxDiasEntregaIntercontinental());
        dto.setMaxHorasRecojo(entity.getMaxHorasRecojo());
        dto.setMaxHorasEstancia(entity.getMaxHorasEstancia());
        dto.setMinHorasEstancia(entity.getMinHorasEstancia());
        dto.setProbabilidadReplanificacion(entity.getProbabilidadReplanificacion());
        dto.setCodOrigenes(entity.getCodOrigenes());
        dto.setDMin(entity.getDMin());
        dto.setIMax(entity.getIMax());
        dto.setEleMin(entity.getEleMin());
        dto.setEleMax(entity.getEleMax());
        dto.setKMin(entity.getKMin());
        dto.setKMax(entity.getKMax());
        dto.setTMax(entity.getTMax());
        dto.setNMax(entity.getNMax());
        dto.setFactorDeUmbralDeAberracion(entity.getFactorDeUmbralDeAberracion());
        dto.setFactorDeUtilizacionTemporal(entity.getFactorDeUtilizacionTemporal());
        dto.setFactorDeDesviacionEspacial(entity.getFactorDeDesviacionEspacial());
        dto.setFactorDeDisposicionOperacional(entity.getFactorDeDisposicionOperacional());
        return dto;
    }
}
