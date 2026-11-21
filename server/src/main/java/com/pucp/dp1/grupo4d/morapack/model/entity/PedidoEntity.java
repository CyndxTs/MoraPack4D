/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       PedidoEntity.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.EstadoPedido;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.TipoEscenario;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "PEDIDO", uniqueConstraints = {@UniqueConstraint(name = "UK_PEDIDO_CODIGO_TIPO_ESCENARIO", columnNames = {"codigo", "tipo_escenario"})})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class PedidoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String codigo;

    @Column(name = "cantidad_solicitada", nullable = false)
    private Integer cantidadSolicitada;

    @Column(name = "fh_generacion", nullable = false)
    private LocalDateTime fechaHoraGeneracion;

    @Column(name = "fh_procesamiento")
    private LocalDateTime fechaHoraProcesamiento;

    @Column(name = "fh_expiracion")
    private LocalDateTime fechaHoraExpiracion;

    @Column(name = "fue_atendido", nullable = false)
    private Boolean fueAtendido = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoPedido estado = EstadoPedido.ACTIVO;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_escenario", nullable = false)
    private TipoEscenario tipoEscenario = TipoEscenario.SIMULACION;

    // Solo SIMULACION: el cliente no existe en BD, se genera en RAM a partir de este código
    @Column(name = "cod_cliente_simulado", length = 20)
    private String codClienteSimulado;

    // Solo OPERACION
    @ManyToOne
    @JoinColumn(name = "id_cliente")
    private UsuarioEntity cliente;

    @ManyToOne
    @JoinColumn(name = "id_aeropuerto_destino", nullable = false)
    private AeropuertoEntity destino;

    @OneToMany(mappedBy = "pedido")
    @OrderBy("fechaHoraAplicacion ASC")
    private List<SegmentacionEntity> segmentaciones = new ArrayList<>();

    public PedidoEntity() {}

    // Código del cliente sin importar el escenario
    public String getCodCliente() {
        return (cliente != null) ? cliente.getCodigo() : codClienteSimulado;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PedidoEntity that = (PedidoEntity) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public Integer getCantidadSolicitada() { return cantidadSolicitada; }
    public void setCantidadSolicitada(Integer cantidadSolicitada) { this.cantidadSolicitada = cantidadSolicitada; }
    public LocalDateTime getFechaHoraGeneracion() { return fechaHoraGeneracion; }
    public void setFechaHoraGeneracion(LocalDateTime fechaHoraGeneracion) { this.fechaHoraGeneracion = fechaHoraGeneracion; }
    public LocalDateTime getFechaHoraProcesamiento() { return fechaHoraProcesamiento; }
    public void setFechaHoraProcesamiento(LocalDateTime fechaHoraProcesamiento) { this.fechaHoraProcesamiento = fechaHoraProcesamiento; }
    public LocalDateTime getFechaHoraExpiracion() { return fechaHoraExpiracion; }
    public void setFechaHoraExpiracion(LocalDateTime fechaHoraExpiracion) { this.fechaHoraExpiracion = fechaHoraExpiracion; }
    public Boolean getFueAtendido() { return fueAtendido; }
    public void setFueAtendido(Boolean fueAtendido) { this.fueAtendido = fueAtendido; }
    public EstadoPedido getEstado() { return estado; }
    public void setEstado(EstadoPedido estado) { this.estado = estado; }
    public TipoEscenario getTipoEscenario() { return tipoEscenario; }
    public void setTipoEscenario(TipoEscenario tipoEscenario) { this.tipoEscenario = tipoEscenario; }
    public String getCodClienteSimulado() { return codClienteSimulado; }
    public void setCodClienteSimulado(String codClienteSimulado) { this.codClienteSimulado = codClienteSimulado; }
    public UsuarioEntity getCliente() { return cliente; }
    public void setCliente(UsuarioEntity cliente) { this.cliente = cliente; }
    public AeropuertoEntity getDestino() { return destino; }
    public void setDestino(AeropuertoEntity destino) { this.destino = destino; }
    public List<SegmentacionEntity> getSegmentaciones() { return segmentaciones; }
    public void setSegmentaciones(List<SegmentacionEntity> segmentaciones) { this.segmentaciones = segmentaciones; }
}
