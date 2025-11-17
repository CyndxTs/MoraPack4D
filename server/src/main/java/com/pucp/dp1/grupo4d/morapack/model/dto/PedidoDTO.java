/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       PedidoDTO.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class PedidoDTO implements DTO {
    private String codigo;
    private String codCliente;
    private Integer cantidadSolicitada;
    private String fechaHoraGeneracion;
    private String fechaHoraExpiracion;
    private String codDestino;
    private List<LotePorRutaDTO> lotesPorRuta = new ArrayList<>();
}
