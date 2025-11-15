/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       UsuarioDTO.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto;

public class UsuarioDTO implements DTO {
    private String codigo;
    private String nombre;
    private String correo;
    private String tipoUsuario;
    private String estado;

    public UsuarioDTO() {}

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getTipoUsuario() { return tipoUsuario; }
    public void setTipoUsuario(String tipoUsuario) { this.tipoUsuario = tipoUsuario; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
