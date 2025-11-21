/**]
 >> Project:    MoraPack
 >> Version:    Solstice
 >> Author:     Grupo 4D
 >> File:       TipoRuta.java 
[**/

package pucp.dp1.grupo4d.algorithm.version.solstice.enums;

import pucp.dp1.grupo4d.algorithm.version.solstice.Problematica;

public enum TipoRuta {
    INTRACONTINENTAL(Problematica.MAX_DIAS_ENTREGA_INTRACONTINENTAL),
    INTERCONTINENTAL(Problematica.MAX_DIAS_ENTREGA_INTERCONTINENTAL);

    private final Long maxMinutosParaEntrega;
    private final Double maxHorasParaEntrega;

    TipoRuta(Integer maxDiasParaEntrega) {
        this.maxMinutosParaEntrega = (long)(60*24*maxDiasParaEntrega);
        this.maxHorasParaEntrega = 24.0*maxDiasParaEntrega;
    }

    public Long getMaxMinutosParaEntrega() {
        return maxMinutosParaEntrega;
    }

    public Double getMaxHorasParaEntrega() {
        return maxHorasParaEntrega;
    }
}
