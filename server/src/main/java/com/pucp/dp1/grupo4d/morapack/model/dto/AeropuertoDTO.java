/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       AeropuertoDTO.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AeropuertoDTO implements DTO {
    @EqualsAndHashCode.Include
    private String codigo;

    private String ciudad;
    private String pais;
    private String continente;
    private String alias;
    private Integer husoHorario;
    private Integer capacidad;
    private Double latitud;
    private Double longitud;
    private Boolean esSede;
    private List<RegistroDTO> registros = new ArrayList<>();
}
