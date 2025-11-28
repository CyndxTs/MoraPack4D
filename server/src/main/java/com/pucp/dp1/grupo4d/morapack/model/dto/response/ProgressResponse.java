/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       ProgressResponse.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProgressResponse extends GenericResponse {
    Integer completados;
    Integer total;

    public ProgressResponse(Boolean exito, String mensaje) {
        super(exito, mensaje);
        this.completados = 0;
        this.total = 0;
    }

    public ProgressResponse(Boolean exito, String mensaje, Integer completados, Integer total) {
        super(exito, mensaje);
        this.completados = completados;
        this.total = total;
    }
}
