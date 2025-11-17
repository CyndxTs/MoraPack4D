/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       LoteDTO.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto;

import lombok.Data;

@Data
public class LoteDTO implements DTO {
    private String codigo;
    private Integer tamanio;
}
