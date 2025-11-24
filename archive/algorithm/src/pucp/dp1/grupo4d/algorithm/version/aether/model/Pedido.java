/**]
 >> Project:    MoraPack
 >> Version:    Aether
 >> Author:     Grupo 4D
 >> File:       Pedido.java
[**/

package pucp.dp1.grupo4d.algorithm.version.aether.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import pucp.dp1.grupo4d.algorithm.version.aether.enums.EstadoPedido;
import pucp.dp1.grupo4d.algorithm.version.aether.enums.TipoRuta;
import pucp.dp1.grupo4d.util.G4D;

public class Pedido {
    private String codigo;
    private Integer cantidadSolicitada;
    private LocalDateTime fechaHoraGeneracion;
    private LocalDateTime fechaHoraExpiracion;
    private EstadoPedido estado;
    private Cliente cliente;
    private Aeropuerto destino;
    private List<Segmentacion> segmentaciones;

    public Pedido() {
        this.codigo = G4D.Generator.getUniqueString("PED");
        this.cantidadSolicitada = 0;
        this.estado = EstadoPedido.NO_ATENDIDO;
        this.segmentaciones = new ArrayList<>();
    }

    public Pedido replicar(Map<String, Cliente> poolClientes, Map<String,Aeropuerto> poolAeropuertos, Map<String, Lote> poolLotes, Map<String, Ruta> poolRutas, Map<String,Vuelo> poolVuelos, Map<String, Plan> poolPlanes) {
        Pedido pedido = new Pedido();
        pedido.codigo = this.codigo;
        pedido.cantidadSolicitada = this.cantidadSolicitada;
        pedido.fechaHoraGeneracion = this.fechaHoraGeneracion;
        pedido.fechaHoraExpiracion = this.fechaHoraExpiracion;
        pedido.estado = this.estado;
        pedido.cliente = (this.cliente != null) ? poolClientes.computeIfAbsent(this.cliente.getCodigo(), codigo -> this.cliente.replicar()) : null;
        pedido.destino = (this.destino != null) ? poolAeropuertos.computeIfAbsent(this.destino.getCodigo(), codigo -> this.destino.replicar(poolLotes)) : null;
        this.segmentaciones.forEach(s -> pedido.segmentaciones.add(s.replicar(poolAeropuertos, poolLotes, poolRutas, poolVuelos, poolPlanes)));
        return pedido;
    }

    public Segmentacion obtenerSegementacionVigente() {
        return (!this.segmentaciones.isEmpty()) ? this.segmentaciones.getLast() : null;
    }

    public Integer obtenerCantidadDeProductosEnRuta(Ruta ruta) {
        Lote lote = obtenerSegementacionVigente().getLotesPorRuta().get(ruta);
        return (lote != null) ? lote.getTamanio() : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pedido that = (Pedido) o;
        return codigo != null && codigo.equals(that.codigo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigo);
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

    public LocalDateTime getFechaHoraGeneracion() {
        return fechaHoraGeneracion;
    }

    public void setFechaHoraGeneracion(LocalDateTime fechaHoraGeneracion) {
        this.fechaHoraGeneracion = fechaHoraGeneracion;
    }

    public LocalDateTime getFechaHoraExpiracion() {
        return fechaHoraExpiracion;
    }

    public void setFechaHoraExpiracion() {
        boolean tieneIntercontinental = this.obtenerSegementacionVigente().getLotesPorRuta().keySet().stream().anyMatch(r -> r.getTipo().equals(TipoRuta.INTERCONTINENTAL));
        TipoRuta tipo = tieneIntercontinental ? TipoRuta.INTERCONTINENTAL : TipoRuta.INTRACONTINENTAL;
        this.fechaHoraExpiracion = this.fechaHoraGeneracion.plusMinutes(tipo.getMaxMinutosParaEntrega());
    }

    public void setFechaHoraExpiracion(LocalDateTime fechaHoraExpiracion) {
        this.fechaHoraExpiracion = fechaHoraExpiracion;
    }

    public Aeropuerto getDestino() {
        return destino;
    }

    public void setDestino(Aeropuerto destino) {
        this.destino = destino;
    }

    public EstadoPedido getEstado() {
        return estado;
    }

    public void setEstado(EstadoPedido estado) {
        this.estado = estado;
    }

    public List<Segmentacion> getSegmentaciones() {
        return segmentaciones;
    }

    public void setSegmentaciones(List<Segmentacion> segmentaciones) {
        this.segmentaciones = segmentaciones;
    }
}
