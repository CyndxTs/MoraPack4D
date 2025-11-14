/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       SimulationResponse.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto.response;

import com.pucp.dp1.grupo4d.morapack.model.dto.AeropuertoDTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.PedidoDTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.RutaDTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.VueloDTO;
import com.pucp.dp1.grupo4d.morapack.model.entity.AeropuertoEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.PedidoEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.RutaEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.VueloEntity;
import java.util.ArrayList;
import java.util.List;

public class SimulationResponse extends GenericResponse {
    private List<PedidoDTO> pedidosAtendidos;
    private List<AeropuertoDTO> aeropuertosTransitados;
    private List<VueloDTO> vuelosEnTransito;
    private List<RutaDTO> rutasEnOperacion;

    public SimulationResponse(Boolean success, String message) {
        super(success, message);
        this.pedidosAtendidos = null;
        this.aeropuertosTransitados = null;
        this.vuelosEnTransito = null;
        this.rutasEnOperacion = null;
    }

    public SimulationResponse(Boolean success, String message,List<PedidoEntity> pedidos, List<AeropuertoEntity> aeropuertos, List<VueloEntity> vuelos, List<RutaEntity> rutas) {
        super(success, message);
        this.pedidosAtendidos = new ArrayList<>();
        pedidos.forEach(p -> this.pedidosAtendidos.add(new PedidoDTO(p)));
        this.aeropuertosTransitados = new ArrayList<>();
        aeropuertos.forEach(a -> this.aeropuertosTransitados.add(new AeropuertoDTO(a)));
        this.vuelosEnTransito = new ArrayList<>();
        vuelos.forEach(v -> this.vuelosEnTransito.add(new VueloDTO(v)));
        this.rutasEnOperacion = new ArrayList<>();
        rutas.forEach(r -> this.rutasEnOperacion.add(new RutaDTO(r)));
    }

    public List<PedidoDTO> getPedidosAtendidos() { return pedidosAtendidos; }
    public void setPedidosAtendidos(List<PedidoDTO> pedidosAtendidos) { this.pedidosAtendidos = pedidosAtendidos; }
    public List<AeropuertoDTO> getAeropuertosTransitados() { return aeropuertosTransitados; }
    public void setAeropuertosTransitados(List<AeropuertoDTO> aeropuertosTransitados) { this.aeropuertosTransitados = aeropuertosTransitados; }
    public List<VueloDTO> getVuelosEnTransito() { return vuelosEnTransito; }
    public void setVuelosEnTransito(List<VueloDTO> vuelosEnTransito) { this.vuelosEnTransito = vuelosEnTransito; }
    public List<RutaDTO> getRutasEnOperacion() { return rutasEnOperacion; }
    public void setRutasEnOperacion(List<RutaDTO> rutasEnOperacion) { this.rutasEnOperacion = rutasEnOperacion; }
}
