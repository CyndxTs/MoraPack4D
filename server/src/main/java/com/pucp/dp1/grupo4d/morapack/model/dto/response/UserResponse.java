package com.pucp.dp1.grupo4d.morapack.model.dto.response;

import com.pucp.dp1.grupo4d.morapack.model.entity.AdministradorEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.ClienteEntity;

public class UserResponse {
    private Integer id;
    private String codigo;
    private String nombre;
    private String correo;
    private String tipoUsuario;
    private String estado;

    public UserResponse(ClienteEntity cliente) {
        this.id = cliente.getId();
        this.codigo = cliente.getCodigo();
        this.nombre = cliente.getNombre();
        this.correo = cliente.getCorreo();
        this.estado = cliente.getEstado().toString();
        this.tipoUsuario = "CLIENTE";
    }

    public UserResponse(AdministradorEntity administrador) {
        this.id = administrador.getId();
        this.codigo = administrador.getCodigo();
        this.nombre = administrador.getNombre();
        this.correo = administrador.getCorreo();
        this.estado = administrador.getEstado().toString();
        this.tipoUsuario = "ADMINISTRADOR";
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
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
