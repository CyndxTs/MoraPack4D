/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       SesionActual.java
 [**/

package com.pucp.dp1.grupo4d.morapack.security;

import com.pucp.dp1.grupo4d.morapack.model.enumeration.TipoUsuario;
import com.pucp.dp1.grupo4d.morapack.model.exception.G4DAuthException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.Optional;

// Usuario de la petición HTTP en curso (no usar dentro de tareas @Async: allí no hay petición)
public final class SesionActual {
    private SesionActual() {}

    public static Optional<UsuarioPrincipal> obtener() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UsuarioPrincipal principal) return Optional.of(principal);
        return Optional.empty();
    }

    public static UsuarioPrincipal requerir() {
        return obtener().orElseThrow(() -> new G4DAuthException(G4DAuthException.NO_AUTENTICADO, "Inicia sesión para continuar."));
    }

    public static boolean esAdministrador() {
        return obtener().map(p -> p.rol() == TipoUsuario.ADMINISTRADOR).orElse(false);
    }
}
