/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       RegistroDTO.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto;

import java.time.LocalDateTime;

public class RegistroDTO {
    private String codigo;
    private LocalDateTime fechaHoraIngreso;
    private LocalDateTime fechaHoraEgreso;
    private String codLote;

    public RegistroDTO() {}

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public LocalDateTime getFechaHoraIngreso() { return fechaHoraIngreso; }
    public void setFechaHoraIngreso(LocalDateTime fechaHoraIngreso) { this.fechaHoraIngreso = fechaHoraIngreso; }
    public LocalDateTime getFechaHoraEgreso() { return fechaHoraEgreso; }
    public void setFechaHoraEgreso(LocalDateTime fechaHoraEgreso) { this.fechaHoraEgreso = fechaHoraEgreso; }
    public String getCodLote() { return codLote; }
    public void setCodLote(String codLote) { this.codLote = codLote; }
}
