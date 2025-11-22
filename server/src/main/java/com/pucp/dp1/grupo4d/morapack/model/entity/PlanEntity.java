/**
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       PlanEntity.java
 **/

package com.pucp.dp1.grupo4d.morapack.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "PLAN", schema = "morapack4d")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class PlanEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 30)
    private String codigo;

    @Column(nullable = false)
    private Integer capacidad;

    @Column(nullable = false)
    private Double duracion;

    @Column(nullable = false)
    private Double distancia;

    @Column(name = "h_salida_local", nullable = false)
    private LocalTime horaSalidaLocal;

    @Column(name = "h_salida_utc", nullable = false)
    private LocalTime horaSalidaUTC;

    @Column(name = "h_llegada_local", nullable = false)
    private LocalTime horaLlegadaLocal;

    @Column(name = "h_llegada_utc", nullable = false)
    private LocalTime horaLlegadaUTC;

    @ManyToOne
    @JoinColumn(name = "id_aeropuerto_origen", nullable = false)
    private AeropuertoEntity origen;

    @ManyToOne
    @JoinColumn(name = "id_aeropuerto_destino", nullable = false)
    private AeropuertoEntity destino;

    @OneToMany(mappedBy = "plan")
    private List<VueloEntity> vuelos = new ArrayList<>();

    @OneToMany(mappedBy = "plan")
    private List<EventoEntity> eventos = new ArrayList<>();

    public PlanEntity() {}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlanEntity that = (PlanEntity) o;
        return Objects.equals(codigo, that.codigo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigo);
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public Integer getCapacidad() { return capacidad; }
    public void setCapacidad(Integer capacidad) { this.capacidad = capacidad; }
    public Double getDuracion() { return duracion; }
    public void setDuracion(Double duracion) { this.duracion = duracion; }
    public Double getDistancia() { return distancia; }
    public void setDistancia(Double distancia) { this.distancia = distancia; }
    public LocalTime getHoraSalidaLocal() { return horaSalidaLocal; }
    public void setHoraSalidaLocal(LocalTime horaSalidaLocal) { this.horaSalidaLocal = horaSalidaLocal; }
    public LocalTime getHoraSalidaUTC() { return horaSalidaUTC; }
    public void setHoraSalidaUTC(LocalTime horaSalidaUTC) { this.horaSalidaUTC = horaSalidaUTC; }
    public LocalTime getHoraLlegadaLocal() { return horaLlegadaLocal; }
    public void setHoraLlegadaLocal(LocalTime horaLlegadaLocal) { this.horaLlegadaLocal = horaLlegadaLocal; }
    public LocalTime getHoraLlegadaUTC() { return horaLlegadaUTC; }
    public void setHoraLlegadaUTC(LocalTime horaLlegadaUTC) { this.horaLlegadaUTC = horaLlegadaUTC; }
    public AeropuertoEntity getOrigen() { return origen; }
    public void setOrigen(AeropuertoEntity origen) { this.origen = origen; }
    public AeropuertoEntity getDestino() { return destino; }
    public void setDestino(AeropuertoEntity destino) { this.destino = destino; }
    public List<VueloEntity> getVuelos() { return vuelos; }
    public void setVuelos(List<VueloEntity> vuelos) { this.vuelos = vuelos; }
    public List<EventoEntity> getEventos() { return eventos; }
    public void setEventos(List<EventoEntity> eventos) { this.eventos = eventos; }
}