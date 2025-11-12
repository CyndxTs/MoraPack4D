/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       LoteResponse.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto.model;

import com.pucp.dp1.grupo4d.morapack.model.entity.LoteEntity;

public class LoteResponse {
    private String codigo;
    private Integer tamanio;

    public LoteResponse(LoteEntity lote) {
        this.codigo = lote.getCodigo();
        this.tamanio = lote.getTamanio();
    }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public Integer getTamanio() { return tamanio; }
    public void setTamanio(Integer tamanio) { this.tamanio = tamanio; }
}
