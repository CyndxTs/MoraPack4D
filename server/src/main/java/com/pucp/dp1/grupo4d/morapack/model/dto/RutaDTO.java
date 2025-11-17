/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       RutaDTO.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class RutaDTO implements DTO {
    private String codigo;
    private Double duracion;
    private Double distancia;
    private String fechaHoraSalida;
    private String fechaHoraLlegada;
    private String tipo;
    private String codOrigen;
    private String codDestino;
    private List<String> codVuelos = new ArrayList<>();
}
