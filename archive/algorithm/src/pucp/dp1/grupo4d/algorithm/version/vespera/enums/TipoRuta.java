/**]
 >> Project:    MoraPack
 >> Version:    Vespera
 >> Author:     Grupo 4D
 >> File:       TipoRuta.java
[**/

package pucp.dp1.grupo4d.algorithm.version.vespera.enums;

import pucp.dp1.grupo4d.algorithm.version.vespera.Problematica;

public enum TipoRuta {
    INTRACONTINENTAL(Problematica.MAX_DIAS_ENTREGA_INTRACONTINENTAL),
    INTERCONTINENTAL(Problematica.MAX_DIAS_ENTREGA_INTERCONTINENTAL);

    private final Double maxHorasParaEntrega;

    TipoRuta(Integer maxDiasParaEntrega) {
        this.maxHorasParaEntrega = 24.0*maxDiasParaEntrega;
    }

    public Double getMaxHorasParaEntrega() {
        return maxHorasParaEntrega;
    }
}
