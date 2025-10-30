/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       ProblematicResponse.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto.response;

import com.pucp.dp1.grupo4d.morapack.algorithm.Problematica;
import com.pucp.dp1.grupo4d.morapack.model.algorithm.Aeropuerto;
import java.util.List;

public class ProblematicResponse {
    private List<Aeropuerto> origenes;
    private List<Aeropuerto> destinos;

    public ProblematicResponse(Problematica problematica) {
        this.origenes = problematica.origenes;
        this.destinos = problematica.destinos;
    }

    public List<Aeropuerto> getOrigenes() { return origenes; }
    public void setOrigenes(List<Aeropuerto> origenes) { this.origenes = origenes; }
    public List<Aeropuerto> getDestinos() { return destinos; }
    public void setDestinos(List<Aeropuerto> destinos) { this.destinos = destinos; }
}
