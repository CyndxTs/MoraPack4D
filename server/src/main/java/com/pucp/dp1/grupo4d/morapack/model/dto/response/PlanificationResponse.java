package com.pucp.dp1.grupo4d.morapack.model.dto.response;

import com.pucp.dp1.grupo4d.morapack.algorithm.Solucion;

public class PlanificationResponse {
    private boolean exito;
    private String mensaje;
    private SolutionResponse solucion;
    private ProblematicResponse problematica;

    public PlanificationResponse(boolean exito, String mensaje, SolutionResponse solucion, ProblematicResponse problematica) {
        this.exito = exito;
        this.mensaje = mensaje;
        this.solucion = solucion;
        this.problematica = problematica;
    }

    public boolean isExito() { return exito; }
    public void setExito(boolean exito) { this.exito = exito; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    public SolutionResponse getSolucion() { return solucion; }
    public void setSolucion(SolutionResponse solucion) { this.solucion = solucion; }
    public ProblematicResponse getProblematica() { return problematica; }
    public void setProblematica(ProblematicResponse problematica) { this.problematica = problematica; }

}
