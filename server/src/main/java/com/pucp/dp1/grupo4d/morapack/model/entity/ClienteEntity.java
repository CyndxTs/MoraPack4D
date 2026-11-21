/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       ClienteEntity.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.TipoUsuario;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "CLIENTE")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ClienteEntity extends UsuarioEntity {
    @OneToMany(mappedBy = "cliente")
    @OrderBy("fechaHoraGeneracion ASC")
    private List<PedidoEntity> pedidos = new ArrayList<>();

    public ClienteEntity() {}

    @Override
    public TipoUsuario getTipoUsuario() { return TipoUsuario.CLIENTE; }

    public List<PedidoEntity> getPedidos() { return pedidos; }
    public void setPedidos(List<PedidoEntity> pedidos) { this.pedidos = pedidos; }
}
