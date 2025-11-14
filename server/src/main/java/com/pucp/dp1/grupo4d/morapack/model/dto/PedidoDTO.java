/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       PedidoDTO.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PedidoDTO {
    private String codigo;
    private String codCliente;
    private Integer cantidadSolicitada;
    private LocalDateTime fechaHoraGeneracion;
    private LocalDateTime fechaHoraExpiracion;
    private String codDestino;
    private List<LotePorRutaDTO> lotesPorRuta;

    public PedidoDTO() {
        this.lotesPorRuta = new ArrayList<>();
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
    public List<LotePorRutaDTO> getLotesPorRuta() { return lotesPorRuta; }
    public void setLotesPorRuta(List<LotePorRutaDTO> lotesPorRuta) { this.lotesPorRuta = lotesPorRuta; }
}
