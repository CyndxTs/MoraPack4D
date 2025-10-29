/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       RutaEntity.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.pucp.dp1.grupo4d.morapack.model.enums.TipoRuta;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "RUTA", schema = "morapack4d")
public class RutaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "codigo", length = 30, nullable = false, unique = true)
    private String codigo;

    @Column(name = "duracion", nullable = false)
    private Double duracion;

    @Column(name = "distancia", nullable = false)
    private Double distancia;

    @Column(name = "fecha_hora_salida_local", nullable = false)
    private LocalDateTime fechaHoraSalidaLocal;

    @Column(name = "fecha_hora_salida_utc", nullable = false)
    private LocalDateTime fechaHoraSalidaUTC;

    @Column(name = "fecha_hora_llegada_local", nullable = false)
    private LocalDateTime fechaHoraLlegadaLocal;

    @Column(name = "fecha_hora_llegada_utc", nullable = false)
    private LocalDateTime fechaHoraLlegadaUTC;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoRuta tipo = TipoRuta.INTRACONTINENTAL;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_aeropuerto_origen", nullable = false)
    @JsonBackReference
    private AeropuertoEntity origen;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_aeropuerto_destino", nullable = false)
    @JsonBackReference
    private AeropuertoEntity destino;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "RUTA_POR_VUELO",
            joinColumns = @JoinColumn(name = "id_ruta"),
            inverseJoinColumns = @JoinColumn(name = "id_vuelo")
    )
    @JsonManagedReference
    private List<VueloEntity> vuelos = new ArrayList<>();

    @ManyToMany(mappedBy = "rutas", fetch = FetchType.LAZY)
    @JsonBackReference
    private List<PedidoEntity> pedidos = new ArrayList<>();

    @OneToMany(mappedBy = "ruta", fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<LoteEntity> lotes = new ArrayList<>();

    public RutaEntity() {}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RutaEntity)) return false;
        RutaEntity that = (RutaEntity) o;
        return Objects.equals(codigo, that.codigo);
    }

    @Override
    public int hashCode() { return Objects.hash(codigo); }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public Double getDuracion() { return duracion; }
    public void setDuracion(Double duracion) { this.duracion = duracion; }
    public Double getDistancia() { return distancia; }
    public void setDistancia(Double distancia) { this.distancia = distancia; }
    public LocalDateTime getFechaHoraSalidaLocal() { return fechaHoraSalidaLocal; }
    public void setFechaHoraSalidaLocal(LocalDateTime fechaHoraSalidaLocal) { this.fechaHoraSalidaLocal = fechaHoraSalidaLocal; }
    public LocalDateTime getFechaHoraSalidaUTC() { return fechaHoraSalidaUTC; }
    public void setFechaHoraSalidaUTC(LocalDateTime fechaHoraSalidaUTC) { this.fechaHoraSalidaUTC = fechaHoraSalidaUTC; }
    public LocalDateTime getFechaHoraLlegadaLocal() { return fechaHoraLlegadaLocal; }
    public void setFechaHoraLlegadaLocal(LocalDateTime fechaHoraLlegadaLocal) { this.fechaHoraLlegadaLocal = fechaHoraLlegadaLocal; }
    public LocalDateTime getFechaHoraLlegadaUTC() { return fechaHoraLlegadaUTC; }
    public void setFechaHoraLlegadaUTC(LocalDateTime fechaHoraLlegadaUTC) { this.fechaHoraLlegadaUTC = fechaHoraLlegadaUTC; }
    public TipoRuta getTipo() { return tipo; }
    public void setTipo(TipoRuta tipo) { this.tipo = tipo; }
    public AeropuertoEntity getOrigen() { return origen; }
    public void setOrigen(AeropuertoEntity origen) { this.origen = origen; }
    public AeropuertoEntity getDestino() { return destino; }
    public void setDestino(AeropuertoEntity destino) { this.destino = destino; }
    public List<VueloEntity> getVuelos() { return vuelos; }
    public void setVuelos(List<VueloEntity> vuelos) { this.vuelos = vuelos; }
    public List<PedidoEntity> getPedidos() { return pedidos; }
    public void setPedidos(List<PedidoEntity> pedidos) { this.pedidos = pedidos; }
    public List<LoteEntity> getLotes() { return lotes; }
    public void setLotes(List<LoteEntity> lotes) { this.lotes = lotes; }
}
