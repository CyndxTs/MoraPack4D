/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       AuthenticationResponse.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto.response;

import com.pucp.dp1.grupo4d.morapack.util.G4D;

public class AuthenticationResponse {
    private String token;
    private String mensaje;
    private UserResponse usuario;

    public AuthenticationResponse(String mensaje, UserResponse usuario) {
        this.token = G4D.Generator.getUniqueString("TOK");
        this.mensaje = mensaje;
        this.usuario = usuario;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    public UserResponse getUsuario() { return usuario; }
    public void setUsuario(UserResponse usuario) { this.usuario = usuario; }
}
