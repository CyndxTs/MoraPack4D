package com.pucp.dp1.grupo4d.morapack.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import com.pucp.dp1.grupo4d.morapack.utils.G4D;

@Entity
@Table(name = "PEDIDO", schema = "morapack4d")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "codigo", length = 20, nullable = false, unique = true)
    private String codigo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @Column(name = "cantidad_solicitada", nullable = false)
    private Integer cantidadSolicitada;

    @Column(name = "fecha_hora_generacion_local", nullable = false)
    private LocalDateTime fechaHoraGeneracionLocal;

    @Column(name = "fecha_hora_generacion_utc", nullable = false)
    private LocalDateTime fechaHoraGeneracionUTC;

    @Column(name = "fecha_hora_expiracion_local", nullable = false)
    private LocalDateTime fechaHoraExpiracionLocal;

    @Column(name = "fecha_hora_expiracion_utc", nullable = false)
    private LocalDateTime fechaHoraExpiracionUTC;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_aeropuerto_destino", nullable = false)
    private Aeropuerto destino;

    // Representa la tabla intermedia PEDIDO_POR_RUTA_POR_LOTE
    @ManyToMany
    @JoinTable(
            name = "PEDIDO_POR_RUTA_POR_LOTE",
            joinColumns = @JoinColumn(name = "id_pedido"),
            inverseJoinColumns = @JoinColumn(name = "id_ruta")
    )
    @MapKeyJoinColumn(name = "id_ruta")
    @Transient // <- se mantiene transitorio porque JPA no soporta Map<Ruta, Lote> directamente sin entidad intermedia
    private Map<Ruta, Lote> lotesPorRuta = new HashMap<>();

    public Pedido() {
        this.id = null;
        this.codigo = G4D.Generator.getUniqueString("PED");
        this.cantidadSolicitada = 0;
        this.lotesPorRuta = new HashMap<>();
    }

    public Integer obtenerCantidadDeProductosEnRuta(Ruta ruta) {
        Lote lote = lotesPorRuta.get(ruta);
        return (lote != null) ? lote.getTamanio() : 0;
    }

    public Pedido replicar(Map<String, Cliente> poolClientes, Map<String,Aeropuerto> poolAeropuertos, Map<String, Lote> poolLotes, Map<String, Ruta> poolRutas, Map<String,Vuelo> poolVuelos, Map<String, Plan> poolPlanes) {
        Pedido pedido = new Pedido();
        pedido.id = this.id;
        pedido.codigo = this.codigo;
        pedido.cliente = (this.cliente != null) ? poolClientes.computeIfAbsent(this.cliente.getCodigo(), codigo -> this.cliente.replicar()) : null;
        pedido.cantidadSolicitada = this.cantidadSolicitada;
        pedido.fechaHoraGeneracionLocal = this.fechaHoraGeneracionLocal;
        pedido.fechaHoraGeneracionUTC = this.fechaHoraGeneracionUTC;
        pedido.fechaHoraExpiracionLocal = this.fechaHoraExpiracionLocal;
        pedido.fechaHoraExpiracionUTC = this.fechaHoraExpiracionUTC;
        pedido.destino = (this.destino != null) ? poolAeropuertos.computeIfAbsent(this.destino.getCodigo(), codigo -> this.destino.replicar(poolLotes)) : null;
        for(Map.Entry<Ruta, Lote> entry : this.lotesPorRuta.entrySet()) {
            Ruta ruta = poolRutas.computeIfAbsent(entry.getKey().getCodigo(), codigo -> entry.getKey().replicar(poolAeropuertos, poolLotes, poolVuelos, poolPlanes));
            Lote lote = poolLotes.computeIfAbsent(entry.getValue().getCodigo(), codigo -> entry.getValue().replicar());
            pedido.lotesPorRuta.put(ruta, lote);
        }
        return pedido;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pedido pedido = (Pedido) o;
        return Objects.equals(codigo, pedido.codigo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigo);
    }

    public Integer getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Integer getCantidadSolicitada() {
        return cantidadSolicitada;
    }

    public void setCantidadSolicitada(int cantidadSolicitada) {
        this.cantidadSolicitada = cantidadSolicitada;
    }

    public LocalDateTime getFechaHoraGeneracionLocal() {
        return fechaHoraGeneracionLocal;
    }

    public void setFechaHoraGeneracionLocal() {
        this.fechaHoraGeneracionLocal = G4D.toLocal(this.fechaHoraGeneracionUTC, this.destino.getHusoHorario());
    }

    public void setFechaHoraGeneracionLocal(LocalDateTime fechaHoraGeneracionLocal) {
        this.fechaHoraGeneracionLocal = fechaHoraGeneracionLocal;
    }

    public LocalDateTime getFechaHoraGeneracionUTC() {
        return fechaHoraGeneracionUTC;
    }

    public void setFechaHoraGeneracionUTC() {
        this.fechaHoraGeneracionUTC = G4D.toUTC(this.fechaHoraGeneracionLocal, this.destino.getHusoHorario());
    }

    public void setFechaHoraGeneracionUTC(LocalDateTime fechaHoraGeneracionUTC) {
        this.fechaHoraGeneracionUTC = fechaHoraGeneracionUTC;
    }

    public void setFechaHoraExpiracion() {
        boolean tieneIntercontinental = this.lotesPorRuta.keySet().stream().anyMatch(r -> r.getTipo().equals(TipoRuta.INTERCONTINENTAL));
        TipoRuta tipo = tieneIntercontinental ? TipoRuta.INTERCONTINENTAL : TipoRuta.INTRACONTINENTAL;
        this.fechaHoraExpiracionUTC = this.fechaHoraGeneracionUTC.plusMinutes(tipo.getMaxMinutosParaEntrega());
        this.setFechaHoraExpiracionLocal();
    }

    public LocalDateTime getFechaHoraExpiracionLocal() {
        return fechaHoraExpiracionLocal;
    }

    public void setFechaHoraExpiracionLocal() {
        this.fechaHoraExpiracionLocal = G4D.toLocal(this.fechaHoraExpiracionUTC, this.destino.getHusoHorario());
    }

    public void setFechaHoraExpiracionLocal(LocalDateTime fechaHoraExpiracionLocal) {
        this.fechaHoraExpiracionLocal = fechaHoraExpiracionLocal;
    }

    public LocalDateTime getFechaHoraExpiracionUTC() {
        return fechaHoraExpiracionUTC;
    }

    public void setFechaHoraExpiracionUTC() {
        this.fechaHoraExpiracionUTC = G4D.toUTC(this.fechaHoraExpiracionLocal, this.destino.getHusoHorario());
    }

    public void setFechaHoraExpiracionUTC(LocalDateTime fechaHoraExpiracionUTC) {
        this.fechaHoraExpiracionUTC = fechaHoraExpiracionUTC;
    }

    public Aeropuerto getDestino() {
        return destino;
    }

    public void setDestino(Aeropuerto destino) {
        this.destino = destino;
    }

    public Map<Ruta, Lote> getLotesPorRuta() {
        return lotesPorRuta;
    }

    public void setLotesPorRuta(Map<Ruta, Lote> lotesPorRuta) {
        this.lotesPorRuta = lotesPorRuta;
    }
}