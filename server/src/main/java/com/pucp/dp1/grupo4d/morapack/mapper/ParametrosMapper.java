/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       ParametrosMapper.java
 [**/

package com.pucp.dp1.grupo4d.morapack.mapper;

import com.pucp.dp1.grupo4d.morapack.algorithm.GVNS;
import com.pucp.dp1.grupo4d.morapack.algorithm.Problematica;
import com.pucp.dp1.grupo4d.morapack.algorithm.Solucion;
import com.pucp.dp1.grupo4d.morapack.model.dto.ParametrosDTO;
import com.pucp.dp1.grupo4d.morapack.model.entity.ParametrosEntity;
import com.pucp.dp1.grupo4d.morapack.util.G4DUtility;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ParametrosMapper {

    public void toAlgorithm(ParametrosDTO dto) {
        Problematica.MAX_DIAS_ENTREGA_INTRACONTINENTAL = G4DUtility.Convertor.toAdmissible(dto.getMaxDiasEntregaIntracontinental(), 2);
        Problematica.MAX_DIAS_ENTREGA_INTERCONTINENTAL = G4DUtility.Convertor.toAdmissible(dto.getMaxDiasEntregaIntercontinental(), 3);
        Problematica.MAX_HORAS_RECOJO = G4DUtility.Convertor.toAdmissible(dto.getMaxHorasRecojo(), 2.0);
        Problematica.MAX_HORAS_ESTANCIA = G4DUtility.Convertor.toAdmissible(dto.getMaxHorasEstancia(), 12.0);
        Problematica.MIN_HORAS_ESTANCIA = G4DUtility.Convertor.toAdmissible(dto.getMinHorasEstancia(), 1.0);
        Problematica.CODIGOS_DE_ORIGENES = G4DUtility.Convertor.toAdmissible(dto.getCodOrigenes(),() -> List.of("SPIM", "EBCI", "UBBB"));
        GVNS.D_MIN = G4DUtility.Convertor.toAdmissible(dto.getDMin(), 0.005);
        GVNS.I_MAX = G4DUtility.Convertor.toAdmissible(dto.getIMax(), 2);
        GVNS.L_MIN = G4DUtility.Convertor.toAdmissible(dto.getEleMin(), 1);
        GVNS.L_MAX = G4DUtility.Convertor.toAdmissible(dto.getEleMax(), 2);
        GVNS.K_MIN = G4DUtility.Convertor.toAdmissible(dto.getKMin(), 3);
        GVNS.K_MAX = G4DUtility.Convertor.toAdmissible(dto.getKMax(), 5);
        GVNS.T_MAX = G4DUtility.Convertor.toAdmissible(dto.getTMax(), 7);
        GVNS.MAX_INTENTOS = G4DUtility.Convertor.toAdmissible(dto.getMaxIntentos(), 12);
        Solucion.f_UA = G4DUtility.Convertor.toAdmissible(dto.getFactorDeUmbralDeAberracion(), 1.015);
        Solucion.f_UT = G4DUtility.Convertor.toAdmissible(dto.getFactorDeUtilizacionTemporal(), 5000.0);
        Solucion.f_DE = G4DUtility.Convertor.toAdmissible(dto.getFactorDeDesviacionEspacial(), 2000.0);
        Solucion.f_DO = G4DUtility.Convertor.toAdmissible(dto.getFactorDeDisposicionOperacional(), 3000.0);
    }

    public void toDTO() {
        ParametrosDTO dto = new ParametrosDTO();
        dto.setMaxDiasEntregaIntracontinental(Problematica.MAX_DIAS_ENTREGA_INTRACONTINENTAL);
        dto.setMaxDiasEntregaIntercontinental(Problematica.MAX_DIAS_ENTREGA_INTERCONTINENTAL);
        dto.setMaxHorasRecojo(Problematica.MAX_HORAS_RECOJO);
        dto.setMaxHorasEstancia(Problematica.MAX_HORAS_ESTANCIA);
        dto.setMinHorasEstancia(Problematica.MIN_HORAS_ESTANCIA);
        dto.setCodOrigenes(Problematica.CODIGOS_DE_ORIGENES);
        dto.setDMin(GVNS.D_MIN);
        dto.setIMax(GVNS.I_MAX);
        dto.setEleMin(GVNS.L_MIN);
        dto.setEleMax(GVNS.L_MAX);
        dto.setKMin(GVNS.K_MIN);
        dto.setKMax(GVNS.K_MAX);
        dto.setTMax(GVNS.T_MAX);
        dto.setMaxIntentos(GVNS.MAX_INTENTOS);
        dto.setFactorDeUmbralDeAberracion(Solucion.f_UA);
        dto.setFactorDeUtilizacionTemporal(Solucion.f_UT);
        dto.setFactorDeDesviacionEspacial(Solucion.f_DE);
        dto.setFactorDeDisposicionOperacional(Solucion.f_DO);
    }

    public ParametrosDTO toDTO(ParametrosEntity entity) {
        ParametrosDTO dto = new ParametrosDTO();
        dto.setMaxDiasEntregaIntracontinental(entity.getMaxDiasEntregaIntracontinental());
        dto.setMaxDiasEntregaIntercontinental(entity.getMaxDiasEntregaIntercontinental());
        dto.setMaxHorasRecojo(entity.getMaxHorasRecojo());
        dto.setMaxHorasEstancia(entity.getMaxHorasEstancia());
        dto.setMinHorasEstancia(entity.getMinHorasEstancia());
        dto.setCodOrigenes(entity.getCodOrigenes());
        dto.setDMin(entity.getDMin());
        dto.setIMax(entity.getIMax());
        dto.setEleMin(entity.getEleMin());
        dto.setEleMax(entity.getEleMax());
        dto.setKMin(entity.getKMin());
        dto.setKMax(entity.getKMax());
        dto.setTMax(entity.getTMax());
        dto.setMaxIntentos(entity.getMaxIntentos());
        dto.setFactorDeUmbralDeAberracion(entity.getFactorDeUmbralDeAberracion());
        dto.setFactorDeUtilizacionTemporal(entity.getFactorDeUtilizacionTemporal());
        dto.setFactorDeDesviacionEspacial(entity.getFactorDeDesviacionEspacial());
        dto.setFactorDeDisposicionOperacional(entity.getFactorDeDisposicionOperacional());
        return dto;
    }

    public ParametrosEntity toEntity(ParametrosDTO dto) {
        ParametrosEntity entity = new ParametrosEntity();
        entity.setMaxDiasEntregaIntracontinental(G4DUtility.Convertor.toAdmissible(dto.getMaxDiasEntregaIntracontinental(), 2));
        entity.setMaxDiasEntregaIntercontinental(G4DUtility.Convertor.toAdmissible(dto.getMaxDiasEntregaIntercontinental(), 3));
        entity.setMaxHorasRecojo(G4DUtility.Convertor.toAdmissible(dto.getMaxHorasRecojo(), 2.0));
        entity.setMaxHorasEstancia(G4DUtility.Convertor.toAdmissible(dto.getMaxHorasEstancia(), 12.0));
        entity.setMinHorasEstancia(G4DUtility.Convertor.toAdmissible(dto.getMinHorasEstancia(), 1.0));
        entity.setCodOrigenes(G4DUtility.Convertor.toAdmissible(dto.getCodOrigenes(), () -> List.of("SPIM", "EBCI", "UBBB")));
        entity.setDMin(G4DUtility.Convertor.toAdmissible(dto.getDMin(), 0.005));
        entity.setIMax(G4DUtility.Convertor.toAdmissible(dto.getIMax(), 2));
        entity.setEleMin(G4DUtility.Convertor.toAdmissible(dto.getEleMin(), 1));
        entity.setEleMax(G4DUtility.Convertor.toAdmissible(dto.getEleMax(), 2));
        entity.setKMin(G4DUtility.Convertor.toAdmissible(dto.getKMin(), 3));
        entity.setKMax(G4DUtility.Convertor.toAdmissible(dto.getKMax(), 5));
        entity.setTMax(G4DUtility.Convertor.toAdmissible(dto.getTMax(), 7));
        entity.setMaxIntentos(G4DUtility.Convertor.toAdmissible(dto.getMaxIntentos(), 12));
        entity.setFactorDeUmbralDeAberracion(G4DUtility.Convertor.toAdmissible(dto.getFactorDeUmbralDeAberracion(), 1.015));
        entity.setFactorDeUtilizacionTemporal(G4DUtility.Convertor.toAdmissible(dto.getFactorDeUtilizacionTemporal(), 5000.0));
        entity.setFactorDeDesviacionEspacial(G4DUtility.Convertor.toAdmissible(dto.getFactorDeDesviacionEspacial(), 2000.0));
        entity.setFactorDeDisposicionOperacional(G4DUtility.Convertor.toAdmissible(dto.getFactorDeDisposicionOperacional(), 3000.0));
        return entity;
    }
}
