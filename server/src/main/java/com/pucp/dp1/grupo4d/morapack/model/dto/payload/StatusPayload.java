/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       StatusPayload.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto.payload;

import com.pucp.dp1.grupo4d.morapack.model.enumeration.EstadoEjecucion;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.EstadoFinalizacion;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatusPayload {
    private EstadoEjecucion estadoEjecucion;
    private EstadoFinalizacion estadoFinalizacion;

    public StatusPayload(EstadoEjecucion estadoEjecucion) {
        this.estadoEjecucion = estadoEjecucion;
    }
}
