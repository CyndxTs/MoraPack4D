/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       PlanificationRequest.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto.request;

import com.pucp.dp1.grupo4d.morapack.model.dto.ParametrosDTO;

public class PlanificationRequest {
    private Boolean replanificar;
    private Boolean guardarPlanificacion;
    private Boolean reparametrizar;
    private ParametrosDTO parameters;
    private Boolean guardarParametrizacion;

    public PlanificationRequest(Boolean replanificar, Boolean guardarPlanificacion, Boolean reparametrizar,
                                ParametrosDTO parameters, Boolean guardarParametrizacion) {
        this.replanificar = replanificar;
        this.guardarPlanificacion = guardarPlanificacion;
        this.reparametrizar = reparametrizar;
        this.parameters = parameters;
        this.guardarParametrizacion = guardarParametrizacion;
    }

    public Boolean getReplanificar() { return replanificar; }
    public void setReplanificar(Boolean replanificar) { this.replanificar = replanificar; }
    public Boolean getGuardarPlanificacion() { return guardarPlanificacion; }
    public void setGuardarPlanificacion(Boolean guardarPlanificacion) { this.guardarPlanificacion = guardarPlanificacion; }
    public Boolean getReparametrizar() { return reparametrizar; }
    public void setReparametrizar(Boolean reparametrizar) { this.reparametrizar = reparametrizar; }
    public ParametrosDTO getParameters() { return parameters; }
    public void setParameters(ParametrosDTO parameters) { this.parameters = parameters; }
    public Boolean getGuardarParametrizacion() { return guardarParametrizacion; }
    public void setGuardarParametrizacion(Boolean guardarParametrizacion) { this.guardarParametrizacion = guardarParametrizacion; }
}
