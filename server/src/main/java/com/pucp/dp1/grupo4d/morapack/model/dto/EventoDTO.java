/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       EventoDTO.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class EventoDTO implements DTO {
    @EqualsAndHashCode.Include
    private String codigo;

    private String tipo;
    private String fechaHoraInicio;
    private String fechaHoraFin;
    private String horaSalidaReprogramada;
    private String horaLlegadaReprogramada;
}
