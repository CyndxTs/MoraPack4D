/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       UsuarioEntity.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.db;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "USUARIO", schema = "morapack4d")
public class UsuarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "codigo", length = 7, nullable = false, unique = true)
    private String codigo;

    @Column(name = "nombre", length = 50, nullable = false)
    private String nombre;

    @Column(name = "correo", length = 40, nullable = false, unique = true)
    private String correo;

    @Column(name = "contrasenia", length = 255, nullable = false)
    private String contrasenia;

    @Column(name = "tipo", nullable = false, length = 20, columnDefinition = "varchar(20) default 'CLIENTE'")
    private String tipo;

    @OneToMany(mappedBy = "cliente", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<PedidoEntity> pedidos = new ArrayList<>();

    public UsuarioEntity() {}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UsuarioEntity)) return false;
        UsuarioEntity that = (UsuarioEntity) o;
        return Objects.equals(codigo, that.codigo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigo);
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getContrasenia() { return contrasenia; }
    public void setContrasenia(String contrasenia) { this.contrasenia = contrasenia; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public List<PedidoEntity> getPedidos() { return pedidos; }
    public void setPedidos(List<PedidoEntity> pedidos) { this.pedidos = pedidos; }
}
