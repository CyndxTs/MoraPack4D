package com.pucp.dp1.grupo4d.morapack.model.dto.response;

public class PlanificationResponse {
    private boolean exito;
    private String mensaje;

    public PlanificationResponse(boolean exito, String mensaje) {
        this.exito = exito;
        this.mensaje = mensaje;
    }

    public boolean isExito() { return exito; }
    public void setExito(boolean exito) { this.exito = exito; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
}
