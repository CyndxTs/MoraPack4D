/**]
 >> Project:    MoraPack
 >> Version:    Aether
 >> Author:     Grupo 4D
 >> File:       Pedido.java
[**/

package pucp.dp1.grupo4d.algorithm.version.aether.model;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import pucp.dp1.grupo4d.algorithm.version.aether.enums.EstadoPedido;
import pucp.dp1.grupo4d.algorithm.version.aether.enums.TipoRuta;
import pucp.dp1.grupo4d.util.G4D;

public class Pedido {
    private String codigo;
    private Integer cantidadSolicitada;
    private LocalDateTime fechaHoraGeneracionLocal;
    private LocalDateTime fechaHoraGeneracionUTC;
    private LocalDateTime fechaHoraExpiracionLocal;
    private LocalDateTime fechaHoraExpiracionUTC;
    private Cliente cliente;
    private Aeropuerto destino;
    private EstadoPedido estado;
    private Map<Ruta, Lote> lotesPorRuta;

    public Pedido() {
        this.codigo = G4D.Generator.getUniqueString("PED");
        this.cantidadSolicitada = 0;
        this.estado = EstadoPedido.NO_ATENDIDO;
        this.lotesPorRuta = new HashMap<>();
    }

    public Integer obtenerCantidadDeProductosEnRuta(Ruta ruta) {
        Lote lote = lotesPorRuta.get(ruta);
        return (lote != null) ? lote.getTamanio() : 0;
    }

    public Pedido replicar(Map<String, Cliente> poolClientes, Map<String,Aeropuerto> poolAeropuertos, Map<String, Lote> poolLotes, Map<String, Ruta> poolRutas, Map<String,Vuelo> poolVuelos, Map<String, Plan> poolPlanes) {
        Pedido pedido = new Pedido();
        pedido.codigo = this.codigo;
        pedido.cliente = (this.cliente != null) ? poolClientes.computeIfAbsent(this.cliente.getCodigo(), codigo -> this.cliente.replicar()) : null;
        pedido.cantidadSolicitada = this.cantidadSolicitada;
        pedido.fechaHoraGeneracionLocal = this.fechaHoraGeneracionLocal;
        pedido.fechaHoraGeneracionUTC = this.fechaHoraGeneracionUTC;
        pedido.fechaHoraExpiracionLocal = this.fechaHoraExpiracionLocal;
        pedido.fechaHoraExpiracionUTC = this.fechaHoraExpiracionUTC;
        pedido.destino = (this.destino != null) ? poolAeropuertos.computeIfAbsent(this.destino.getCodigo(), codigo -> this.destino.replicar(poolLotes)) : null;
        pedido.estado = this.estado;
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

    public EstadoPedido getEstado() {
        return estado;
    }

    public void setEstado(EstadoPedido estado) {
        this.estado = estado;
    }

    public Map<Ruta, Lote> getLotesPorRuta() {
        return lotesPorRuta;
    }

    public void setLotesPorRuta(Map<Ruta, Lote> lotesPorRuta) {
        this.lotesPorRuta = lotesPorRuta;
    }
}
