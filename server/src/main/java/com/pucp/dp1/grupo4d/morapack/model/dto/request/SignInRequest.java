package com.pucp.dp1.grupo4d.morapack.model.dto.request;

public class SignInRequest {
    private String correo;
    private String contrasenia;
    private String tipoUsuario;

    public SignInRequest(String correo, String contrasenia, String tipoUsuario) {
        this.correo = correo;
        this.contrasenia = contrasenia;
        this.tipoUsuario = tipoUsuario;
    }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getContrasenia() { return contrasenia; }
    public void setContrasenia(String contrasenia) { this.contrasenia = contrasenia; }
    public String getTipoUsuario() { return tipoUsuario; }
    public void setTipoUsuario(String tipoUsuario) { this.tipoUsuario = tipoUsuario; }
}