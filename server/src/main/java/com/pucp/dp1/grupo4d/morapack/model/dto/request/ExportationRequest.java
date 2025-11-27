package com.pucp.dp1.grupo4d.morapack.model.dto.request;

import com.pucp.dp1.grupo4d.morapack.model.dto.AeropuertoDTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.PedidoDTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.RutaDTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.VueloDTO;

import java.util.List;

public class ExportationRequest {
    private List<PedidoDTO> pedidosAtendidos;
    private List<AeropuertoDTO> aeropuertosTransitados;
    private List<VueloDTO> vuelosEnTransito;
    private List<RutaDTO> rutasEnOperacion;
}
