/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       PlanDTO.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto;

import lombok.Data;

@Data
public class PlanDTO implements DTO {
    private String codigo;
    private String codOrigen;
    private String codDestino;
    private String HoraSalida;
    private String HoraLlegada;
    private Integer capacidad;
    private Double duracion;
    private Double distancia;
}
