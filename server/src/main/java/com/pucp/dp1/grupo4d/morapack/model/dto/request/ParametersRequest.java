/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       ParametersRequest.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto.request;

import java.util.List;

public class ParametersRequest {
    private Integer eleMin = 1;
    private Integer eleMax = 3;
    private Integer kMin = 1;
    private Integer kMax = 5;
    private Integer tMax = 12;
    private Integer maxIntentos = 12;
    private Integer maxDiasEntregaIntracontinental = 2;
    private Integer maxDiasEntregaIntercontinental = 3;
    private Double maxHorasRecojo = 2.0;
    private Double maxHorasEstancia = 12.0;
    private Double minHorasEstancia = 2.0;
    private List<String> codigosDeOrigenes = List.of("SPIM", "EBCI", "UBBB");
    private Double factorDeUmbralDeAberracion = 1.015;
    private Double factorDeUtilizacionTemporal = 5000.0;
    private Double factorDeDesviacionEspacial = 2000.0;
    private Double factorDeDisposicionOperacional = 3000.0;

    public ParametersRequest() {}

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
    public List<String> getCodigosDeOrigenes() { return codigosDeOrigenes; }
    public void setCodigosDeOrigenes(List<String> codigosDeOrigenes) { this.codigosDeOrigenes = codigosDeOrigenes; }
    public Double getFactorDeUmbralDeAberracion() { return factorDeUmbralDeAberracion; }
    public void setFactorDeUmbralDeAberracion(Double factorDeUmbralDeAberracion) { this.factorDeUmbralDeAberracion = factorDeUmbralDeAberracion; }
    public Double getFactorDeUtilizacionTemporal() { return factorDeUtilizacionTemporal; }
    public void setFactorDeUtilizacionTemporal(Double factorDeUtilizacionTemporal) { this.factorDeUtilizacionTemporal = factorDeUtilizacionTemporal; }
    public Double getFactorDeDesviacionEspacial() { return factorDeDesviacionEspacial; }
    public void setFactorDeDesviacionEspacial(Double factorDeDesviacionEspacial) { this.factorDeDesviacionEspacial = factorDeDesviacionEspacial; }
    public Double getFactorDeDisposicionOperacional() { return factorDeDisposicionOperacional; }
    public void setFactorDeDisposicionOperacional(Double factorDeDisposicionOperacional) { this.factorDeDisposicionOperacional = factorDeDisposicionOperacional; }
}