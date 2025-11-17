/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       VueloDTO.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto;

import lombok.Data;

@Data
public class VueloDTO implements DTO {
    private String codigo;
    private String fechaHoraSalida;
    private String fechaHoraLlegada;
    private Integer capacidadOcupada;
    private PlanDTO plan;
}
