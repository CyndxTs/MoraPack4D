/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       VueloResponse.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto.model;

import java.time.LocalDateTime;
import com.pucp.dp1.grupo4d.morapack.model.entity.VueloEntity;

public class VueloResponse {
    private String codigo;
    private String codOrigen;
    private String codDestino;
    private LocalDateTime fechaHoraSalida;
    private LocalDateTime fechaHoraLlegada;
    private Integer capacidadOcupada;
    private Integer capacidadMaxima;

    public VueloResponse(VueloEntity vuelo) {
        this.codigo = vuelo.getCodigo();
        this.codOrigen = vuelo.getPlan().getOrigen().getCodigo();
        this.codDestino = vuelo.getPlan().getDestino().getCodigo();
        this.fechaHoraSalida = vuelo.getFechaHoraSalidaUTC();
        this.fechaHoraLlegada = vuelo.getFechaHoraLlegadaUTC();
        this.capacidadOcupada = vuelo.getPlan().getCapacidad() - vuelo.getCapacidadDisponible();
        this.capacidadMaxima = vuelo.getPlan().getCapacidad();
    }

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
