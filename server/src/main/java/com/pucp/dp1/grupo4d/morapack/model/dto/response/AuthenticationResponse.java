/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       AuthenticationResponse.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto.response;

import com.pucp.dp1.grupo4d.morapack.model.dto.UsuarioDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthenticationResponse extends GenericResponse {
    private UsuarioDTO user;

    public AuthenticationResponse(Boolean success, String message) {
        super(success, message);
    }

    public AuthenticationResponse(Boolean success, String message, UsuarioDTO user) {
        super(success, message);
        this.user = user;
    }
}
