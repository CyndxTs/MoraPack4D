/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       PlanEntity.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "PLAN")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class PlanEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(nullable = false)
    private Integer capacidad;

    // Horas
    @Column(nullable = false)
    private Double duracion;

    @Column(nullable = false)
    private Double distancia;

    @Column(name = "h_salida", nullable = false)
    private LocalTime horaSalida;

    @Column(name = "h_llegada", nullable = false)
    private LocalTime horaLlegada;

    @ManyToOne
    @JoinColumn(name = "id_aeropuerto_origen", nullable = false)
    private AeropuertoEntity origen;

    @ManyToOne
    @JoinColumn(name = "id_aeropuerto_destino", nullable = false)
    private AeropuertoEntity destino;

    @OneToMany(mappedBy = "plan")
    private List<VueloEntity> vuelos = new ArrayList<>();

    @OneToMany(mappedBy = "plan")
    @OrderBy("fechaHoraInicio ASC")
    private List<EventoEntity> eventos = new ArrayList<>();

    public PlanEntity() {}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlanEntity that = (PlanEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
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
    public LocalTime getHoraSalida() { return horaSalida; }
    public void setHoraSalida(LocalTime horaSalida) { this.horaSalida = horaSalida; }
    public LocalTime getHoraLlegada() { return horaLlegada; }
    public void setHoraLlegada(LocalTime horaLlegada) { this.horaLlegada = horaLlegada; }
    public AeropuertoEntity getOrigen() { return origen; }
    public void setOrigen(AeropuertoEntity origen) { this.origen = origen; }
    public AeropuertoEntity getDestino() { return destino; }
    public void setDestino(AeropuertoEntity destino) { this.destino = destino; }
    public List<VueloEntity> getVuelos() { return vuelos; }
    public void setVuelos(List<VueloEntity> vuelos) { this.vuelos = vuelos; }
    public List<EventoEntity> getEventos() { return eventos; }
    public void setEventos(List<EventoEntity> eventos) { this.eventos = eventos; }
}
