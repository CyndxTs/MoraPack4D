/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       PlanificationRequest.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto.request;

public class PlanificationRequest {
    private Boolean reparametrizar;
    private ParametersRequest parameters;

    public PlanificationRequest(Boolean reparametrizar, ParametersRequest parameters) {
        this.reparametrizar = reparametrizar;
        this.parameters = parameters;
    }

    public Boolean getReparametrizar() { return reparametrizar; }
    public void setReparametrizar(Boolean reparametrizar) { this.reparametrizar = reparametrizar; }
    public ParametersRequest getParameters() { return parameters; }
    public void setParameters(ParametersRequest parameters) { this.parameters = parameters; }
}
