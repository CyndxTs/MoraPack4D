/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       LoteDTO.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class LoteDTO implements DTO {

    @EqualsAndHashCode.Include
    private String codigo;

    private Integer tamanio;
}
