/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       TipoRuta.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.enumeration;
import com.pucp.dp1.grupo4d.morapack.algorithm.Problematica;

public enum TipoRuta {
    INTRACONTINENTAL,
    INTERCONTINENTAL;

    public Long getMaxMinutosParaEntrega(Problematica problematica) {
        if(this.equals(TipoRuta.INTERCONTINENTAL)) {
            return 1440L*problematica.maxDiasDeEntregaIntercontinental;
        } else {
            return 1440L*problematica.maxDiasDeEntregaIntracontinental;
        }
    }

    public Double getMaxHorasParaEntrega(Problematica problematica) {
        if(this.equals(TipoRuta.INTERCONTINENTAL)) {
            return 24.0*problematica.maxDiasDeEntregaIntercontinental;
        } else {
            return 24.0*problematica.maxDiasDeEntregaIntracontinental;
        }
    }
}
