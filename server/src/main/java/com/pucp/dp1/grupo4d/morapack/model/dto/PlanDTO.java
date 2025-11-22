/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       PlanDTO.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PlanDTO implements DTO {

    @EqualsAndHashCode.Include
    private String codigo;

    private String codOrigen;
    private String codDestino;
    private String HoraSalida;
    private String HoraLlegada;
    private Integer capacidad;
    private Double duracion;
    private Double distancia;
    private List<EventoDTO> eventos;
}
