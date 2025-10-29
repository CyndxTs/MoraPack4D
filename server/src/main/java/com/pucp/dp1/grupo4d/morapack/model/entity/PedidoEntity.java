/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       PedidoEntity.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "PEDIDO", schema = "morapack4d")
public class PedidoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "codigo", length = 20, nullable = false, unique = true)
    private String codigo;

    @Column(name = "cantidad_solicitada", nullable = false)
    private Integer cantidadSolicitada;

    @Column(name = "fecha_hora_generacion_local", nullable = false)
    private LocalDateTime fechaHoraGeneracionLocal;

    @Column(name = "fecha_hora_generacion_utc", nullable = false)
    private LocalDateTime fechaHoraGeneracionUTC;

    @Column(name = "fecha_hora_expiracion_local", nullable = true)
    private LocalDateTime fechaHoraExpiracionLocal;

    @Column(name = "fecha_hora_expiracion_utc", nullable = true)
    private LocalDateTime fechaHoraExpiracionUTC;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_cliente", nullable = false)
    @JsonBackReference
    private ClienteEntity cliente;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_aeropuerto_destino", nullable = false)
    @JsonBackReference
    private AeropuertoEntity destino;

    @ManyToMany
    @JoinTable(
            name = "PEDIDO_POR_RUTA",
            joinColumns = @JoinColumn(name = "id_pedido"),
            inverseJoinColumns = @JoinColumn(name = "id_ruta")
    )
    @JsonManagedReference
    private List<RutaEntity> rutas = new ArrayList<>();

    @OneToMany(mappedBy = "pedido", fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<LoteEntity> lotes = new ArrayList<>();

    public PedidoEntity() {}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LoteEntity)) return false;
        PedidoEntity that = (PedidoEntity) o;
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
    public Integer getCantidadSolicitada() { return cantidadSolicitada; }
    public void setCantidadSolicitada(Integer cantidadSolicitada) { this.cantidadSolicitada = cantidadSolicitada; }
    public LocalDateTime getFechaHoraGeneracionLocal() { return fechaHoraGeneracionLocal; }
    public void setFechaHoraGeneracionLocal(LocalDateTime fechaHoraGeneracionLocal) { this.fechaHoraGeneracionLocal = fechaHoraGeneracionLocal; }
    public LocalDateTime getFechaHoraGeneracionUTC() { return fechaHoraGeneracionUTC; }
    public void setFechaHoraGeneracionUTC(LocalDateTime fechaHoraGeneracionUTC) { this.fechaHoraGeneracionUTC = fechaHoraGeneracionUTC; }
    public LocalDateTime getFechaHoraExpiracionLocal() { return fechaHoraExpiracionLocal; }
    public void setFechaHoraExpiracionLocal(LocalDateTime fechaHoraExpiracionLocal) { this.fechaHoraExpiracionLocal = fechaHoraExpiracionLocal; }
    public LocalDateTime getFechaHoraExpiracionUTC() { return fechaHoraExpiracionUTC; }
    public void setFechaHoraExpiracionUTC(LocalDateTime fechaHoraExpiracionUTC) { this.fechaHoraExpiracionUTC = fechaHoraExpiracionUTC; }
    public ClienteEntity getCliente() { return cliente; }
    public void setCliente(ClienteEntity cliente) { this.cliente = cliente; }
    public AeropuertoEntity getDestino() { return destino; }
    public void setDestino(AeropuertoEntity destino) { this.destino = destino; }
    public List<RutaEntity> getRutas() { return rutas; }
    public void setRutas(List<RutaEntity> rutas) { this.rutas = rutas; }
    public List<LoteEntity> getLotes() { return lotes; }
    public void setLotes(List<LoteEntity> lotes) { this.lotes = lotes; }
}
