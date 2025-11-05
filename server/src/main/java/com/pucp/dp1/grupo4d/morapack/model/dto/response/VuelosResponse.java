package com.pucp.dp1.grupo4d.morapack.model.dto.response;

import java.time.LocalDateTime;

public class VuelosResponse {
    private Long id;
    private String codigo;
    private String origenCodigo;
    private String destinoCodigo;
    private LocalDateTime fechaSalida;
    private LocalDateTime fechaLlegada;
    private Integer capacidadOcupada;

    public VuelosResponse(Long id, String codigo, String origenCodigo, String destinoCodigo,
                         LocalDateTime fechaSalida, LocalDateTime fechaLlegada, Integer capacidadOcupada) {
        this.id = id;
        this.codigo = codigo;
        this.origenCodigo = origenCodigo;
        this.destinoCodigo = destinoCodigo;
        this.fechaSalida = fechaSalida;
        this.fechaLlegada = fechaLlegada;
        this.capacidadOcupada = capacidadOcupada;
    }

    // Getters y setters
    public Long getId() { return id; }
    public String getCodigo() { return codigo; }
    public String getOrigenCodigo() { return origenCodigo; }
    public String getDestinoCodigo() { return destinoCodigo; }
    public LocalDateTime getFechaSalida() { return fechaSalida; }
    public LocalDateTime getFechaLlegada() { return fechaLlegada; }
    public Integer getCapacidadOcupada() { return capacidadOcupada; }
}
