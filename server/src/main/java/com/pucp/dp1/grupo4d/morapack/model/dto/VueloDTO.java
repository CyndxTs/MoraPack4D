/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       VueloDTO.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto;

import java.time.LocalDateTime;
import com.pucp.dp1.grupo4d.morapack.model.entity.VueloEntity;

public class VueloDTO {
    private String codigo;
    private String codOrigen;
    private String codDestino;
    private LocalDateTime fechaHoraSalida;
    private LocalDateTime fechaHoraLlegada;
    private Integer capacidadOcupada;
    private Integer capacidadMaxima;

    public VueloDTO() {}

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getCodOrigen() { return codOrigen; }
    public void setCodOrigen(String codOrigen) { this.codOrigen = codOrigen; }
    public String getCodDestino() { return codDestino; }
    public void setCodDestino(String codDestino) { this.codDestino = codDestino; }
    public LocalDateTime getFechaHoraSalida() { return fechaHoraSalida; }
    public void setFechaHoraSalida(LocalDateTime fechaHoraSalida) { this.fechaHoraSalida = fechaHoraSalida; }
    public LocalDateTime getFechaHoraLlegada() { return fechaHoraLlegada; }
    public void setFechaHoraLlegada(LocalDateTime fechaHoraLlegada) { this.fechaHoraLlegada = fechaHoraLlegada; }
    public Integer getCapacidadOcupada() { return capacidadOcupada; }
    public void setCapacidadOcupada(Integer capacidadOcupada) { this.capacidadOcupada = capacidadOcupada; }
    public Integer getCapacidadMaxima() { return capacidadMaxima; }
    public void setCapacidadMaxima(Integer capacidadMaxima) { this.capacidadMaxima = capacidadMaxima; }
}
