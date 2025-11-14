/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       PlanificationRequest.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto.request;

public class PlanificationRequest {
    private Boolean guardarSolucion;
    private Boolean replanificar;
    private Boolean reparametrizar;
    private ReparameterizationRequest parameters;

    public PlanificationRequest(Boolean guardarSolucion, Boolean replanificar, Boolean reparametrizar, ReparameterizationRequest parameters) {
        this.guardarSolucion = guardarSolucion;
        this.replanificar = replanificar;
        this.reparametrizar = reparametrizar;
        this.parameters = parameters;
    }

    public Boolean getGuardarSolucion() { return guardarSolucion; }
    public void setGuardarSolucion(Boolean guardarSolucion) { this.guardarSolucion = guardarSolucion; }
    public Boolean getReplanificar() { return replanificar; }
    public void setReplanificar(Boolean replanificar) { this.replanificar = replanificar; }
    public Boolean getReparametrizar() { return reparametrizar; }
    public void setReparametrizar(Boolean reparametrizar) { this.reparametrizar = reparametrizar; }
    public ReparameterizationRequest getParameters() { return parameters; }
    public void setParameters(ReparameterizationRequest parameters) { this.parameters = parameters; }
}
