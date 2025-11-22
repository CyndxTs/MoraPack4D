/**
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       PedidoEntity.java
 **/

package com.pucp.dp1.grupo4d.morapack.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.pucp.dp1.grupo4d.morapack.model.enums.EstadoPedido;
import com.pucp.dp1.grupo4d.morapack.model.enums.TipoEscenario;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "PEDIDO", schema = "morapack4d")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class PedidoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 15)
    private String codigo;

    @Column(name = "cantidad_solicitada", nullable = false)
    private Integer cantidadSolicitada;

    @Column(name = "fh_generacion_local", nullable = false)
    private LocalDateTime fechaHoraGeneracionLocal;

    @Column(name = "fh_generacion_utc", nullable = false)
    private LocalDateTime fechaHoraGeneracionUTC;

    @Column(name = "fh_expiracion_local")
    private LocalDateTime fechaHoraExpiracionLocal;

    @Column(name = "fh_expiracion_utc")
    private LocalDateTime fechaHoraExpiracionUTC;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPedido estado = EstadoPedido.NO_ATENDIDO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoEscenario tipoEscenario = TipoEscenario.SIMULACION;

    @ManyToOne
    @JoinColumn(name = "id_cliente", nullable = false)
    private ClienteEntity cliente;

    @ManyToOne
    @JoinColumn(name = "id_aeropuerto_destino", nullable = false)
    private AeropuertoEntity destino;

    @OneToMany(mappedBy = "pedido")
    private List<SegmentacionEntity> segmentaciones = new ArrayList<>();

    public PedidoEntity() {}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
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
    public EstadoPedido getEstado() { return estado; }
    public void setEstado(EstadoPedido estado) { this.estado = estado; }
    public TipoEscenario getTipoEscenario() { return tipoEscenario; }
    public void setTipo(TipoEscenario tipoEscenario) { this.tipoEscenario = tipoEscenario; }
    public ClienteEntity getCliente() { return cliente; }
    public void setCliente(ClienteEntity cliente) { this.cliente = cliente; }
    public AeropuertoEntity getDestino() { return destino; }
    public void setDestino(AeropuertoEntity destino) { this.destino = destino; }
    public List<SegmentacionEntity> getSegmentaciones() { return segmentaciones; }
    public void setSegmentaciones(List<SegmentacionEntity> segmentaciones) { this.segmentaciones = segmentaciones; }
}
