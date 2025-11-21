/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       VueloDTO.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class VueloDTO implements DTO {

    @EqualsAndHashCode.Include
    private String codigo;

    private String fechaHoraSalida;
    private String fechaHoraLlegada;
    private Integer capacidadOcupada;
    private Integer capacidadMaxima;
    private String codOrigen;
    private String codDestino;
    private Double duracion;
    private Double distancia;
}
