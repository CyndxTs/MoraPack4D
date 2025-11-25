/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       RegistroDTO.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RegistroDTO implements DTO {

    @EqualsAndHashCode.Include
    private String codigo;

    private Boolean sigueVigente;
    private String fechaHoraIngreso;
    private String fechaHoraEgreso;
    private String codLote;
}
