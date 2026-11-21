/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       AuthenticationResponse.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto.response;

import com.pucp.dp1.grupo4d.morapack.model.dto.UsuarioDTO;
import lombok.Getter;
import lombok.Setter;

/**
 * Respuesta de los flujos de autenticación.
 *  - Sesión iniciada: usuario + tokenAcceso (10 min) + tokenRefresco + expiraAcceso / expiraSesion (epoch ms).
 *  - Código enviado : correo + segundosParaReenvio (sin tokens).
 *  - Google, primer ingreso sin datos completos: requiereDatos = true + usuario con lo que dio Google (sin tokens).
 */
@Getter
@Setter
public class AuthenticationResponse extends GenericResponse {
    private UsuarioDTO usuario;
    private String tokenAcceso;
    private String tokenRefresco;
    private Long expiraAcceso;
    private Long expiraSesion;
    private Boolean requiereDatos;
    private String correo;
    private Integer segundosParaReenvio;

    public AuthenticationResponse(Boolean exito, String mensaje) {
        super(exito, mensaje);
    }

    public AuthenticationResponse(Boolean exito, String mensaje, UsuarioDTO usuario) {
        super(exito, mensaje);
        this.usuario = usuario;
    }
}
