/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       G4DService.java
 [**/

package com.pucp.dp1.grupo4d.morapack.service;

import com.pucp.dp1.grupo4d.morapack.adapter.*;
import com.pucp.dp1.grupo4d.morapack.algorithm.GVNS;
import com.pucp.dp1.grupo4d.morapack.algorithm.Problematica;
import com.pucp.dp1.grupo4d.morapack.algorithm.Solucion;
import com.pucp.dp1.grupo4d.morapack.mapper.*;
import com.pucp.dp1.grupo4d.morapack.model.algorithm.*;
import com.pucp.dp1.grupo4d.morapack.model.dto.*;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.ExportationRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.OperationRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.SimulationRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.GenericResponse;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.ProcessStatusResponse;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.SolutionResponse;
import com.pucp.dp1.grupo4d.morapack.model.entity.*;
import com.pucp.dp1.grupo4d.morapack.model.enums.EstadoProceso;
import com.pucp.dp1.grupo4d.morapack.model.enums.TipoEscenario;
import com.pucp.dp1.grupo4d.morapack.service.model.*;
import com.pucp.dp1.grupo4d.morapack.util.G4D;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
public class G4DService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

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

    @Autowired
    private SegmentacionAdapter segmentacionAdapter;

    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private Future<?> simulationTask = null;
    private Future<?> operationTask = null;
    private Future<?> exportationTask = null;

    public ProcessStatusResponse iniciarSimulacion(SimulationRequest request) {
        try {
            if(simulationTask != null) {
                return new ProcessStatusResponse(false, "Ya hay una simulación en proceso!", EstadoProceso.INICIADO);
            }
            simulationTask = executorService.submit(() -> {
                TipoEscenario escenario = TipoEscenario.SIMULACION;
                LocalDateTime inicioDeSimulacion = G4D.toDateTime(request.getFechaHoraInicio());
                LocalDateTime finDeSimulacion = G4D.toAdmissibleValue(request.getFechaHoraFin(), LocalDateTime.MAX);
                ParametrosDTO parametros = request.getParametros();
                Double multiplicadorTemporal = request.getMultiplicadorTemporal();
                Double saltoTemporalEnHoras = G4D.toAdmissibleValue(request.getTamanioDeSaltoTemporal(), 2.0);
                LocalDateTime inicioDePlanificacion = inicioDeSimulacion;
                LocalDateTime umbralDeReplanificacion = inicioDeSimulacion;
                long saltoTemporalEnMinutos = (long) (60*saltoTemporalEnHoras);
                double horasPlanificadas = 0.0, horasTranscurridas = 0.0;
                while(inicioDePlanificacion.isBefore(finDeSimulacion)) {
                    LocalDateTime finDePlanificacion = inicioDePlanificacion.plusMinutes(saltoTemporalEnMinutos);
                    Long desfaseTemporal = (long) (60*(Math.min(horasPlanificadas, Math.max(parametros.getMaxDiasEntregaIntracontinental(), parametros.getMaxDiasEntregaIntercontinental()))));
                    SolutionResponse solutionResponse = planificar(escenario, parametros, desfaseTemporal, inicioDePlanificacion, finDePlanificacion, umbralDeReplanificacion, null);
                    if(solutionResponse.getSuccess()) {
                        messagingTemplate.convertAndSend("/topic/simulator", solutionResponse);
                    } else {
                        messagingTemplate.convertAndSend("/topic/simulator-status", new ProcessStatusResponse(true, "COLAPSO!", EstadoProceso.COLAPSADO));
                        break;
                    }
                    horasPlanificadas += saltoTemporalEnHoras;
                    horasTranscurridas += saltoTemporalEnHoras;
                    inicioDePlanificacion = finDePlanificacion;
                    umbralDeReplanificacion = umbralDeReplanificacion.plusMinutes((long) (60 * horasTranscurridas));
                }
                messagingTemplate.convertAndSend("/topic/simulator-status", new ProcessStatusResponse(true, "Simulación correctamente finalizada!", EstadoProceso.FINALIZADO));
            });
            return new ProcessStatusResponse(true, "Simulación iniciada!", EstadoProceso.INICIADO);
        } catch (Exception e) {
            return new ProcessStatusResponse(false, "ERROR - SIMULACION: " + e.getMessage());
        }
    }

    public ProcessStatusResponse detenerSimulacion() {
        try {
            if(simulationTask == null) {
                return new ProcessStatusResponse(false, "No hay ninguna simulación en proceso!", EstadoProceso.INACTIVO);
            }
            simulationTask.cancel(true);
            simulationTask = null;
            return new ProcessStatusResponse(true, "Simulación detenida!", EstadoProceso.DETENIDO);
        } catch (Exception e) {
            return new ProcessStatusResponse(false, "ERROR - DETENIMIENTO: " + e.getMessage());
        }
    }

    public ProcessStatusResponse replanificarOperacion(OperationRequest request) {
        try {
            if(operationTask != null) {
                return new ProcessStatusResponse(false, "Ya hay una replanificación en proceso!", EstadoProceso.INICIADO);
            }
            operationTask = executorService.submit(() -> {
                TipoEscenario escenario = TipoEscenario.OPERACION;
                LocalDateTime fechaHoraActual = G4D.toAdmissibleValue(request.getFechaHoraActual(), (LocalDateTime) null);
                ParametrosDTO parametros = request.getParametros();
                Long desfaseTemporal = 60L*(Math.max(parametros.getMaxDiasEntregaIntracontinental(), parametros.getMaxDiasEntregaIntercontinental()));
                LocalDateTime finDePlanificacion = G4D.toAdmissibleValue(request.getFechaHoraActual(), (LocalDateTime) null);
                SolutionResponse solutionResponse = planificar(escenario, parametros, desfaseTemporal, null, finDePlanificacion, fechaHoraActual, fechaHoraActual);
                if(solutionResponse.getSuccess()) {
                    messagingTemplate.convertAndSend("/topic/operator", solutionResponse);
                } else {
                    messagingTemplate.convertAndSend("/topic/operator-status", new ProcessStatusResponse(true, "COLAPSO!", EstadoProceso.COLAPSADO));
                }
                messagingTemplate.convertAndSend("/topic/operator-status", new ProcessStatusResponse(true, "Operación correctamente planificada!", EstadoProceso.FINALIZADO));
            });
            return new ProcessStatusResponse(true, "Replanificación iniciada!", EstadoProceso.INICIADO);
        } catch (Exception e) {
            return new ProcessStatusResponse(false, "ERROR - REPLANIFICACIÓN: " + e.getMessage());
        }
    }

    public ProcessStatusResponse exportarSolucion(ExportationRequest request) {
        try {
            if(exportationTask != null) {
                return new ProcessStatusResponse(false, "Ya hay una exportación en proceso!", EstadoProceso.INICIADO);
            }
            exportationTask = executorService.submit(() -> {
                messagingTemplate.convertAndSend("/topic/generator-status", new ProcessStatusResponse(true, "Exportación correctamente finalizada!", EstadoProceso.FINALIZADO));
            });
            return new ProcessStatusResponse(true, "Exportación iniciada!", EstadoProceso.INICIADO);
        } catch (Exception e) {
            return new ProcessStatusResponse(false, "ERROR - EXPORTACIÓN: " + e.getMessage());
        }
    }

    private SolutionResponse planificar(TipoEscenario escenario, ParametrosDTO parametros, Long desfaseTemporal, LocalDateTime inicioDePlanificacion, LocalDateTime finDePlanificacion, LocalDateTime umbralDeReplanificacion, LocalDateTime fechaHoraActual) {
        try {
            boolean esSimulacion = escenario.equals(TipoEscenario.SIMULACION);
            Problematica.FIN_PLANIFICACION = finDePlanificacion;
            Problematica.INICIO_PLANIFICACION = (inicioDePlanificacion != null) ? inicioDePlanificacion.minusMinutes(desfaseTemporal) : finDePlanificacion.minusMinutes(desfaseTemporal);
            Problematica.UMBRAL_REPLANIFICACION = umbralDeReplanificacion;
            Problematica.TIEMPO_ACTUAL = fechaHoraActual;
            Problematica.ESCENARIO = escenario.toString().toUpperCase();
            parametrosMapper.toAlgorithm(parametros);
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
            Solucion solucion = gvns.getSolucion();
            if(solucion == null) {
                return new SolutionResponse(false, "COLAPSO!");
            }
            if(!esSimulacion) {
                almacenarSolucion(solucion);
            }
            return devolverSolucion(solucion);
        } finally {
            limpiarPools();
        }
    }

    @Transactional
    public void almacenarSolucion(Solucion solucion) {
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
