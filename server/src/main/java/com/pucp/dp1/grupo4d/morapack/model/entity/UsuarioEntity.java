/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       UsuarioEntity.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.entity;

import com.pucp.dp1.grupo4d.morapack.model.enumeration.EstadoUsuario;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.TipoUsuario;
import jakarta.persistence.*;
import java.util.Objects;

/**
 * Atributos comunes de CLIENTE, TRABAJADOR y ADMINISTRADOR.
 * Cada subclase sigue teniendo su propia tabla (no hay herencia en BD), pero se
 * pueden tratar de forma polimórfica (mapper, login, pools) a través de este tipo.
 */
@MappedSuperclass
public abstract class UsuarioEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(nullable = false, length = 60)
    private String nombres;

    @Column(nullable = false, length = 60)
    private String apellidos;

    @Column(nullable = false, unique = true, length = 80)
    private String correo;

    @Column(nullable = false, length = 255)
    private String contrasenia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoUsuario estado = EstadoUsuario.OFFLINE;

    @ManyToOne
    @JoinColumn(name = "id_pais", nullable = false)
    private PaisEntity pais;

    protected UsuarioEntity() {}

    public abstract TipoUsuario getTipoUsuario();

    public String getNombreCompleto() {
        return String.format("%s %s", nombres, apellidos).trim();
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
        return Objects.hash(getClass(), id);
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
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
