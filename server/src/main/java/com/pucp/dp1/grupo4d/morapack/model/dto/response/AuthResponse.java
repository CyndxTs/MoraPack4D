package com.pucp.dp1.grupo4d.morapack.model.dto.response;

import com.pucp.dp1.grupo4d.morapack.util.G4D;

public class AuthResponse {
    private String token;
    private UserResponse usuario;
    private String mensaje;

    public AuthResponse(UserResponse usuario, String mensaje) {
        this.token = G4D.Generator.getUniqueString("TOK");
        this.usuario = usuario;
        this.mensaje = mensaje;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public UserResponse getUsuario() { return usuario; }
    public void setUsuario(UserResponse usuario) { this.usuario = usuario; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
}
