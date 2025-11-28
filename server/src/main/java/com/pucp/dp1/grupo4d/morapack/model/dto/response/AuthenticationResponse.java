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
    private UsuarioDTO usuario;

    public AuthenticationResponse(Boolean exito, String mensaje) {
        super(exito, mensaje);
    }

    public AuthenticationResponse(Boolean exito, String mensaje, UsuarioDTO usuario) {
        super(exito, mensaje);
        this.usuario = usuario;
    }
}
