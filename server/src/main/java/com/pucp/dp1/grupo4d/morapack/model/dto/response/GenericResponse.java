/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       GenericResponse.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto.response;

import com.pucp.dp1.grupo4d.morapack.util.G4D;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GenericResponse {
    private String token;
    private Boolean success;
    private String message;

    public GenericResponse(Boolean success, String message) {
        this.token = G4D.Generator.getUniqueString("TOK");
        this.success = success;
        this.message = message;
    }
}
