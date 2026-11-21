/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       ClientOrderRequest.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto.request;

import lombok.Data;

// Pedido creado por el propio cliente: el cliente sale de la sesión y la fecha de generación es "ahora"
@Data
public class ClientOrderRequest {
    private String codDestino;
    private Integer cantidadSolicitada;
}
