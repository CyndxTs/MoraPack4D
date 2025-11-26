/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       MonitorService.java
 [**/

package com.pucp.dp1.grupo4d.morapack.service;

import com.pucp.dp1.grupo4d.morapack.adapter.*;
import com.pucp.dp1.grupo4d.morapack.algorithm.GVNS;
import com.pucp.dp1.grupo4d.morapack.algorithm.Problematica;
import com.pucp.dp1.grupo4d.morapack.algorithm.Solucion;
import com.pucp.dp1.grupo4d.morapack.mapper.*;
import com.pucp.dp1.grupo4d.morapack.model.algorithm.*;
import com.pucp.dp1.grupo4d.morapack.model.dto.*;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.OperationRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.PlanificationRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.SimulationRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.SolutionResponse;
import com.pucp.dp1.grupo4d.morapack.model.entity.*;
import com.pucp.dp1.grupo4d.morapack.model.enums.TipoEscenario;
import com.pucp.dp1.grupo4d.morapack.service.model.*;
import com.pucp.dp1.grupo4d.morapack.util.G4D;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class MonitorService {

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private PedidoAdapter pedidoAdapter;

    @Autowired
    private AeropuertoService aeropuertoService;

    @Autowired
    private AeropuertoAdapter aeropuertoAdapter;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private UsuarioAdapter usuarioAdapter;

    @Autowired
    private PlanService planService;

    @Autowired
    private PlanAdapter planAdapter;

    @Autowired
    private LoteService loteService;

    @Autowired
    private LoteAdapter loteAdapter;

    @Autowired
    private RutaService rutaService;

    @Autowired
    private RutaAdapter rutaAdapter;

    @Autowired
    private AdministradorService administradorService;

    @Autowired
    private VueloAdapter vueloAdapter;

    @Autowired
    private VueloService vueloService;

    @Autowired
    private RegistroAdapter registroAdapter;

    @Autowired
    private ParametrosService parametrosService;

    @Autowired
    private ParametrosMapper parametrosMapper;

    @Autowired
    private PedidoMapper pedidoMapper;

    @Autowired
    private AeropuertoMapper aeropuertoMapper;

    @Autowired
    private VueloMapper vueloMapper;

    @Autowired
    private RutaMapper rutaMapper;
    private SegmentacionAdapter segmentacionAdapter;

    public SolutionResponse ejecutarAlgoritmo(PlanificationRequest request) {
        try {
            TipoEscenario escenario = G4D.toAdmissibleValue(request.getEscenario(), TipoEscenario.class);
            boolean guardarPlanificacion = escenario.equals(TipoEscenario.OPERACION);
            ParametrosDTO parametrosDTO = request.getParametros();
            long desfaseTemporal = (long) (60*(Math.min(request.getDesfaseTemporalPorIteracion(), Math.max(parametrosDTO.getMaxDiasEntregaIntracontinental(), parametrosDTO.getMaxDiasEntregaIntercontinental()))));
            Problematica.INICIO_PLANIFICACION = G4D.toDateTime(request.getFechaHoraInicio()).minusMinutes(desfaseTemporal);
            Problematica.FIN_PLANIFICACION = G4D.toDateTime(request.getFechaHoraFin());
            Problematica.INICIO_REPLANIFICACION = G4D.toDateTime(request.getUmbralDeReplanificacion());
            Problematica.ESCENARIO = request.getEscenario().toUpperCase();
            parametrosMapper.toAlgorithm(parametrosDTO);
            Problematica problematica = new Problematica();
            problematica.cargarDatos(
                    aeropuertoService, aeropuertoAdapter,
                    clienteService, usuarioAdapter,
                    planService, planAdapter,
                    pedidoService, pedidoAdapter,
                    rutaService, rutaAdapter,
                    vueloService, vueloAdapter
            );
            GVNS gvns = new GVNS();
            gvns.planificar(problematica);
            if(gvns.getSolucion() == null) {
                return new SolutionResponse(false, "COLAPSO!");
            }
            Solucion solucion = gvns.getSolucion();
            if(guardarPlanificacion) {
                guardarSolucion(solucion, problematica);
            }
            return devolverSolucion(solucion);
        } catch(Exception e) {
            return new SolutionResponse(false, "ERROR - PLANIFICACIÓN: " + e.getMessage());
        } finally {
            limpiarPools();
        }
    }
    // Falta editar el almacenar Solucion debido al nuevo model....
    @Transactional
    public void guardarSolucion(Solucion solucion, Problematica problematica) {
        if (solucion == null || solucion.getPedidosAtendidos() == null) {
            return;
        }
        System.out.println("\nGuardando solución en bd..\n");
        // Vuelos (ADD)
        for (Vuelo vuelo : solucion.getVuelosEnTransito()) {
            VueloEntity vueloEntity = vueloAdapter.toEntity(vuelo);
            if (vueloEntity != null && vueloEntity.getId() == null) {
                vueloService.save(vueloEntity);
                System.out.println("[*] VUELO: " + vueloEntity.getCodigo());
            }
        }
        // Rutas (ADD/UP) & Vuelos (UP)
        for (Ruta ruta : solucion.getRutasEnOperacion()) {
            RutaEntity rutaEntity = rutaAdapter.toEntity(ruta);
            if (rutaEntity != null) {
                for (Vuelo vuelo : ruta.getVuelos()) {
                    VueloEntity vueloEntity = vueloAdapter.toEntity(vuelo);
                    if (vueloEntity != null) {
                        if(rutaEntity.getVuelos().contains(vueloEntity)) {
                            rutaEntity.getVuelos().remove(vueloEntity);
                        }
                        rutaEntity.getVuelos().add(vueloEntity);
                    }
                }
                rutaService.save(rutaEntity);
                System.out.println("[*] RUTA: " + rutaEntity.getCodigo() + " {'" + rutaEntity.getVuelos().size() + "' vuelos!}");
            }
        }
        // Pedidos (UP) & Segmentaciones (ADD/UP)
        for (Pedido pedido : solucion.getPedidosAtendidos()) {
            PedidoEntity pedidoEntity = pedidoAdapter.toEntity(pedido);
            if(pedidoEntity != null) {
                for(Segmentacion segmentacion : pedido.getSegmentaciones()) {
                    SegmentacionEntity segmentacionEntity = segmentacionAdapter.toEntity(segmentacion);
                    if(segmentacionEntity != null) {
                        if(pedidoEntity.getSegmentaciones().contains(segmentacionEntity)) {
                            pedidoEntity.getSegmentaciones().remove(segmentacionEntity);
                        }
                        pedidoEntity.getSegmentaciones().add(segmentacionEntity);
                    }
                }
            }
            pedidoService.save(pedidoEntity);
            System.out.println("[*] PEDIDO: " + pedidoEntity.getCodigo() + " ('" + pedidoEntity.getSegmentaciones().getLast().getLotes().size() + "' lotes!)");
        }
        // Aeropuertos (UP) && Registros (ADD/UP)
        for(Aeropuerto aeropuerto : solucion.getAeropuertosTransitados()) {
            AeropuertoEntity aeropuertoEntity = aeropuertoAdapter.toEntity(aeropuerto);
            if(aeropuertoEntity != null) {
                for(Registro registro : aeropuerto.getRegistros()) {
                    RegistroEntity registroEntity = registroAdapter.toEntity(registro);
                    if(registroEntity != null) {
                        if(aeropuertoEntity.getRegistros().contains(registroEntity)) {
                            aeropuertoEntity.getRegistros().remove(registroEntity);
                        }
                        aeropuertoEntity.getRegistros().add(registroEntity);
                    }
                }
                aeropuertoService.save(aeropuertoEntity);
                System.out.println("[*] AEROPUERTO: " + aeropuertoEntity.getCodigo());
            }
        }
        System.out.println("\nSOLUCIÓN ALMACENADA!");
    }

    private SolutionResponse devolverSolucion(Solucion solucion) {
        List<PedidoDTO> pedidosDTO = new ArrayList<>();
        solucion.getPedidosAtendidos().forEach(p -> pedidosDTO.add(pedidoMapper.toDTO(p)));
        List<AeropuertoDTO> aeropuertosDTO = new ArrayList<>();
        solucion.getAeropuertosTransitados().forEach(a ->  aeropuertosDTO.add(aeropuertoMapper.toDTO(a)));
        List<VueloDTO> vuelosDTO = new ArrayList<>();
        solucion.getVuelosEnTransito().forEach(v -> vuelosDTO.add(vueloMapper.toDTO(v)));
        List<RutaDTO> rutasDTO = new ArrayList<>();
        solucion.getRutasEnOperacion().forEach(r -> rutasDTO.add(rutaMapper.toDTO(r)));
        return new SolutionResponse(true, "Planificación correctamente concluida!", pedidosDTO, aeropuertosDTO, vuelosDTO, rutasDTO);
    }

    public SolutionResponse obtenerOperacion(OperationRequest request) {
        try {
            LocalDateTime fechaHoraInicio = (G4D.isAdmissible(request.getFechaHoraInicio())) ? G4D.toDateTime(request.getFechaHoraInicio()) : G4D.toDateTime("1999-12-31 23:59:59");
            return devolverSolucion(fechaHoraInicio);
        } catch (Exception e) {
            e.printStackTrace();
            return new SolutionResponse(false, "ERROR - ENVÍO HACIA OPERACIÓN: " + e.getMessage());
        }
    }

    private SolutionResponse devolverSolucion(LocalDateTime fechaHoraInicio) {
        String tipoEscenario = "OPERACION";
        List<PedidoDTO> pedidosDTO = new ArrayList<>();
        List<PedidoEntity> pedidosEntity = pedidoService.findAllSinceDateTime(fechaHoraInicio, tipoEscenario);
        pedidosEntity.forEach(p -> pedidosDTO.add(pedidoMapper.toDTO(p)));
        List<AeropuertoDTO> aeropuertosDTO = new ArrayList<>();
        List<AeropuertoEntity> aeropuertosEntity = aeropuertoService.findAll();
        aeropuertosEntity.forEach(a ->  aeropuertosDTO.add(aeropuertoMapper.toDTO(a)));
        List<VueloDTO> vuelosDTO = new ArrayList<>();
        List<VueloEntity> vuelosEntity = vueloService.findAllSinceDateTime(fechaHoraInicio, tipoEscenario);
        vuelosEntity.forEach(v -> vuelosDTO.add(vueloMapper.toDTO(v)));
        List<RutaDTO> rutasDTO = new ArrayList<>();
        List<RutaEntity> rutasEntity = rutaService.findAllSinceDateTime(fechaHoraInicio, tipoEscenario);
        rutasEntity.forEach(r -> rutasDTO.add(rutaMapper.toDTO(r)));
        limpiarPools();
        return new SolutionResponse(true, "Simulación correctamente enviada!", pedidosDTO, aeropuertosDTO, vuelosDTO, rutasDTO);
    }

    private void limpiarPools() {
        vueloAdapter.clearPools();
        vueloMapper.clearPools();
        rutaAdapter.clearPools();
        rutaMapper.clearPools();
        loteAdapter.clearPools();
        pedidoAdapter.clearPools();
        pedidoMapper.clearPools();
        aeropuertoAdapter.clearPools();
        aeropuertoMapper.clearPools();
        usuarioAdapter.clearPools();
        planAdapter.clearPools();
        registroAdapter.clearPools();
    }
}
