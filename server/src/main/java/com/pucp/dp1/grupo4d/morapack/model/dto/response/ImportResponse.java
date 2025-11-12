/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       ImportResponse.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto.response;

public class ImportResponse {
    private Boolean exito;
    private String mensaje;

    public ImportResponse(boolean exito, String mensaje) {
        this.exito = exito;
        this.mensaje = mensaje;
    }

    public Boolean isExito() { return exito; }
    public void setExito(boolean exito) { this.exito = exito; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
}
