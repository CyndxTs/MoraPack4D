/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       UsuarioPrincipal.java
 [**/

package com.pucp.dp1.grupo4d.morapack.security;

import com.pucp.dp1.grupo4d.morapack.model.enumeration.TipoUsuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

/**
 * Usuario autenticado (sale del token de acceso). El administrador también tiene ROLE_TRABAJADOR:
 * puede ver todas las pantallas del trabajador.
 */
public record UsuarioPrincipal(Integer id, String codigo, TipoUsuario rol, Long idSesion) implements Principal {

    @Override
    public String getName() {
        return codigo;
    }

    public boolean es(TipoUsuario r) {
        return rol == r;
    }

    public boolean esTrabajador() {
        return rol == TipoUsuario.TRABAJADOR || rol == TipoUsuario.ADMINISTRADOR;
    }

    public List<GrantedAuthority> autoridades() {
        List<GrantedAuthority> autoridades = new ArrayList<>();
        autoridades.add(new SimpleGrantedAuthority("ROLE_" + rol));
        if (rol == TipoUsuario.ADMINISTRADOR) autoridades.add(new SimpleGrantedAuthority("ROLE_" + TipoUsuario.TRABAJADOR));
        return autoridades;
    }
}
