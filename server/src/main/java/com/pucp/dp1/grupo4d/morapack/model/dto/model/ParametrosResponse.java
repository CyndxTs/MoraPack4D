/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       ParametrosResponse.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto.model;

import com.pucp.dp1.grupo4d.morapack.model.entity.ParametrosEntity;
import java.time.LocalDateTime;
import java.util.List;

public class ParametrosResponse {
    private Integer maxDiasEntregaIntracontinental;
    private Integer maxDiasEntregaIntercontinental;
    private Double maxHorasRecojo;
    private Double maxHorasEstancia;
    private Double minHorasEstancia;
    private LocalDateTime fechaHoraIni;
    private LocalDateTime fechaHoraFin;
    private List<String> codigosDeOrigenes;
    private Double dMin;
    private Integer iMax;
    private Integer eleMin;
    private Integer eleMax;
    private Integer kMin;
    private Integer kMax;
    private Integer tMax;
    private Integer maxIntentos;
    private Double factorDeUmbralDeAberracion;
    private Double factorDeUtilizacionTemporal;
    private Double factorDeDesviacionEspacial;
    private Double factorDeDisposicionOperacional;

    public ParametrosResponse(ParametrosEntity parametrizacion) {
        this.maxDiasEntregaIntracontinental = parametrizacion.getMaxDiasEntregaIntracontinental();
        this.maxDiasEntregaIntercontinental = parametrizacion.getMaxDiasEntregaIntercontinental();
        this.maxHorasRecojo = parametrizacion.getMaxHorasRecojo();
        this.maxHorasEstancia = parametrizacion.getMaxHorasEstancia();
        this.minHorasEstancia = parametrizacion.getMinHorasEstancia();
        this.fechaHoraIni = parametrizacion.getFechaHoraInicio().toLocalDateTime();
        this.fechaHoraFin = parametrizacion.getFechaHoraFin().toLocalDateTime();
        this.codigosDeOrigenes = parametrizacion.getCodOrigenes();
        this.dMin = parametrizacion.getDMin();
        this.iMax = parametrizacion.getIMax();
        this.eleMin = parametrizacion.getEleMin();
        this.eleMax = parametrizacion.getEleMax();
        this.kMin = parametrizacion.getKMin();
        this.kMax = parametrizacion.getKMax();
        this.tMax = parametrizacion.getTMax();
        this.maxIntentos = parametrizacion.getMaxIntentos();
        this.factorDeUmbralDeAberracion = parametrizacion.getFactorDeUmbralDeAberracion();
        this.factorDeUtilizacionTemporal = parametrizacion.getFactorDeUtilizacionTemporal();
        this.factorDeDesviacionEspacial = parametrizacion.getFactorDeDesviacionEspacial();
        this.factorDeDisposicionOperacional = parametrizacion.getFactorDeDisposicionOperacional();
    }

    public Integer getMaxDiasEntregaIntracontinental() { return maxDiasEntregaIntracontinental; }
    public void setMaxDiasEntregaIntracontinental(Integer maxDiasEntregaIntracontinental) { this.maxDiasEntregaIntracontinental = maxDiasEntregaIntracontinental; }
    public Integer getMaxDiasEntregaIntercontinental() { return maxDiasEntregaIntercontinental; }
    public void setMaxDiasEntregaIntercontinental(Integer maxDiasEntregaIntercontinental) { this.maxDiasEntregaIntercontinental = maxDiasEntregaIntercontinental; }
    public Double getMaxHorasRecojo() { return maxHorasRecojo; }
    public void setMaxHorasRecojo(Double maxHorasRecojo) { this.maxHorasRecojo = maxHorasRecojo; }
    public Double getMaxHorasEstancia() { return maxHorasEstancia; }
    public void setMaxHorasEstancia(Double maxHorasEstancia) { this.maxHorasEstancia = maxHorasEstancia; }
    public Double getMinHorasEstancia() { return minHorasEstancia; }
    public void setMinHorasEstancia(Double minHorasEstancia) { this.minHorasEstancia = minHorasEstancia; }
    public LocalDateTime getFechaHoraIni() { return fechaHoraIni; }
    public void setFechaHoraIni(LocalDateTime fechaHoraIni) { this.fechaHoraIni = fechaHoraIni; }
    public LocalDateTime getFechaHoraFin() { return fechaHoraFin; }
    public void setFechaHoraFin(LocalDateTime fechaHoraFin) { this.fechaHoraFin = fechaHoraFin; }
    public List<String> getCodigosDeOrigenes() { return codigosDeOrigenes; }
    public void setCodigosDeOrigenes(List<String> codigosDeOrigenes) { this.codigosDeOrigenes = codigosDeOrigenes; }
    public Double getDMin() { return dMin; }
    public void setDMin(Double dMin) { this.dMin = dMin; }
    public Integer getIMax() { return iMax; }
    public void setIMax(Integer iMax) { this.iMax = iMax; }
    public Integer getEleMin() { return eleMin; }
    public void setEleMin(Integer eleMin) { this.eleMin = eleMin; }
    public Integer getEleMax() { return eleMax; }
    public void setEleMax(Integer eleMax) { this.eleMax = eleMax; }
    public Integer getKMin() { return kMin; }
    public void setKMin(Integer kMin) { this.kMin = kMin; }
    public Integer getKMax() { return kMax; }
    public void setKMax(Integer kMax) { this.kMax = kMax; }
    public Integer getTMax() { return tMax; }
    public void setTMax(Integer tMax) { this.tMax = tMax; }
    public Integer getMaxIntentos() { return maxIntentos; }
    public void setMaxIntentos(Integer maxIntentos) { this.maxIntentos = maxIntentos; }
    public Double getFactorDeUmbralDeAberracion() { return factorDeUmbralDeAberracion; }
    public void setFactorDeUmbralDeAberracion(Double factorDeUmbralDeAberracion) { this.factorDeUmbralDeAberracion = factorDeUmbralDeAberracion; }
    public Double getFactorDeUtilizacionTemporal() { return factorDeUtilizacionTemporal; }
    public void setFactorDeUtilizacionTemporal(Double factorDeUtilizacionTemporal) { this.factorDeUtilizacionTemporal = factorDeUtilizacionTemporal; }
    public Double getFactorDeDesviacionEspacial() { return factorDeDesviacionEspacial; }
    public void setFactorDeDesviacionEspacial(Double factorDeDesviacionEspacial) { this.factorDeDesviacionEspacial = factorDeDesviacionEspacial; }
    public Double getFactorDeDisposicionOperacional() { return factorDeDisposicionOperacional; }
    public void setFactorDeDisposicionOperacional(Double factorDeDisposicionOperacional) { this.factorDeDisposicionOperacional = factorDeDisposicionOperacional; }
}