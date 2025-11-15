/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       LoteDTO.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto;

public class LoteDTO implements DTO {
    private String codigo;
    private Integer tamanio;

    public LoteDTO() {}

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public Integer getTamanio() { return tamanio; }
    public void setTamanio(Integer tamanio) { this.tamanio = tamanio; }
}
