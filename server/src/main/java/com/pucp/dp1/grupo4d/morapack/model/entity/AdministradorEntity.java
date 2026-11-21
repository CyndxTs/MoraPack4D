/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       AdministradorEntity.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.TipoUsuario;
import jakarta.persistence.*;

@Entity
@Table(name = "ADMINISTRADOR")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class AdministradorEntity extends UsuarioEntity {
    public AdministradorEntity() {}

    @Override
    public TipoUsuario getTipoUsuario() { return TipoUsuario.ADMINISTRADOR; }
}
