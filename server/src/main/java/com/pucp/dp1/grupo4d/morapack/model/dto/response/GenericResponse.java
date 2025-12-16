/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       GenericResponse.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto.response;

import com.pucp.dp1.grupo4d.morapack.util.G4DUtility;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GenericResponse {
    private String token;
    private Boolean exito;
    private String mensaje;

    public GenericResponse(Boolean exito, String mensaje) {
        this.token = G4DUtility.Generator.getUniqueString("TOK");
        this.exito = exito;
        this.mensaje = mensaje;
    }
}
