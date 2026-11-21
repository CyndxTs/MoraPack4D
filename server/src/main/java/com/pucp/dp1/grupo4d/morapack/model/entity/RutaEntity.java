/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       RutaEntity.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.EstadoRuta;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.TipoRuta;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "RUTA")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class RutaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    // Horas
    @Column(nullable = false)
    private Double duracion;

    @Column(nullable = false)
    private Double distancia;

    @Column(name = "fh_salida", nullable = false)
    private LocalDateTime fechaHoraSalida;

    @Column(name = "fh_llegada", nullable = false)
    private LocalDateTime fechaHoraLlegada;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoRuta estado = EstadoRuta.OPERATIVA;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoRuta tipo;

    @ManyToOne
    @JoinColumn(name = "id_aeropuerto_origen", nullable = false)
    private AeropuertoEntity origen;

    @ManyToOne
    @JoinColumn(name = "id_aeropuerto_destino", nullable = false)
    private AeropuertoEntity destino;

    @OneToMany(mappedBy = "ruta")
    private List<LoteEntity> lotes = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "RUTA_POR_VUELO",
            joinColumns = @JoinColumn(name = "id_ruta"),
            inverseJoinColumns = @JoinColumn(name = "id_vuelo")
    )
    @OrderBy("fechaHoraSalida ASC")
    private List<VueloEntity> vuelos = new ArrayList<>();

    public RutaEntity() {}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RutaEntity that = (RutaEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public EstadoRuta getEstado() { return estado; }
    public void setEstado(EstadoRuta estado) { this.estado = estado; }
    public TipoRuta getTipo() { return tipo; }
    public void setTipo(TipoRuta tipo) { this.tipo = tipo; }
    public AeropuertoEntity getOrigen() { return origen; }
    public void setOrigen(AeropuertoEntity origen) { this.origen = origen; }
    public AeropuertoEntity getDestino() { return destino; }
    public void setDestino(AeropuertoEntity destino) { this.destino = destino; }
    public List<LoteEntity> getLotes() { return lotes; }
    public void setLotes(List<LoteEntity> lotes) { this.lotes = lotes; }
    public List<VueloEntity> getVuelos() { return vuelos; }
    public void setVuelos(List<VueloEntity> vuelos) { this.vuelos = vuelos; }
}
