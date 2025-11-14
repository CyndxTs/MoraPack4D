/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       ClienteEntity.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.pucp.dp1.grupo4d.morapack.model.enums.EstadoUsuario;
import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "CLIENTE", schema = "morapack4d")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ClienteEntity extends UsuarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "codigo", length = 7, nullable = false, unique = true)
    private String codigo;

    @Column(name = "nombre", length = 60, nullable = false)
    private String nombre;

    @Column(name = "correo", length = 60, nullable = false, unique = true)
    private String correo;

    @Column(name = "contrasenia", length = 255, nullable = false)
    private String contrasenia;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoUsuario estado = EstadoUsuario.OFFLINE;

    @OneToMany(mappedBy = "cliente", fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<PedidoEntity> pedidos = new ArrayList<>();

    public ClienteEntity() {}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClienteEntity)) return false;
        ClienteEntity that = (ClienteEntity) o;
        return Objects.equals(codigo, that.codigo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigo);
    }

    public Integer getId() { return id; } public void setId(Integer id) { this.id = id; }
    public String getCodigo() { return codigo; } public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getNombre() { return nombre; } public void setNombre(String nombre) { this.nombre = nombre; }
    public String getCorreo() { return correo; } public void setCorreo(String correo) { this.correo = correo; }
    public String getContrasenia() { return contrasenia; } public void setContrasenia(String contrasenia) { this.contrasenia = contrasenia; }
    public EstadoUsuario getEstado() { return estado; } public void setEstado(EstadoUsuario estado) { this.estado = estado; }
    public List<PedidoEntity> getPedidos() { return pedidos; } public void setPedidos(List<PedidoEntity> pedidos) { this.pedidos = pedidos; }
}
