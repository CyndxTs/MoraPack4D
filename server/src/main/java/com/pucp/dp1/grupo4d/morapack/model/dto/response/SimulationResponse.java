/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       SimulationResponse.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto.response;

import com.pucp.dp1.grupo4d.morapack.model.dto.model.AeropuertoResponse;
import com.pucp.dp1.grupo4d.morapack.model.dto.model.PedidoResponse;
import com.pucp.dp1.grupo4d.morapack.model.dto.model.RutaResponse;
import com.pucp.dp1.grupo4d.morapack.model.dto.model.VueloResponse;
import com.pucp.dp1.grupo4d.morapack.model.entity.AeropuertoEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.PedidoEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.RutaEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.VueloEntity;
import java.util.ArrayList;
import java.util.List;

public class SimulationResponse {
    private List<PedidoResponse> pedidosAtendidos;
    private List<AeropuertoResponse> aeropuertosTransitados;
    private List<VueloResponse> vuelosEnTransito;
    private List<RutaResponse> rutasEnOperacion;

    public SimulationResponse(List<PedidoEntity> pedidos, List<AeropuertoEntity> aeropuertos, List<VueloEntity> vuelos, List<RutaEntity> rutas) {
        this.pedidosAtendidos = new ArrayList<>();
        pedidos.forEach(p -> this.pedidosAtendidos.add(new PedidoResponse(p)));
        this.aeropuertosTransitados = new ArrayList<>();
        aeropuertos.forEach(a -> this.aeropuertosTransitados.add(new AeropuertoResponse(a)));
        this.vuelosEnTransito = new ArrayList<>();
        vuelos.forEach(v -> this.vuelosEnTransito.add(new VueloResponse(v)));
        this.rutasEnOperacion = new ArrayList<>();
        rutas.forEach(r -> this.rutasEnOperacion.add(new RutaResponse(r)));
    }

    public List<PedidoResponse> getPedidosAtendidos() { return pedidosAtendidos; }
    public void setPedidosAtendidos(List<PedidoResponse> pedidosAtendidos) { this.pedidosAtendidos = pedidosAtendidos; }
    public List<AeropuertoResponse> getAeropuertosTransitados() { return aeropuertosTransitados; }
    public void setAeropuertosTransitados(List<AeropuertoResponse> aeropuertosTransitados) { this.aeropuertosTransitados = aeropuertosTransitados; }
    public List<VueloResponse> getVuelosEnTransito() { return vuelosEnTransito; }
    public void setVuelosEnTransito(List<VueloResponse> vuelosEnTransito) { this.vuelosEnTransito = vuelosEnTransito; }
    public List<RutaResponse> getRutasEnOperacion() { return rutasEnOperacion; }
    public void setRutasEnOperacion(List<RutaResponse> rutasEnOperacion) { this.rutasEnOperacion = rutasEnOperacion; }
}
