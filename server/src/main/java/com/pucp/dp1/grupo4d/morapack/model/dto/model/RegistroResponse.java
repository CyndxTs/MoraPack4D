/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       RegistroResponse.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto.model;

import java.time.LocalDateTime;
import com.pucp.dp1.grupo4d.morapack.model.entity.RegistroEntity;

public class RegistroResponse {
    private String codigo;
    private LocalDateTime fechaHoraIngreso;
    private LocalDateTime fechaHoraEgreso;
    private String codLote;
    private String codPedido;

    public RegistroResponse(RegistroEntity registro) {
        this.codigo = registro.getCodigo();
        this.fechaHoraIngreso = registro.getFechaHoraIngresoUTC();
        this.fechaHoraEgreso = registro.getFechaHoraEgresoUTC();
        this.codLote = registro.getLote().getCodigo();
        this.codPedido = registro.getLote().getPedido().getCodigo();
    }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public LocalDateTime getFechaHoraIngreso() { return fechaHoraIngreso; }
    public void setFechaHoraIngreso(LocalDateTime fechaHoraIngreso) { this.fechaHoraIngreso = fechaHoraIngreso; }
    public LocalDateTime getFechaHoraEgreso() { return fechaHoraEgreso; }
    public void setFechaHoraEgreso(LocalDateTime fechaHoraEgreso) { this.fechaHoraEgreso = fechaHoraEgreso; }
    public String getCodLote() { return codLote; }
    public void setCodLote(String codLote) { this.codLote = codLote; }
    public String getCodPedido() { return codPedido; }
    public void setCodPedido(String codPedido) { this.codPedido = codPedido; }
}
