/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       RegistroDTO.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto;

public class RegistroDTO implements DTO {
    private String codigo;
    private String fechaHoraIngreso;
    private String fechaHoraEgreso;
    private String codLote;

    public RegistroDTO() {}

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getFechaHoraIngreso() { return fechaHoraIngreso; }
    public void setFechaHoraIngreso(String fechaHoraIngreso) { this.fechaHoraIngreso = fechaHoraIngreso; }
    public String getFechaHoraEgreso() { return fechaHoraEgreso; }
    public void setFechaHoraEgreso(String fechaHoraEgreso) { this.fechaHoraEgreso = fechaHoraEgreso; }
    public String getCodLote() { return codLote; }
    public void setCodLote(String codLote) { this.codLote = codLote; }
}
