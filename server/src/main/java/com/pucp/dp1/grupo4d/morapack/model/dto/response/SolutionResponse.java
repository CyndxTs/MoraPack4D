/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       SolutionResponse.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto.response;

import com.pucp.dp1.grupo4d.morapack.model.dto.AeropuertoDTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.PedidoDTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.RutaDTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.VueloDTO;

import java.util.ArrayList;
import java.util.List;

public class SolutionResponse extends GenericResponse {
    private List<PedidoDTO> pedidosAtendidos;
    private List<AeropuertoDTO> aeropuertosTransitados;
    private List<VueloDTO> vuelosEnTransito;
    private List<RutaDTO> rutasEnOperacion;

    public SolutionResponse(Boolean success, String message) {
        super(success, message);
        this.pedidosAtendidos = new ArrayList<>();
        this.aeropuertosTransitados = new ArrayList<>();
        this.vuelosEnTransito = new ArrayList<>();
        this.rutasEnOperacion = new ArrayList<>();
    }

    public SolutionResponse(Boolean success, String message, List<PedidoDTO> pedidosDTO, List<AeropuertoDTO> aeropuertosDTO, List<VueloDTO> vuelosDTO, List<RutaDTO> rutasDTO) {
        super(success, message);
        this.pedidosAtendidos = pedidosDTO;
        this.aeropuertosTransitados = aeropuertosDTO;
        this.vuelosEnTransito = vuelosDTO;
        this.rutasEnOperacion = rutasDTO;
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
