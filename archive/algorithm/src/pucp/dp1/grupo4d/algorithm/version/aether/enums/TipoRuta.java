/**]
 >> Project:    MoraPack
 >> Version:    Aether
 >> Author:     Grupo 4D
 >> File:       TipoRuta.java 
[**/

package pucp.dp1.grupo4d.algorithm.version.aether.enums;

import pucp.dp1.grupo4d.algorithm.version.aether.Problematica;

public enum TipoRuta {
    INTRACONTINENTAL,
    INTERCONTINENTAL;

    public Long getMaxMinutosParaEntrega() {
        if(this.equals(TipoRuta.INTERCONTINENTAL)) {
            return 1440L*Problematica.MAX_DIAS_ENTREGA_INTERCONTINENTAL;
        } else {
            return 1440L*Problematica.MAX_DIAS_ENTREGA_INTRACONTINENTAL;
        }
    }

    public Double getMaxHorasParaEntrega() {
        if(this.equals(TipoRuta.INTERCONTINENTAL)) {
            return 24.0*Problematica.MAX_DIAS_ENTREGA_INTERCONTINENTAL;
        } else {
            return 24.0*Problematica.MAX_DIAS_ENTREGA_INTRACONTINENTAL;
        }
    }
}
