/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       ParametrosDTO.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class ParametrosDTO implements DTO {
    private Integer maxDiasEntregaIntracontinental;
    private Integer maxDiasEntregaIntercontinental;
    private Double maxHorasRecojo;
    private Double minHorasEstancia;
    private Double maxHorasEstancia;
    private String fechaHoraInicioPlanificacion;
    private String fechaHoraFinPlanificacion;
    private List<String> codOrigenes = new ArrayList<>();

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
}
