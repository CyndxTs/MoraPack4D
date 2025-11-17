/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       AeropuertoDTO.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class AeropuertoDTO implements DTO {
    private String codigo;
    private String ciudad;
    private String pais;
    private String continente;
    private String alias;
    private Integer husoHorario;
    private Integer capacidad;
    private Double latitudDEC;
    private Double longitudDEC;
    private Boolean esSede;
    private List<RegistroDTO> registros = new ArrayList<>();
}
