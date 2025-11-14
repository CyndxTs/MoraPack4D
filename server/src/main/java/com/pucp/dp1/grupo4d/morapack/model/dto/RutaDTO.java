/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       RutaDTO.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RutaDTO {
    private String codigo;
    private Double duracion;
    private Double distancia;
    private LocalDateTime fechaHoraSalida;
    private LocalDateTime fechaHoraLlegada;
    private String tipo;
    private String codOrigen;
    private String codDestino;
    private List<String> codVuelos;

    public RutaDTO() {
        this.codVuelos = new ArrayList<>();
    }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public Double getDuracion() { return duracion; }
    public void setDuracion(Double duracion) { this.duracion = duracion; }
    public Double getDistancia() { return distancia; }
    public void setDistancia(Double distancia) { this.distancia = distancia; }
    public LocalDateTime getFechaHoraSalida() { return fechaHoraSalida; }
    public void setFechaHoraSalida(LocalDateTime fechaHoraSalida) { this.fechaHoraSalida = fechaHoraSalida; }
    public LocalDateTime getFechaHoraLlegada() { return fechaHoraLlegada; }
    public void setFechaHoraLlegada(LocalDateTime fechaHoraLlegada) { this.fechaHoraLlegada = fechaHoraLlegada; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getCodOrigen() { return codOrigen; }
    public void setCodOrigen(String codOrigen) { this.codOrigen = codOrigen; }
    public String getCodDestino() { return codDestino; }
    public void setCodDestino(String codDestino) { this.codDestino = codDestino; }
    public List<String> getCodVuelos() { return codVuelos; }
    public void setCodVuelos(List<String> codVuelos) { this.codVuelos = codVuelos; }
}

