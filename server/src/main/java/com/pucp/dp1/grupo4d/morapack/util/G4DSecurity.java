/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       G4DSecurity.java
 [**/

package com.pucp.dp1.grupo4d.morapack.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Hash de contraseñas con BCrypt (formato "$2a$10$..."; 60 caracteres, cabe en VARCHAR(255)).
 * Nunca se guarda la contraseña en texto plano: al iniciar sesión se compara con 'verificar'.
 */
public final class G4DSecurity {
    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder(10);

    private G4DSecurity() {}

    public static String hashear(String contrasenia) {
        return ENCODER.encode(contrasenia);
    }

    public static boolean verificar(String contrasenia, String hash) {
        if (contrasenia == null || hash == null || hash.isBlank()) return false;
        return ENCODER.matches(contrasenia, hash);
    }
}
