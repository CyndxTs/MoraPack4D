/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       RegistroDTO.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto;

import lombok.Data;

@Data
public class RegistroDTO implements DTO {
    private String codigo;
    private String fechaHoraIngreso;
    private String fechaHoraEgreso;
    private String codLote;
}
