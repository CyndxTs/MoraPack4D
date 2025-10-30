/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       PedidoResponse.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto.response;

import com.pucp.dp1.grupo4d.morapack.model.algorithm.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PedidoResponse {
    private String codigo;
    private Cliente cliente;
    private Integer cantidadSolicitada;
    private LocalDateTime fechaHoraGeneracionLocal;
    private LocalDateTime fechaHoraGeneracionUTC;
    private LocalDateTime fechaHoraExpiracionLocal;
    private LocalDateTime fechaHoraExpiracionUTC;
    private Aeropuerto destino;
    private List<Ruta> rutas;
    private List<Lote> lotes;

    public PedidoResponse(Pedido pedido) {
        this.codigo = pedido.getCodigo();
        this.cliente = pedido.getCliente();
        this.cantidadSolicitada = pedido.getCantidadSolicitada();
        this.fechaHoraGeneracionLocal = pedido.getFechaHoraGeneracionLocal();
        this.fechaHoraGeneracionUTC = pedido.getFechaHoraGeneracionUTC();
        this.fechaHoraExpiracionLocal = pedido.getFechaHoraExpiracionLocal();
        this.fechaHoraExpiracionUTC = pedido.getFechaHoraExpiracionUTC();
        this.destino = pedido.getDestino();
        this.rutas = new ArrayList<>();
        this.rutas.addAll(pedido.getLotesPorRuta().keySet());
        this.lotes = new ArrayList<>();
        this.lotes.addAll(pedido.getLotesPorRuta().values());
    }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public Integer getCantidadSolicitada() { return cantidadSolicitada; }
    public void setCantidadSolicitada(Integer cantidadSolicitada) { this.cantidadSolicitada = cantidadSolicitada; }
    public LocalDateTime getFechaHoraGeneracionLocal() { return fechaHoraGeneracionLocal; }
    public void setFechaHoraGeneracionLocal(LocalDateTime fechaHoraGeneracionLocal) { this.fechaHoraGeneracionLocal = fechaHoraGeneracionLocal; }
    public LocalDateTime getFechaHoraGeneracionUTC() { return fechaHoraGeneracionUTC; }
    public void setFechaHoraGeneracionUTC(LocalDateTime fechaHoraGeneracionUTC) { this.fechaHoraGeneracionUTC = fechaHoraGeneracionUTC; }
    public LocalDateTime getFechaHoraExpiracionLocal() { return fechaHoraExpiracionLocal; }
    public void setFechaHoraExpiracionLocal(LocalDateTime fechaHoraExpiracionLocal) { this.fechaHoraExpiracionLocal = fechaHoraExpiracionLocal; }
    public LocalDateTime getFechaHoraExpiracionUTC() { return fechaHoraExpiracionUTC; }
    public void setFechaHoraExpiracionUTC(LocalDateTime fechaHoraExpiracionUTC) { this.fechaHoraExpiracionUTC = fechaHoraExpiracionUTC; }
    public Aeropuerto getDestino() { return destino; }
    public void setDestino(Aeropuerto destino) { this.destino = destino; }
    public List<Ruta> getRutas() { return rutas; }
    public void setRutas(List<Ruta> rutas) { this.rutas = rutas; }
    public List<Lote> getLotes() { return lotes; }
    public void setLotes(List<Lote> lotes) { this.lotes = lotes; }
}
