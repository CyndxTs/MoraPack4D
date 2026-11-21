/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       PaisDTO.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PaisDTO implements DTO {
    @EqualsAndHashCode.Include
    private String nombre;
    private Integer husoHorario;
    private String continente;
}
