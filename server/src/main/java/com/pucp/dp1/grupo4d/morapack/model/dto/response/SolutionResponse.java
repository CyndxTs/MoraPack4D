/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       SolutionResponse.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto.response;

import com.pucp.dp1.grupo4d.morapack.algorithm.Solucion;
import java.util.ArrayList;
import java.util.List;

public class SolutionResponse {
    private Double ratioPromedioDeUtilizacionTemporal;
    private Double ratioPromedioDeDesviacionEspacial;
    private Double ratioPromedioDeDisposicionOperacional;
    private List<PedidoResponse> pedidos;

    public SolutionResponse(Solucion solucion) {
        this.ratioPromedioDeUtilizacionTemporal = solucion.getRatioPromedioDeUtilizacionTemporal();
        this.ratioPromedioDeDesviacionEspacial = solucion.getRatioPromedioDeDesviacionEspacial();
        this.ratioPromedioDeDisposicionOperacional = solucion.getRatioPromedioDeDisposicionOperacional();
        this.pedidos = new ArrayList<>();
        solucion.getPedidosAtendidos().stream().forEach(p -> this.pedidos.add(new PedidoResponse(p)));
    }

    public Double getRatioPromedioDeUtilizacionTemporal() { return ratioPromedioDeUtilizacionTemporal; }
    public void setRatioPromedioDeUtilizacionTemporal(Double ratioPromedioDeUtilizacionTemporal) { this.ratioPromedioDeUtilizacionTemporal = ratioPromedioDeUtilizacionTemporal; }
    public Double getRatioPromedioDeDesviacionEspacial() { return ratioPromedioDeDesviacionEspacial; }
    public void setRatioPromedioDeDesviacionEspacial(Double ratioPromedioDeDesviacionEspacial) { this.ratioPromedioDeDesviacionEspacial = ratioPromedioDeDesviacionEspacial; }
    public Double getRatioPromedioDeDisposicionOperacional() { return ratioPromedioDeDisposicionOperacional; }
    public void setRatioPromedioDeDisposicionOperacional(Double ratioPromedioDeDisposicionOperacional) { this.ratioPromedioDeDisposicionOperacional = ratioPromedioDeDisposicionOperacional; }
    public List<PedidoResponse> getPedidos() { return pedidos; }
    public void setPedidos(List<PedidoResponse> pedidos) { this.pedidos = pedidos; }
}
