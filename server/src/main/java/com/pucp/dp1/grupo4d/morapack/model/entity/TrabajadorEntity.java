/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       TrabajadorEntity.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.TipoUsuario;
import jakarta.persistence.*;

@Entity
@Table(name = "TRABAJADOR")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class TrabajadorEntity extends UsuarioEntity {
    public TrabajadorEntity() {}

    @Override
    public TipoUsuario getTipoUsuario() { return TipoUsuario.TRABAJADOR; }
}
