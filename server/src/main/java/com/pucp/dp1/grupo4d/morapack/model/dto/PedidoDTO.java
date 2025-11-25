/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       PedidoDTO.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PedidoDTO implements DTO {

    @EqualsAndHashCode.Include
    private String codigo;

    private String codCliente;
    private Integer cantidadSolicitada;
    private Boolean fueAtendido;
    private String fechaHoraGeneracion;
    private String fechaHoraExpiracion;
    private String codDestino;
    private List<SegmentacionDTO> segmentaciones = new ArrayList<>();
}
