/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       ClientePortalService.java
 [**/

package com.pucp.dp1.grupo4d.morapack.service;

import com.pucp.dp1.grupo4d.morapack.model.dto.AeropuertoDTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.DTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.PedidoDTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.payload.SolutionPayload;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.ClientOrderRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.ListRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.GenericResponse;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.ListResponse;
import com.pucp.dp1.grupo4d.morapack.model.entity.AeropuertoEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.ParametrosEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.PedidoEntity;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.TipoEscenario;
import com.pucp.dp1.grupo4d.morapack.model.exception.G4DException;
import com.pucp.dp1.grupo4d.morapack.service.model.AeropuertoService;
import com.pucp.dp1.grupo4d.morapack.service.model.ParametrosService;
import com.pucp.dp1.grupo4d.morapack.service.model.PedidoService;
import com.pucp.dp1.grupo4d.morapack.util.G4DUtility;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;

// Lo que ve y hace un cliente: sus pedidos de operación, crear uno nuevo y cancelar los suyos
@Service
public class ClientePortalService {
    public static final int MAX_PAQUETES = 999;

    private final PedidoService pedidoService;
    private final AeropuertoService aeropuertoService;
    private final ParametrosService parametrosService;
    private final G4DService g4dService;

    public ClientePortalService(PedidoService pedidoService, AeropuertoService aeropuertoService, ParametrosService parametrosService, G4DService g4dService) {
        this.pedidoService = pedidoService;
        this.aeropuertoService = aeropuertoService;
        this.parametrosService = parametrosService;
        this.g4dService = g4dService;
    }

    // Catálogo mínimo de aeropuertos (sin almacenes ni registros)
    public ListResponse aeropuertos() {
        List<DTO> dtos = new ArrayList<>();
        for (AeropuertoEntity a : aeropuertoService.findAll()) {
            AeropuertoDTO dto = new AeropuertoDTO();
            dto.setCodigo(a.getCodigo());
            dto.setCiudad(a.getCiudad());
            dto.setPais(a.getNombrePais());
            dto.setContinente(a.getNombreContinente());
            dto.setAlias(a.getAlias());
            dto.setHusoHorario(a.getHusoHorario());
            dto.setCapacidad(a.getCapacidad());
            dto.setLatitud(a.getLatitudDEC());
            dto.setLongitud(a.getLongitudDEC());
            dto.setEsSede(a.getEsSede());
            dtos.add(dto);
        }
        dtos.sort(Comparator.comparing(d -> ((AeropuertoDTO) d).getCiudad()));
        return new ListResponse(true, "Aeropuertos listados correctamente!", dtos);
    }

    // Plazos de entrega vigentes (para explicarle al cliente cuándo llega su pedido)
    public Map<String, Object> condiciones() {
        ParametrosEntity p = parametrosService.findById(1);
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("maxDiasEntregaIntracontinental", p != null ? p.getMaxDiasEntregaIntracontinental() : 2);
        r.put("maxDiasEntregaIntercontinental", p != null ? p.getMaxDiasEntregaIntercontinental() : 3);
        r.put("maxHorasRecojo", p != null ? p.getMaxHorasRecojo() : 2.0);
        r.put("maxPaquetes", MAX_PAQUETES);
        return r;
    }

    public SolutionPayload operacion(String codCliente) {
        return g4dService.obtenerOperacionDeCliente(codCliente);
    }

    public ListResponse pedidos(String codCliente, ListRequest request) {
        return pedidoService.listarDeCliente(codCliente, request);
    }

    public GenericResponse registrar(String codCliente, ClientOrderRequest request) {
        if (request == null || G4DUtility.Convertor.toAdmissible(request.getCodDestino()) == null) {
            throw new G4DException("Elige el destino del pedido.");
        }
        Integer cantidad = request.getCantidadSolicitada();
        if (cantidad == null || cantidad < 1 || cantidad > MAX_PAQUETES) {
            throw new G4DException(String.format("La cantidad debe estar entre 1 y %d paquetes.", MAX_PAQUETES));
        }
        PedidoDTO dto = new PedidoDTO();
        dto.setTipoEscenario(TipoEscenario.OPERACION.toString());
        dto.setCodCliente(codCliente);
        dto.setCodDestino(request.getCodDestino().trim().toUpperCase());
        dto.setCantidadSolicitada(cantidad);
        dto.setFechaHoraGeneracion(G4DUtility.Convertor.toSqlString(LocalDateTime.now()));
        PedidoEntity pedido = pedidoService.importar(G4DUtility.Generator.getUniqueString("CLI"), dto);
        boolean entregado = Boolean.TRUE.equals(pedido.getFueAtendido());
        if (!entregado) g4dService.programarReplanificacion(false);
        GenericResponse response = new GenericResponse(true, entregado
                ? String.format("Pedido %s registrado y entregado en la sede de %s.", pedido.getCodigo(), pedido.getDestino().getCiudad())
                : String.format("Pedido %s registrado. Te avisaremos su ruta en cuanto se planifique.", pedido.getCodigo()));
        response.setToken(pedido.getCodigo());
        return response;
    }

    public GenericResponse cancelar(String codCliente, String codigo) {
        GenericResponse response = pedidoService.cancelarDeCliente(codigo, codCliente);
        g4dService.programarReplanificacion(false);
        return response;
    }
}
