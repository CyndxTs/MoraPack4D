/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       CodigoUsuario.java
 [**/

package com.pucp.dp1.grupo4d.morapack.util;

import com.pucp.dp1.grupo4d.morapack.model.enumeration.TipoUsuario;
import java.util.Collection;
import java.util.regex.Pattern;

/**
 * Formato de código por rol:
 *  - CLIENTE       : 7 dígitos        ('0000001' .. '9999999')
 *  - TRABAJADOR    : 'TRA' + 4 dígitos ('TRA0001' .. 'TRA9999')
 *  - ADMINISTRADOR : 'ADM' + 4 dígitos ('ADM0001' .. 'ADM9999')
 * El siguiente código es (máximo numérico válido del rol) + 1. Los códigos mal formados se ignoran.
 */
public final class CodigoUsuario {
    private CodigoUsuario() {}

    public static String prefijo(TipoUsuario rol) {
        return switch (rol) {
            case CLIENTE -> "";
            case TRABAJADOR -> "TRA";
            case ADMINISTRADOR -> "ADM";
        };
    }

    public static int digitos(TipoUsuario rol) {
        return (rol == TipoUsuario.CLIENTE) ? 7 : 4;
    }

    public static int maximo(TipoUsuario rol) {
        return (int) Math.pow(10, digitos(rol)) - 1;
    }

    private static Pattern patron(TipoUsuario rol) {
        return Pattern.compile("^" + prefijo(rol) + "(\\d{" + digitos(rol) + "})$");
    }

    public static boolean esValido(TipoUsuario rol, String codigo) {
        return codigo != null && patron(rol).matcher(codigo).matches();
    }

    // Máximo numérico entre los códigos bien formados del rol (0 si no hay ninguno)
    public static int maximoActual(TipoUsuario rol, Collection<String> codigos) {
        Pattern p = patron(rol);
        int max = 0;
        for (String codigo : codigos) {
            if (codigo == null) continue;
            var m = p.matcher(codigo.trim());
            if (m.matches()) max = Math.max(max, Integer.parseInt(m.group(1)));
        }
        return max;
    }

    public static String formatear(TipoUsuario rol, int numero) {
        if (numero < 1 || numero > maximo(rol)) {
            throw new IllegalArgumentException(String.format("Número de código fuera de rango para %s: %d", rol, numero));
        }
        return prefijo(rol) + String.format("%0" + digitos(rol) + "d", numero);
    }
}
