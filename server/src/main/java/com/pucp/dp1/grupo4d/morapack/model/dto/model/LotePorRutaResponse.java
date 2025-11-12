/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       LotePorRutaResponse.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto.model;

import com.pucp.dp1.grupo4d.morapack.model.entity.LoteEntity;

public class LotePorRutaResponse {
    private String codRuta;
    private LoteResponse lote;

    public LotePorRutaResponse(String codRuta, LoteEntity lote) {
        this.codRuta = codRuta;
        this.lote = new LoteResponse(lote);
    }

    public String getCodRuta() { return codRuta; }
    public void setCodRuta(String codRuta) { this.codRuta = codRuta; }
    public LoteResponse getLote() { return lote; }
    public void setLote(LoteResponse lote) { this.lote = lote; }
}

