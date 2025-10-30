/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       SignOutRequest.java
 [**/


package com.pucp.dp1.grupo4d.morapack.model.dto.request;

public class SignOutRequest {
    private String correo;
    private String tipoUsuario;

    public SignOutRequest(String correo, String tipoUsuario) {
        this.correo = correo;
        this.tipoUsuario = tipoUsuario;
    }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getTipoUsuario() { return tipoUsuario; }
    public void setTipoUsuario(String tipoUsuario) { this.tipoUsuario = tipoUsuario; }
}
