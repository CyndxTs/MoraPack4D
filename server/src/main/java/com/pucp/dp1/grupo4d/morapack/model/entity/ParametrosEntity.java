/**
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       ParametrosEntity.java
 **/

package com.pucp.dp1.grupo4d.morapack.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.pucp.dp1.grupo4d.morapack.util.G4D;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "PARAMETROS", schema = "morapack4d")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ParametrosEntity {

    @Id
    @Column(name = "id", nullable = false)
    private Integer id = 1;

    @Column(name = "max_dias_entrega_intercontinental", nullable = false)
    private Integer maxDiasEntregaIntercontinental = 3;

    @Column(name = "max_dias_entrega_intracontinental", nullable = false)
    private Integer maxDiasEntregaIntracontinental = 2;

    @Column(name = "max_horas_recojo", nullable = false)
    private Double maxHorasRecojo = 2.0;

    @Column(name = "min_horas_estancia", nullable = false)
    private Double minHorasEstancia = 1.0;

    @Column(name = "max_horas_estancia", nullable = false)
    private Double maxHorasEstancia = 12.0;

    @Column(name = "fecha_hora_inicio", nullable = false)
    private LocalDateTime fechaHoraInicio = G4D.toDateTime("1999-12-31 23:59:59");

    @Column(name = "fecha_hora_fin", nullable = false)
    private LocalDateTime fechaHoraFin = LocalDateTime.now();

    @Column(name = "d_min", nullable = false)
    private Double dMin = 0.001;

    @Column(name = "i_max", nullable = false)
    private Integer iMax = 3;

    @Column(name = "ele_min", nullable = false)
    private Integer eleMin = 1;

    @Column(name = "ele_max", nullable = false)
    private Integer eleMax = 2;

    @Column(name = "k_min", nullable = false)
    private Integer kMin = 3;

    @Column(name = "k_max", nullable = false)
    private Integer kMax = 4;

    @Column(name = "t_max", nullable = false)
    private Integer tMax = 60;

    @Column(name = "max_intentos", nullable = false)
    private Integer maxIntentos = 5;

    @Column(name = "f_ua", nullable = false)
    private Double factorDeUmbralDeAberracion = 1.015;

    @Column(name = "f_ut", nullable = false)
    private Double factorDeUtilizacionTemporal = 5000.0;

    @Column(name = "f_de", nullable = false)
    private Double factorDeDesviacionEspacial = 2000.0;

    @Column(name = "f_do", nullable = false)
    private Double factorDeDisposicionOperacional = 3000.0;

    @Transient
    private List<String> codOrigenes = new ArrayList<>();

    public Integer getId() { return id; } public void setId(Integer id) { this.id = id; }
    public Integer getMaxDiasEntregaIntercontinental() { return maxDiasEntregaIntercontinental; } public void setMaxDiasEntregaIntercontinental(Integer maxDiasEntregaIntercontinental) { this.maxDiasEntregaIntercontinental = maxDiasEntregaIntercontinental; }
    public Integer getMaxDiasEntregaIntracontinental() { return maxDiasEntregaIntracontinental; } public void setMaxDiasEntregaIntracontinental(Integer maxDiasEntregaIntracontinental) { this.maxDiasEntregaIntracontinental = maxDiasEntregaIntracontinental; }
    public Double getMaxHorasRecojo() { return maxHorasRecojo; } public void setMaxHorasRecojo(Double maxHorasRecojo) { this.maxHorasRecojo = maxHorasRecojo; }
    public Double getMinHorasEstancia() { return minHorasEstancia; } public void setMinHorasEstancia(Double minHorasEstancia) { this.minHorasEstancia = minHorasEstancia; }
    public Double getMaxHorasEstancia() { return maxHorasEstancia; } public void setMaxHorasEstancia(Double maxHorasEstancia) { this.maxHorasEstancia = maxHorasEstancia; }
    public LocalDateTime getFechaHoraInicio() { return fechaHoraInicio; } public void setFechaHoraInicio(LocalDateTime fechaHoraInicio) { this.fechaHoraInicio = fechaHoraInicio; }
    public LocalDateTime getFechaHoraFin() { return fechaHoraFin; } public void setFechaHoraFin(LocalDateTime fechaHoraFin) { this.fechaHoraFin = fechaHoraFin; }
    public Double getDMin() { return dMin; } public void setDMin(Double dMin) { this.dMin = dMin; }
    public Integer getIMax() { return iMax; } public void setIMax(Integer iMax) { this.iMax = iMax; }
    public Integer getEleMin() { return eleMin; } public void setEleMin(Integer eleMin) { this.eleMin = eleMin; }
    public Integer getEleMax() { return eleMax; } public void setEleMax(Integer eleMax) { this.eleMax = eleMax; }
    public Integer getKMin() { return kMin; } public void setKMin(Integer kMin) { this.kMin = kMin; }
    public Integer getKMax() { return kMax; } public void setKMax(Integer kMax) { this.kMax = kMax; }
    public Integer getTMax() { return tMax; } public void setTMax(Integer tMax) { this.tMax = tMax; }
    public Integer getMaxIntentos() { return maxIntentos; } public void setMaxIntentos(Integer maxIntentos) { this.maxIntentos = maxIntentos; }
    public Double getFactorDeUmbralDeAberracion() { return factorDeUmbralDeAberracion; } public void setFactorDeUmbralDeAberracion(Double factorDeUmbralDeAberracion) { this.factorDeUmbralDeAberracion = factorDeUmbralDeAberracion; }
    public Double getFactorDeUtilizacionTemporal() { return factorDeUtilizacionTemporal; } public void setFactorDeUtilizacionTemporal(Double factorDeUtilizacionTemporal) { this.factorDeUtilizacionTemporal = factorDeUtilizacionTemporal; }
    public Double getFactorDeDesviacionEspacial() { return factorDeDesviacionEspacial; } public void setFactorDeDesviacionEspacial(Double factorDeDesviacionEspacial) { this.factorDeDesviacionEspacial = factorDeDesviacionEspacial; }
    public Double getFactorDeDisposicionOperacional() { return factorDeDisposicionOperacional; } public void setFactorDeDisposicionOperacional(Double factorDeDisposicionOperacional) { this.factorDeDisposicionOperacional = factorDeDisposicionOperacional; }
    public List<String> getCodOrigenes() { return codOrigenes; } public void setCodOrigenes(List<String> codOrigenes) { this.codOrigenes = codOrigenes; }
}
