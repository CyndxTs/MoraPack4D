/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       SignUpRequest.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto.request;

public class SignUpRequest {
    private String nombre;
    private String correo;
    private String contrasenia;
    private String tipoUsuario;

    public SignUpRequest(String nombre, String correo, String contrasenia, String tipoUsuario) {
        this.nombre = nombre;
        this.correo = correo;
        this.contrasenia = contrasenia;
        this.tipoUsuario = tipoUsuario;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getContrasenia() { return contrasenia; }
    public void setContrasenia(String contrasenia) { this.contrasenia = contrasenia; }
    public String getTipoUsuario() { return tipoUsuario; }
    public void setTipoUsuario(String tipoUsuario) { this.tipoUsuario = tipoUsuario; }
}