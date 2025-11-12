/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       PedidoResponse.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto.model;

import com.pucp.dp1.grupo4d.morapack.model.algorithm.*;
import com.pucp.dp1.grupo4d.morapack.model.entity.PedidoEntity;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PedidoResponse {
    private String codigo;
    private String codCliente;
    private Integer cantidadSolicitada;
    private LocalDateTime fechaHoraGeneracion;
    private LocalDateTime fechaHoraExpiracion;
    private String codDestino;
    private List<LotePorRutaResponse> lotesPorRuta;

    public PedidoResponse(PedidoEntity pedido) {
        this.codigo = pedido.getCodigo();
        this.codCliente = pedido.getCliente().getCodigo();
        this.cantidadSolicitada = pedido.getCantidadSolicitada();
        this.fechaHoraGeneracion = pedido.getFechaHoraGeneracionUTC();
        this.fechaHoraExpiracion = pedido.getFechaHoraExpiracionUTC();
        this.codDestino = pedido.getDestino().getCodigo();
        this.lotesPorRuta = new ArrayList<>();
        pedido.getLotes().forEach( l -> this.lotesPorRuta.add(new LotePorRutaResponse(l.getRuta().getCodigo(), l)));
    }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getCodCliente() { return codCliente; }
    public void setCodCliente(String codCliente) { this.codCliente = codCliente; }
    public Integer getCantidadSolicitada() { return cantidadSolicitada; }
    public void setCantidadSolicitada(Integer cantidadSolicitada) { this.cantidadSolicitada = cantidadSolicitada; }
    public LocalDateTime getFechaHoraGeneracion() { return fechaHoraGeneracion; }
    public void setFechaHoraGeneracion(LocalDateTime fechaHoraGeneracion) { this.fechaHoraGeneracion = fechaHoraGeneracion; }
    public LocalDateTime getFechaHoraExpiracion() { return fechaHoraExpiracion; }
    public void setFechaHoraExpiracion(LocalDateTime fechaHoraExpiracion) { this.fechaHoraExpiracion = fechaHoraExpiracion; }
    public String getCodDestino() { return codDestino; }
    public void setCodDestino(String codDestino) { this.codDestino = codDestino; }
    public List<LotePorRutaResponse> getLotesPorRuta() { return lotesPorRuta; }
    public void setLotesPorRuta(List<LotePorRutaResponse> lotesPorRuta) { this.lotesPorRuta = lotesPorRuta; }
}
