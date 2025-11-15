/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       ParametrosDTO.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class ParametrosDTO implements DTO {
    private Integer maxDiasEntregaIntracontinental;
    private Integer maxDiasEntregaIntercontinental;
    private Double maxHorasRecojo;
    private Double maxHorasEstancia;
    private Double minHorasEstancia;
    private String fechaHoraInicio;
    private String fechaHoraFin;
    private Boolean considerarDesfaseTemporal;
    private List<String> codOrigenes;

    @JsonProperty("dMin")
    private Double dMin;

    @JsonProperty("iMax")
    private Integer iMax;

    private Integer eleMin;
    private Integer eleMax;

    @JsonProperty("kMin")
    private Integer kMin;

    @JsonProperty("kMax")
    private Integer kMax;

    @JsonProperty("tMax")
    private Integer tMax;

    private Integer maxIntentos;
    private Double factorDeUmbralDeAberracion;
    private Double factorDeUtilizacionTemporal;
    private Double factorDeDesviacionEspacial;
    private Double factorDeDisposicionOperacional;

    public ParametrosDTO() {
        this.codOrigenes = new ArrayList<>();
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
    public String getFechaHoraInicio() { return fechaHoraInicio; }
    public void setFechaHoraInicio(String fechaHoraInicio) { this.fechaHoraInicio = fechaHoraInicio; }
    public String getFechaHoraFin() { return fechaHoraFin; }
    public void setFechaHoraFin(String fechaHoraFin) { this.fechaHoraFin = fechaHoraFin; }
    public Boolean getConsiderarDesfaseTemporal() { return considerarDesfaseTemporal; }
    public void setConsiderarDesfaseTemporal(Boolean considerarDesfaseTemporal) { this.considerarDesfaseTemporal = considerarDesfaseTemporal; }
    public List<String> getCodOrigenes() { return codOrigenes; }
    public void setCodOrigenes(List<String> codOrigenes) { this.codOrigenes = codOrigenes; }
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