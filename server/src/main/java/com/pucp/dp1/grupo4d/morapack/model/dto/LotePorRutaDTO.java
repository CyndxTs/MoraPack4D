/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       LotePorRutaDTO.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto;

import lombok.Data;

@Data
public class LotePorRutaDTO implements DTO {
    private String codRuta;
    private LoteDTO lote;
}
