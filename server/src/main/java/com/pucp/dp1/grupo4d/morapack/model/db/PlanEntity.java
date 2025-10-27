/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       PlanEntity.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.db;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "PLAN", schema = "morapack4d")
public class PlanEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "codigo", length = 20, nullable = false, unique = true)
    private String codigo;

    @Column(name = "capacidad", nullable = false)
    private Integer capacidad;

    @Column(name = "duracion", nullable = false)
    private Double duracion;

    @Column(name = "distancia", nullable = false)
    private Double distancia;

    @Column(name = "hora_salida_local", nullable = false)
    private LocalTime horaSalidaLocal;

    @Column(name = "hora_salida_utc", nullable = false)
    private LocalTime horaSalidaUTC;

    @Column(name = "hora_llegada_local", nullable = false)
    private LocalTime horaLlegadaLocal;

    @Column(name = "hora_llegada_utc", nullable = false)
    private LocalTime horaLlegadaUTC;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_aeropuerto_origen", nullable = false)
    @JsonBackReference
    private AeropuertoEntity origen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_aeropuerto_destino", nullable = false)
    @JsonBackReference
    private AeropuertoEntity destino;

    @OneToMany(mappedBy = "plan", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<VueloEntity> vuelosActivados = new ArrayList<>();

    public PlanEntity() {}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlanEntity)) return false;
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
    public List<VueloEntity> getVuelosActivados() { return vuelosActivados; }
    public void setVuelosActivados(List<VueloEntity> vuelosActivados) { this.vuelosActivados = vuelosActivados; }
}
