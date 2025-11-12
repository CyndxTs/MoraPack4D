/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       TipoRuta.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.enums;
import com.pucp.dp1.grupo4d.morapack.algorithm.Problematica;

public enum TipoRuta {
    INTRACONTINENTAL(Problematica.MAX_DIAS_ENTREGA_INTRACONTINENTAL),
    INTERCONTINENTAL(Problematica.MAX_DIAS_ENTREGA_INTERCONTINENTAL);

    private final Long maxMinutosParaEntrega;
    private final Double maxHorasParaEntrega;

    TipoRuta(Integer maxDiasParaEntrega) {
        this.maxMinutosParaEntrega = 1440L*maxDiasParaEntrega;
        this.maxHorasParaEntrega = 24.0*maxDiasParaEntrega;
    }

    public Long getMaxMinutosParaEntrega() {
        return maxMinutosParaEntrega;
    }

    public Double getMaxHorasParaEntrega() {
        return maxHorasParaEntrega;
    }
}
