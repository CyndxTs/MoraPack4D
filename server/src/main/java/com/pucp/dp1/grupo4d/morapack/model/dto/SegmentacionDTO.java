/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       SegmentacionDTO.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class SegmentacionDTO implements DTO {
    @EqualsAndHashCode.Include
    private String codigo;

    private String fechaHoraAplicacion;
    private String fechaHoraSustitucion;
    private List<LotePorRutaDTO> lotesPorRuta;
}
