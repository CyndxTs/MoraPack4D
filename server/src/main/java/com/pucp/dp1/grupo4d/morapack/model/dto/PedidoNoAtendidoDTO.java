/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       PedidoNoAtendidoDTO.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PedidoNoAtendidoDTO implements DTO {
    @EqualsAndHashCode.Include
    private String codigo;
    private String codCliente;
    private String codDestino;
    private Integer cantidadSolicitada;
    private String fechaHoraGeneracion;
    private String motivo;          // SIN_RUTA | EVENTO
}
