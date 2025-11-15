/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       ParametrosAdapter.java
 [**/

package com.pucp.dp1.grupo4d.morapack.adapter;

import com.pucp.dp1.grupo4d.morapack.algorithm.GVNS;
import com.pucp.dp1.grupo4d.morapack.algorithm.Problematica;
import com.pucp.dp1.grupo4d.morapack.algorithm.Solucion;
import com.pucp.dp1.grupo4d.morapack.model.dto.ParametrosDTO;
import com.pucp.dp1.grupo4d.morapack.model.entity.ParametrosEntity;
import com.pucp.dp1.grupo4d.morapack.util.G4D;
import org.springframework.stereotype.Component;

@Component
public class ParametrosAdapter {

    public void toAlgorithm(ParametrosEntity entity) {
        Problematica.MAX_DIAS_ENTREGA_INTRACONTINENTAL = entity.getMaxDiasEntregaIntracontinental();
        Problematica.MAX_DIAS_ENTREGA_INTERCONTINENTAL = entity.getMaxDiasEntregaIntercontinental();
        Problematica.MAX_HORAS_RECOJO = entity.getMaxHorasRecojo();
        Problematica.MAX_HORAS_ESTANCIA = entity.getMaxHorasEstancia();
        Problematica.MIN_HORAS_ESTANCIA = entity.getMinHorasEstancia();
        Problematica.FECHA_HORA_INICIO = entity.getFechaHoraInicio();
        Problematica.FECHA_HORA_FIN = entity.getFechaHoraFin();
        Problematica.DESFASE_TEMPORAL = entity.getDesfaseTemporal();
        GVNS.D_MIN = entity.getDMin();
        GVNS.I_MAX = entity.getIMax();
        GVNS.L_MIN = entity.getEleMin();
        GVNS.L_MAX = entity.getEleMax();
        GVNS.K_MIN = entity.getKMin();
        GVNS.K_MAX = entity.getKMax();
        GVNS.T_MAX = entity.getTMax();
        GVNS.MAX_INTENTOS = entity.getMaxIntentos();
        Solucion.f_UA = entity.getFactorDeUmbralDeAberracion();
        Solucion.f_UT = entity.getFactorDeUtilizacionTemporal();
        Solucion.f_DE = entity.getFactorDeDesviacionEspacial();
        Solucion.f_DO = entity.getFactorDeDisposicionOperacional();
    }

    public void toAlgorithm(ParametrosDTO dto) {
        Problematica.MAX_DIAS_ENTREGA_INTRACONTINENTAL = dto.getMaxDiasEntregaIntracontinental();
        Problematica.MAX_DIAS_ENTREGA_INTERCONTINENTAL = dto.getMaxDiasEntregaIntercontinental();
        Problematica.MAX_HORAS_RECOJO = dto.getMaxHorasRecojo();
        Problematica.MAX_HORAS_ESTANCIA = dto.getMaxHorasEstancia();
        Problematica.MIN_HORAS_ESTANCIA = dto.getMinHorasEstancia();
        Problematica.FECHA_HORA_INICIO = G4D.toDateTime(dto.getFechaHoraInicio());
        Problematica.FECHA_HORA_FIN = G4D.toDateTime(dto.getFechaHoraFin());
        Problematica.DESFASE_TEMPORAL = ((dto.getConsiderarDesfaseTemporal()) ? dto.getMaxDiasEntregaIntercontinental(): 0);
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

    public ParametrosEntity toEntity(ParametrosDTO dto) {
        ParametrosEntity entity = new ParametrosEntity();
        entity.setMaxDiasEntregaIntracontinental(dto.getMaxDiasEntregaIntracontinental());
        entity.setMaxDiasEntregaIntercontinental(dto.getMaxDiasEntregaIntercontinental());
        entity.setMaxHorasRecojo(dto.getMaxHorasRecojo());
        entity.setMaxHorasEstancia(dto.getMaxHorasEstancia());
        entity.setMinHorasEstancia(dto.getMinHorasEstancia());
        entity.setFechaHoraInicio(G4D.toDateTime(dto.getFechaHoraInicio()));
        entity.setFechaHoraFin(G4D.toDateTime(dto.getFechaHoraFin()));
        entity.setDesfaseTemporal((dto.getConsiderarDesfaseTemporal()) ? dto.getMaxDiasEntregaIntercontinental(): 0);
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
