/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       G4DAuthException.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.exception;

/**
 * Error de autenticación (HTTP 401). 'codigo' le dice al front qué hacer:
 *  - TOKEN_EXPIRADO / TOKEN_INVALIDO: renovar el token de acceso con el de refresco.
 *  - SESION_EXPIRADA: la sesión de 12 h terminó o fue cerrada: volver a iniciar sesión.
 *  - NO_AUTENTICADO: la petición no trae token.
 */
public class G4DAuthException extends G4DException {
    public static final String TOKEN_EXPIRADO = "TOKEN_EXPIRADO";
    public static final String TOKEN_INVALIDO = "TOKEN_INVALIDO";
    public static final String SESION_EXPIRADA = "SESION_EXPIRADA";
    public static final String NO_AUTENTICADO = "NO_AUTENTICADO";

    private final String codigo;

    public G4DAuthException(String codigo, String message) {
        super(message);
        this.codigo = codigo;
    }

    public String getCodigo() { return codigo; }
}
