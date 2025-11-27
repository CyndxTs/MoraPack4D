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
import org.springframework.stereotype.Component;

@Component
public class ParametrosMapper {

    public void toAlgorithm(ParametrosDTO dto) {
        Problematica.MAX_DIAS_ENTREGA_INTRACONTINENTAL = dto.getMaxDiasEntregaIntracontinental();
        Problematica.MAX_DIAS_ENTREGA_INTERCONTINENTAL = dto.getMaxDiasEntregaIntercontinental();
        Problematica.MAX_HORAS_RECOJO = dto.getMaxHorasRecojo();
        Problematica.MAX_HORAS_ESTANCIA = dto.getMaxHorasEstancia();
        Problematica.MIN_HORAS_ESTANCIA = dto.getMinHorasEstancia();
        Problematica.CODIGOS_DE_ORIGENES = dto.getCodOrigenes();
        GVNS.D_MIN = dto.getDMin();
        GVNS.I_MAX = dto.getIMax();
        GVNS.L_MIN = dto.getEleMin();
        GVNS.L_MAX = dto.getEleMax();
        GVNS.K_MIN = dto.getKMin();
        GVNS.K_MAX = dto.getKMax();
        GVNS.T_MAX = dto.getTMax();
        GVNS.MAX_INTENTOS = dto.getMaxIntentos();
        Solucion.f_UA = dto.getFactorDeUmbralDeAberracion();
        Solucion.f_UT = dto.getFactorDeUtilizacionTemporal();
        Solucion.f_DE = dto.getFactorDeDesviacionEspacial();
        Solucion.f_DO = dto.getFactorDeDisposicionOperacional();
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
        entity.setMaxDiasEntregaIntracontinental(dto.getMaxDiasEntregaIntracontinental());
        entity.setMaxDiasEntregaIntercontinental(dto.getMaxDiasEntregaIntercontinental());
        entity.setMaxHorasRecojo(dto.getMaxHorasRecojo());
        entity.setMaxHorasEstancia(dto.getMaxHorasEstancia());
        entity.setMinHorasEstancia(dto.getMinHorasEstancia());
        entity.setCodOrigenes(dto.getCodOrigenes());
        entity.setDMin(dto.getDMin());
        entity.setIMax(dto.getIMax());
        entity.setEleMin(dto.getEleMin());
        entity.setEleMax(dto.getEleMax());
        entity.setKMin(dto.getKMin());
        entity.setKMax(dto.getKMax());
        entity.setTMax(dto.getTMax());
        entity.setMaxIntentos(dto.getMaxIntentos());
        entity.setFactorDeUmbralDeAberracion(dto.getFactorDeUmbralDeAberracion());
        entity.setFactorDeUtilizacionTemporal(dto.getFactorDeUtilizacionTemporal());
        entity.setFactorDeDesviacionEspacial(dto.getFactorDeDesviacionEspacial());
        entity.setFactorDeDisposicionOperacional(dto.getFactorDeDisposicionOperacional());
        return entity;
    }
}
