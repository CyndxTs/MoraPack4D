/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       LotePorRutaDTO.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto;

public class LotePorRutaDTO {
    private String codRuta;
    private LoteDTO lote;

    public LotePorRutaDTO() {}

    public String getCodRuta() { return codRuta; }
    public void setCodRuta(String codRuta) { this.codRuta = codRuta; }
    public LoteDTO getLote() { return lote; }
    public void setLote(LoteDTO lote) { this.lote = lote; }
}
