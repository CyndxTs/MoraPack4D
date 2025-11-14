/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       AuthenticationResponse.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto.response;

import com.pucp.dp1.grupo4d.morapack.model.dto.UsuarioDTO;

public class AuthenticationResponse extends GenericResponse {
    private UsuarioDTO user;

    public AuthenticationResponse(Boolean success, String message) {
        super(success, message);
        this.user = null;
    }

    public AuthenticationResponse(Boolean success, String message, UsuarioDTO user) {
        super(success, message);
        this.user = user;
    }

    public UsuarioDTO getUser() { return user; }
    public void setUser(UsuarioDTO user) { this.user = user; }
}
