/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       ListRequest.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto.request;

import lombok.Data;

@Data
public class ListRequest {
    private Integer page;
    private Integer size;
}