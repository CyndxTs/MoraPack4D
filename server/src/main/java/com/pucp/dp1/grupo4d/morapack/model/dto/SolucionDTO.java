/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       SolucionDTO.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class SolucionDTO implements DTO {
    private Double ratioPromedioDeUtilizacionTemporal;
    private Double ratioPromedioDeDesviacionEspacial;
    private Double ratioPromedioDeDisposicionOperacional;
    private List<PedidoDTO> pedidosAtendidos = new ArrayList<>();
    private List<AeropuertoDTO> aeropuertosTransitados = new ArrayList<>();
    private List<VueloDTO> vuelosEnTransito =  new ArrayList<>();
    private List<RutaDTO> rutasEnOperacion =  new ArrayList<>();
}
