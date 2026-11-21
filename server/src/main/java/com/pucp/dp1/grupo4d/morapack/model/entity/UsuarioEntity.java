/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       UsuarioEntity.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.EstadoUsuario;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.TipoUsuario;
import jakarta.persistence.*;
import java.util.Objects;

/**
 * Tabla única USUARIO para clientes, trabajadores y administradores (el rol decide los permisos).
 * Códigos: CLIENTE '0000001' | TRABAJADOR 'TRA0001' | ADMINISTRADOR 'ADM0001' (ver CodigoUsuario).
 * 'contrasenia' es null cuando la cuenta se creó con Google y aún no definió una contraseña.
 */
@Entity
@Table(name = "USUARIO")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class UsuarioEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoUsuario rol = TipoUsuario.CLIENTE;

    @Column(nullable = false, length = 60)
    private String nombres;

    @Column(length = 60)
    private String apellidos;

    @Column(nullable = false, unique = true, length = 80)
    private String correo;

    @Column(length = 255)
    private String contrasenia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoUsuario estado = EstadoUsuario.OFFLINE;

    @ManyToOne
    @JoinColumn(name = "id_pais", nullable = false)
    private PaisEntity pais;

    public UsuarioEntity() {}

    public TipoUsuario getTipoUsuario() { return rol; }

    public boolean tieneContrasenia() {
        return contrasenia != null && !contrasenia.isBlank();
    }

    public String getNombreCompleto() {
        return (apellidos != null && !apellidos.isBlank()) ? String.format("%s %s", nombres, apellidos).trim() : nombres;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UsuarioEntity that = (UsuarioEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public TipoUsuario getRol() { return rol; }
    public void setRol(TipoUsuario rol) { this.rol = rol; }
    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }
    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getContrasenia() { return contrasenia; }
    public void setContrasenia(String contrasenia) { this.contrasenia = contrasenia; }
    public EstadoUsuario getEstado() { return estado; }
    public void setEstado(EstadoUsuario estado) { this.estado = estado; }
    public PaisEntity getPais() { return pais; }
    public void setPais(PaisEntity pais) { this.pais = pais; }
}
